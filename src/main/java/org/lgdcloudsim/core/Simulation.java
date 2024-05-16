package org.lgdcloudsim.core;

import org.lgdcloudsim.core.events.SimEvent;
import org.lgdcloudsim.network.NetworkTopology;
import org.lgdcloudsim.datacenter.CollaborationManager;
import org.lgdcloudsim.record.SqlRecord;

import java.util.function.Predicate;

/**
 * An interface to be implemented by a class that manages simulation
 * execution, controlling all the simulation life cycle.
 *
 * @author Rodrigo N. Calheiros
 * @author Anton Beloglazov
 * @author Manoel Campos da Silva Filho
 * @author Anonymous
 * @since LGDCloudSim 1.0
 */
public interface Simulation {
    /**
     * An attribute that implements the Null Object Design Pattern for {@link Simulation}
     * objects.
     */
    Simulation NULL=new SimulationNull();

    /**
     * Gets the current simulation time in seconds.
     *
     * @return the current simulation time
     */
    double clock();

    /**
     * Gets the current simulation time in seconds as a formatted String.
     *
     * @return the current simulation time as a formatted String
     * @see #clock()
     */
    String clockStr();

    /**
     * Sets the current simulation time in milliseconds.
     *
     * @param time the time to set
     * @return the current simulation instance
     */
    Simulation setClock(double time);

    /**
     * Adds a new entity to the simulation. Each {@link CloudSimEntity} object
     * register itself when it is instantiated.
     *
     * @param entity The new entity
     */
    void addEntity(CloudSimEntity entity);

    /**
     * Selects the first deferred event that matches a given predicate
     * and removes it from the queue.
     *
     * @param dest entity that the event has to be sent to
     * @param predicate    the event selection predicate
     * @return the removed event or {@link SimEvent#NULL} if not found
     */
    SimEvent select(SimEntity dest, Predicate<SimEvent> predicate);

    /**
     * Find first deferred event matching a predicate.
     *
     * @param dest id of entity that the event has to be sent to
     * @param predicate    the event selection predicate
     * @return the first matched event or {@link SimEvent#NULL} if not found
     */
    SimEvent findFirstDeferred(SimEntity dest, Predicate<SimEvent> predicate);

    /**
     * Sends an event where all data required is defined inside the event instance.
     * @param evt the event to send
     */
    void send(SimEvent evt);

    /**
     * Gets the {@link CloudInformationService}.
     *
     * @return the Entity
     */
    CloudInformationService getCis();

    /**
     * Schedules the termination of the simulation for a given time (in seconds).
     *
     * <p>If a termination time is set, the simulation stays running even
     * if there is no event to process.
     * It keeps waiting for new dynamic events, such as the creation
     * of Cloudlets and VMs at runtime.
     * If no event happens, the clock is increased to simulate time passing.
     *
     * @param time the time at which the simulation has to be terminated (in seconds)
     * @return true if the time given is greater than the current simulation time, false otherwise
     */
    boolean terminateAt(double time);

    /**
     * Starts simulation execution and <b>waits for all entities to finish</b>,
     * i.e. until all entities threads reach
     * non-RUNNABLE state or there are no more events in the future event queue.
     *
     * <p>
     * <b>Note</b>: This method should be called only after all the entities
     * have been setup and added. The method blocks until the simulation is ended.
     * </p>
     *
     * @return the last clock time
     * @throws UnsupportedOperationException When the simulation has already run once.
     * If you paused the simulation and wants to resume it,
     *
     * @see #startSync()
     */
    double start();


    /**
     * Starts simulation execution in synchronous mode, retuning immediately.
     *
     * <b>Note</b>: This method should be called only after all entities have been set up and added.
     * The method returns immediately after preparing the internal state of the simulation.
     *
     *
     * @throws UnsupportedOperationException When the simulation has already run once.
     * If you paused the simulation and wants to resume it,
     */
    void startSync();

    /**
     * Get the current number of entities in the simulation.
     *
     * @return The number of entities
     */
    int getNumEntities();

    /**
     * Sets the network topology used for Network simulations.
     *
     * @param networkTopology the network topology to set
     */
    void setNetworkTopology(NetworkTopology networkTopology);

    /**
     * Gets the network topology used for Network simulations.
     * @return the network topology
     */
    NetworkTopology getNetworkTopology();

    /**
     * Sets the collaboration manager used for Network simulations.
     * @param collaborationManager the collaboration manager to set
     */
    void setCollaborationManager(CollaborationManager collaborationManager);

    /**
     * Gets the collaboration manager used for Network simulations.
     * @return the collaboration manager
     */
    CollaborationManager getCollaborationManager();

    /**
     * Get the time accuracy of simulated events.
     *
     * @return the termination time
     */
    // TODO Set to a precision value rather than the number of reserved bits to support precisions like 10ms
    int getSimulationAccuracy();

    /**
     * Set the time accuracy of simulated events, that is, to the number of decimal places. The default is 2 digits, which is 0.01ms.
     * @param simulationAccuracy the number of decimal places to set
     */
    void setSimulationAccuracy(int simulationAccuracy);

    /**
     * Get the sql record object.
     * @return the sql record object
     */
    SqlRecord getSqlRecord();

    /**
     * Set the sql record object.
     * @param sqlRecord the sql record object to set
     */
    void setSqlRecord(SqlRecord sqlRecord);

    /**
     * Get whether the termination time is set.
     * If the termination time is set, the simulation stops running even if there is no event to process.
     * @return true if the termination time is set, false otherwise
     */
    boolean isTerminationTimeSet();

    /**
     * Get whether the time to terminate the run has been reached.
     * @return true if the time to terminate the run has been reached, false otherwise
     */
    boolean isTimeToTerminateSimulationUnderRequest();

    /**
     * Get whether the sql record is enabled.
     * @return true if the sql record is enabled, false otherwise
     */
    boolean getIsSqlRecord();

    /**
     * Set whether the sql record is enabled.
     * @param isSqlRecord true to enable the sql record, false otherwise
     */
    void setIsSqlRecord(boolean isSqlRecord);

    /**
     * Get whether the simulation is simulating a single datacenter scenario.
     * @return true if the simulation is simulating a single datacenter scenario, false otherwise
     */
    boolean isSingleDatacenterFlag();

    /**
     * Set whether the simulation is simulating a single datacenter scenario.
     * @param isSingleDatacenter true to set the simulation to simulate a single datacenter scenario, false otherwise
     */
    Simulation setSingleDatacenterFlag(boolean isSingleDatacenter);

    /**
     * Set the name of the database used to store simulation details.
     * @param dbName the name of the database
     * @return the current simulation instance
     */
    Simulation setDbName(String dbName);

    /**
     * Get the name of the database used to store simulation details.
     * @return the name of the database
     */
    String getDbName();
}
