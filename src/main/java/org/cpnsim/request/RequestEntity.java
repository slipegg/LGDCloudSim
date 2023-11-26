package org.cpnsim.request;

import org.cpnsim.core.ChangeableId;

public interface RequestEntity extends ChangeableId {
    UserRequest getUserRequest();

    void setUserRequest(UserRequest userRequest);
}
