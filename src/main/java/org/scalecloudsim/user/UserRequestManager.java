package org.scalecloudsim.user;

import org.scalecloudsim.request.UserRequest;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface UserRequestManager {
    Logger LOGGER = LoggerFactory.getLogger(UserRequestManager.class.getSimpleName());

    List<UserRequest> getUserRequestMap(double startTime, double endTime, int datacenterId);//时间前闭后开，dcId前闭后闭

    Map<Integer, List<UserRequest>> generateOnceUserRequests();

    double getNextSendTime();
}
