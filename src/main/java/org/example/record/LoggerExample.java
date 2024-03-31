package org.example.record;

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
 * You can control the output content by modifying the Logger level.
 * The level of Logger can be adjusted to: DEBUG, INFO, WARN, ERROR, OFF.
 * Setting the Logger level to Off means directly turning OFF the Logger output,
 * which helps improve the simulation running speed.
 *
 * @author Anonymous
 * @since LGDCloudSim 1.0
 */
public class LoggerExample {
    private static final String DATACENTER_CONFIG_FILE = "./src/main/resources/example/BasicFirstExample/DatacentersConfig.json";
    private static final String USER_REQUEST_FILE = "./src/main/resources/example/BasicFirstExample/generateRequestParameter.csv";
    private static final String DATACENTER_BW_FILE = "./src/main/resources/example/BasicFirstExample/DatacenterBwConfig.csv";
    private static final String REGION_DELAY_FILE = "./src/main/resources/regionDelay.csv";
    private static final String AREA_DELAY_FILE = "./src/main/resources/areaDelay.csv";

    private final Simulation lgdcloudsim;
    private final Factory factory;

    public static void main(String[] args) {
        new LoggerExample();
    }

    private LoggerExample() {
        // Try to change the level to DEBUG, INFO, WARN, ERROR, OFF
        Log.setLevel(Level.OFF);
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
        lgdcloudsim.setNetworkTopology(networkTopology);
    }
}
