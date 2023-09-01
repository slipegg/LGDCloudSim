package org.cpnsim.request;

import lombok.Getter;
import lombok.Setter;
import org.cpnsim.datacenter.Datacenter;

import java.util.ArrayList;
import java.util.List;

@Getter@Setter
public class InstanceGroupSimple implements InstanceGroup{
    int id;
    UserRequest userRequest;
    List<Instance> instanceList;
    int groupType;
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
    int retryNum;
    int retryMaxNum;
    int state;
    Datacenter receiveDatacenter;
    double receivedTime;
    double finishTime;
    int successInstanceNum;

    public InstanceGroupSimple(int id) {
        this.id = id;
        this.instanceList = new ArrayList<>();
        this.groupType = 0;
        this.retryNum = 0;
        this.retryMaxNum = 3;
        this.state = UserRequest.WAITING;
        this.accessLatency = Double.MAX_VALUE;
        this.receiveDatacenter = Datacenter.NULL;
        this.receivedTime = -1;
        this.finishTime = -1;
        this.successInstanceNum = 0;
    }

    public InstanceGroupSimple(int id, List<Instance> instanceList) {
        this(id);
        setInstanceList(instanceList);
    }

    @Override
    public void setUserRequest(UserRequest userRequest) {
        this.userRequest = userRequest;
        for (Instance instance : instanceList) {
            instance.setUserRequest(userRequest);
        }
    }

    @Override
    public InstanceGroup setInstanceList(List<Instance> instanceList) {
        this.instanceList = instanceList;
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
    public InstanceGroup addRetryNum() {
        this.retryNum++;
        if (this.retryNum >= this.retryMaxNum) {
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
        if (this.successInstanceNum == this.instanceList.size()) {
            this.state = UserRequest.SUCCESS;
        }
        return this;
    }

    @Override
    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "InstanceGroupSimple [id=" + id
                + ", instanceList=" + instanceList + "]";
    }
}
