package org.cloudsimplus.hosts;

import org.cloudsimplus.core.Simulation;
import org.scalecloudsim.Instances.Instance;
import org.scalecloudsim.datacenters.Datacenter;
import org.scalecloudsim.resourcemanager.HostHistoryManager;

public class HostNull  implements Host{
    @Override
    public long getId() {
        return 0;
    }

    @Override
    public void setId(long id) {
        return;
    }

    @Override
    public HostHistoryManager getHostHistoryManager() {
        return null;
    }

    @Override
    public HostSuitability createInstance(Instance instance) {
        return HostSuitability.NULL;
    }

    @Override
    public Host updateState() {
        return this;
    }

    @Override
    public Simulation getSimulation() {
        return null;
    }

    @Override
    public Host setSimulation(Simulation simulation) {
        return this;
    }

    @Override
    public Datacenter getDatacenter() {
        return null;
    }

    @Override
    public void setDatacenter(Datacenter datacenter) {

    }
}
