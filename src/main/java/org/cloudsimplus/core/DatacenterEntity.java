package org.cloudsimplus.core;

import org.scalecloudsim.datacenter.Datacenter;

public interface DatacenterEntity extends ChangeableId{

    Datacenter getDatacenter();
    void setDatacenter(Datacenter datacenter);

}
