package org.lgdcloudsim.shadowresource.hostsrmapper;

import org.lgdcloudsim.shadowresource.requestmapper.SRRequest;

import java.util.*;

public class HostSRMapperSimple implements HostSRMapper{
    TreeMap<Integer, TreeMap<Integer, LinkedList<HostSR>>> hostSRMap;

    HashMap<Integer, HostSR> existHostSRMap;

    int size;

    long HostSRCpuTotal;

    long HostSRMemoryTotal;

    public HostSRMapperSimple() {
        this.hostSRMap = new TreeMap<>();
        this.existHostSRMap = new HashMap<>();
        this.size = 0;
        this.HostSRCpuTotal = 0;
        this.HostSRMemoryTotal = 0;
    }

    @Override
    public HostSRMapper push(HostSR hostSR) {
        if (existHostSRMap.containsKey(hostSR.getHostId())) {
            HostSR oldHostSR = existHostSRMap.get(hostSR.getHostId());

            hostSR.setSRCpu(hostSR.getSRCpu() + oldHostSR.getSRCpu());
            hostSR.setSRMemory(hostSR.getSRMemory() + oldHostSR.getSRMemory());

            remove(oldHostSR.getHostId());
        }

        int cpu = hostSR.getSRCpu();
        int memory = hostSR.getSRMemory();

        hostSRMap.putIfAbsent(cpu, new TreeMap<>());
        hostSRMap.get(cpu).putIfAbsent(memory, new LinkedList<>());
        hostSRMap.get(cpu).get(memory).add(hostSR);

        existHostSRMap.put(hostSR.getHostId(), hostSR);

        size++;
        HostSRCpuTotal += cpu;
        HostSRMemoryTotal += memory;

        return this;
    }

    @Override
    public HostSRMapper push(List<HostSR> hostSRs) {
        for (HostSR hostSR : hostSRs) {
            push(hostSR);
        }
        return this;
    }

    @Override
    public HostSRMapper update(HostSR hostSR) {
        remove(hostSR.getHostId());
        if (hostSR.getSRCpu() > 0 && hostSR.getSRMemory() > 0) {
            push(hostSR);
        }
        return this;
    }

    @Override
    public HostSRMapper remove(int hostId) {
        HostSR hostSR = existHostSRMap.get(hostId);
        if (hostSR != null) {
            int cpu = hostSR.getSRCpu();
            int memory = hostSR.getSRMemory();
            if (hostSRMap.containsKey(cpu) && hostSRMap.get(cpu).containsKey(memory) && !hostSRMap.get(cpu).get(memory).isEmpty()) {
                hostSRMap.get(cpu).get(memory).remove(hostSR);
                size--;
                HostSRCpuTotal -= cpu;
                HostSRMemoryTotal -= memory;
            }
            existHostSRMap.remove(hostId);
        }
        return this;
    }

    @Override
    public HostSR poll(int cpu, int memory) {
        if (hostSRMap.containsKey(cpu) && hostSRMap.get(cpu).containsKey(memory) && !hostSRMap.get(cpu).get(memory).isEmpty()) {
            size--;
            HostSRCpuTotal -= cpu;
            HostSRMemoryTotal -= memory;
        }

        return hostSRMap.get(cpu).get(memory).poll();
    }

    @Override
    public int schedule(SRRequest srRequest) {
        int hostId = tryScheduleWithInSR(srRequest);
        if (hostId == -1) {
            // TODO 尝试超额分配
        }
        return hostId;
    }

    private int tryScheduleWithInSR(SRRequest srRequest){
        int srRequestCpu = srRequest.getInstance().getCpu();
        int srRequestMemory = srRequest.getInstance().getRam();

        for (int cpu : hostSRMap.keySet()) {
            if (cpu < srRequestCpu) {
                continue;
            }

            for (int memory : hostSRMap.get(cpu).keySet()) {
                if (memory < srRequestMemory) {
                    continue;
                }

                HostSR hostSR = poll(cpu, memory);
                if (hostSR != null) {
                    hostSR.setSRCpu(hostSR.getSRCpu() - srRequestCpu);
                    hostSR.setSRMemory(hostSR.getSRMemory() - srRequestMemory);
                    update(hostSR);

                    return hostSR.getHostId();
                }
            }
        }
        return -1;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public long HostSRCpuTotal() {
        return HostSRCpuTotal;
    }

    @Override
    public long HostSRMemoryTotal() {
        return HostSRMemoryTotal;
    }
}
