package org.cpnsim.statemanager;

import org.apache.commons.lang3.mutable.MutableInt;

import java.util.List;
import java.util.Map;

public interface SimpleState {
    SimpleState updateStorageSum(int changeStorage);

    SimpleState updateBwSum(int changeBw);

    SimpleState addCpuRamRecord(int cpu, int ram);

    SimpleState updateCpuRamMap(int originCpu, int originRam, int nowCpu, int nowRam);

    long getCpuAvaiableSum();

    long getRamAvaiableSum();

    long getStorageAvaiableSum();

    long getBwAvaiableSum();

    int getCpuRamSum(int cpu, int ram);

    Map<Integer, Map<Integer, MutableInt>> getCpuRamMap();
}
