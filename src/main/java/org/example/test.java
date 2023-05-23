package org.example;

import ch.qos.logback.classic.Level;
import org.cloudsimplus.core.CloudSim;
import org.cloudsimplus.core.Factory;
import org.cloudsimplus.core.FactorySimple;
import org.cloudsimplus.core.Simulation;
import org.cloudsimplus.network.topologies.BriteNetworkTopology;
import org.cloudsimplus.util.Log;
import org.scalecloudsim.datacenter.*;
import org.scalecloudsim.innerscheduler.InnerScheduler;
import org.scalecloudsim.innerscheduler.InnerSchedulerSimple;
import org.scalecloudsim.interscheduler.InterScheduler;
import org.scalecloudsim.interscheduler.InterSchedulerSimple;
import org.scalecloudsim.record.MemoryRecord;
import org.scalecloudsim.statemanager.*;
import org.scalecloudsim.user.UserRequestManager;
import org.scalecloudsim.user.UserRequestManagerCsv;
import org.scalecloudsim.user.UserRequestManagerEasy;
import org.scalecloudsim.user.UserSimple;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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
        Log.setLevel(Level.DEBUG);
        scaleCloudSim = new CloudSim();
//        scaleCloudSim.terminateAt(1000);
        factory = new FactorySimple();
        initUser();
        initDatacenters();
        initNetwork();
        double endInit = System.currentTimeMillis();
        scaleCloudSim.start();
        double end = System.currentTimeMillis();
        System.out.println("\n运行情况：");
        System.out.println("初始化耗时：" + (endInit - start) / 1000 + "s");
        System.out.println("模拟运行耗时：" + (end - endInit) / 1000 + "s");
        System.out.println("模拟总耗时：" + (end - start) / 1000 + "s");
        MemoryRecord.recordMemory();
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory(); //总内存
        long freeMemory = runtime.freeMemory(); //空闲内存
        long usedMemory = totalMemory - freeMemory; //已用内存
        System.out.println("占用内存: " + usedMemory / 1000000 + " Mb");
        System.out.println("运行过程占用最大内存: " + MemoryRecord.getMaxUsedMemory() / 1000000 + " Mb");
        System.out.println("运行结果保存路径:" + scaleCloudSim.getSqlRecord().getDbPath());
        for (Datacenter datacenter : scaleCloudSim.getCollaborationManager().getDatacenters(1)) {
            System.out.println(datacenter.getName() + " cost:" + datacenter.getAllCost());
        }
    }

    private void initUser() {
        userRequestManager = new UserRequestManagerCsv("src/main/resources/generateRequestParament.csv");
        user = new UserSimple(scaleCloudSim, 100, userRequestManager);
    }

    private void initDatacenters() {
        InitDatacenter.initDatacenters(scaleCloudSim, factory, "src/main/resources/DatacentersConfig.json");
//        hostStateGenerator = new IsomorphicHostStateGenerator();
//
//        dc1 = getDatacenter(1);
//        dc2 = getDatacenter(2);
//        dc3 = getDatacenter(3);
//
//        collaborationManager = new CollaborationManagerSimple(scaleCloudSim);
//        collaborationManager.addDatacenter(dc1, 0);
//        collaborationManager.addDatacenter(dc2, 0);
//        collaborationManager.addDatacenter(dc3, 0);
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
