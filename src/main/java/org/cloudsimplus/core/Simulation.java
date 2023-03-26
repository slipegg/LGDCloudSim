package org.cloudsimplus.core;

import org.cloudsimplus.core.SimulationNull;

public interface Simulation {
    Simulation NULL=new SimulationNull();
    double clock();
    Simulation setClock(double time);
}
