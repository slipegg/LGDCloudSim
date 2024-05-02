package org.lgdcloudsim.record;

import org.lgdcloudsim.request.Instance;
import org.lgdcloudsim.request.InstanceGroup;
import org.lgdcloudsim.request.UserRequest;

import java.util.List;

/**
 * It is an implementation of the SqlRecord interface.
 * If you do not want to record the simulation information, you can use this class.
 *
 * @author Anonymous
 * @since LGDCloudSim 1.0
 */
public class SqlRecordNull implements SqlRecord {
    public SqlRecordNull() {
    }

    @Override
    public String getDbPath() {
        return null;
    }

    @Override
    public void setDbPath(String dbPath) {

    }

    @Override
    public void recordUserRequestsSubmitInfo(List<UserRequest> userRequests) {

    }

    @Override
    public void recordUserRequestFinishInfo(UserRequest userRequest) {

    }

    @Override
    public void recordInstanceGroupsReceivedInfo(List instanceGroups) {

    }

    @Override
    public void recordInstanceGroupFinishInfo(InstanceGroup instanceGroup) {

    }

    @Override
    public void recordInstanceGroupAllInfo(InstanceGroup instanceGroup) {

    }

    @Override
    public void recordInstanceGroupGraphAllocateInfo(int srcDcId, int srcInstanceGroupId, int dstDcId, int dstInstanceGroupId, double bw, double startTime) {

    }

    @Override
    public void recordInstanceGroupsGraph(List<InstanceGroup> instanceGroups) {

    }

    @Override
    public void recordInstanceGroupGraphReleaseInfoForFailedUserRequest(int srcInstanceGroupId, int dstInstanceGroupId) {
        
    }

    @Override
    public void recordInstanceGroupGraphReleaseInfo(int srcInstanceGroupId, int srcDcId, int dstInstanceGroupId, int dstDcId, double finishTime) {

    }

    @Override
    public void recordInstancesCreateInfo(List<?> instancesOrInstanceGroups) {

    }

    @Override
    public void recordInstancesFinishInfo(List<Instance> instances) {

    }

    @Override
    public void recordInstancesAllInfo(List<Instance> instances) {
    }

    @Override
    public void recordConflict(double time, int sum) {

    }

    @Override
    public void updateMigratedInstanceGroupInfo(List<InstanceGroup> instanceGroups) {

    }

    @Override
    public void close() {

    }
}
