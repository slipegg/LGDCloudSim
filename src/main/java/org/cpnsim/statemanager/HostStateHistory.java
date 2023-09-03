package org.cpnsim.statemanager;

import lombok.Getter;
import lombok.Setter;

/**
 * A class to record the state of a host history.
 * Note that it is inherited from {@link HostState}.
 * Compared with {@link HostState}, it has one more time attribute
 *
 * @author Jiawen Liu
 * @since CPNSim 1.0
 */
@Getter
@Setter
public class HostStateHistory extends HostState {
    /** The time when the host state is recorded */
    double time;

    public HostStateHistory(int cpu, int ram, int storage, int bw, double time) {
        super(cpu, ram, storage, bw);
        this.time = time;
    }

    public HostStateHistory(int[] hostState, double time) {
        super(hostState[0], hostState[1], hostState[2], hostState[3]);
        this.time = time;
    }

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
