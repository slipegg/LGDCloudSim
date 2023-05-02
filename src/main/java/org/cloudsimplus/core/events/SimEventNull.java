package org.cloudsimplus.core.events;

import org.cloudsimplus.core.CloudSimTag;
import org.cloudsimplus.core.SimEntity;
import org.cloudsimplus.core.Simulation;

public class SimEventNull implements SimEvent{

    @Override
    public SimEvent setSimulation(Simulation simulation) {
        return null;
    }

    @Override
    public SimEntity getDestination() {
        return null;
    }

    @Override
    public SimEntity getSource() {
        return null;
    }

    @Override
    public double getEndWaitingTime() {
        return 0;
    }

    @Override
    public int getTag() {
        return 0;
    }

    @Override
    public Object getData() {
        return null;
    }

    @Override
    public SimEvent setSource(SimEntity source) {
        return null;
    }

    @Override
    public SimEvent setDestination(SimEntity destination) {
        return null;
    }

    @Override
    public long getSerial() {
        return 0;
    }

    @Override
    public SimEvent setSerial(long serial) {
        return null;
    }

    @Override
    public Simulation getSimulation() {
        return null;
    }

    @Override
    public double getTime() {
        return 0;
    }

    @Override
    public int compareTo(SimEvent evt) {
        return 0;
    }
}
