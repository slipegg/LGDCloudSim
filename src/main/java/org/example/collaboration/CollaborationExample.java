package org.example.collaboration;

import ch.qos.logback.classic.Level;
import org.lgdcloudsim.core.CloudSim;
import org.lgdcloudsim.core.Factory;
import org.lgdcloudsim.core.FactorySimple;
import org.lgdcloudsim.core.Simulation;
import org.lgdcloudsim.datacenter.CollaborationManagerSimple;
import org.lgdcloudsim.datacenter.InitDatacenter;
import org.lgdcloudsim.network.NetworkTopology;
import org.lgdcloudsim.network.NetworkTopologySimple;
import org.lgdcloudsim.user.UserRequestManager;
import org.lgdcloudsim.user.UserRequestManagerCsv;
import org.lgdcloudsim.user.UserSimple;
import org.lgdcloudsim.util.Log;

/**
 * This example shows how to divide 4 data centers into 2 collaboration areas. See the {@link #DATACENTER_CONFIG_FILE} for more details.
 * In the example, the CollaborationManager is also set so that the collaboration zone is adjusted every 1000ms in the system.
 * The default strategy adjusted here is:
 * traverse to find the data center with the smallest remaining number of CPUs,
 * and then traverse to find the data center with the largest remaining number of CPUs in other collaboration zones.
 * Finally, the collaboration zones where the two data centers are located
 * are exchanged to balance the remaining number of CPUs in different collaboration areas.
 * For more details, see the {@link CollaborationManagerSimple#changeCollaboration()} function.
 * You can also extend {@link CollaborationManagerSimple} and customize the {@link CollaborationManagerSimple#changeCollaboration()} function.
 * Note that in order for the centralized inter-scheduler to know
 * how to synchronize the status of the exchanged data center,
 * when customizing the file, you need to define how the inter-scheduler synchronizes all data centers.
 *
 * @author Anonymous
 * @since LGDCloudSim 1.0
 */
public class CollaborationExample {
    private static final String DATACENTER_CONFIG_FILE = "./src/main/resources/example/Collaboration/DatacentersConfig.json";
    private static final String USER_REQUEST_FILE = "./src/main/resources/example/Collaboration/generateRequestParameter.csv";
    private static final String DATACENTER_BW_FILE = "./src/main/resources/DatacenterBwConfig.csv";
    private static final String REGION_DELAY_FILE = "./src/main/resources/regionDelay.csv";
    private static final String AREA_DELAY_FILE = "./src/main/resources/areaDelay.csv";

    private final Simulation lgdcloudsim;
    private final Factory factory;

    public static void main(String[] args) {
        new CollaborationExample();
    }

    private CollaborationExample() {
        Log.setLevel(Level.INFO);
        lgdcloudsim = new CloudSim();
        factory = new FactorySimple();

        initUser();
        initDatacenters();
        initNetwork();

        lgdcloudsim.getCollaborationManager().setIsChangeCollaborationSyn(true);
        lgdcloudsim.getCollaborationManager().setChangeCollaborationSynTime(1000);

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
