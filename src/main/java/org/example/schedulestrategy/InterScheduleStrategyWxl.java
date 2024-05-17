package org.example.schedulestrategy;

import ch.qos.logback.classic.Level;
import org.lgdcloudsim.core.CloudSim;
import org.lgdcloudsim.core.Factory;
import org.lgdcloudsim.core.FactorySimple;
import org.lgdcloudsim.core.Simulation;
import org.lgdcloudsim.datacenter.Datacenter;
import org.lgdcloudsim.datacenter.InitDatacenter;
import org.lgdcloudsim.interscheduler.wxl.DatacenterDTO;
import org.lgdcloudsim.network.NetworkTopology;
import org.lgdcloudsim.network.NetworkTopologySimple;
import org.lgdcloudsim.user.UserRequestManager;
import org.lgdcloudsim.user.UserRequestManagerCsv;
import org.lgdcloudsim.user.UserSimple;
import org.lgdcloudsim.util.Client;
import org.lgdcloudsim.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * You can customize InterScheduler's scheduling strategy,
 * see {@link org.lgdcloudsim.interscheduler.InterScheduler} and {@link FactorySimple} for more details.
 * By setting the "type" field in InterScheduler in the data center config file,
 * we can modify the scheduling algorithm of InterScheduler.
 *
 * @author Anonymous
 * @since LGDCloudSim 1.0
 */
public class InterScheduleStrategyWxl {
    private static final String scheduleStrategy = "wxl";
//    private static final String scheduleStrategy = "leastRequested";

    //The "types" field of centerScheduler in different files are different,
    // and the scheduling algorithms called are also different.
//    private static final String DATACENTER_CONFIG_FILE = "./src/main/resources/example/ScheduleStrategy/InterScheduleStrategy/wxl/DatacentersConfig-" + scheduleStrategy + ".json";
    private static final String DATACENTER_CONFIG_FILE = "./src/main/resources/example/ScheduleStrategy/InterScheduleStrategy/reginless_example/DatacentersConfig.json";
//    private static final String USER_REQUEST_FILE = "./src/main/resources/example/ScheduleStrategy/InterScheduleStrategy/wxl/generateRequestParameter.csv";
    private static final String USER_REQUEST_FILE = "./src/main/resources/example/ScheduleStrategy/InterScheduleStrategy/reginless_example/generateRequestParameter.csv";
    private static final String DATACENTER_BW_FILE = "./src/main/resources/example/ScheduleStrategy/InterScheduleStrategy/reginless_example/DatacenterBwConfig.csv";
    private static final String REGION_DELAY_FILE = "./src/main/resources/regionDelay.csv";
    private static final String AREA_DELAY_FILE = "./src/main/resources/example/ScheduleStrategy/InterScheduleStrategy/reginless_example/areaDelay.csv";

    private final Simulation lgdcloudsim;
    private final Factory factory;

    public static void main(String[] args) {
        new InterScheduleStrategyWxl();
    }

    private InterScheduleStrategyWxl() {
        Log.setLevel(Level.INFO);
        lgdcloudsim = new CloudSim();
        factory = new FactorySimple();

        initUser();
        initDatacenters();
        initNetwork();

        List<DatacenterDTO> datacenterDTOList=new ArrayList<>();
        for(Datacenter datacenter: lgdcloudsim.getCollaborationManager().getDatacenters(1)){
            DatacenterDTO datacenterDTO=new DatacenterDTO();
            datacenterDTO.setId(datacenter.getId());
            datacenterDTO.setCpuPrice(datacenter.getPricePerCpuPerSec());
            datacenterDTO.setStoPrice(datacenter.getPricePerStoragePerSec());
            datacenterDTOList.add(datacenterDTO);
            System.out.println(datacenterDTO);
        }
        Client.request("init", datacenterDTOList);

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
