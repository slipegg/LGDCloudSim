package org.example;

import ch.qos.logback.classic.Level;
import org.cpnsim.core.CloudSim;
import org.cpnsim.core.Factory;
import org.cpnsim.core.FactorySimple;
import org.cpnsim.core.Simulation;
import org.cpnsim.datacenter.InitDatacenter;
import org.cpnsim.network.NetworkTopology;
import org.cpnsim.network.NetworkTopologySimple;
import org.cpnsim.network.RandomDelayDynamicModel;
import org.cpnsim.record.MemoryRecord;
import org.cpnsim.user.UserRequestManager;
import org.cpnsim.user.UserRequestManagerCsv;
import org.cpnsim.user.UserSimple;
import org.cpnsim.util.Log;

public class largeScaleScheduling_xyh {
    Simulation cpnSim;
    Factory factory;
    UserSimple user;
    UserRequestManager userRequestManager;
    String REGION_DELAY_FILE = "./src/main/resources/regionDelay.csv";
    String AREA_DELAY_FILE = "./src/main/resources/areaDelay.csv";
    
    String testRequest = "simpleRequest";
    // String testRequest = "complexRequest";
    String testTime = "intermittent";
    // String testTime = "continued";
    // String testAlgorithm = "1-heuristic";
    // String testAlgorithm = "2-HFRS"; // heuristicFiltering-randomScoring
    // String testAlgorithm = "3-RFHS"; // randomFiltering-heuristicScoring
    String testAlgorithm = "4-random";

    String DATACENTER_BW_FILE = "./src/main/resources/experiment/largeScaleScheduling_xyh/"+testTime+"/"+testRequest+"/"+testAlgorithm+"/DatacenterBwConfig.csv";
    String USER_REQUEST_FILE = "./src/main/resources/experiment/largeScaleScheduling_xyh/"+testTime+"/"+testRequest+"/"+testAlgorithm+"/generateRequestParameter.csv";
    String DATACENTER_CONFIG_FILE = "./src/main/resources/experiment/largeScaleScheduling_xyh/"+testTime+"/"+testRequest+"/"+testAlgorithm+"/DatacentersConfig.json";

    String DBNAME = testTime+"."+testRequest+"."+testAlgorithm+".db";

    public static void main(String[] args) {
        new largeScaleScheduling_xyh(args);
    }

    private void setArgs(String[] args) {
        if(args.length > 0) {
            testRequest = args[0];
            testTime = args[1];
            testAlgorithm = args[2];
        }
    }

    private largeScaleScheduling_xyh(String[] args) {
        setArgs(args);
        
        double start = System.currentTimeMillis();
        Log.setLevel(Level.INFO);
        cpnSim = new CloudSim();
        cpnSim.setDbName(DBNAME);
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