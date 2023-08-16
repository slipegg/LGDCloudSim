package org.cloudsimplus.core;

import org.cpnsim.datacenter.Datacenter;

public interface DatacenterEntity extends ChangeableId{
    Datacenter getDatacenter();

    void setDatacenter(Datacenter datacenter);
}
