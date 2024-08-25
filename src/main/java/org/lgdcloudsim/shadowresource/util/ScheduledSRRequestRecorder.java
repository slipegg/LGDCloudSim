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

    public ScheduledSRRequestRecorder() {
        scheduledSRRequestMap = new HashMap<>();
        SRRequestUsedCpuMap = new HashMap<>();
        SRRequestUsedMemoryMap = new HashMap<>();
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

    private ScheduledSRRequestRecorder updateUsedCpuMemory(SRRequest srRequest, int flag) {
        Instance instance = srRequest.getInstance();
        int hostId = instance.getHost();
        int usedCpu = SRRequestUsedCpuMap.getOrDefault(hostId, 0);
        int usedMemory = SRRequestUsedMemoryMap.getOrDefault(hostId, 0);
        SRRequestUsedCpuMap.put(hostId, usedCpu + flag * instance.getCpu());
        SRRequestUsedMemoryMap.put(hostId, usedMemory + flag * instance.getRam());
        return this;
    }
}