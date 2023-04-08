package org.scalecloudsim.Instances;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter@Setter
public class InstanceGroupSimple implements InstanceGroup{
    int id;
    UserRequest userRequest;

    List<Instance> instanceList;

    int groupType;

    int destDatacenterId;

    double acessLatency;

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

    public InstanceGroupSimple(int id) {
        this.id = id;
        this.instanceList = new ArrayList<>();
        this.groupType = 0;
        this.retryNum = 0;
        this.retryMaxNum = 3;
    }

    @Override
    public void setUserRequest(UserRequest userRequest) {
        this.userRequest = userRequest;
    }

    @Override
    public InstanceGroup setInstanceList(List<Instance> instanceList) {
        this.instanceList = instanceList;
        for (Instance instance : instanceList) {
            storageSum += instance.getStorage();
            bwSum += instance.getBw();
            cpuSum += instance.getCpu();
            ramSum += instance.getRam();
        }
        return this;
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
