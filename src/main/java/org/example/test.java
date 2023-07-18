package org.example;

import ch.qos.logback.classic.Level;
import org.cloudsimplus.core.CloudSim;
import org.cloudsimplus.core.Factory;
import org.cloudsimplus.core.FactorySimple;
import org.cloudsimplus.core.Simulation;
import org.cloudsimplus.network.topologies.BriteNetworkTopology;
import org.cloudsimplus.util.Log;
import org.cpnsim.datacenter.*;
import org.cpnsim.historyrecord.DArray;
import org.cpnsim.historyrecord.DArraySimple;
import org.cpnsim.historyrecord.HistoryRecord;
import org.cpnsim.historyrecord.HostRecordSimple;
import org.cpnsim.innerscheduler.InnerScheduler;
import org.cpnsim.innerscheduler.InnerSchedulerSimple;
import org.cpnsim.interscheduler.InterScheduler;
import org.cpnsim.interscheduler.InterSchedulerSimple;
import org.cpnsim.record.MemoryRecord;
import org.cpnsim.statemanager.*;
import org.cpnsim.user.UserRequestManager;
import org.cpnsim.user.UserRequestManagerCsv;
import org.cpnsim.user.UserSimple;

import java.util.*;

public class test {
    Simulation scaleCloudSim;
    Factory factory;
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
        double start = System.currentTimeMillis();
//        HistoryRecord historyRecord = new HostRecordSimple();
//        Map<Integer,HostStateHistory> recordMap = new HashMap<>();
//        Map<Integer,int[]> recordMapNew = new HashMap<>();
        Map<Integer,Integer> hostIndexMap = new HashMap<>();
        DArray dArray = new DArraySimple(30_000_000);
        int time = 0;
        for(;time<1;time++){
            for(int hostId = 0;hostId<6_000_000;hostId++){
//                historyRecord.record(hostId,time,time,time,time,time);
//                recordMap.put(hostId,new HostStateHistory(time,time,time,time,time));
//                recordMapNew.put(hostId,new int[]{time,time,time,time,time});
                hostIndexMap.put(hostId,hostId*5);
                for(int i=0;i<5;i++)
                    dArray.put(hostId*5+i,time);
            }
        }
        double end = System.currentTimeMillis();
        System.out.println(end-start);

//        time= 19;
//        historyRecord.record(6,time,time,time,time,time);
//        List<HostStateHistory> hostStateHistories = historyRecord.get(6);
//        for(HostStateHistory hostStateHistory:hostStateHistories){
//            System.out.println(hostStateHistory);
//        }
    }

    private void initUser() {
        userRequestManager = new UserRequestManagerCsv("src/main/resources/generateRequestParament.csv");
        user = new UserSimple(scaleCloudSim, userRequestManager);
    }

    private void initDatacenters() {
        InitDatacenter.initDatacenters(scaleCloudSim, factory, "src/main/resources/DatacentersConfig.json");
    }

    private Datacenter getDatacenter(int id) {
        Datacenter dc = new DatacenterSimple(scaleCloudSim, id, hostNum);
        PartitionRangesManager partitionRangesManager = new PartitionRangesManager();
        partitionRangesManager.setAverageCutting(0, hostNum - 1, 5);
        List<InnerScheduler> innerSchedulers = getInnerSchedulers(partitionRangesManager);
        dc.setInnerSchedulers(innerSchedulers);
        StatesManager statesManager = new StatesManagerSimple(hostNum, partitionRangesManager, 500);
        PredictionManager predictionManager = new PredictionManagerSimple();
        statesManager.setPredictionManager(predictionManager);
        statesManager.setPredictable(true);
        dc.setStatesManager(statesManager);
        LoadBalance loadBalance = new LoadBalanceRound();
        dc.setLoadBalance(loadBalance);
        dc.getStatesManager().initHostStates(hostStateGenerator);
        InterScheduler interScheduler = new InterSchedulerSimple();
        dc.setInterScheduler(interScheduler);
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
        int i = 0;
        for (Datacenter datacenter : scaleCloudSim.getCollaborationManager().getDatacenters(0)) {
            networkTopology.mapNode(datacenter, i++);
        }
    }
}
