package org.scalecloudsim.datacenters;

import org.cloudsimplus.hosts.Host;
import org.scalecloudsim.resourcemanager.StateManager;

import java.util.List;

public interface Datacenter {
//    long getHo
    List<Host> getHostList();
    Datacenter setStateManager(StateManager stateManager);
    StateManager getStateManager();
}
