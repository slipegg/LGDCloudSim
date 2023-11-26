package org.cpnsim.network;

import org.cloudsimplus.core.SimEntity;
import org.cpnsim.datacenter.Datacenter;
import org.cpnsim.request.UserRequest;

public interface NetworkTopology {
    double getDelay(SimEntity src, SimEntity dst);

    NetworkTopology setDelayDynamicModel(DelayDynamicModel delayDynamicModel);

    double getDynamicDelay(SimEntity src, SimEntity dst, double time);

    double getBw(SimEntity src, SimEntity dst);

    boolean allocateBw(SimEntity src, SimEntity dst, double allocateBw);

    NetworkTopology releaseBw(SimEntity src, SimEntity dst, double releaseBw);

    double getNetworkTCO();

    double getAccessLatency(UserRequest userRequest, Datacenter datacenter);
}
