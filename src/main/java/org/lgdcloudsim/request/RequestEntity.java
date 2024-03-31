package org.lgdcloudsim.request;

import org.lgdcloudsim.core.ChangeableId;

/**
 * {@link Instance}, {@link InstanceGroup} and {@link InstanceGroupGraph} in user requests
 * all need to extend this interface.
 * So that they can find the user request to which they belong and set their own id.
 *
 * @author Anonymous
 * @since LGDCloudSim 1.0
 */

public interface RequestEntity extends ChangeableId {
    /**
     * Get the user request to which the request entity belongs.
     *
     * @return the user request to which the request entity belongs.
     */
    UserRequest getUserRequest();

    /**
     * Set the user request to which the request entity belongs.
     * @param userRequest the user request to which the request entity belongs.
     */
    void setUserRequest(UserRequest userRequest);
}
