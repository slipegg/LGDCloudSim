package org.lgdcloudsim.user;

import org.lgdcloudsim.request.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * UserRequestManagerSimple is a simple implementation of the {@link UserRequestManager} interface.
 * It uses RandomUserRequestGenerator to generate a batch of user requests every 100ms and sends them to the designated data center 1.
 *
 * @author Anonymous
 * @since LGDCloudSim 1.0
 */
public class UserRequestManagerSimple implements UserRequestManager {
    @Override
    public Map<Integer, List<UserRequest>> generateOnceUserRequests() {
        Map<Integer, List<UserRequest>> userRequestMap = new HashMap<>();
        int[] datacenterIds = {1};
        for (int datacenterId : datacenterIds) {
            List<UserRequest> userRequests = getUserRequestMap(0, 100, datacenterId);
            userRequestMap.put(datacenterId, userRequests);
        }
        return userRequestMap;
    }

    @Override
    public double getNextSendTime() {
        return 100;
    }

    /**
     * Get a list of user requests from the start time to the end time and send them to the designated data center.
     *
     * @param startTime    the start time.
     * @param endTime      the end time.
     * @param datacenterId the designated data center id.
     * @return a list of user requests.
     */
    private List<UserRequest> getUserRequestMap(double startTime, double endTime, int datacenterId) {
        List<UserRequest> userRequests = new ArrayList<>();
        UserRequestGenerator userRequestGenerator = new RandomUserRequestGenerator();
        int num = 3;
        for (int j = 0; j < num; j++) {
            UserRequest userRequest = userRequestGenerator.generateAUserRequest();
            userRequest.setSubmitTime(startTime);
            userRequest.setBelongDatacenterId(datacenterId);
            userRequests.add(userRequest);
        }
        return userRequests;
    }
}
