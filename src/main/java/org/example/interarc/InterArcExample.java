package org.example.interarc;

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
 * Here is an example to show the inter-data center scheduling architectures(inter-arc)
 * for multiple data centers which LGDCloudSim supports.
 * In this example, you can modify the "DatacentersConfig.json" file in different directories
 * to make the system use different inter-arc.
 * There are 10 data centers here with a total of 200,000 hosts.
 * Among them, 2 data centers have 40,000 hosts, 4 data centers have 20,000 hosts, and 4 data centers have 10,000 hosts.
 * All user requests are ordinary requests, sent every 100ms,
 * 1000 requests are sent each time, and a total of 304 times are sent.
 * The resources occupied in the later stage of request sending account for about 95% of the total resources of the data center.
 * Note that we set the status to real-time synchronization.
 * <p>
 * The inter-arc we show in this example are as follows:
 * <ul>
 *     <li>Centralized-one-stage: There is only a centralized scheduler located on the upper layer of all data centers.
 *     The scheduler has information on all host resource states.
 *     It receives all user requests and directly schedules them to the hosts.</li>
 *     <li>Centralized-two-stage: It divides the scheduling process into two stages.
 *     In the first stage, the upper-layer centralized scheduler distributes the request to each data center.
 *     In the second stage, each data center’s scheduler schedules the request to a host.</li>
 *     <li>Distributed-two-stage: There is no upper-layer centralized scheduler.
 *     User requests are sent to the nearest data center for two-stage scheduling.
 *     Firstly, the data center’s scheduler attempts to find a suitable host within the data center for the requests.
 *     If unsuccessful, it then schedules the requests to other data centers.</li>
 *     <li>Hybrid-two-stage: It is similar to a mix of centralizedtwo-stage and distributed-two-stage SA.
 *     Compared with centralized-two-stage SA, after the upper-layer centralized scheduler
 *     distributes the request to the data center,
 *     the data center’s scheduler can schedule the request to a host or just forward the request
 *     to other data centers if there is no suitable host.</li>
 *     <li>Distributed-once-forward: There is no upper-layer centralized scheduler.
 *     User requests are sent to the nearest data center for scheduling.
 *     The inter-scheduler in the data center will forward the request to a suitable data center
 *     and no further forwarding will be performed.</li>
 * </ul>
 *
 * "xxxx-MultiInterSchedulers" means that there are 5 inter-schedulers in the CIS or data center.
 *
 * @author Anonymous
 * @since LGDCloudSim 1.0
 */
public class InterArcExample {
    private static final String interArcType = "CentralizedOneStage";
    //    private static final String interArcType = "CentralizedOneStage-MultiInterSchedulers";
//    private static final String interArcType = "CentralizedTwoStage";
//    private static final String interArcType = "CentralizedTwoStage-MultiInterSchedulers";
//    private static final String interArcType = "DistributedTwoStage";
//    private static final String interArcType = "DistributedTwoStage-MultiInterSchedulers";
//    private static final String interArcType = "HybridTwoStage";
//    private static final String interArcType = "HybridTwoStage-MultiInterSchedulers";
//    private static final String interArcType = "DistributedOnceForward";
//    private static final String interArcType = "DistributedOnceForward-MultiInterSchedulers";
    private static final String DATACENTER_CONFIG_FILE = "./src/main/resources/example/InterArc/" + interArcType + "/DatacentersConfig.json";
    private static final String USER_REQUEST_FILE = "./src/main/resources/example/InterArc/generateRequestParameter.csv";
    private static final String DATACENTER_BW_FILE = "./src/main/resources/example/InterArc/DatacenterBwConfig.csv";
    private static final String REGION_DELAY_FILE = "./src/main/resources/regionDelay.csv";
    private static final String AREA_DELAY_FILE = "./src/main/resources/areaDelay.csv";

    private final Simulation lgdcloudsim;
    private final Factory factory;

    public static void main(String[] args) {
        new InterArcExample();
    }

    private InterArcExample() {
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
