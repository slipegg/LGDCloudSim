package org.example.network;

import ch.qos.logback.classic.Level;
import org.lgdcloudsim.core.CloudSim;
import org.lgdcloudsim.core.Factory;
import org.lgdcloudsim.core.FactorySimple;
import org.lgdcloudsim.core.Simulation;
import org.lgdcloudsim.datacenter.InitDatacenter;
import org.lgdcloudsim.network.NetworkTopology;
import org.lgdcloudsim.network.NetworkTopologySimple;
import org.lgdcloudsim.network.RandomDelayDynamicModel;
import org.lgdcloudsim.user.UserRequestManager;
import org.lgdcloudsim.user.UserRequestManagerCsv;
import org.lgdcloudsim.user.UserSimple;
import org.lgdcloudsim.util.Log;

/**
 * LGDCloudSim supports setting dynamic network delay.
 * After setting up, the network delay for information transfer between the cloud administrator
 * and the data center and between each data center will fluctuate based on the standard network delay between regions.
 * You can also extend {@link RandomDelayDynamicModel} to implement different network delay fluctuation models.
 *
 * @author Anonymous
 * @since LGDCloudSim 1.0
 */
public class DynamicNetworkExample {
    private static final String DATACENTER_CONFIG_FILE = "./src/main/resources/example/BasicFirstExample/DatacentersConfig.json";
    private static final String USER_REQUEST_FILE = "./src/main/resources/example/BasicFirstExample/generateRequestParameter.csv";
    private static final String DATACENTER_BW_FILE = "./src/main/resources/example/BasicFirstExample/DatacenterBwConfig.csv";
    private static final String REGION_DELAY_FILE = "./src/main/resources/regionDelay.csv";
    private static final String AREA_DELAY_FILE = "./src/main/resources/areaDelay.csv";

    private final Simulation lgdcloudsim;
    private final Factory factory;

    public static void main(String[] args) {
        new DynamicNetworkExample();
    }

    private DynamicNetworkExample() {
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
        NetworkTopology networkTopology = new NetworkTopologySimple(REGION_DELAY_FILE, AREA_DELAY_FILE, DATACENTER_BW_FILE);
        networkTopology.setDelayDynamicModel(new RandomDelayDynamicModel());// Set the network delay fluctuation model
        lgdcloudsim.setNetworkTopology(networkTopology);
    }
}
