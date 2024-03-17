package org.example;

import ch.qos.logback.classic.Level;
import org.cpnsim.core.CloudSim;
import org.cpnsim.core.Factory;
import org.cpnsim.core.FactorySimple;
import org.cpnsim.core.Simulation;
import org.cpnsim.datacenter.InitDatacenter;
import org.cpnsim.network.NetworkTopology;
import org.cpnsim.record.MemoryRecord;
import org.cpnsim.user.UserRequestManager;
import org.cpnsim.user.UserRequestManagerCsv;
import org.cpnsim.user.UserSimple;
import org.cpnsim.util.Log;

public class IntraFrameExample {
    Simulation cpnSim;
    Factory factory;
    UserSimple user;
    UserRequestManager userRequestManager;
    String USER_REQUEST_FILE = "./src/main/resources/experiment/intraFrameSmall/generateRequestParameter.csv";
    //    String DATACENTER_CONFIG_FILE = "./src/main/resources/experiment/intraFrameSmall/two-level/DatacentersConfig.json";
//    String DATACENTER_CONFIG_FILE = "./src/main/resources/experiment/intraFrameSmall/shared-state-one-partition/DatacentersConfig.json";
    String DATACENTER_CONFIG_FILE = "./src/main/resources/experiment/intraFrameSmall/monolithic/DatacentersConfig.json";
//    String DATACENTER_CONFIG_FILE = "./src/main/resources/experiment/intraFrameSmall/shared-state-mul-partitions-all-random/DatacentersConfig.json";
//    String DATACENTER_CONFIG_FILE = "./src/main/resources/experiment/intraFrameSmall/shared-state-mul-partitions-partition-random/DatacentersConfig.json";

    public static void main(String[] args) {
        new IntraFrameExample();
    }

    private IntraFrameExample() {
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
        cpnSim.setNetworkTopology(NetworkTopology.NULL);
    }
}
