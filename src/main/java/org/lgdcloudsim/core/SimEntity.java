/*
 * Title: CloudSim Toolkit Description: CloudSim (Cloud Simulation) Toolkit for Modeling and
 * Simulation of Clouds Licence: GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */
package org.lgdcloudsim.core;

//import org.cloudbus.cloudsim.core.events.SimEvent;

import org.lgdcloudsim.core.events.SimEvent;
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
     * An attribute that implements the Null Object Design Pattern for {@link SimEntity}
     * objects.
     */
    SimEntity NULL = new SimEntityNull();

    /**
     * Defines the event state.
     */
    enum State {RUNNABLE, WAITING, HOLDING, FINISHED}

    /**
     * Sets the Entity name.
     *
     * @param newName the new name
     * @return the entity
     * @throws IllegalArgumentException when the entity name is null or empty
     */
    SimEntity setName(String newName) throws IllegalArgumentException;

    /**
     * Gets the LGDCloudSim instance that represents the simulation to each the Entity belongs to.
     *
     * @return the simulation instance
     */
    Simulation getSimulation();

    /**
     * Starts the entity during simulation start.
     * This method is invoked by the {@link CloudSim} class when the simulation is started.
     * @return true if the entity started successfully; false if it was already started
     */
    boolean start();

    /**
     * The run loop to process events fired during the simulation. The events
     * that will be processed are defined in the
     * {@link #processEvent(SimEvent)} method.
     *
     * @see #processEvent(SimEvent)
     */
    @Override void run();

    /**
     * Sends an event where all data required is defined inside the event instance.
     * @param evt the event to send
     * @return true if the event was sent; false if the simulation was not started yet
     */
    boolean schedule(SimEvent evt);

    /**
     * Sends an event to another entity with <b>no</b> attached data.
     * @param dest the destination entity
     * @param delay How many seconds after the current simulation time the event should be sent
     * @param tag   a tag representing the type of event.
     * @return true if the event was sent; false if the simulation was not started yet
     */
    boolean schedule(SimEntity dest, double delay, CloudActionTags  tag);

    /**
     * Sends an event to another entity.
     * @param dest  the destination entity
     * @param delay How many seconds after the current simulation time the event should be sent
     * @param tag   a tag representing the type of event.
     * @param data  The data to be sent with the event.
     * @return true if the event was sent; false if the simulation was not started yet
     */
    boolean schedule(SimEntity dest, double delay, CloudActionTags tag, Object data);

    /**
     * Processes events or services that are available for the entity. This
     * method is invoked by the {@link CloudSim} class whenever there is an
     * event in the deferred queue, which needs to be processed by the entity.
     *
     * @param evt information about the event just happened
     */
    void processEvent(SimEvent evt);
}
