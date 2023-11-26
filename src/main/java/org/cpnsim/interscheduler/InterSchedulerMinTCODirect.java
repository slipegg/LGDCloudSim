package org.cpnsim.interscheduler;

import org.cloudsimplus.core.Simulation;
import org.cpnsim.network.NetworkTopology;
import org.cpnsim.datacenter.Datacenter;
import org.cpnsim.request.InstanceGroup;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InterSchedulerMinTCODirect extends InterSchedulerSimple {

    public InterSchedulerMinTCODirect(int id, Simulation simulation, int collaborationId) {
        super(id, simulation, collaborationId);
    }

    @Override
    public Map<InstanceGroup, List<Datacenter>> filterSuitableDatacenter(List<InstanceGroup> instanceGroups) {
        List<Datacenter> allDatacenters = simulation.getCollaborationManager().getDatacenters(collaborationId);
        NetworkTopology networkTopology = simulation.getNetworkTopology();
        Map<InstanceGroup, List<Datacenter>> instanceGroupAvaiableDatacenters = new HashMap<>();
        double startTime = System.currentTimeMillis();
        for (InstanceGroup instanceGroup : instanceGroups) {
            List<Datacenter> availableDatacenters = getAvailableDatacenters(instanceGroup, allDatacenters, networkTopology);
            instanceGroupAvaiableDatacenters.put(instanceGroup, availableDatacenters);
        }
        double finishTime = System.currentTimeMillis();
        interScheduleByNetworkTopology(instanceGroupAvaiableDatacenters, networkTopology);
        this.scheduleTime = finishTime - startTime;//TODO 为了模拟没有随机性，先设置为每一个亲和组调度花费0.2ms
        getDatacenter().getSimulation().getSqlRecord().addInterScheduleTime(this.scheduleTime);
        for (InstanceGroup instanceGroup : instanceGroups) {
            List<Datacenter> datacenters = instanceGroupAvaiableDatacenters.get(instanceGroup);
            Datacenter targetDatacenter = getMinTCODatacenter(datacenters, instanceGroup);
            datacenters.clear();
            datacenters.add(targetDatacenter);
        }
        return instanceGroupAvaiableDatacenters;
    }

    private Datacenter getMinTCODatacenter(List<Datacenter> datacenters, InstanceGroup instanceGroup) {
        Datacenter minTCODatacenter = null;
        double minTCO = Double.MAX_VALUE;
        for (Datacenter datacenter : datacenters) {
            double tco = datacenter.getEstimatedTCO(instanceGroup);
            if (tco < minTCO) {
                minTCO = tco;
                minTCODatacenter = datacenter;
            }
        }
        return minTCODatacenter;
    }


}
