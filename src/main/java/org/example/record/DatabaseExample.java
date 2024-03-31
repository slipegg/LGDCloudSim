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
 * LGDCloudSim records simulation results through Sqlite database.
 * See {@link org.lgdcloudsim.record.SqlRecord} for more details.
 * You can set the name of the stored database yourself, the default is lgdcloudsim.db.
 * You can also turn off logging in the database by setting the {@link Simulation#setIsSqlRecord(boolean)} to false.
 * which can effectively speed up the simulation.
 *
 * @author Anonymous
 * @since LGDCloudSim 1.0
 */
public class DatabaseExample {
    private static final String DATACENTER_CONFIG_FILE = "./src/main/resources/example/BasicFirstExample/DatacentersConfig.json";
    private static final String USER_REQUEST_FILE = "./src/main/resources/example/BasicFirstExample/generateRequestParameter.csv";
    private static final String DATACENTER_BW_FILE = "./src/main/resources/example/BasicFirstExample/DatacenterBwConfig.csv";
    private static final String REGION_DELAY_FILE = "./src/main/resources/regionDelay.csv";
    private static final String AREA_DELAY_FILE = "./src/main/resources/areaDelay.csv";

    private final Simulation lgdcloudsim;
    private final Factory factory;

    public static void main(String[] args) {
        new DatabaseExample();
    }

    private DatabaseExample() {
        Log.setLevel(Level.INFO);
        lgdcloudsim = new CloudSim();
        lgdcloudsim.setDbName("test.db"); // Set the name of the database (default is lgdcloudsim.db)
//        lgdcloudsim.setIsSqlRecord(false); //When you cancel this comment, you can cancel the database record of the running process.
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
