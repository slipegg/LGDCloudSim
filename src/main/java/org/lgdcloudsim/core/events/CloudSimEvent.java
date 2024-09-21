package org.lgdcloudsim.core.events;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import org.lgdcloudsim.core.CloudActionTags;
import org.lgdcloudsim.core.SimEntity;
import org.lgdcloudsim.core.Simulation;

import java.math.RoundingMode;
import java.util.Objects;
import java.math.BigDecimal;

/**
 * An event which is passed between the entities in the simulation.
 *
 * @author Costas Simatos
 * @see SimEntity
 */
@Accessors @Getter @Setter
public final class CloudSimEvent implements SimEvent {
    @NonNull
    private Simulation simulation;

    private final double time;

    @Setter(AccessLevel.NONE)
    private double endWaitingTime;

    @NonNull
    private SimEntity source;

    @NonNull
    private SimEntity destination;

    private final CloudActionTags tag;

    private final Object data;

    private long serial = -1;

    public CloudSimEvent(final double delay, final SimEntity destination, final CloudActionTags tag, Object data) {
        this(delay, destination, destination, tag, data);
    }

    /**
     * Creates a {@link Type#SEND} CloudSimEvent where the sender and destination are the same entity.
     *
     * @param delay       how many seconds after the current simulation time the event should be scheduled
     * @param destination the destination entity which has to receive the message
     * @param tag         the tag that identifies the type of the message
     *                    (which is used by the destination entity to perform operations based on the message type)
     */
    public CloudSimEvent(final double delay, final SimEntity destination, final CloudActionTags tag) {
        this(delay, destination, destination, tag, null);
    }

    /**
     * Creates a {@link Type#SEND} CloudSimEvent where the sender and destination are the same entity
     * and the message is sent with no delay.
     *
     * @param destination the destination entity which has to receive the message
     * @param tag the tag that identifies the type of the message
     *            (which is used by the destination entity to perform operations based on the message type)
     * @param data the data attached to the message, that depends on the message tag
     */
    public CloudSimEvent(final SimEntity destination, final CloudActionTags tag, Object data) {
        this(0, destination, tag, data);
    }

    /**
     * Creates a {@link Type#SEND} CloudSimEvent where the sender and destination are the same entity,
     * the message has no delay and no data.
     *
     * @param destination the source entity which has to receive the message
     * @param tag the tag that identifies the type of the message
     *            (which is used by the destination entity to perform operations based on the message type)
     */
    public CloudSimEvent(
            final SimEntity destination, final CloudActionTags tag) {
        this(0, destination, destination, tag, null);
    }


    /**
     * Creates a CloudSimEvent cloning another given one.
     *
     * @param source the event to clone
     */
    public CloudSimEvent(final SimEvent source) {
        this(source.getTime(), source.getSource(), source.getDestination(), source.getTag(), source.getData());
    }

    /**
     * Creates a CloudSimEvent.
     * @param delay how many seconds after the current simulation time the event should be scheduled
     * @param source the source entity which is sending the message
     * @param destination the destination entity which has to receive the message
     * @param tag the tag that identifies the type of the message
     *            (which is used by the destination entity to perform operations based on the message type)
     * @param data the data attached to the message, that depends on the message tag
     */
    public CloudSimEvent(
            final double delay,
            final SimEntity source, final SimEntity destination,
            final CloudActionTags tag, final Object data)
    {
        if (delay < 0) {
            throw new IllegalArgumentException("Delay can't be negative.");
        }

        this.setSource(source);
        this.setDestination(destination);
        this.setSimulation(source.getSimulation());
        this.time = BigDecimal.valueOf(simulation.clock() + delay).setScale(getSimulation().getSimulationAccuracy(), RoundingMode.HALF_UP).doubleValue();
        this.tag = tag;
        this.data = data;
    }

    @Override
    public int compareTo(final SimEvent that) {
        if (that == null || that == NULL) {
            return 1;
        }

        if (this == that) {
            return 0;
        }

        int res = Double.compare(time, that.getTime());
        if (res != 0) {
            return res;
        }

        res = this.tag.compareTo(that.getTag());
        if (res != 0) {
            return res;
        }

        return Long.compare(serial, that.getSerial());
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        final CloudSimEvent that = (CloudSimEvent) obj;
        return Double.compare(that.getTime(), getTime()) == 0 && getTag() == that.getTag() && getSerial() == that.getSerial();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getTime(), getTag(), getSerial());
    }


    @Override
    public String toString() {
        return "Event tag = " + CloudActionTags .tagToString(tag) + " source = " + source.getName() +
                " target = " + destination.getName() + " time = " + time;
    }
}
