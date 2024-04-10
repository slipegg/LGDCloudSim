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
 * In this example, we set up two partitions and two intra-schedulers in data center.
 * Each scheduler initially synchronizes different partitions.
 * The synchronization period of the data center is 500ms,
 * so each scheduler synchronizes a different partition every 250ms.
 *
 * @author Anonymous
 * @since LGDCloudSim 1.0
 */
public class IntraStateSyn {
    private static final String DATACENTER_CONFIG_FILE = "./src/main/resources/example/StateSyn/IntraStateSyn/DatacentersConfig.json";
    private static final String USER_REQUEST_FILE = "./src/main/resources/example/StateSyn/IntraStateSyn/generateRequestParameter.csv";

    private final Simulation lgdcloudsim;
    private final Factory factory;

    public static void main(String[] args) {
        new IntraStateSyn();
    }

    private IntraStateSyn() {
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
