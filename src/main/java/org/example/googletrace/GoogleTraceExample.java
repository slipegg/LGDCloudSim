package org.example.googletrace;

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
import org.lgdcloudsim.user.UserRequestManagerGoogleTrace;
import org.lgdcloudsim.user.UserSimple;
import org.lgdcloudsim.util.GoogleTraceRequestFile;
import org.lgdcloudsim.util.Log;

import java.util.HashMap;
import java.util.Map;

/**
 * Here is an example showing how to simulate using a 2019 Google cluster dataset(https://github.com/google/cluster-data).
 * We have organized this dataset into an appropriate format.
 * For the data center, we organized it into json configuration files of data centers divided into different collaboration areas.
 * For user requests, we organize them into csv files and support reading through UserRequestManagerGoogleTrace
 * as shown in the example below, and they can be configured as ordinary requests or affinity requests.
 * We also provide our bigQuery script for organizing the Google Cluster dataset,
 * see the "exmaple/GoogleTrace/readme.md" file.
 *
 * @author Anonymous
 * @since LGDCloudSim 1.0
 */
public class GoogleTraceExample {
    private static final String DATACENTER_CONFIG_FILE = "./src/main/resources/example/GoogleTrace/datacenter/1_collaboration.json";
    private static final String DATACENTER_BW_FILE = "./src/main/resources/DatacenterBwConfig.csv";
    private static final String REGION_DELAY_FILE = "./src/main/resources/regionDelay.csv";
    private static final String AREA_DELAY_FILE = "./src/main/resources/areaDelay.csv";
    Map<Integer, GoogleTraceRequestFile> GOOGLE_TRACE_REQUEST_FILE_DC_MAP = new HashMap<>() {{
        put(1, new GoogleTraceRequestFile("./src/main/resources/example/googleTrace/userRequest/2019_a_user_requests.csv", "United States", 5000));
        put(2, new GoogleTraceRequestFile("./src/main/resources/example/googleTrace/userRequest/2019_b_user_requests.csv", "United States", 5000));
        put(3, new GoogleTraceRequestFile("./src/main/resources/example/googleTrace/userRequest/2019_c_user_requests.csv", "United States", 5000));
        put(4, new GoogleTraceRequestFile("./src/main/resources/example/googleTrace/userRequest/2019_d_user_requests.csv", "United States", 5000));
        put(5, new GoogleTraceRequestFile("./src/main/resources/example/googleTrace/userRequest/2019_e_user_requests.csv", "United States", 5000));
        put(6, new GoogleTraceRequestFile("./src/main/resources/example/googleTrace/userRequest/2019_f_user_requests.csv", "United States", 5000));
        put(7, new GoogleTraceRequestFile("./src/main/resources/example/googleTrace/userRequest/2019_g_user_requests.csv", "United States", 5000));
        put(8, new GoogleTraceRequestFile("./src/main/resources/example/googleTrace/userRequest/2019_h_user_requests.csv", "United States", 5000));
    }};
    int MAX_CPU_CAPACITY = 100000;
    int MAX_RAM_CAPACITY = 100000;
    int STORAGE_CAPACITY = 100;
    int BW_CAPACITY = 100;
    int LIFE_TIME_MEAN = -1;
    int LIFE_TIME_STD = 0;
    double ACCESS_LATENCY_PERCENTAGE = 1;
    double ACCESS_LATENCY_MEAN = 100;
    double ACCESS_LATENCY_STD = 20;
    boolean IS_EDGE_DIRECTED = false;
    double EDGE_PERCENTAGE = 1;
    double EDGE_DELAY_MEAN = 200;
    double EDGE_DELAY_STD = 10;
    double EDGE_BW_MEAN = 10;
    double EDGE_BW_STD = 1;
    int INSTANCE_GROUP_RETRY_TIMES = 0;
    int INSTANCE_RETRY_TIMES = 0;

    private final Simulation lgdcloudsim;
    private final Factory factory;

    public static void main(String[] args) {
        new GoogleTraceExample();
    }

    private GoogleTraceExample() {
        Log.setLevel(Level.INFO);
        lgdcloudsim = new CloudSim();
        factory = new FactorySimple();

        initUser();
        initDatacenters();
        initNetwork();

        lgdcloudsim.start();
    }

    private void initUser() {
        UserRequestManager userRequestManager = new UserRequestManagerGoogleTrace(GOOGLE_TRACE_REQUEST_FILE_DC_MAP, MAX_CPU_CAPACITY, MAX_RAM_CAPACITY, STORAGE_CAPACITY, BW_CAPACITY, LIFE_TIME_MEAN, LIFE_TIME_STD, INSTANCE_GROUP_RETRY_TIMES, INSTANCE_RETRY_TIMES,
                ACCESS_LATENCY_PERCENTAGE, ACCESS_LATENCY_MEAN, ACCESS_LATENCY_STD, IS_EDGE_DIRECTED, EDGE_PERCENTAGE, EDGE_DELAY_MEAN, EDGE_DELAY_STD, EDGE_BW_MEAN, EDGE_BW_STD);
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
