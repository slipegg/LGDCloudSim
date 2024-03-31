package org.lgdcloudsim.user;

import org.lgdcloudsim.request.UserRequest;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * UserRequestManager is an interface for the user request manager.
 * It is responsible for sending a batch of user requests every once in a while
 *
 * @author Anonymous
 * @since LGDCloudSim 1.0
 */
public interface UserRequestManager {
    /**
     * Logger for this class.
     */
    Logger LOGGER = LoggerFactory.getLogger(UserRequestManager.class.getSimpleName());

    /**
     * Generate a batch of user requests.
     *
     * @return a map of user requests, the key is the data center id which the user requests are sent to, and the value is the list of user requests.
     */
    Map<Integer, List<UserRequest>> generateOnceUserRequests();

    /**
     * Get the next send time.
     * @return the next send time.
     */
    double getNextSendTime();
}
