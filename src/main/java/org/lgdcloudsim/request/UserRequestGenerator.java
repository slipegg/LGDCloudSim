package org.lgdcloudsim.request;

/**
 * A simple user request generator to generate a user request.
 *
 * @author Anonymous
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
