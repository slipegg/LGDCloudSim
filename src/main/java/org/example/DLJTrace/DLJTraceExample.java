package org.example.DLJTrace;

import ch.qos.logback.classic.Level;

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
public class DLJTraceExample {
    private static final String DATACENTER_CONFIG_FILE = "./src/main/resources/example/DLJTrace/small/datacenter.json";
    private static final String DATACENTER_BW_FILE = "./src/main/resources/DatacenterBwConfig.csv";
    private static final String REGION_DELAY_FILE = "./src/main/resources/regionDelay.csv";
    private static final String AREA_DELAY_FILE = "./src/main/resources/areaDelay.csv";
    private static final String HOST_TOPO_FILE = "./src/main/resources/example/DLJTrace/small/HostTopoConfig.csv";
    private static final Map<Integer, String> DC_JOB_MAP = Map.of(
        1, "./src/main/resources/example/DLJTrace/small/job.csv"
    );
    private final Simulation lgdcloudsim;
    private final Factory factory;

    public static void main(String[] args) {
        new DLJTraceExample();
    }

    private DLJTraceExample() {
        Log.setLevel(Level.INFO);
        factory = new FactorySimple();

        lgdcloudsim = new CloudSim();
        SqlRecord sqlRecord = new SqlRecordSimple("DLJTrace.db");
        lgdcloudsim.setSqlRecord(sqlRecord);

        initUser();
        initDatacenters();
        initNetwork();

        lgdcloudsim.start();
    }

    private void initUser() {
        UserRequestManager userRequestManager = new UserRequestManagerDLJTrace(DC_JOB_MAP);
        new UserSimple(lgdcloudsim, userRequestManager);
    }

    private void initDatacenters() {
        InitDatacenter.initDatacenters(lgdcloudsim, factory, DATACENTER_CONFIG_FILE);
    }

    private void initNetwork() {
        lgdcloudsim.setNetworkTopology(new NetworkTopologySimple(REGION_DELAY_FILE, AREA_DELAY_FILE, DATACENTER_BW_FILE, HOST_TOPO_FILE));
    }
}
