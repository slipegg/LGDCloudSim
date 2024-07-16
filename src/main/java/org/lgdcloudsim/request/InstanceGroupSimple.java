package org.lgdcloudsim.request;

import lombok.Getter;
import lombok.Setter;
import org.lgdcloudsim.datacenter.Datacenter;

import java.util.ArrayList;
import java.util.List;

/**
 * InstanceGroupSimple is a simple implementation of the {@link InstanceGroup} interface.
 *
 * @author Anonymous
 * @since LGDCloudSim 1.0
 */

@Getter
@Setter
public class InstanceGroupSimple implements InstanceGroup {
    /**
     * The id of the instance group. Each instance group has a unique id.
     */
    int id;

    /**
     * The user request to which the instance group belongs.
     */
    UserRequest userRequest;

    List<Instance> instances;

    int destDatacenterId;

    double accessLatency;

    @Getter
    long storageSum;

    @Getter
    long bwSum;

    @Getter
    long cpuSum;

    @Getter
    long ramSum;

    int retryMaxNum;

    int retryNum;

    int state;

    Datacenter receiveDatacenter;

    double receivedTime;

    double finishTime;

    @Getter
    double interScheduleEndTime;

    int successInstanceNum;

    List<Integer> forwardDatacenterIdsHistory;

    /**
     * Create an instance group with the specified id.
     * The instances in the instance group are initially empty.
     *
     * @param id the id of the instance group.
     */
    public InstanceGroupSimple(int id) {
        this.id = id;
        this.instances = new ArrayList<>();
        this.retryNum = 0;
        this.retryMaxNum = 0;
        this.state = UserRequest.WAITING;
        this.accessLatency = Double.MAX_VALUE;
        this.receiveDatacenter = Datacenter.NULL;
        this.receivedTime = -1;
        this.finishTime = -1;
        this.successInstanceNum = 0;
        this.forwardDatacenterIdsHistory = new ArrayList<>();
        this.destDatacenterId = -1;
    }

    /**
     * Create an instance group with the specified id and instances.
     *
     * @param id        the id of the instance group.
     * @param instances the instances in the instance group.
     */
    public InstanceGroupSimple(int id, List<Instance> instances) {
        this(id);
        setInstances(instances);
    }

    @Override
    public void setUserRequest(UserRequest userRequest) {
        this.userRequest = userRequest;
        for (Instance instance : instances) {
            instance.setUserRequest(userRequest);
        }
    }

    /**
     * Set the instances in the instance group
     * and recalculate the sum of the storage, bandwidth, CPU, and memory required by the instances in the instance group.
     *
     * @param instanceList the instances in the instance group.
     * @return the instance group itself.
     */
    public InstanceGroup setInstances(List<Instance> instanceList) {
        this.instances = instanceList;
        this.storageSum = 0;
        this.bwSum = 0;
        this.cpuSum = 0;
        this.ramSum = 0;
        for (Instance instance : instanceList) {
            storageSum += instance.getStorage();
            bwSum += instance.getBw();
            cpuSum += instance.getCpu();
            ramSum += instance.getRam();
            instance.setInstanceGroup(this);
        }
        return this;
    }

    @Override
    public boolean isSetDestDatacenter() {
        return destDatacenterId != -1;
    }

    @Override
    public InstanceGroup addRetryNum() {
        this.retryNum++;
        if (this.retryNum > this.retryMaxNum) {
            this.state = UserRequest.FAILED;
        }
        return this;
    }

    @Override
    public boolean isFailed() {
        return this.state == UserRequest.FAILED;
    }

    @Override
    public InstanceGroup addSuccessInstanceNum() {
        this.successInstanceNum++;
        if (this.successInstanceNum == this.instances.size()) {
            this.state = UserRequest.SUCCESS;
        }
        return this;
    }

    @Override
    public InstanceGroup addForwardDatacenterIdHistory(int datacenterId) {
        this.forwardDatacenterIdsHistory.add(datacenterId);
        return this;
    }

    @Override
    public boolean isNetworkLimited() {
        return this.accessLatency != Double.MAX_VALUE || getUserRequest().getInstanceGroupGraph().isEdgeLinked(this);
    }

    @Override
    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "InstanceGroupSimple [id=" + id
                + ", instanceList=" + instances + "]";
    }

    @Override
    public InstanceGroup setInterScheduleEndTime(double interScheduleEndTime) {
        this.interScheduleEndTime = interScheduleEndTime;
        return this;
    }
}
