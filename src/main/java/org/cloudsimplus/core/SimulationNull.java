package org.cloudsimplus.core;

import org.cloudsimplus.core.Simulation;

public class SimulationNull implements Simulation {
    @Override public double clock() { return 0.0; }

    @Override
    public Simulation setClock(double time) {
        return this;
    }
}
