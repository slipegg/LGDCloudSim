package org.scalecloudsim.Instances;

import lombok.Getter;
import lombok.Setter;
import org.scalecloudsim.datacenters.Datacenter;

import java.util.List;

public class UserRequestSimple implements UserRequest {
    @Getter
    int id;
    @Getter@Setter
    double submitTime;
    @Getter@Setter
    List<InstanceGroup> instanceGroups;
    @Getter@Setter
    InstanceGroupGraph instanceGroupGraph;
    @Getter@Setter
    int belongDatacenterId;

    public UserRequestSimple(int id, List<InstanceGroup> instanceGroups, InstanceGroupGraph instanceGroupGraph) {
        this.id = id;
        this.instanceGroups = instanceGroups;
        this.instanceGroupGraph = instanceGroupGraph;
    }


    public UserRequestSimple(int id) {
        this.id = id;
    }

    public UserRequestSimple() {
        this.id = -1;
    }

    @Override
    public String toString() {
        return "UserRequestSimple [id=" + id + ", submitTime=" + submitTime + ", instanceGroups=" + instanceGroups
                + ", instanceGroupGraph=" + instanceGroupGraph + "]";
    }

    @Override
    public void setId(int id) {
        this.id=id;
    }
}
