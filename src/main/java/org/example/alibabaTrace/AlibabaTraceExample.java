package org.example.alibabaTrace;

import ch.qos.logback.classic.Level;
import org.lgdcloudsim.core.CloudSim;
import org.lgdcloudsim.core.Factory;
import org.lgdcloudsim.core.FactorySimple;
import org.lgdcloudsim.core.Simulation;
import org.lgdcloudsim.datacenter.InitDatacenter;
import org.lgdcloudsim.network.NetworkTopology;
import org.lgdcloudsim.record.SqlRecord;
import org.lgdcloudsim.record.SqlRecordSimple;
import org.lgdcloudsim.user.UserRequestManagerAlibabaTrace;
import org.lgdcloudsim.user.UserSimple;
import org.lgdcloudsim.util.Log;

/**
 * /usr/bin/env /root/.jdks/corretto-17.0.8.1/bin/java @/tmp/cp_ccct2spureqg2mcwrrm7kqh1k.argfile org.example.alibabaTrace.AlibabaTraceExample 
 *
 * @author Anonymous
 * @since LGDCloudSim 1.0
 */
public class AlibabaTraceExample {
    private static final String DATACENTER_CONFIG_FILE = "./src/main/resources/example/Alibaba2018Trace/datacenter.json";
    private static final String BATCH_JOB_FILE = "./src/main/resources/example/Alibaba2018Trace/tinyBatchJob.csv";
    private static final String CONTAINER_FILE = "./src/main/resources/example/Alibaba2018Trace/tinyContainer.csv";
    private static final int DATACENTER_ID = 1;
    
    private final Simulation lgdcloudsim;
    private final Factory factory;

    public static void main(String[] args) {
        new AlibabaTraceExample();
    }

    private AlibabaTraceExample() {
        Log.setLevel(Level.INFO);
        factory = new FactorySimple();

        lgdcloudsim = new CloudSim();
        SqlRecord sqlRecord = new SqlRecordSimple("alibaba.db");
        lgdcloudsim.setSqlRecord(sqlRecord);

        initUser();
        initDatacenters();
        initNetwork();

        lgdcloudsim.start();
    }

    private void initUser() {
        UserRequestManagerAlibabaTrace userRequestManager = new UserRequestManagerAlibabaTrace(BATCH_JOB_FILE, CONTAINER_FILE, DATACENTER_ID, -1, -1);
        // userRequestManager.setContainerSubmitAccelerationRatio(0.01);
        // userRequestManager.setContainerCpuScale(2);
        new UserSimple(lgdcloudsim, userRequestManager);
    }

    private void initDatacenters() {
        InitDatacenter.initDatacenters(lgdcloudsim, factory, DATACENTER_CONFIG_FILE);
    }

    private void initNetwork() {
        lgdcloudsim.setNetworkTopology(NetworkTopology.NULL);
    }
}
