package org.cloudsimplus.core;

import org.cloudsimplus.core.events.SimEvent;
import org.cloudsimplus.network.topologies.NetworkTopology;
import org.cpnsim.datacenter.CollaborationManager;
import org.cpnsim.record.SqlRecord;

import java.util.function.Predicate;

public class SimulationNull implements Simulation {
    @Override public double clock() { return 0.0; }

    @Override
    public String clockStr() {
        return null;
    }

    @Override
    public Simulation setClock(double time) {
        return this;
    }

    @Override
    public void addEntity(CloudSimEntity entity) {

    }

    @Override
    public SimEvent select(SimEntity dest, Predicate<SimEvent> predicate) {
        return null;
    }

    @Override
    public SimEvent findFirstDeferred(SimEntity dest, Predicate<SimEvent> predicate) {
        return null;
    }

    @Override
    public void send(SimEvent evt) {

    }

    @Override
    public CloudInformationService getCis() {
        return null;
    }

    @Override
    public boolean terminateAt(double time) {
        return false;
    }

    @Override
    public double start() {
        return 0;
    }

    @Override
    public void startSync() {

    }

    @Override
    public int getNumEntities() {
        return 0;
    }

    @Override
    public void setNetworkTopology(NetworkTopology networkTopology) {

    }

    @Override
    public NetworkTopology getNetworkTopology() {
        return null;
    }

    @Override
    public void setCollaborationManager(CollaborationManager collaborationManager) {

    }

    @Override
    public CollaborationManager getCollaborationManager() {
        return null;
    }

    @Override
    public int getSimulationAccuracy() {
        return 0;
    }

    @Override
    public void setSimulationAccuracy(int simulationAccuracy) {

    }

    @Override
    public SqlRecord getSqlRecord() {
        return null;
    }

    @Override
    public boolean isTerminationTimeSet() {
        return false;
    }

    @Override
    public boolean isTimeToTerminateSimulationUnderRequest() {
        return false;
    }
}
