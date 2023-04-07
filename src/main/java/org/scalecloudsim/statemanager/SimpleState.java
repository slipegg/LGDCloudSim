package org.scalecloudsim.statemanager;

import org.apache.commons.lang3.mutable.MutableInt;

import java.util.List;
import java.util.Map;

public interface SimpleState {
    SimpleState updateStorageSum(int changeStorage);

    SimpleState updateBwSum(int changeBw);

    SimpleState addCpuRamRecord(int cpu, int ram);

    SimpleState updateCpuRamMap(int originCpu, int originRam, int nowCpu, int nowRam);

    long getStorageAvaiableSum();

    long getBwAvaiableSum();

    Map<Integer, Map<Integer, MutableInt>> getCpuRamMap();

    int getCpuRamSum(int cpu, int ram);

    List<List<Integer>> getCpuRamItem();

}
