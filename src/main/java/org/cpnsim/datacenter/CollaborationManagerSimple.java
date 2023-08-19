package org.cpnsim.datacenter;

import lombok.Getter;
import lombok.Setter;
import org.cloudsimplus.core.CloudSim;
import org.cloudsimplus.core.Simulation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Manage the collaboration among datacenters.
 * This class implements the interface {@link CollaborationManager}.
 *
 * @author Jiawen Liu
 * @author Xinlei Wei
 * @since CPNSim 1.0
 */
public class CollaborationManagerSimple implements CollaborationManager {
    /**
     * The interval time for periodic adjustment of collaboration areas.
     */
    @Getter
    @Setter
    private double changeCollaborationSynTime = 0.0;

    /**
     * whether the collaboration areas is adjusted periodically.
     */
    private boolean isChangeCollaborationSyn = false;

    /**
     * The collaboration Map.
     */
    private Map<Integer, Set<Datacenter>> collaborationMap;

    /**
     * The Simulation.
     */
    private Simulation cloudSim;

    public CollaborationManagerSimple(Simulation simulation) {
        this.cloudSim = simulation;
        this.collaborationMap = new HashMap<>();
        simulation.setCollaborationManager(this);
    }

    @Override
    public CollaborationManager addDatacenter(Datacenter datacenter, int collaborationId) {
        Set<Datacenter> datacenters = collaborationMap.get(collaborationId);
        if (datacenters == null) {
            datacenters = new HashSet<>();
            collaborationMap.put(collaborationId, datacenters);
        }
        datacenters.add(datacenter);
        datacenterAddCollaborationId(datacenter, collaborationId);
        return this;
    }

    /**
     * Adding a record of the CollaborationId to Datacenter.
     *
     * @param datacenter      the datacenter.
     * @param collaborationId the id of the collaboration
     */
    private void datacenterAddCollaborationId(Datacenter datacenter, int collaborationId) {
        if (datacenter == null) return;
        Set<Integer> collaborationIds = datacenter.getCollaborationIds();
        if (collaborationIds.contains(collaborationId)) {
            LOGGER.warn("the datacenter(" + datacenter + ") already belongs to the collaboration " + collaborationId);
        } else {
            collaborationIds.add(collaborationId);
        }
    }

    @Override
    public CollaborationManager addDatacenter(Map<Integer, Set<Datacenter>> collaborationMap) {
        for (Map.Entry<Integer, Set<Datacenter>> entry : collaborationMap.entrySet()) {
            int collaborationId = entry.getKey();
            Set<Datacenter> addDatacenters = entry.getValue();
            // if (addDatacenters == null) continue;
            Set<Datacenter> datacenters = this.collaborationMap.get(entry.getKey());
            if (datacenters == null) {
                datacenters = new HashSet<>();
                this.collaborationMap.put(collaborationId, datacenters);
            }
            datacenters.addAll(addDatacenters);
            for (Datacenter datacenter : addDatacenters) {
                datacenterAddCollaborationId(datacenter, collaborationId);
            }
        }
        return this;
    }

    @Override
    public CollaborationManager removeDatacenter(Datacenter datacenter, int collaborationId) {
        if (datacenter == null) return this;
        Set<Datacenter> datacenters = collaborationMap.get(collaborationId);
        if (datacenters != null) {
            datacenters.remove(datacenter);
        }
        datacenterRemoveCollaborationId(datacenter, collaborationId);
        return this;
    }

    /**
     * Removing a record of the CollaborationId in Datacenter.
     *
     * @param datacenter      the datacenter.
     * @param collaborationId the id of the collaboration
     */
    private void datacenterRemoveCollaborationId(Datacenter datacenter, int collaborationId) {
        if (datacenter == null) return;
        Set<Integer> collaborationIds = datacenter.getCollaborationIds();
        if (!collaborationIds.contains(collaborationId)) {
            LOGGER.warn("the datacenter(" + datacenter + ") does not belong to the collaboration " + collaborationId + " to be removed");
        } else {
            collaborationIds.remove(collaborationId);
        }
    }

    @Override
    public CollaborationManager removeDatacenter(Datacenter datacenter) {
        if (datacenter == null) return this;
        Set<Integer> collaborationIds = new HashSet<>(datacenter.getCollaborationIds());
        for (Integer collaborationId : collaborationIds) {
            removeDatacenter(datacenter, collaborationId);
        }
        return this;
    }

    @Override
    public List<Datacenter> getDatacenters(int collaborationId) {
        Set<Datacenter> datacenters = collaborationMap.get(collaborationId);
        if (datacenters == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(datacenters);
    }

    @Override
    public List<Datacenter> getDatacenters(Datacenter datacenter) {
        Set<Datacenter> datacenters = new HashSet<>();
        for (Map.Entry<Integer, Set<Datacenter>> entry : collaborationMap.entrySet()) {
            Integer collaborationId = entry.getKey();
            if (datacenter.getCollaborationIds().contains(collaborationId)) {
                Set<Datacenter> collaborationDatacenters = entry.getValue();
                datacenters.addAll(collaborationDatacenters);
            }
        }
        return new ArrayList<>(datacenters);
    }

    @Override
    public Map<Integer, Set<Datacenter>> getCollaborationMap() {
        return collaborationMap;
    }

    @Override
    public List<Integer> getCollaborationIds() {
        return new ArrayList<>(collaborationMap.keySet());
    }

    @Override
    public boolean getIsChangeCollaborationSyn() {
        return isChangeCollaborationSyn;
    }

    @Override
    public CollaborationManager setIsChangeCollaborationSyn(boolean isChangeCollaborationSyn) {
        this.isChangeCollaborationSyn = isChangeCollaborationSyn;
        return this;
    }

    @Override
    public List<Datacenter> getOtherDatacenters(Datacenter datacenter) {
        List<Datacenter> datacenters = getDatacenters(datacenter);
        datacenters.remove(datacenter);
        return datacenters;
    }

    @Override
    public CollaborationManager changeCollaboration() {
        Datacenter minCpuDatacenter = null;
        Datacenter maxCpuDatacenter = null;
        long smallCpuSum = Long.MAX_VALUE;
        long maxCpuSum = -1;
        int smallCpuCollaborationId = -1;
        int maxCpuCollaborationId = -1;
//        遍历Map<Integer, Set<Datacenter>> collaborationMap;
        for (Map.Entry<Integer, Set<Datacenter>> entry : collaborationMap.entrySet()) {
            Set<Datacenter> datacenters = entry.getValue();
            for (Datacenter datacenter : datacenters) {
                if (datacenter.getStatesManager().getSimpleState().getCpuAvaiableSum() < smallCpuSum) {
                    smallCpuSum = datacenter.getStatesManager().getSimpleState().getCpuAvaiableSum();
                    smallCpuCollaborationId = entry.getKey();
                    minCpuDatacenter = datacenter;
                }
            }
        }
        for (Map.Entry<Integer, Set<Datacenter>> entry : collaborationMap.entrySet()) {
            if (entry.getKey() == smallCpuCollaborationId) {
                continue;
            }
            Set<Datacenter> datacenters = entry.getValue();
            for (Datacenter datacenter : datacenters) {
                if (datacenter.getStatesManager().getSimpleState().getCpuAvaiableSum() > maxCpuSum) {
                    maxCpuSum = datacenter.getStatesManager().getSimpleState().getCpuAvaiableSum();
                    maxCpuCollaborationId = entry.getKey();
                    maxCpuDatacenter = datacenter;
                }
            }
        }
        //交换
        LOGGER.info("{}: change collaboration, minCpuDatacenter: Collaboration{}-{} <--> maxCpuDatacenter: Collaboration{}-Datacenter{}",
                cloudSim.clockStr(), smallCpuCollaborationId, minCpuDatacenter.getName(), maxCpuCollaborationId, maxCpuDatacenter.getName());
        Set<Datacenter> smallCpuDatacenters = collaborationMap.get(smallCpuCollaborationId);
        Set<Datacenter> maxCpuDatacenters = collaborationMap.get(maxCpuCollaborationId);
        smallCpuDatacenters.remove(minCpuDatacenter);
        maxCpuDatacenters.remove(maxCpuDatacenter);
        smallCpuDatacenters.add(maxCpuDatacenter);
        maxCpuDatacenters.add(minCpuDatacenter);
        return this;
    }
}
