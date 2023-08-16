/*
 * Title: CloudSim Toolkit Description: CloudSim (Cloud Simulation) Toolkit for Modeling and
 * Simulation of Clouds Licence: GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */
package org.cloudsimplus.core;

//import org.cloudbus.cloudsim.core.events.SimEvent;

import lombok.NonNull;
import org.cloudsimplus.core.events.SimEvent;
/**
 * An interface that represents a simulation entity. An entity handles events and can
 * send events to other entities.
 *
 * @author Marcos Dias de Assuncao
 * @author Manoel Campos da Silva Filho
 * @since CloudSim Plus 1.0
 */
public interface SimEntity extends Nameable, Runnable, Comparable<SimEntity> {
    /**
     * Defines the event state.
     */
    SimEntity NULL = new SimEntityNull();

    enum State {RUNNABLE, WAITING, HOLDING, FINISHED}

    SimEntity setName(String newName) throws IllegalArgumentException;

    Simulation getSimulation();

    boolean start();

    @Override void run();

    boolean schedule(SimEvent evt);

    boolean schedule(SimEntity dest, double delay, int tag);

    boolean schedule(SimEntity dest, double delay, int tag, Object data);

    void processEvent(SimEvent evt);
}
