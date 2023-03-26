package org.cloudsimplus.core;

public class Cloudsim implements Simulation{
    double clock;
    public Cloudsim(){
        clock=0;
    }
    @Override
    public double clock() {
        return clock;
    }

    @Override
    public Simulation setClock(double time) {
        this.clock=time;
        return this;
    }
}
