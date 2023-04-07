package org.cloudsimplus.core;

import org.cloudsimplus.core.SimulationNull;
import org.cloudsimplus.core.events.SimEvent;
import org.cloudsimplus.network.topologies.NetworkTopology;
import org.scalecloudsim.datacenters.CollaborationManager;

import java.util.function.Predicate;

public interface Simulation {
    Simulation NULL=new SimulationNull();
    double clock();

    String clockStr();

    Simulation setClock(double time);

    void addEntity(CloudSimEntity entity);

    SimEvent select(SimEntity dest, Predicate<SimEvent> predicate);

    SimEvent findFirstDeferred(SimEntity dest, Predicate<SimEvent> predicate);

    void send(SimEvent evt);

    CloudInformationService getCis();

    double start();

    void startSync();

    int getNumEntities();

    void setNetworkTopology(NetworkTopology networkTopology);

    NetworkTopology getNetworkTopology();

    void setCollaborationManager(CollaborationManager collaborationManager);

    CollaborationManager getCollaborationManager();
}
