package org.scalecloudsim.datacenters;

import org.cloudsimplus.core.SimEntity;
import org.scalecloudsim.statemanager.StateManager;

import java.util.Set;

public interface Datacenter extends SimEntity {
    Datacenter setStateManager(StateManager stateManager);

    StateManager getStateManager();

    Datacenter addCollaborationId(int collaborationId);

    Datacenter removeCollaborationId(int collaborationId);

    Set<Integer> getCollaborationIds();
}
