package org.lgdcloudsim.queue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.lgdcloudsim.request.Instance;
import org.lgdcloudsim.request.InstanceGroup;
import org.lgdcloudsim.request.UserRequest;

public class InstanceQueueGroup extends InstanceQueueFifo {
    int instanceGroupNum;
    public InstanceQueueGroup(int instanceGroupNum) {
        super();
        this.instanceGroupNum = instanceGroupNum;
    }
    @Override
    public QueueResult<Instance> getItems(int num, double nowTime) {
        List<Instance> sendInstances = new ArrayList<>();
        Set<UserRequest> failedUserRequests = new HashSet<>();

        InstanceGroup targetInstanceGroup = null;
        int groupCount = 0;
        while (!getInstances().isEmpty()) {
            UserRequest userRequest = getInstances().get(0).getUserRequest();
            if (userRequest.getState() == UserRequest.FAILED) {
                getInstances().remove(0);
                continue;
            }

            if (checkOutdatedFlag && userRequest.getScheduleDelayLimit() > 0 && nowTime - userRequest.getSubmitTime() > userRequest.getScheduleDelayLimit()) {
                failedUserRequests.add(userRequest);
                getInstances().remove(0);
                continue;
            }

            if (targetInstanceGroup == null) {
                targetInstanceGroup = getInstances().get(0).getInstanceGroup();
            } else if (getInstances().get(0).getInstanceGroup() != targetInstanceGroup) {
                groupCount++;
                if (groupCount >= instanceGroupNum) {
                    break;
                }
                targetInstanceGroup = getInstances().get(0).getInstanceGroup();
            }

            sendInstances.add(getInstances().remove(0));
        }
        return new QueueResult<Instance>(sendInstances, failedUserRequests);
    }   
}
