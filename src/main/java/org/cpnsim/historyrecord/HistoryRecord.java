package org.cpnsim.historyrecord;

import org.cpnsim.statemanager.HostStateHistory;

import java.util.List;

public interface HistoryRecord {
    void record(int hostId, int time, int cpu, int ram, int storage, int bw);
    HostStateHistory get(int hostId, int time);
    List<HostStateHistory> get(int hostId);
}
