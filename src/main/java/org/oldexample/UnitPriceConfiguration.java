package org.oldexample;

import ch.qos.logback.classic.Level;
import org.lgdcloudsim.core.CloudSim;
import org.lgdcloudsim.core.Factory;
import org.lgdcloudsim.core.FactorySimple;
import org.lgdcloudsim.core.Simulation;
import org.lgdcloudsim.datacenter.InitDatacenter;
import org.lgdcloudsim.network.NetworkTopology;
import org.lgdcloudsim.record.MemoryRecord;
import org.lgdcloudsim.user.UserRequestManager;
import org.lgdcloudsim.user.UserRequestManagerCsv;
import org.lgdcloudsim.user.UserSimple;
import org.lgdcloudsim.util.Log;

/**
 * A class to configure the simulation of setting the unit price of the datacenter via file.
 * When the lifecycle of the instance in your user request is infinite, you can set the long-term rental price of each CPU, Ram, Storage, and BW in the file.
 * When the lifecycle of the instances in your user requests is limited, you can set the price per second of renting each CPU, Ram, Storage, and BW in the file.
 * Of course, you can also set two at the same time.
 * You need to add the "resourceUnitPrice" field in the data center file.
 * If you do not customize it in the file, the default value will be used, as follows.
 * pricePerCpuPerSec = 1.0;
 * pricePerCpu = 1.0;
 * pricePerRamPerSec = 1.0;
 * pricePerRam = 1.0;
 * pricePerStoragePerSec = 1.0;
 * pricePerStorage = 1.0;
 * pricePerBwPerSec = 1.0;
 * pricePerBw = 1.0;
 * unitRackPrice = 100.0;
 * hostNumPerRack = 10;
 *
 * @author Anonymous
 * @author Anonymous3
 * @since LGDCloudSim 1.0
 */
public class UnitPriceConfiguration {
    Simulation cpnSim;
    Factory factory;
    UserSimple user;
    UserRequestManager userRequestManager;
    String USER_REQUEST_FILE = "./src/main/resources/experiment/setUnitPriceViaFile/generateRequestParameterInfiniteLife.csv";
    String DATACENTER_CONFIG_FILE = "./src/main/resources/experiment/setUnitPriceViaFile/DatacentersConfigInfiniteLife.json";

    //    String USER_REQUEST_FILE = "./src/main/resources/experiment/setUnitPriceViaFile/generateRequestParameterLimitedLife.csv";
//    String DATACENTER_CONFIG_FILE = "./src/main/resources/experiment/setUnitPriceViaFile/DatacentersConfigLimitedLife.json";
    public static void main(String[] args) {
        new UnitPriceConfiguration();
    }

    private UnitPriceConfiguration() {
        double start = System.currentTimeMillis();
        Log.setLevel(Level.INFO);
        cpnSim = new CloudSim();
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
        userRequestManager = new UserRequestManagerCsv(USER_REQUEST_FILE);
        user = new UserSimple(cpnSim, userRequestManager);
    }

    private void initDatacenters() {
        InitDatacenter.initDatacenters(cpnSim, factory, DATACENTER_CONFIG_FILE);
    }

    private void initNetwork() {
        cpnSim.setNetworkTopology(NetworkTopology.NULL);
    }
}
