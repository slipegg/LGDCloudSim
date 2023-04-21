package org.scalecloudsim.user;

import org.scalecloudsim.request.UserRequest;

import java.util.List;

public interface UserRequestManager {
    List<UserRequest> getUserRequestMap(double startTime, double endTime, int datacenterId);//时间前闭后开，dcId前闭后闭
}
