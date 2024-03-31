package org.example.largescale;

import ch.qos.logback.classic.Level;
import org.lgdcloudsim.core.CloudSim;
import org.lgdcloudsim.core.Factory;
import org.lgdcloudsim.core.FactorySimple;
import org.lgdcloudsim.core.Simulation;
import org.lgdcloudsim.datacenter.InitDatacenter;
import org.lgdcloudsim.network.NetworkTopology;
import org.lgdcloudsim.network.NetworkTopologySimple;
import org.lgdcloudsim.record.MemoryRecord;
import org.lgdcloudsim.user.UserRequestManager;
import org.lgdcloudsim.user.UserRequestManagerCsv;
import org.lgdcloudsim.user.UserSimple;
import org.lgdcloudsim.util.Log;

/**
 * Here is an example to test LGDCloudSim's large-scale simulation capabilities.
 * When you want to test the memory and time consumption of LGDCloudSim when simulating different numbers of hosts,
 * you can change the host num in the "DatacentersConfig.json" of the "TestHostOfSingleDC" and "TestHostOfMultiDC" file.
 * In "xxxOfSingleDC", the simulated scenario is a single data center,
 * and in "xxxOfMultiDC", the simulated scenario is 100 data centers.
 * Now, the maximum number of hosts that LGDCloudSim can support is INT_MAX/4 = 536870911.
 * Simulating the maximum number of hosts requires 12GB of memory and takes about 16 seconds to run
 * on a server running the Ubuntu18.04 system and equipped
 * with a 16-core Intel(R) Xeon(R) Gold 6230 CPU @ 2.10GHz and 32GB memory.
 * <p>
 * When you want to test the memory and time consumption of LGDCloudSim when simulating different numbers
 * of simple ordinary request while fixed the num of host to 100000,
 * you can change the "RequestPerNum" which meas the request num sent by the user in the "generateRequestParameter.csv" file
 * of the "TestRequestOfSingleDC" and "TestRequestOfMultiDC" file.
 * <p>
 * When you want to test the maximum number of hosts and request concurrency that LGDCloudSim can support,
 * as well as its memory and time consumption, you can see "TestScaleOfSingleDC" and "TestScaleOfMultiDC".
 * In these files, we set the host num to 536870911. You can change the request num to test the memory and time consumption
 *
 * @author Anonymous
 * @since LGDCloudSim 1.0
 */
public class LargeScaleTest {
    private static final String testType = "TestHostOfSingleDC";
    //    private static final String testType = "TestHostOfMultiDC";
//     private static final String testType = "TestRequestOfSingleDC";
//     private static final String testType = "TestRequestOfMultiDC";
//     private static final String testType = "TestScaleOfSingleDC";
//     private static final String testType = "TestScaleOfMultiDC";
    private static final String DATACENTER_CONFIG_FILE = "./src/main/resources/example/LargeScale/" + testType + "/DatacentersConfig.json";
    private static final String USER_REQUEST_FILE = "./src/main/resources/example/LargeScale/" + testType + "/generateRequestParameter.csv";
    private static final String DATACENTER_BW_FILE = "./src/main/resources/DatacenterBwConfig.csv";
    private static final String REGION_DELAY_FILE = "./src/main/resources/regionDelay.csv";
    private static final String AREA_DELAY_FILE = "./src/main/resources/areaDelay.csv";

    private final Simulation lgdcloudsim;
    private final Factory factory;

    public static void main(String[] args) {
        new LargeScaleTest();
    }

    private LargeScaleTest() {
        double start = System.currentTimeMillis();

        Log.setLevel(Level.OFF);
        lgdcloudsim = new CloudSim();
        lgdcloudsim.setIsSqlRecord(false);
        factory = new FactorySimple();

        initUser();
        initDatacenters();
        initNetwork();

        double endInit = System.currentTimeMillis();

        lgdcloudsim.start();

        double end = System.currentTimeMillis();

        System.out.println("\n运行情况：");
        System.out.println("初始化耗时：" + (endInit - start) / 1000 + "s");
        System.out.println("模拟运行耗时：" + (end - endInit) / 1000 + "s");
        System.out.println("模拟总耗时：" + (end - start) / 1000 + "s");
        System.out.println("运行过程占用最大内存: " + MemoryRecord.getMaxUsedMemory() / 1000000 + " Mb");
    }

    private void initUser() {
        UserRequestManager userRequestManager = new UserRequestManagerCsv(USER_REQUEST_FILE);
        new UserSimple(lgdcloudsim, userRequestManager);
    }

    private void initDatacenters() {
        InitDatacenter.initDatacenters(lgdcloudsim, factory, DATACENTER_CONFIG_FILE);
    }

    private void initNetwork() {
        NetworkTopology networkTopology;
        if (testType.contains("MultiDC")) {
            networkTopology = new NetworkTopologySimple(REGION_DELAY_FILE, AREA_DELAY_FILE, DATACENTER_BW_FILE);
        } else {
            networkTopology = NetworkTopology.NULL;
        }

        lgdcloudsim.setNetworkTopology(networkTopology);
    }
}
