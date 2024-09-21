package org.lgdcloudsim.core;

import org.lgdcloudsim.core.events.SimEvent;

/**
 * A base interface used internally to implement the Null Object Design Pattern
 * for interfaces extending {@link SimEntity}.
 * It's just used to avoid the boilerplate code in such Null Object implementations.
 *
 * @author Manoel Campos da Silva Filho
 * @see SimEntity#NULL
 */
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
    public boolean schedule(SimEntity dest, double delay, CloudActionTags tag) {
        return false;
    }

    @Override
    public boolean schedule(SimEntity dest, double delay, CloudActionTags tag, Object data) {
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
