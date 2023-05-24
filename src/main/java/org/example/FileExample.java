package org.example;

import ch.qos.logback.classic.Level;
import org.cloudsimplus.core.CloudSim;
import org.cloudsimplus.core.Factory;
import org.cloudsimplus.core.FactorySimple;
import org.cloudsimplus.core.Simulation;
import org.cloudsimplus.network.topologies.BriteNetworkTopology;
import org.cloudsimplus.util.Log;
import org.scalecloudsim.datacenter.CollaborationManager;
import org.scalecloudsim.datacenter.Datacenter;
import org.scalecloudsim.datacenter.InitDatacenter;
import org.scalecloudsim.record.MemoryRecord;
import org.scalecloudsim.statemanager.HostStateGenerator;
import org.scalecloudsim.user.UserRequestManager;
import org.scalecloudsim.user.UserRequestManagerCsv;
import org.scalecloudsim.user.UserSimple;

public class FileExample {
    Simulation scaleCloudSim;
    Factory factory;
    UserSimple user;
    UserRequestManager userRequestManager;
    String NETWORK_TOPOLOGY_FILE = "./src/main/resources/experiment/changeProcess/topology.brite";
    String DATACENTER_CONFIG_FILE = "./src/main/resources/experiment/changeProcess/DatacentersConfig.json";
    String USER_REQUEST_FILE = "./src/main/resources/experiment/changeProcess/generateRequestParament.csv";

    public static void main(String[] args) {
        FileExample fileExample = new FileExample();
    }

    private FileExample() {
        double start = System.currentTimeMillis();
        Log.setLevel(Level.INFO);
        scaleCloudSim = new CloudSim();
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
        System.out.println("运行过程占用最大内存: " + MemoryRecord.getMaxUsedMemory() / 1000000 + " Mb");
        System.out.println("运行结果保存路径:" + scaleCloudSim.getSqlRecord().getDbPath());
    }


    private void initUser() {
        userRequestManager = new UserRequestManagerCsv(USER_REQUEST_FILE);
        user = new UserSimple(scaleCloudSim, userRequestManager);
    }

    private void initDatacenters() {
        InitDatacenter.initDatacenters(scaleCloudSim, factory, DATACENTER_CONFIG_FILE);
    }

    private void initNetwork() {
        BriteNetworkTopology networkTopology = BriteNetworkTopology.getInstance(NETWORK_TOPOLOGY_FILE);
        scaleCloudSim.setNetworkTopology(networkTopology);
        int i = 0;
        for(int collabId : scaleCloudSim.getCollaborationManager().getCollaborationIds()) {
            for (Datacenter datacenter : scaleCloudSim.getCollaborationManager().getDatacenters(collabId)) {
                networkTopology.mapNode(datacenter, i++);
            }
        }
    }
}
