package org.cpnsim.record;

import org.cpnsim.request.Instance;
import org.cpnsim.request.InstanceGroup;
import org.cpnsim.request.UserRequest;

import java.util.List;
import java.util.Map;

public class SqlRecordNull implements SqlRecord {
    private double instanceDelaySum = 0.0;
    private long instanceNum = 0L;

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
    public void recordUserRequestsSubmitinfo(List<UserRequest> userRequests) {

    }

    @Override
    public void recordUserRequestFinishInfo(UserRequest userRequest) {

    }

    @Override
    public void recordInstanceGroupsReceivedInfo(List<InstanceGroup> instanceGroups) {

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
    public void recordInstanceGroupGraphReleaseInfo(int srcDcId, int dstDcId, double finishTime) {

    }

    @Override
    public void recordInstancesCreateInfo(Map<Integer, List<Instance>> instances) {
        for (List<Instance> instanceList : instances.values()) {
            for (Instance instance : instanceList) {
                instanceDelaySum += instance.getStartTime() - instance.getInstanceGroup().getReceivedTime();
                instanceNum++;
            }
        }
    }

    @Override
    public void recordInstancesFinishInfo(List<Instance> instances) {

    }

    @Override
    public void recordInstancesAllInfo(List<Instance> instances) {
        for (Instance instance : instances) {
            if (instance.getStartTime() == -1) {
                instanceDelaySum += instance.getStartTime() - instance.getInstanceGroup().getReceivedTime();
                instanceNum++;
            }
        }
    }

    @Override
    public void recordConflict(double time, int sum) {

    }

    @Override
    public double getAvgInstanceSubmitDelay() {
        System.out.println("instanceDelaySum: " + instanceDelaySum);
        System.out.println("instanceNum: " + instanceNum);
        return instanceDelaySum / instanceNum;
    }

    @Override
    public void close() {

    }
}
