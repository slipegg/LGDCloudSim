package org.scalecloudsim.statemanager;

public class HostStateHistory extends HostState {
    double time;

    public HostStateHistory(int cpu, int ram, int storage, int bw, double time) {
        super(cpu, ram, storage, bw);
        this.time = time;
    }

    public HostStateHistory() {
        super(-1, -1, -1, -1);
        this.time = -1;
    }

    public double getTime() {
        return time;
    }

    public void setTime(double time) {
        this.time = time;
    }

    public HostStateHistory setHistoryStatus(HostStateHistory hostStateHistory) {
        setState((HostState) (hostStateHistory));
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
