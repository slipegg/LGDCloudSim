package org.example.request;

import ch.qos.logback.classic.Level;
import org.lgdcloudsim.core.CloudSim;
import org.lgdcloudsim.core.Factory;
import org.lgdcloudsim.core.FactorySimple;
import org.lgdcloudsim.core.Simulation;
import org.lgdcloudsim.datacenter.InitDatacenter;
import org.lgdcloudsim.network.NetworkTopology;
import org.lgdcloudsim.network.NetworkTopologySimple;
import org.lgdcloudsim.request.RandomUserRequestGenerator;
import org.lgdcloudsim.request.UserRequest;
import org.lgdcloudsim.request.UserRequestGenerator;
import org.lgdcloudsim.user.UserRequestManager;
import org.lgdcloudsim.user.UserSimple;
import org.lgdcloudsim.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Here is an example on how to generate affinity requests by code.
 *
 * @author Anonymous
 * @since LGDCloudSim 1.0
 */
public class InitRequestByCode {
    private static final String DATACENTER_CONFIG_FILE = "./src/main/resources/example/Request/DatacentersConfig.json";
    private static final String DATACENTER_BW_FILE = "./src/main/resources/example/Request/DatacenterBwConfig.csv";
    private static final String REGION_DELAY_FILE = "./src/main/resources/regionDelay.csv";
    private static final String AREA_DELAY_FILE = "./src/main/resources/areaDelay.csv";

    private final Simulation lgdcloudsim;
    private final Factory factory;

    public static void main(String[] args) {
        new InitRequestByCode();
    }

    private InitRequestByCode() {
        Log.setLevel(Level.INFO);
        lgdcloudsim = new CloudSim();
        factory = new FactorySimple();

        initUser();
        initDatacenters();
        initNetwork();

        lgdcloudsim.start();
    }

    private void initDatacenters() {
        InitDatacenter.initDatacenters(lgdcloudsim, factory, DATACENTER_CONFIG_FILE);
    }

    private void initNetwork() {
        NetworkTopology networkTopology = new NetworkTopologySimple(REGION_DELAY_FILE, AREA_DELAY_FILE, DATACENTER_BW_FILE);
        lgdcloudsim.setNetworkTopology(networkTopology);
    }

    private class UserRequestManagerTmp implements UserRequestManager {
        int maxSendTimes = 10;

        int sendTimes = 0;

        @Override
        public Map<Integer, List<UserRequest>> generateOnceUserRequests() {
            if (sendTimes >= maxSendTimes) {
                return null;
            }

            Map<Integer, List<UserRequest>> sentUserRequests = new HashMap<>();
            UserRequestGenerator userRequestGenerator = new RandomUserRequestGenerator();

            for (int targetDcId = 1; targetDcId <= 3; targetDcId++) {
                List<UserRequest> userRequests = new ArrayList<>();
                for (int i = 0; i < 3; i++) {
                    userRequests.add(userRequestGenerator.generateAUserRequest());
                }
                sentUserRequests.put(targetDcId, userRequests);
            }

            sendTimes++;
            return sentUserRequests;
        }

        @Override
        public double getNextSendTime() {
            return sendTimes * 100;
        }

    }

    private void initUser() {
        UserRequestManager userRequestManager = new UserRequestManagerTmp();
        new UserSimple(lgdcloudsim, userRequestManager);
    }
}
