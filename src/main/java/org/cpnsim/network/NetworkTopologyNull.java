package org.cpnsim.network;

import org.cpnsim.core.SimEntity;
import org.cpnsim.datacenter.Datacenter;
import org.cpnsim.request.UserRequest;

/**
 * The null network topology.
 *
 * @author Jiawen Liu
 * @since LGDCloudSim 1.0
 */
public class NetworkTopologyNull implements NetworkTopology {
    @Override
    public double getDelay(SimEntity src, SimEntity dst) {
        return 0;
    }

    @Override
    public NetworkTopology setDelayDynamicModel(DelayDynamicModel delayDynamicModel) {
        return this;
    }

    @Override
    public double getDynamicDelay(SimEntity src, SimEntity dst, double time) {
        return 0;
    }

    @Override
    public double getBw(SimEntity src, SimEntity dst) {
        return 0;
    }

    @Override
    public boolean allocateBw(SimEntity src, SimEntity dst, double allocateBw) {
        return true;
    }

    @Override
    public NetworkTopology releaseBw(SimEntity src, SimEntity dst, double releaseBw) {
        return this;
    }

    @Override
    public double getNetworkTCO() {
        return 0;
    }

    @Override
    public double getAccessLatency(UserRequest userRequest, Datacenter datacenter) {
        return 0;
    }
}
