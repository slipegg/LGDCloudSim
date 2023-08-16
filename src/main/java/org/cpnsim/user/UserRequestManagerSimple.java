package org.cpnsim.user;

import org.cpnsim.request.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserRequestManagerSimple implements UserRequestManager {
    @Override
    public Map<Integer, List<UserRequest>> generateOnceUserRequests() {
        Map<Integer, List<UserRequest>> userRequestMap = new HashMap<>();
        int[] datacenterIds = {0, 1};
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

    private List<UserRequest> getUserRequestMap(double startTime, double endTime, int datacenterId) {
        List<UserRequest> userRequests = new ArrayList<>();
        UserRequestGenerator userRequestGenerator = new RandomUserRequestGenerator();
        int num = 3;
        for (double time = startTime; time < endTime; time += 10) {
            for (int j = 0; j < num; j++) {
                UserRequest userRequest = userRequestGenerator.generateAUserRequest();
                userRequest.setSubmitTime(time);
                userRequest.setBelongDatacenterId(datacenterId);
                userRequests.add(userRequest);
            }
        }
        return userRequests;
    }
}
