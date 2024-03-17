package org.cpnsim.request;

/**
 * A simple user request generator to generate a user request.
 *
 * @author Jiawen Liu
 * @since LGDCloudSim 1.0
 */
public interface UserRequestGenerator {
    /**
     * Generate a user request.
     *
     * @return a user request.
     */
    UserRequest generateAUserRequest();
}
