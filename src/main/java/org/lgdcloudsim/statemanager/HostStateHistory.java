package org.lgdcloudsim.statemanager;

import lombok.Getter;
import lombok.Setter;

/**
 * A class to record the state of a host history.
 * Note that it is inherited from {@link HostState}.
 * Compared with {@link HostState}, it has one more time attribute
 *
 * @author Anonymous
 * @since LGDCloudSim 1.0
 */
@Getter
@Setter
public class HostStateHistory extends HostState {
    /** The time when the host state is recorded */
    double time;

    /**
     * Create a HostStateHistory object with the given 4 resource and time.
     *
     * @param cpu     the amount of cpu that is available on the host
     * @param ram     the amount of ram that is available on the host
     * @param storage the amount of storage that is available on the host
     * @param bw      the amount of bw that is available on the host
     * @param time    the time when the host state is recorded
     */
    public HostStateHistory(int cpu, int ram, int storage, int bw, double time) {
        super(cpu, ram, storage, bw);
        this.time = time;
    }

    /**
     * Create a HostStateHistory object with the given state and time.
     * @param hostState the state of the host
     * @param time the time when the host state is recorded
     */
    public HostStateHistory(int[] hostState, double time) {
        super(hostState[0], hostState[1], hostState[2], hostState[3]);
        this.time = time;
    }

    /**
     * Set the state of the host history through the given HostStateHistory.
     * @param hostStateHistory the state of the host history
     * @return the HostStateHistory object itself
     */
    public HostStateHistory setHistoryStatus(HostStateHistory hostStateHistory) {
        setState(hostStateHistory);
        setTime(hostStateHistory.time);
        return this;
    }

    @Override
    public String toString() {
        return "HostStateHistory{" +
                "time=" + time +
                ", cpu=" + cpu +
                ", ram=" + ram +
                ", storage=" + storage +
                ", bw=" + bw +
                '}';
    }
}
