package org.example.DLJTrace;

import ch.qos.logback.classic.Level;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Map;

import org.lgdcloudsim.core.CloudSim;
import org.lgdcloudsim.core.Factory;
import org.lgdcloudsim.core.FactorySimple;
import org.lgdcloudsim.core.Simulation;
import org.lgdcloudsim.datacenter.InitDatacenter;
import org.lgdcloudsim.network.NetworkTopologySimple;
import org.lgdcloudsim.record.SqlRecord;
import org.lgdcloudsim.record.SqlRecordSimple;
import org.lgdcloudsim.user.UserRequestManager;
import org.lgdcloudsim.user.UserRequestManagerDLJTrace;
import org.lgdcloudsim.user.UserSimple;
import org.lgdcloudsim.util.Log;

/**
 * /usr/bin/env /root/.jdks/corretto-17.0.8.1/bin/java @/tmp/cp_ccct2spureqg2mcwrrm7kqh1k.argfile org.example.alibabaTrace.AlibabaTraceExample 
 *
 * @author Anonymous
 * @since LGDCloudSim 1.0
 */
public class DLJTraceTest {
    private static final String DATACENTER_BW_FILE = "./src/main/resources/DatacenterBwConfig.csv";
    private static final String REGION_DELAY_FILE = "./src/main/resources/regionDelay.csv";
    private static final String AREA_DELAY_FILE = "./src/main/resources/areaDelay.csv";
    private final Simulation lgdcloudsim;
    private final Factory factory;

    public static void main(String[] args) {
        if (args.length < 4) {
            System.out.println("用法: java BasicTest <实验名称> <Datacenter文件路径> <HostTopology文件路径> <UserRequest文件路径>");
            System.exit(1);
        }

        if (args.length == 4) {
            new DLJTraceTest(args[0], args[1], args[2], args[3]);
        } else {
            new DLJTraceTest(args[0], args[1], args[2], args[3]);
        }
    }

    private DLJTraceTest (String name, String datacenterConfigFile, String hostTopologyFile, String jobFile) {
        try {
            initCreateFolder();
            System.setOut(new PrintStream(new FileOutputStream("RecordDb/logs/" + name + ".log")));
        } catch (Exception e) {
            System.err.println("无法设置输出文件: " + e.getMessage());
            System.exit(1);
        }

        System.out.println("Name = " + name + " datacenterConfigFile = " + datacenterConfigFile + " hostTopologyFile = " + hostTopologyFile + " jobFile = "+ jobFile);

        Log.setLevel(Level.INFO);
        factory = new FactorySimple();

        lgdcloudsim = new CloudSim();
        SqlRecord sqlRecord = new SqlRecordSimple(name + ".db");
        sqlRecord.setNeedRecordDatacenterUtilization(true);
        sqlRecord.setRecordDatacenterUtilizationInterval(5*60*1000); // 5 minutes
        lgdcloudsim.setSqlRecord(sqlRecord);

        initUser(jobFile);
        initDatacenters(datacenterConfigFile);
        initNetwork(hostTopologyFile);

        lgdcloudsim.start();
    }

    private void initCreateFolder(){
        File file = new File("RecordDb");
        if (!file.exists()) {
            file.mkdirs();
        }
        file = new File("RecordDb/logs");
        if (!file.exists()) {
            file.mkdirs();
        }
    }

    private void initUser(String jobFile) {
        Map<Integer, String> dc_job_map = Map.of(
            1, jobFile
        );
        UserRequestManager userRequestManager = new UserRequestManagerDLJTrace(dc_job_map);
        new UserSimple(lgdcloudsim, userRequestManager);
    }

    private void initDatacenters(String datacenterFile) {
        InitDatacenter.initDatacenters(lgdcloudsim, factory, datacenterFile);
    }

    private void initNetwork(String hostTopoFile) {
        lgdcloudsim.setNetworkTopology(new NetworkTopologySimple(REGION_DELAY_FILE, AREA_DELAY_FILE, DATACENTER_BW_FILE, hostTopoFile));
    }
}
