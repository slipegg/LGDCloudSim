package org.example.statesyn;

import ch.qos.logback.classic.Level;
import org.lgdcloudsim.core.CloudSim;
import org.lgdcloudsim.core.Factory;
import org.lgdcloudsim.core.FactorySimple;
import org.lgdcloudsim.core.Simulation;
import org.lgdcloudsim.datacenter.InitDatacenter;
import org.lgdcloudsim.network.NetworkTopology;
import org.lgdcloudsim.network.NetworkTopologySimple;
import org.lgdcloudsim.user.UserRequestManager;
import org.lgdcloudsim.user.UserRequestManagerCsv;
import org.lgdcloudsim.user.UserSimple;
import org.lgdcloudsim.util.Log;

/**
 * The basic first example of LGDCloudSim.
 * It is used to show how to use LGDCloudSim to simulate the cloud computing system.
 * Note that there is not much configuration code here, but all relies on files for initialization.
 * There are two data centers, one in the us-east1 region and the other in northamerica-northeast1 region.
 * The data center in us-east1 has 400 hosts, and the data center in northamerica-northeast1 has 200 hosts.
 * Each host has 4 CPU cores, 256GB memory, 1600GB storage, and 1000Mbps bandwidth.
 * States synchronization is all set to real-time synchronization.
 * The user requests sent to the data centers come from the United States.
 * 2 requests are sent each time, once every 100ms, for a total of 3 times.
 * Each request is a normal request. Each request contains 3 instance groups.
 * Each instance group contains 10 instances.
 * Each instance requires 8-core CPU, 16GB memory, 100GB storage, 50Mips bandwidth,
 * and the instance life cycle is 5000ms.
 * More details can be found in the configuration file
 *
 * @author Anonymous
 * @since LGDCloudSim 1.0
 */
public class Heartbeat {
    private static final String DATACENTER_CONFIG_FILE = "./src/main/resources/example/StateSyn/Heartbeat/DatacentersConfig.json";
    private static final String USER_REQUEST_FILE = "./src/main/resources/example/StateSyn/Heartbeat/generateRequestParameter.csv";

    private final Simulation lgdcloudsim;
    private final Factory factory;

    public static void main(String[] args) {
        new Heartbeat();
    }

    private Heartbeat() {
        Log.setLevel(Level.INFO);
        lgdcloudsim = new CloudSim();
        factory = new FactorySimple();

        initUser();
        initDatacenters();
        initNetwork();

        lgdcloudsim.start();
    }

    private void initUser() {
        UserRequestManager userRequestManager = new UserRequestManagerCsv(USER_REQUEST_FILE);
        new UserSimple(lgdcloudsim, userRequestManager);
    }

    private void initDatacenters() {
        InitDatacenter.initDatacenters(lgdcloudsim, factory, DATACENTER_CONFIG_FILE);
    }

    private void initNetwork() {
        lgdcloudsim.setNetworkTopology(NetworkTopology.NULL);
    }
}
