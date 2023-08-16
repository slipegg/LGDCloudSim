package org.cpnsim.user;

import org.cpnsim.request.UserRequest;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface UserRequestManager {
    Logger LOGGER = LoggerFactory.getLogger(UserRequestManager.class.getSimpleName());

    Map<Integer, List<UserRequest>> generateOnceUserRequests();

    double getNextSendTime();
}
