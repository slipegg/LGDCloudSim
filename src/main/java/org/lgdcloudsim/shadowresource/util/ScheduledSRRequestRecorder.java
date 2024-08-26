package org.lgdcloudsim.shadowresource.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lgdcloudsim.request.Instance;
import org.lgdcloudsim.shadowresource.requestmapper.SRRequest;

public class ScheduledSRRequestRecorder {
    Map<Integer, List<SRRequest>> scheduledSRRequestMap;
    Map<Integer, Integer> SRRequestUsedCpuMap;
    Map<Integer, Integer> SRRequestUsedMemoryMap;
    int SRRequestUsedCpuSum;
    int SRRequestUsedMemorySum;

    public ScheduledSRRequestRecorder() {
        scheduledSRRequestMap = new HashMap<>();
        SRRequestUsedCpuMap = new HashMap<>();
        SRRequestUsedMemoryMap = new HashMap<>();
        SRRequestUsedCpuSum = 0;
        SRRequestUsedMemorySum = 0;
    }

    public ScheduledSRRequestRecorder addScheduledSRRequest(SRRequest srRequest) {
        Instance instance = srRequest.getInstance();
        scheduledSRRequestMap.computeIfAbsent(instance.getExpectedScheduleHostId(), k -> new ArrayList<>()).add(srRequest);

        updateUsedCpuMemory(srRequest, 1);
        return this;
    }

    public ScheduledSRRequestRecorder removeScheduledSRRequest(SRRequest srRequest) {
        Instance instance = srRequest.getInstance();
        scheduledSRRequestMap.get(instance.getHost()).remove(srRequest);
     
        updateUsedCpuMemory(srRequest, -1);
        return this;
    }

    public List<SRRequest> getScheduledSRRequests(int hostId) {
        return scheduledSRRequestMap.getOrDefault(hostId, new ArrayList<>());
    }

    public int getSRUsedCpu(int hostId) {
        return SRRequestUsedCpuMap.getOrDefault(hostId, 0);
    }

    public int getSRUsedMemory(int hostId) {
        return SRRequestUsedMemoryMap.getOrDefault(hostId, 0);
    }

    public int getSRRequestUsedCpuSum() {
        return SRRequestUsedCpuSum;
    }

    public int getSRRequestUsedMemorySum() {
        return SRRequestUsedMemorySum;
    }

    private ScheduledSRRequestRecorder updateUsedCpuMemory(SRRequest srRequest, int flag) {
        Instance instance = srRequest.getInstance();
        int hostId = instance.getHost();
        int usedCpu = SRRequestUsedCpuMap.getOrDefault(hostId, 0);
        int usedMemory = SRRequestUsedMemoryMap.getOrDefault(hostId, 0);
        int cpuChange = flag * instance.getCpu();
        int memoryChange = flag * instance.getRam();
        SRRequestUsedCpuMap.put(hostId, usedCpu + cpuChange);
        SRRequestUsedMemoryMap.put(hostId, usedMemory + memoryChange);
        SRRequestUsedCpuSum += cpuChange;
        SRRequestUsedMemorySum += memoryChange;
        return this;
    }
}