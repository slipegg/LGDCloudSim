package org.scalecloudsim.users;

import org.cloudsimplus.core.SimEntity;
import org.scalecloudsim.Instances.InstanceGroup;
import org.scalecloudsim.Instances.InstanceGroupGraph;
import org.scalecloudsim.datacenters.Datacenter;
import org.scalecloudsim.datacenters.Regin;

import java.util.List;

class UserNull implements User{

    @Override
    public long getId() {
        return 0;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public SimEntity setName(String newName) throws IllegalArgumentException {
        return null;
    }

    @Override
    public List<InstanceGroup> getInstanceGroupList() {
        return null;
    }

    @Override
    public User setInstanceGoupList() {
        return null;
    }

    @Override
    public InstanceGroupGraph getInstanceGroupGraph() {
        return InstanceGroupGraph.NULL;
    }

    @Override
    public User setInstanceGroupGraph(InstanceGroupGraph instanceGroupGraph) {
        return this;
    }

    @Override
    public User setRegin(Regin regin) {
        return this;
    }

    @Override
    public Regin getRegin() {
        return null;
    }

    @Override
    public Datacenter findNearestDatacenter() {
        return null;
    }

    @Override
    public User setTargetDatacenter(Datacenter datacenter) {
        return this;
    }

    @Override
    public Datacenter getTargetDatacenter() {
        return null;
    }

    @Override
    public User submitInstanceGroup(InstanceGroup instanceGroup) {
        return this;
    }

    @Override
    public User submitInstanceGroup(List<? extends InstanceGroup> list) {
        return this;
    }

    @Override
    public int compareTo(SimEntity o) {
        return 0;
    }

    @Override
    public void run() {

    }
}
