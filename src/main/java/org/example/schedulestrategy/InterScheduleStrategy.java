package org.example.schedulestrategy;

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
 * You can customize InterScheduler's scheduling strategy,
 * see {@link org.lgdcloudsim.interscheduler.InterScheduler} and {@link FactorySimple} for more details.
 * By setting the "type" field in InterScheduler in the data center config file,
 * we can modify the scheduling algorithm of InterScheduler.
 *
 * @author Anonymous
 * @since LGDCloudSim 1.0
 */
public class InterScheduleStrategy {
    private static final String scheduleStrategy = "simple";
//    private static final String scheduleStrategy = "leastRequested";

    //The "types" field of centerScheduler in different files are different,
    // and the scheduling algorithms called are also different.
    private static final String DATACENTER_CONFIG_FILE = "./src/main/resources/example/ScheduleStrategy/InterScheduleStrategy/DatacentersConfig-" + scheduleStrategy + ".json";
    private static final String USER_REQUEST_FILE = "./src/main/resources/example/ScheduleStrategy/InterScheduleStrategy/generateRequestParameter.csv";
    private static final String DATACENTER_BW_FILE = "./src/main/resources/DatacenterBwConfig.csv";
    private static final String REGION_DELAY_FILE = "./src/main/resources/regionDelay.csv";
    private static final String AREA_DELAY_FILE = "./src/main/resources/areaDelay.csv";

    private final Simulation lgdcloudsim;
    private final Factory factory;

    public static void main(String[] args) {
        new InterScheduleStrategy();
    }

    private InterScheduleStrategy() {
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
