package org.cloudsimplus.core;

import org.cloudsimplus.core.Simulation;
import org.cloudsimplus.core.events.SimEvent;

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
}
