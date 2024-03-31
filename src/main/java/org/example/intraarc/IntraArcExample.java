package org.example.intraarc;

import ch.qos.logback.classic.Level;
import org.lgdcloudsim.core.CloudSim;
import org.lgdcloudsim.core.Factory;
import org.lgdcloudsim.core.FactorySimple;
import org.lgdcloudsim.core.Simulation;
import org.lgdcloudsim.datacenter.InitDatacenter;
import org.lgdcloudsim.network.NetworkTopology;
import org.lgdcloudsim.user.UserRequestManager;
import org.lgdcloudsim.user.UserRequestManagerCsv;
import org.lgdcloudsim.user.UserSimple;
import org.lgdcloudsim.util.Log;

/**
 * Here is an example to show the intra-data center scheduling architectures(intra-arc)
 * within a single data center which LGDCloudSim supports.
 * In this example, you can modify the "DatacentersConfig.json" file in different directories
 * to make the system use different intra-arc.
 * There are 40000 hosts in the data center.
 * All user requests are ordinary requests.
 * The user requests are sent 400 times in total.
 * It is sent every 50ms.
 * Each time, 6080 user requests are sent.
 * Note that the status synchronization interval in the data center is set to 500ms.
 * <p>
 * The intra-arc we show in this example are as follows:
 * <ul>
 *     <li>Monolithic: There is only a single intra-scheduler in the data center.
 *     It is responsible for scheduling all the user requests.</li>
 *     <li>Two-level: There are 20 intra-schedulers in the data center.
 *     At each synchronization, hosts are evenly divided into 20 partitioned views
 *     based on the remaining CPU resources of the host in the current data center.
 *     The intra-scheduler only can schedule the user requests in its view.</li>
 *     <li>Shared-state: There are 20 intra-schedulers in the data center, each with a view of all hosts.
 *     The intra-scheduler can schedule user requests on any host in the data center.
 *     Two state synchronization methods have been tested:
 *         <ul>
 *             <li>Global synchronization: Each synchronization updates the state of all hosts in the data center.</li>
 *             <li>Partition synchronization: The data center’s hosts are divided into 20 partitions,
 *             with each intra-scheduler synchronizing one partition sequentially every 25ms. </li>
 *         </ul>
 *     </li>
 * </ul>
 *
 * @author Anonymous
 * @since LGDCloudSim 1.0
 */
public class IntraArcExample {
    private static final String intraArcType = "Monolithic";
    //    private static final String intraArcType = "Two-level";
//    private static final String intraArcType = "Global-syn-shared-state";
//    private static final String intraArcType = "Partition-syn-shared-state";
    private static final String DATACENTER_CONFIG_FILE = "./src/main/resources/example/IntraArc/" + intraArcType + "/DatacentersConfig.json";
    private static final String USER_REQUEST_FILE = "./src/main/resources/example/IntraArc/generateRequestParameter.csv";

    private final Simulation lgdcloudsim;
    private final Factory factory;

    public static void main(String[] args) {
        new IntraArcExample();
    }

    private IntraArcExample() {
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
