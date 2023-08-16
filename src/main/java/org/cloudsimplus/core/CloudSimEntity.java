/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */
package org.cloudsimplus.core;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.cloudsimplus.core.events.CloudSimEvent;
import org.cloudsimplus.core.events.SimEvent;
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

    public SimEvent getNextEvent(final Predicate<SimEvent> predicate) {
        return selectEvent(predicate);
    }
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

    protected final void setId(final int id) {
        this.id = id;
        setAutomaticName();
    }
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
    public boolean schedule(final SimEntity dest, final double delay, final int tag) {
        return schedule(dest, delay, tag, null);
    }
    @Override
    public boolean schedule(final SimEntity dest, final double delay, final int tag, final Object data) {
        return schedule(new CloudSimEvent(delay, this, dest, tag, data));
    }
    protected void send(final SimEntity dest, double delay, final int tag, final Object data) {
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

    protected void sendBetweenDc(final SimEntity dest, double delay, final int tag, final Object data) {
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

        if (dest.getId() != getId()) {
            delay += getNetworkDelay(this, dest);
        }

        schedule(dest, delay, tag, data);
    }

    private double getNetworkDelay(final SimEntity src, final SimEntity dst) {
        return getSimulation().getNetworkTopology().getDynamicDelay(src, dst, getSimulation().clock());
    }

    protected void sendNow(final SimEntity dest, final int tag, final Object data) {
        send(dest, 0, tag, data);
    }

    protected void sendNow(final SimEntity dest, final int tag) {
        send(dest, 0, tag, null);
    }

}
