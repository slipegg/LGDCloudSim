package org.scalecloudsim.users;

import org.apache.commons.lang3.ObjectUtils;
import org.cloudsimplus.core.SimEntity;
import org.scalecloudsim.Instances.InstanceGroup;
import org.scalecloudsim.Instances.InstanceGroupGraph;
import org.scalecloudsim.datacenters.Datacenter;
import org.scalecloudsim.datacenters.Regin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;

public interface User  extends SimEntity {
    Logger LOGGER = LoggerFactory.getLogger(User.class.getSimpleName());
    User NULL=new UserNull();

    List<InstanceGroup> getInstanceGroupList();
    User setInstanceGoupList();

    InstanceGroupGraph getInstanceGroupGraph();
    User setInstanceGroupGraph(InstanceGroupGraph instanceGroupGraph);

    User setRegin(Regin regin);
    Regin getRegin();

    Datacenter findNearestDatacenter();
    User setTargetDatacenter(Datacenter datacenter);
    Datacenter getTargetDatacenter();

    User submitInstanceGroup(InstanceGroup instanceGroup);
    User submitInstanceGroup(List<? extends InstanceGroup> list);


}
