package org.example;

import org.cloudsimplus.core.CloudSim;
import org.cloudsimplus.core.Factory;
import org.cloudsimplus.core.FactorySimple;
import org.cloudsimplus.core.Simulation;
import org.cloudsimplus.util.Log;
import org.cpnsim.datacenter.InitDatacenter;
import org.cpnsim.network.NetworkTopology;
import org.cpnsim.network.NetworkTopologySimple;
import org.cpnsim.user.UserRequestManager;
import org.cpnsim.user.UserRequestManagerCsv;
import org.cpnsim.user.UserSimple;

import ch.qos.logback.classic.Level;

public class UnitPriceConfiguration {
    Simulation cpnSim;
    Factory factory;
    UserSimple user;
    UserRequestManager userRequestManager;
    String REGION_DELAY_FILE = "./src/main/resources/regionDelay.csv";
    String AREA_DELAY_FILE = "./src/main/resources/areaDelay.csv";
    String DATACENTER_BW_FILE = "./src/main/resources/DatacenterBwConfig.csv";
    String DATACENTER_CONFIG_FILE = "./src/main/resources/experiment/setUnitPriceViaFile/DatacentersConfig.json";
    String USER_REQUEST_FILE = "./src/main/resources/experiment/setUnitPriceViaFile/generateRequestParament.csv";

    public static void main(String[] args) {
        new UnitPriceConfiguration();
    }

    private UnitPriceConfiguration() {
        double startTime = System.currentTimeMillis();
        Log.setLevel(Level.DEBUG);
        cpnSim = new CloudSim();
        cpnSim.setIsSqlRecord(false);
        factory = new FactorySimple();
        initUser();
        initDatacenters();
        initNetwork();
        double endInitTime = System.currentTimeMillis();
        cpnSim.start();
        double endTime = System.currentTimeMillis();
        System.out.println("\n运行情况：");
        System.out.println("初始化耗时：" + (endInitTime - startTime) / 1000 + "s");
        System.out.println("模拟运行耗时：" + (endTime - endInitTime) / 1000 + "s");
        System.out.println("模拟总耗时：" + (endTime - startTime) / 1000 + "s");
    }


    private void initUser() {
        userRequestManager = new UserRequestManagerCsv(USER_REQUEST_FILE);
        user = new UserSimple(cpnSim, userRequestManager);
    }

    private void initDatacenters() {
        InitDatacenter.initDatacenters(cpnSim, factory, DATACENTER_CONFIG_FILE);
        cpnSim.getCollaborationManager().setIsChangeCollaborationSyn(false);
    }

    private void initNetwork() {
        NetworkTopology networkTopology = new NetworkTopologySimple(REGION_DELAY_FILE, AREA_DELAY_FILE, DATACENTER_BW_FILE);
        cpnSim.setNetworkTopology(networkTopology);
    }
    
}
