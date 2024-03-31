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
 * The status obtained when synchronizing between data centers can be customized,
 * see {@link org.lgdcloudsim.statemanager.StatesManagerSimple#getStateByType(String)} for more details.
 * You only need to customize the "synStateType" field in the configuration file.
 * In the example, since inter-scheduler's inter-data center scheduling algorithm does not require
 * the use of any status data, we provide configurations for obtaining different inter-data center statuses.
 * In addition, the time interval for status acquisition between data centers is also set to 1000ms.
 * You can also customize it according to your needs.
 *
 * @author Anonymous
 * @since LGDCloudSim 1.0
 */
public class InterStateSyn {
    private static final String InterSynStateType = "Null";
    //    private static final String InterSynStateType = "EasySimple";
//    private static final String InterSynStateType = "Detailed";
    private static final String DATACENTER_CONFIG_FILE = "./src/main/resources/example/StateSyn/InterStateSyn/DatacentersConfig-" + InterSynStateType + ".json";
    private static final String USER_REQUEST_FILE = "./src/main/resources/example/StateSyn/generateRequestParameter.csv";
    private static final String DATACENTER_BW_FILE = "./src/main/resources/DatacenterBwConfig.csv";
    private static final String REGION_DELAY_FILE = "./src/main/resources/regionDelay.csv";
    private static final String AREA_DELAY_FILE = "./src/main/resources/areaDelay.csv";

    private final Simulation lgdcloudsim;
    private final Factory factory;

    public static void main(String[] args) {
        new InterStateSyn();
    }

    private InterStateSyn() {
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
        lgdcloudsim.setNetworkTopology(networkTopology);
    }
}
