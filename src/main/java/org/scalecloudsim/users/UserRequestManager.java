package org.scalecloudsim.users;

import org.scalecloudsim.Instances.UserRequest;

import java.util.List;
import java.util.Map;

public interface UserRequestManager {
    Map<Double, List<UserRequest>> getUserRequestMap(double startTime, double endTime, int datacenterId);//时间前闭后开，dcId前闭后闭
}
