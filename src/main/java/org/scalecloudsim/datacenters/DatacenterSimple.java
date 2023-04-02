package org.scalecloudsim.datacenters;

import org.scalecloudsim.datacenters.Datacenter;
import org.scalecloudsim.statemanager.StateManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DatacenterSimple implements Datacenter{
    public Logger LOGGER = LoggerFactory.getLogger(DatacenterSimple.class.getSimpleName());
    private Set<Integer> collaborationIds;

    public DatacenterSimple() {
        this.collaborationIds = new HashSet<>();
    }

    @Override
    public Datacenter setStateManager(StateManager stateManager) {
        return null;
    }

    @Override
    public StateManager getStateManager() {
        return null;
    }

    @Override
    public Datacenter addCollaborationId(int collaborationId) {
        if(collaborationIds.contains(collaborationId)){
            LOGGER.warn("the datacenter("+this+") already belongs to the collaboration "+collaborationId);
        }
        else {
            collaborationIds.add(collaborationId);
        }
        return this;
    }

    @Override
    public Datacenter removeCollaborationId(int collaborationId) {
        if(!collaborationIds.contains(collaborationId)){
            LOGGER.warn("the datacenter("+this+") does not belong to the collaboration "+collaborationId+" to be removed");
        }
        else{
            collaborationIds.remove(collaborationId);
        }
        return this;
    }

    @Override
    public Set<Integer> getCollaborationIds() {
        return collaborationIds;
    }
}