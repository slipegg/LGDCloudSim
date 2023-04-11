package org.example;

import ch.qos.logback.classic.Level;
import org.cloudsimplus.core.CloudSim;
import org.cloudsimplus.core.Simulation;
import org.cloudsimplus.network.topologies.BriteNetworkTopology;
import org.cloudsimplus.util.Log;
import org.scalecloudsim.datacenter.*;
import org.scalecloudsim.innerscheduler.InnerScheduler;
import org.scalecloudsim.innerscheduler.InnerSchedulerSimple;
import org.scalecloudsim.statemanager.*;
import org.scalecloudsim.user.UserRequestManager;
import org.scalecloudsim.user.UserRequestManagerEasy;
import org.scalecloudsim.user.UserSimple;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class test {
    Simulation scaleCloudSim;
    Datacenter dc1;
    Datacenter dc2;
    Datacenter dc3;
    CollaborationManager collaborationManager;
    UserSimple user;
    UserRequestManager userRequestManager;
    String NETWORK_TOPOLOGY_FILE = "topology.brite";
    int hostNum = 5_000;
    HostStateGenerator hostStateGenerator;

    public static void main(String[] args) {
        test test = new test();

    }

    private test() {
        Log.setLevel(Level.DEBUG);
        scaleCloudSim = new CloudSim();
        initUser();
        initDatacenters();
        initNetwork();
        scaleCloudSim.start();
    }

    private void initUser() {
        userRequestManager = new UserRequestManagerEasy();
        user = new UserSimple(scaleCloudSim, 100, userRequestManager);
    }

    private void initDatacenters() {
        hostStateGenerator = new IsomorphicHostStateGenerator();

        dc1 = getDatacenter(1);
        dc2 = getDatacenter(2);
        dc3 = getDatacenter(3);

        collaborationManager = new CollaborationManagerSimple(scaleCloudSim);
        collaborationManager.addDatacenter(dc1, 0);
        collaborationManager.addDatacenter(dc2, 0);
        collaborationManager.addDatacenter(dc3, 0);
    }

    private Datacenter getDatacenter(int id) {
        Datacenter dc = new DatacenterSimple(scaleCloudSim, id, hostNum);
        PartitionRangesManager partitionRangesManager = new PartitionRangesManager();
        partitionRangesManager.setAverageCutting(0, hostNum - 1, 5);
        List<InnerScheduler> innerSchedulers = getInnerSchedulers(partitionRangesManager);
        dc.setInnerSchedulers(innerSchedulers);
        StateManager stateManager = new StateManagerSimple(hostNum, scaleCloudSim, partitionRangesManager, innerSchedulers);
        dc.setStateManager(stateManager);
        LoadBalance loadBalance = new LoadBalanceRound();
        dc.setLoadBalance(loadBalance);
        dc.getStateManager().initHostStates(hostStateGenerator);
        return dc;
    }

    private List<InnerScheduler> getInnerSchedulers(PartitionRangesManager partitionRangesManager) {
        List<InnerScheduler> schedulers = new ArrayList<>();
        int partitionNum = partitionRangesManager.getPartitionNum();
        for (int i = 0; i < partitionNum; i++) {
            Map<Integer, Double> partitionDelay = new TreeMap<>();
            for (int j = 0; j < partitionNum; j++) {
                partitionDelay.put((i + j) % partitionNum, 3.0 * j);
            }
            InnerScheduler scheduler = new InnerSchedulerSimple(i, partitionDelay);
            schedulers.add(scheduler);
        }
        return schedulers;
    }

    private void initNetwork() {
        BriteNetworkTopology networkTopology = BriteNetworkTopology.getInstance(NETWORK_TOPOLOGY_FILE);
        scaleCloudSim.setNetworkTopology(networkTopology);
        networkTopology.mapNode(dc1, 0);
        networkTopology.mapNode(dc2, 1);
        networkTopology.mapNode(dc3, 2);
    }
}
