package org.oldexample;

import ch.qos.logback.classic.Level;
import org.lgdcloudsim.core.CloudSim;
import org.lgdcloudsim.core.Factory;
import org.lgdcloudsim.core.FactorySimple;
import org.lgdcloudsim.core.Simulation;
import org.lgdcloudsim.datacenter.InitDatacenter;
import org.lgdcloudsim.network.NetworkTopology;
import org.lgdcloudsim.network.NetworkTopologySimple;
import org.lgdcloudsim.network.RandomDelayDynamicModel;
import org.lgdcloudsim.record.MemoryRecord;
import org.lgdcloudsim.user.UserRequestManager;
import org.lgdcloudsim.user.UserRequestManagerCsv;
import org.lgdcloudsim.user.UserSimple;
import org.lgdcloudsim.util.Log;

public class InterFrameExample {
    Simulation cpnSim;
    Factory factory;
    UserSimple user;
    UserRequestManager userRequestManager;
    String REGION_DELAY_FILE = "./src/main/resources/regionDelay.csv";
    String AREA_DELAY_FILE = "./src/main/resources/areaDelay.csv";
    String DATACENTER_BW_FILE = "./src/main/resources/experiment/interFrameExperiment/DatacenterBwConfig.csv";
    String USER_REQUEST_FILE = "./src/main/resources/experiment/interFrameExperiment/generateRequestParameter.csv";
    String DATACENTER_CONFIG_FILE = "./src/main/resources/experiment/interFrameExperiment/centerInterToHostSchedule/DatacentersConfig.json";//    String DATACENTER_CONFIG_FILE = "./src/main/resources/experiment/interFrameExperiment/centerInterToHostSchedule/DatacentersConfig.json";

    //    String DBNAME = "Centralized-one-stage";
//    String DATACENTER_CONFIG_FILE = "./src/main/resources/experiment/interFrameExperiment/centerInterToDcNoForwardSchedule/DatacentersConfig.json";
//    String DBNAME = "Centralized-two-stage";
//    String DATACENTER_CONFIG_FILE = "./src/main/resources/experiment/interFrameExperiment/dcInterToSelfAndForward/DatacentersConfig.json";
//    String DBNAME = "Distributed-two-stage";
//    String DATACENTER_CONFIG_FILE = "./src/main/resources/experiment/interFrameExperiment/centerToDcAndForward/DatacentersConfig.json";
//    String DBNAME = "Hybrid-two-stage";
    public static void main(String[] args) {
        new InterFrameExample();
    }

    private InterFrameExample() {
        double start = System.currentTimeMillis();
        Log.setLevel(Level.INFO);
        cpnSim = new CloudSim();
        factory = new FactorySimple();
        initUser();
        initDatacenters();
        initNetwork();
        double endInit = System.currentTimeMillis();
        cpnSim.start();
        double end = System.currentTimeMillis();
        System.out.println("\n运行情况：");
        System.out.println("初始化耗时：" + (endInit - start) / 1000 + "s");
        System.out.println("模拟运行耗时：" + (end - endInit) / 1000 + "s");
        System.out.println("模拟总耗时：" + (end - start) / 1000 + "s");
        System.out.println("运行过程占用最大内存: " + MemoryRecord.getMaxUsedMemory() / 1000000 + " Mb");
        System.out.println("运行结果保存路径:" + cpnSim.getSqlRecord().getDbPath());
    }


    private void initUser() {
        userRequestManager = new UserRequestManagerCsv(USER_REQUEST_FILE);
        user = new UserSimple(cpnSim, userRequestManager);
    }

    private void initDatacenters() {
        InitDatacenter.initDatacenters(cpnSim, factory, DATACENTER_CONFIG_FILE);
    }

    private void initNetwork() {
        NetworkTopology networkTopology = new NetworkTopologySimple(REGION_DELAY_FILE, AREA_DELAY_FILE, DATACENTER_BW_FILE);
        networkTopology.setDelayDynamicModel(new RandomDelayDynamicModel());
        cpnSim.setNetworkTopology(networkTopology);
    }
}
