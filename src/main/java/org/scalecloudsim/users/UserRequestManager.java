package org.scalecloudsim.users;

import org.scalecloudsim.Instances.UserRequest;

import java.util.List;

public interface UserRequestManager {
    List<UserRequest> getUserRequests(double startTime,double endTime);
}
