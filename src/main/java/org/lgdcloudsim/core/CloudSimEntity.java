/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */
package org.lgdcloudsim.core;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.lgdcloudsim.core.events.CloudSimEvent;
import org.lgdcloudsim.core.events.SimEvent;
import org.lgdcloudsim.user.UserSimple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Predicate;

import static java.util.Objects.requireNonNull;
import static java.util.Objects.requireNonNullElse;

/**
 * Represents a simulation entity. An entity handles events and can
 * send events to other entities.
 *
 * @author Marcos Dias de Assuncao
 * @author Anonymous
 * @since CloudSim Toolkit 1.0
 */
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public abstract class CloudSimEntity implements SimEntity {
    private static final Logger LOGGER = LoggerFactory.getLogger(CloudSimEntity.class.getSimpleName());
    @Getter @EqualsAndHashCode.Include
    private int id;
    @Getter @NonNull @EqualsAndHashCode.Include
    private final Simulation simulation;
    @Getter
    private String name;

    @Getter
    private double startTime;

    @Getter
    private double shutdownTime;

    @Getter @Setter
    private State state;

    /**
     * The buffer for selected incoming events.
     */
    private SimEvent buffer;

    /**
     * Creates a new entity.
     *
     * @param simulation The CloudSimPlus instance that represents the simulation the Entity belongs to
     * @throws IllegalArgumentException when the entity name is invalid
     */
    public CloudSimEntity(@NonNull final Simulation simulation) {
        this.simulation = simulation;
        setId(-1);
        state = State.RUNNABLE;
        this.simulation.addEntity(this);
        this.startTime = -1;
        this.shutdownTime = -1;
    }

    /**
     * {@inheritDoc}.
     * It performs general initialization tasks that are common for every entity
     * and executes the specific entity startup code.
     *
     * @return {@inheritDoc}
     */
    @Override
    public final boolean start() {
        startInternal();
        this.startTime = simulation.clock();
        return true;
    }
    /**
     * Defines the logic to be performed by the entity when the simulation starts.
     */
    protected abstract void startInternal();


    @Override
    public void run() {
        run(Double.MAX_VALUE);
    }

    public void run(final double until) {
        var evt = requireNonNullElse(buffer, getNextEvent(e -> e.getTime() <= until));

        while (evt != SimEvent.NULL) {
            processEvent(evt);
            if (state != State.RUNNABLE) {
                break;
            }

            evt = getNextEvent(e -> e.getTime() <= until);
        }

        buffer = null;
    }

    /**
     * Gets the first event matching a predicate from the deferred queue, or if
     * none match, wait for a matching event to arrive.
     *
     * @param predicate The predicate to match
     * @return the simulation event;
     * or {@link SimEvent#NULL} if not found or the simulation is not running
     */
    public SimEvent getNextEvent(final Predicate<SimEvent> predicate) {
        return selectEvent(predicate);
    }

    /**
     * Extracts the first event matching a predicate waiting in the entity's
     * deferred queue.
     *
     * @param predicate The event selection predicate
     * @return the simulation event;
     *         or {@link SimEvent#NULL} if not found or the simulation is not running
     */
    public SimEvent selectEvent(final Predicate<SimEvent> predicate) {
        return simulation.select(this, predicate);
    }
    @Override
    public SimEntity setName(@NonNull final String name) throws IllegalArgumentException {
        if (name.isBlank()) {
            throw new IllegalArgumentException("Entity names cannot be empty.");
        }

        this.name = name;
        return this;
    }

    /**
     * Sets the entity id and defines its name based on such ID.
     *
     * @param id the new id
     */
    protected final void setId(final int id) {
        this.id = id;
        setAutomaticName();
    }

    /**
     * Sets an automatic generated name for the entity.
     */
    private void setAutomaticName() {
        final long id = this.id >= 0 ? this.id : this.simulation.getNumEntities();
        this.name = "%s%d".formatted(getClass().getSimpleName(), id);
    }

    @Override
    public int compareTo(final SimEntity entity) {
        return Long.compare(this.getId(), entity.getId());
    }

    @Override
    public boolean schedule(final SimEvent evt) {
        simulation.send(evt);
        return true;
    }
    @Override
    public boolean schedule(final SimEntity dest, final double delay, final CloudActionTags tag) {
        return schedule(dest, delay, tag, null);
    }
    @Override
    public boolean schedule(final SimEntity dest, final double delay, final CloudActionTags tag, final Object data) {
        return schedule(new CloudSimEvent(delay, this, dest, tag, data));
    }

    /**
     * Sends an event from one entity to another.
     * It will not only consider the delay but also the network delay between the entities.
     * @param dest the destination entity
     * @param delay how long from now the event should be executed
     * @param tag a user-defined number representing the type of event, @see {@link SimEvent#getTag()}
     * @param data a reference to an object to be sent with the event
     */
    protected void send(final SimEntity dest, double delay, final CloudActionTags tag, final Object data) {
        requireNonNull(dest);
        if (dest.getId() < 0) {
            LOGGER.error("{}.send(): invalid entity id {} for {}", getName(), dest.getId(), dest);
            return;
        }

        // if delay is negative, then it doesn't make sense. So resets to 0.0
        if (delay < 0) {
            delay = 0;
        }
        if (!((this instanceof UserSimple) || (dest instanceof UserSimple))) {
            if (dest.getId() != getId()) {
                delay += getNetworkDelay(this, dest);
            }
        }

        if (Double.isInfinite(delay)) {
            throw new IllegalArgumentException("The specified delay is infinite value");
        }

        schedule(dest, delay, tag, data);
    }

    /**
     * Sends an event from one entity to another without considering the network delay.
     * @param dest the destination entity
     * @param delay how long from now the event should be executed
     * @param tag a user-defined number representing the type of event, @see {@link SimEvent#getTag()}
     * @param data a reference to an object to be sent with the event
     */
    protected void sendWithoutNetwork(final SimEntity dest, double delay, final CloudActionTags tag, final Object data) {
        requireNonNull(dest);
        if (dest.getId() < 0) {
            LOGGER.error("{}.send(): invalid entity id {} for {}", getName(), dest.getId(), dest);
            return;
        }

        // if delay is negative, then it doesn't make sense. So resets to 0.0
        if (delay < 0) {
            delay = 0;
        }

        if (Double.isInfinite(delay)) {
            throw new IllegalArgumentException("The specified delay is infinite value");
        }

        schedule(dest, delay, tag, data);
    }

    /**
     * Get the network delay between two entities.
     * @param src the source entity
     * @param dst the destination entity
     */
    private double getNetworkDelay(final SimEntity src, final SimEntity dst) {
        return getSimulation().getNetworkTopology().getDynamicDelay(src, dst, getSimulation().clock());
    }

    /**
     * Sends an event from one entity to another now.
     * The network delay between the entities will be considered.
     * @param dest the destination entity
     * @param tag a user-defined number representing the type of event, @see {@link SimEvent#getTag()}
     * @param data a reference to an object to be sent with the event
     */
    protected void sendNow(final SimEntity dest, final CloudActionTags tag, final Object data) {
        send(dest, 0, tag, data);
    }

    /**
     * Sends an event from one entity to another now.
     * The network delay between the entities will be considered.
     * @param dest the destination entity
     * @param tag a user-defined number representing the type of event, @see {@link SimEvent#getTag()}
     */
    protected void sendNow(final SimEntity dest, final CloudActionTags tag) {
        send(dest, 0, tag, null);
    }

}
