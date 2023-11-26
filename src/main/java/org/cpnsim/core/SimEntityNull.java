package org.cpnsim.core;

import org.cpnsim.core.events.SimEvent;

public class SimEntityNull implements SimEntity{
    @Override
    public int getId() {
        return 0;
    }

    @Override
    public SimEntity setName(String newName) throws IllegalArgumentException {
        return null;
    }

    @Override
    public Simulation getSimulation() {
        return null;
    }

    @Override
    public boolean start() {
        return false;
    }

    @Override
    public void run() {

    }

    @Override
    public boolean schedule(SimEvent evt) {
        return false;
    }

    @Override
    public boolean schedule(SimEntity dest, double delay, int tag) {
        return false;
    }

    @Override
    public boolean schedule(SimEntity dest, double delay, int tag, Object data) {
        return false;
    }

    @Override
    public void processEvent(SimEvent evt) {

    }

    @Override
    public int compareTo(SimEntity o) {
        return 0;
    }

    @Override
    public String getName() {
        return null;
    }
}
