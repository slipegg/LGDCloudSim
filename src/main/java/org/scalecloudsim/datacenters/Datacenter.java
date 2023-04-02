package org.scalecloudsim.datacenters;

import org.scalecloudsim.statemanager.StateManager;

import java.util.Set;

public interface Datacenter {
    Datacenter setStateManager(StateManager stateManager);

    StateManager getStateManager();

    Datacenter addCollaborationId(int collaborationId);

    Datacenter removeCollaborationId(int collaborationId);

    Set<Integer> getCollaborationIds();
}
