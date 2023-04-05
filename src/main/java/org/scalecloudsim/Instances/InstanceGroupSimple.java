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

    public InstanceGroupSimple(int id){
        this.id=id;
        instanceList=new ArrayList<>();
        groupType =0;
    }

    @Override
    public void setUserRequest(UserRequest userRequest) {
        this.userRequest= userRequest;
    }

    @Override
    public void setId(int id) {
        this.id=id;
    }
    @Override
    public String toString() {
        return "InstanceGroupSimple [id=" + id + ", instanceList=" + instanceList +"]";
    }
}
