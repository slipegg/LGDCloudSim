package org.scalecloudsim.Instances;

import java.util.ArrayList;
import java.util.List;

public class InstanceGroupSimple implements InstanceGroup{
    int id;
    UserRequest request;

    List<Instance> instanceList;
    int type;

    public InstanceGroupSimple(int id){
        this.id=id;
        instanceList=new ArrayList<>();
        type=0;
    }
    
    @Override
    public void setId(int id) {
        this.id = id;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public List<Instance> getInstanceList() {
        return instanceList;
    }

    @Override
    public InstanceGroup setInstanceList(List<Instance> instanceList) {
        this.instanceList = instanceList;
        return this;
    }

    @Override
    public int getGroupType() {
        return type;
    }

    @Override
    public InstanceGroup setGroupType(int type) {
        this.type = type;
        return this;
    }

    @Override
    public UserRequest getUserRequest() {
        return request;
    }

    @Override
    public void setUserRequest(UserRequest userRequest) {
        this.request = userRequest;
    }
}
