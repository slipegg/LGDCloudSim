package org.lgdcloudsim.core;

import org.lgdcloudsim.core.events.SimEvent;
import org.lgdcloudsim.network.NetworkTopology;
import org.lgdcloudsim.datacenter.CollaborationManager;
import org.lgdcloudsim.record.SqlRecord;

import java.util.function.Predicate;

/**
 * A class that implements the Null Object Design Pattern for {@link Simulation}
 * class.
 *
 * @author Manoel Campos da Silva Filho
 * @see Simulation#NULL
 */
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
    public void setSqlRecord(SqlRecord sqlRecord) {

    }

    @Override
    public boolean isTerminationTimeSet() {
        return false;
    }

    @Override
    public boolean isTimeToTerminateSimulationUnderRequest() {
        return false;
    }

    @Override
    public boolean getIsSqlRecord() {
        return false;
    }

    @Override
    public void setIsSqlRecord(boolean isSqlRecord) {

    }

    @Override
    public boolean isSingleDatacenterFlag() {
        return false;
    }

    @Override
    public Simulation setSingleDatacenterFlag(boolean isSingleDatacenter) {
        return  this;
    }

    @Override
    public Simulation setDbName(String dbName) {
        return null;
    }

    @Override
    public String getDbName() {
        return null;
    }
}
