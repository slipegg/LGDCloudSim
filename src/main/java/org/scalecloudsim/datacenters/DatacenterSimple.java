package org.scalecloudsim.datacenters;

import lombok.NonNull;
import org.cloudsimplus.core.CloudSimEntity;
import org.cloudsimplus.core.CloudSimTag;
import org.cloudsimplus.core.SimEntity;
import org.cloudsimplus.core.Simulation;
import org.cloudsimplus.core.events.SimEvent;
import org.scalecloudsim.Instances.UserRequest;
import org.scalecloudsim.datacenters.Datacenter;
import org.scalecloudsim.statemanager.StateManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DatacenterSimple extends CloudSimEntity implements Datacenter{
    public Logger LOGGER = LoggerFactory.getLogger(DatacenterSimple.class.getSimpleName());
    private Set<Integer> collaborationIds;

    /**
     * Creates a new entity.
     *
     * @param simulation The CloudSimPlus instance that represents the simulation the Entity belongs to
     * @throws IllegalArgumentException when the entity name is invalid
     */
    public DatacenterSimple(@NonNull Simulation simulation) {
        super(simulation);
        this.collaborationIds = new HashSet<>();
    }
    public DatacenterSimple(@NonNull Simulation simulation,int id) {
        super(simulation);
        this.collaborationIds = new HashSet<>();
        this.setId(id);
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
        if (collaborationIds.contains(collaborationId)) {
            LOGGER.warn("the datacenter(" + this + ") already belongs to the collaboration " + collaborationId);
        } else {
            collaborationIds.add(collaborationId);
        }
        return this;
    }

    @Override
    public Datacenter removeCollaborationId(int collaborationId) {
        if (!collaborationIds.contains(collaborationId)) {
            LOGGER.warn("the datacenter(" + this + ") does not belong to the collaboration " + collaborationId + " to be removed");
        } else {
            collaborationIds.remove(collaborationId);
        }
        return this;
    }

    @Override
    public Set<Integer> getCollaborationIds() {
        return collaborationIds;
    }

    @Override
    protected void startInternal() {
        LOGGER.info("{}: {} is starting...", getSimulation().clockStr(), getName());
        sendNow(getSimulation().getCis(), CloudSimTag.DC_REGISTRATION_REQUEST, this);
    }

    @Override
    public void processEvent(SimEvent evt) {
        switch (evt.getTag()) {
            case CloudSimTag.USER_REQUEST_SEND -> processUserRequestsSend(evt);
            default -> LOGGER.warn("{}: {} received unknown event {}", getSimulation().clockStr(), getName(), evt.getTag());
        }
    }

    private void processUserRequestsSend(final SimEvent evt) {
        if(evt.getData() instanceof List<?> userRequests){
            LOGGER.info("{}: {} received {} user request", getSimulation().clockStr(), getName(),userRequests.size());
        }
    }

    @Override
    public int compareTo(SimEntity o) {
        return Comparator.comparing(SimEntity::getId).compare(this, o);
    }
}