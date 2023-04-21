package org.scalecloudsim.request;

import lombok.Getter;
import lombok.Setter;
import org.scalecloudsim.datacenter.Datacenter;

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

    @Getter
    @Setter
    int retryNum;

    @Getter
    @Setter
    int retryMaxNum;

    @Getter
    @Setter
    int state;

    @Getter
    @Setter
    Datacenter receiveDatacenter;


    public InstanceGroupSimple(int id) {
        this.id = id;
        this.instanceList = new ArrayList<>();
        this.groupType = 0;
        this.retryNum = 0;
        this.retryMaxNum = 3;
        this.state = UserRequest.WAITING;
        this.accessLatency = Double.MAX_VALUE;
        this.receiveDatacenter = null;//TODO 换成Datacenter.NULL
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
    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "InstanceGroupSimple [id=" + id + ", instanceList=" + instanceList + "]";
    }

}
