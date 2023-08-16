package org.cpnsim.record;

import org.cpnsim.request.Instance;
import org.cpnsim.request.InstanceGroup;
import org.cpnsim.request.UserRequest;

import java.util.List;
import java.util.Map;

public interface SqlRecord {
    String getDbPath();

    void setDbPath(String dbPath);

    void recordUserRequestsSubmitinfo(List<UserRequest> userRequests);

    void recordUserRequestFinishInfo(UserRequest userRequest);

    void recordInstanceGroupsReceivedInfo(List<InstanceGroup> instanceGroups);

    void recordInstanceGroupFinishInfo(InstanceGroup instanceGroup);

    void recordInstanceGroupAllInfo(InstanceGroup instanceGroup);

    void recordInstanceGroupGraphAllocateInfo(int srcDcId, int srcInstanceGroupId, int dstDcId, int dstInstanceGroupId, double bw, double startTime);

    void recordInstanceGroupGraphReleaseInfo(int srcDcId, int dstDcId, double finishTime);

    void recordInstancesCreateInfo(Map<Integer, List<Instance>> instances);

    void recordInstancesFinishInfo(List<Instance> instances);

    void recordInstancesAllInfo(List<Instance> instances);

    void close();
}
