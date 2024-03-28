package org.oldexample;

import ch.qos.logback.classic.Level;
import org.lgdcloudsim.core.CloudSim;
import org.lgdcloudsim.core.Factory;
import org.lgdcloudsim.core.FactorySimple;
import org.lgdcloudsim.core.Simulation;
import org.lgdcloudsim.network.RandomDelayDynamicModel;
import org.lgdcloudsim.util.GoogleTraceRequestFile;
import org.lgdcloudsim.util.Log;
import org.lgdcloudsim.datacenter.InitDatacenter;
import org.lgdcloudsim.network.NetworkTopology;
import org.lgdcloudsim.network.NetworkTopologySimple;
import org.lgdcloudsim.record.MemoryRecord;
import org.lgdcloudsim.user.UserRequestManager;
import org.lgdcloudsim.user.UserRequestManagerGoogleTrace;
import org.lgdcloudsim.user.UserSimple;

import java.util.HashMap;
import java.util.Map;

public class GoogleTraceExample {
    Simulation cpnSim;
    Factory factory;
    UserSimple user;
    UserRequestManager userRequestManager;
    String REGION_DELAY_FILE = "./src/main/resources/regionDelay.csv";
    String AREA_DELAY_FILE = "./src/main/resources/areaDelay.csv";
    String DATACENTER_BW_FILE = "./src/main/resources/DatacenterBwConfig.csv";
    String DATACENTER_CONFIG_FILE = "./src/main/resources/experiment/googleTrace/datacenter/1_collaborations.json";
    Map<Integer, GoogleTraceRequestFile> GOOGLE_TRACE_REQUEST_FILE_DC_MAP = new HashMap<>() {{
        put(1, new GoogleTraceRequestFile("./src/main/resources/experiment/googleTrace/userRequest/2019_a_user_requests.csv", "United States", 5000));
        put(2, new GoogleTraceRequestFile("./src/main/resources/experiment/googleTrace/userRequest/2019_b_user_requests.csv", "United States", 5000));
        put(3, new GoogleTraceRequestFile("./src/main/resources/experiment/googleTrace/userRequest/2019_c_user_requests.csv", "United States", 5000));
        put(4, new GoogleTraceRequestFile("./src/main/resources/experiment/googleTrace/userRequest/2019_d_user_requests.csv", "United States", 5000));
        put(5, new GoogleTraceRequestFile("./src/main/resources/experiment/googleTrace/userRequest/2019_e_user_requests.csv", "United States", 5000));
        put(6, new GoogleTraceRequestFile("./src/main/resources/experiment/googleTrace/userRequest/2019_f_user_requests.csv", "United States", 5000));
        put(7, new GoogleTraceRequestFile("./src/main/resources/experiment/googleTrace/userRequest/2019_g_user_requests.csv", "United States", 5000));
        put(8, new GoogleTraceRequestFile("./src/main/resources/experiment/googleTrace/userRequest/2019_h_user_requests.csv", "United States", 5000));
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

    public static void main(String[] args) {
        new GoogleTraceExample();
    }

    private GoogleTraceExample() {
        double start = System.currentTimeMillis();
        Log.setLevel(Level.INFO);
        cpnSim = new CloudSim();
        cpnSim.setIsSqlRecord(true);
        if (ACCESS_LATENCY_PERCENTAGE == 0 && EDGE_PERCENTAGE == 0) {
            cpnSim.setDbName("cpnSim-com.db");
        } else {
            cpnSim.setDbName("cpnSim-aff.db");
        }
        factory = new FactorySimple();
        initUser();
        initDatacenters();
        initNetwork();
        double endInit = System.currentTimeMillis();
        cpnSim.start();
        double end = System.currentTimeMillis();
        System.out.println("\n运行情况：");
        System.out.println("初始化耗时：" + (endInit - start) / 1000 + "s");
        System.out.println("模拟运行耗时：" + (end - endInit) / 1000 + "s");
        System.out.println("模拟总耗时：" + (end - start) / 1000 + "s");
        System.out.println("运行过程占用最大内存: " + MemoryRecord.getMaxUsedMemory() / 1000000 + " Mb");
        System.out.println("运行结果保存路径:" + cpnSim.getSqlRecord().getDbPath());
    }

    private void initUser() {
        userRequestManager = new UserRequestManagerGoogleTrace(GOOGLE_TRACE_REQUEST_FILE_DC_MAP, MAX_CPU_CAPACITY, MAX_RAM_CAPACITY, STORAGE_CAPACITY, BW_CAPACITY, LIFE_TIME_MEAN, LIFE_TIME_STD, INSTANCE_GROUP_RETRY_TIMES, INSTANCE_RETRY_TIMES,
                ACCESS_LATENCY_PERCENTAGE, ACCESS_LATENCY_MEAN, ACCESS_LATENCY_STD, IS_EDGE_DIRECTED, EDGE_PERCENTAGE, EDGE_DELAY_MEAN, EDGE_DELAY_STD, EDGE_BW_MEAN, EDGE_BW_STD);
        user = new UserSimple(cpnSim, userRequestManager);
    }

    private void initDatacenters() {
        InitDatacenter.initDatacenters(cpnSim, factory, DATACENTER_CONFIG_FILE);
    }

    private void initNetwork() {
        NetworkTopology networkTopology = new NetworkTopologySimple(REGION_DELAY_FILE, AREA_DELAY_FILE, DATACENTER_BW_FILE, false);
        networkTopology.setDelayDynamicModel(new RandomDelayDynamicModel());
        cpnSim.setNetworkTopology(networkTopology);
    }
}

/*
========================================================================================================================
8 datacenter 8 collaborations {0} {1} {2} {3} {4} {5} {6} {7}
------------------------------------------------------------------------------------------------------------------------
DatacenterSimple0's TCO = 14225.395538
DatacenterSimple0 all has 0 conflicts.
DatacenterSimple0 has a maximum of 173 hosts powered on, with a total usage time of 324627857.323000 ms for all hosts
DatacenterSimple1's TCO = 22699.653918
DatacenterSimple1 all has 0 conflicts.
DatacenterSimple1 has a maximum of 187 hosts powered on, with a total usage time of 349823201.921000 ms for all hosts
DatacenterSimple2's TCO = 27004.132186
DatacenterSimple2 all has 0 conflicts.
DatacenterSimple2 has a maximum of 208 hosts powered on, with a total usage time of 386586892.518000 ms for all hosts
DatacenterSimple3's TCO = 12838.991233
DatacenterSimple3 all has 0 conflicts.
DatacenterSimple3 has a maximum of 248 hosts powered on, with a total usage time of 436088586.896000 ms for all hosts
DatacenterSimple4's TCO = 18010.718869
DatacenterSimple4 all has 0 conflicts.
DatacenterSimple4 has a maximum of 153 hosts powered on, with a total usage time of 286112275.695000 ms for all hosts
DatacenterSimple5's TCO = 9639.469130
DatacenterSimple5 all has 0 conflicts.
DatacenterSimple5 has a maximum of 87 hosts powered on, with a total usage time of 168410769.648000 ms for all hosts
DatacenterSimple6's TCO = 17738.798049
DatacenterSimple6 all has 0 conflicts.
DatacenterSimple6 has a maximum of 138 hosts powered on, with a total usage time of 258978457.419000 ms for all hosts
DatacenterSimple7's TCO = 18839.093103
DatacenterSimple7 all has 0 conflicts.
DatacenterSimple7 has a maximum of 126 hosts powered on, with a total usage time of 240403439.862000 ms for all hosts
All TCO = 140996.252027
========================================================================================================================

========================================================================================================================
8 datacenter 4 collaborations {0， 1} {2， 3} {4， 5} {6， 7}
------------------------------------------------------------------------------------------------------------------------
DatacenterSimple0's TCO = 31891.890402
DatacenterSimple0 all has 0 conflicts.
DatacenterSimple0 has a maximum of 346 hosts powered on, with a total usage time of 645850617.640000 ms for all hosts
DatacenterSimple1's TCO = 1530.849036
DatacenterSimple1 all has 0 conflicts.
DatacenterSimple1 has a maximum of 14 hosts powered on, with a total usage time of 27083044.722000 ms for all hosts
DatacenterSimple2's TCO = 12348.811744
DatacenterSimple2 all has 0 conflicts.
DatacenterSimple2 has a maximum of 80 hosts powered on, with a total usage time of 146414166.776000 ms for all hosts
DatacenterSimple3's TCO = 26679.527089
DatacenterSimple3 all has 0 conflicts.
DatacenterSimple3 has a maximum of 730 hosts powered on, with a total usage time of 1239213441.652999 ms for all hosts
DatacenterSimple4's TCO = 2891.778221
DatacenterSimple4 all has 0 conflicts.
DatacenterSimple4 has a maximum of 43 hosts powered on, with a total usage time of 80201757.490000 ms for all hosts
DatacenterSimple5's TCO = 22590.422036
DatacenterSimple5 all has 0 conflicts.
DatacenterSimple5 has a maximum of 195 hosts powered on, with a total usage time of 372134259.319000 ms for all hosts
DatacenterSimple6's TCO = 3281.185790
DatacenterSimple6 all has 0 conflicts.
DatacenterSimple6 has a maximum of 42 hosts powered on, with a total usage time of 74732867.784000 ms for all hosts
DatacenterSimple7's TCO = 31855.849544
DatacenterSimple7 all has 0 conflicts.
DatacenterSimple7 has a maximum of 209 hosts powered on, with a total usage time of 400580458.780000 ms for all hosts
All TCO = 133070.313862
========================================================================================================================

========================================================================================================================
8 datacenter 2 collaborations {0， 1， 2， 3} {4， 5， 6， 7}
------------------------------------------------------------------------------------------------------------------------
DatacenterSimple0's TCO = 46306.936105
DatacenterSimple0 all has 0 conflicts.
DatacenterSimple0 has a maximum of 536 hosts powered on, with a total usage time of 1001503696.869000 ms for all hosts
DatacenterSimple1's TCO = 1530.849036
DatacenterSimple1 all has 0 conflicts.
DatacenterSimple1 has a maximum of 14 hosts powered on, with a total usage time of 27083044.722000 ms for all hosts
DatacenterSimple2's TCO = 12348.811744
DatacenterSimple2 all has 0 conflicts.
DatacenterSimple2 has a maximum of 80 hosts powered on, with a total usage time of 146190251.199000 ms for all hosts
DatacenterSimple3's TCO = 7456.456795
DatacenterSimple3 all has 0 conflicts.
DatacenterSimple3 has a maximum of 86 hosts powered on, with a total usage time of 149061718.323000 ms for all hosts
DatacenterSimple4's TCO = 2891.778221
DatacenterSimple4 all has 0 conflicts.
DatacenterSimple4 has a maximum of 43 hosts powered on, with a total usage time of 80201757.490000 ms for all hosts
DatacenterSimple5's TCO = 46080.467374
DatacenterSimple5 all has 0 conflicts.
DatacenterSimple5 has a maximum of 398 hosts powered on, with a total usage time of 759422767.467000 ms for all hosts
DatacenterSimple6's TCO = 3262.055619
DatacenterSimple6 all has 0 conflicts.
DatacenterSimple6 has a maximum of 40 hosts powered on, with a total usage time of 72019438.101000 ms for all hosts
DatacenterSimple7's TCO = 875.281691
DatacenterSimple7 all has 0 conflicts.
DatacenterSimple7 has a maximum of 7 hosts powered on, with a total usage time of 13762942.65000 ms for all hosts
All TCO = 120752.636585
========================================================================================================================


========================================================================================================================
8 datacenter 1 collaborations {0， 1， 2， 3， 4， 5， 6， 7}
------------------------------------------------------------------------------------------------------------------------
DatacenterSimple0's TCO = 86133.026356
DatacenterSimple0 all has 0 conflicts.
DatacenterSimple0 has a maximum of 912 hosts powered on, with a total usage time of 1722042498.467000 ms for all hosts
DatacenterSimple1's TCO = 1530.849036
DatacenterSimple1 all has 0 conflicts.
DatacenterSimple1 has a maximum of 14 hosts powered on, with a total usage time of 27083044.722000 ms for all hosts
DatacenterSimple2's TCO = 12348.811744
DatacenterSimple2 all has 0 conflicts.
DatacenterSimple2 has a maximum of 80 hosts powered on, with a total usage time of 146649994.201000 ms for all hosts
DatacenterSimple3's TCO = 7456.456795
DatacenterSimple3 all has 0 conflicts.
DatacenterSimple3 has a maximum of 86 hosts powered on, with a total usage time of 149061718.323000 ms for all hosts
DatacenterSimple4's TCO = 2891.778221
DatacenterSimple4 all has 0 conflicts.
DatacenterSimple4 has a maximum of 43 hosts powered on, with a total usage time of 80201757.490000 ms for all hosts
DatacenterSimple5's TCO = 2049.597331
DatacenterSimple5 all has 0 conflicts.
DatacenterSimple5 has a maximum of 22 hosts powered on, with a total usage time of 40433807.485000 ms for all hosts
DatacenterSimple6's TCO = 3262.055619
DatacenterSimple6 all has 0 conflicts.
DatacenterSimple6 has a maximum of 40 hosts powered on, with a total usage time of 72019438.101000 ms for all hosts
DatacenterSimple7's TCO = 875.281691
DatacenterSimple7 all has 0 conflicts.
DatacenterSimple7 has a maximum of 7 hosts powered on, with a total usage time of 13762942.65000 ms for all hosts
All TCO = 116547.856794
========================================================================================================================
==================
1 collaborations
21
2 collaborations
13

 */