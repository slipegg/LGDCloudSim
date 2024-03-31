package org.example.prediction;

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
 * The scheduler in the data center supported by LGDCloudSim periodically synchronizes the host states in the data center.
 * In addition, LGDCloudSim supports the scheduler to predict the current states of the host
 * through the recorded historical synchronized host status.
 * We can set whether to enable the state prediction function in the "DatacentersConfig.json" file
 * by setting the "isPredict" field, the default is false.
 * We can also customize the prediction method by implementing the {@link org.lgdcloudsim.statemanager.PredictionManager} interface.
 * In the file we need to indicate the type of predictor and the amount of historical data it needs to use. For example:
 * "prediction": {
 * "type": "simple",
 * "predictRecordNum": 3
 * },
 *
 * @author Anonymous
 * @since LGDCloudSim 1.0
 */
public class PredictionExample {
    private static final String DATACENTER_CONFIG_FILE = "./src/main/resources/example/Prediction/DatacentersConfig.json";
    private static final String USER_REQUEST_FILE = "./src/main/resources/example/Prediction/generateRequestParameter.csv";
    private static final String DATACENTER_BW_FILE = "./src/main/resources/DatacenterBwConfig.csv";
    private static final String REGION_DELAY_FILE = "./src/main/resources/regionDelay.csv";
    private static final String AREA_DELAY_FILE = "./src/main/resources/areaDelay.csv";

    private final Simulation lgdcloudsim;
    private final Factory factory;

    public static void main(String[] args) {
        new PredictionExample();
    }

    private PredictionExample() {
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
