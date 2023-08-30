package org.example;

import org.cloudsimplus.core.CloudSim;
import org.cloudsimplus.core.Factory;
import org.cloudsimplus.core.FactorySimple;
import org.cloudsimplus.core.Simulation;
import org.cloudsimplus.network.topologies.BriteNetworkTopology;
import org.cloudsimplus.util.Log;
import org.cpnsim.datacenter.Datacenter;
import org.cpnsim.datacenter.InitDatacenter;
import org.cpnsim.user.UserRequestManager;
import org.cpnsim.user.UserRequestManagerCsv;
import org.cpnsim.user.UserSimple;

import ch.qos.logback.classic.Level;

public class UnitPriceConfiguration {
    Simulation cpnSim;
    Factory factory;
    UserSimple user;
    UserRequestManager userRequestManager;
    String NETWORK_TOPOLOGY_FILE = "./src/main/resources/experiment/setUnitPriceViaFile/topology.brite";
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
        BriteNetworkTopology networkTopology = BriteNetworkTopology.getInstance(NETWORK_TOPOLOGY_FILE);
        cpnSim.setNetworkTopology(networkTopology);
        for (int collabId : cpnSim.getCollaborationManager().getCollaborationIds()) {
            for (Datacenter datacenter : cpnSim.getCollaborationManager().getDatacenters(collabId)) {
                networkTopology.mapNode(datacenter, datacenter.getId());
            }
        }
    }
    
}
