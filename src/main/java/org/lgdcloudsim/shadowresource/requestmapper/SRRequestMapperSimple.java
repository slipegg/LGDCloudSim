package org.lgdcloudsim.shadowresource.requestmapper;

import org.lgdcloudsim.shadowresource.hostsrmapper.HostSR;

import java.util.*;

public class SRRequestMapperSimple implements SRRequestMapper {
    private TreeMap<Integer, TreeMap<Integer, LinkedList<SRRequest>>> requestMap;
    private int size ;
    private long SRCpuTotal;
    private long SRMemoryTotal;

    public SRRequestMapperSimple() {
        this.requestMap = new TreeMap<>();
        this.size = 0;
        this.SRCpuTotal = 0;
        this.SRMemoryTotal = 0;
    }

    @Override
    public SRRequestMapper push(SRRequest srRequest) {
        int cpu = srRequest.getInstance().getCpu();
        int memory = srRequest.getInstance().getRam();

        requestMap.putIfAbsent(cpu, new TreeMap<>());
        requestMap.get(cpu).putIfAbsent(memory, new LinkedList<>());
        requestMap.get(cpu).get(memory).add(srRequest);

        size++;
        SRCpuTotal += cpu;
        SRMemoryTotal += memory;

        return this;
    }

    @Override
    public SRRequestMapper push(List<SRRequest> srRequests) {
        for (SRRequest srRequest : srRequests) {
            push(srRequest);
        }
        return this;
    }

    @Override
    public SRRequest poll(int cpu, int memory) {
        if (requestMap.containsKey(cpu) && requestMap.get(cpu).containsKey(memory) && !requestMap.get(cpu).get(memory).isEmpty()) {
            size--;
            SRCpuTotal -= cpu;
            SRMemoryTotal -= memory;
        }

        return requestMap.get(cpu).get(memory).poll();

    }

    @Override
    public List<SRRequest> schedule(HostSR hostSR) {
        if (isAnyRequestSatisfiedShadowResource(hostSR)) {
            return scheduleRequests(hostSR);
        } else {
            // TODO 尝试超额分配
            // TODO 尝试去获取新的请求
            return new ArrayList<>();
        }
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public long SRRequestCpuTotal() {
        return SRCpuTotal;
    }

    @Override
    public long SRRequestMemoryTotal() {
        return SRMemoryTotal;
    }

    private boolean isAnyRequestSatisfiedShadowResource(HostSR hostSR){
        // TODO 后面需要考虑如果最小请求和最大可用请求相差不大，是否可以继续更加激进地调度
        if (requestMap.isEmpty()) {
            return false;
        }

        int shadowResourceCPU = hostSR.getSRCpu();
        int shadowResourceMemory = hostSR.getSRMemory();

        Integer minRequestedCPU = requestMap.firstKey();
        TreeMap<Integer, LinkedList<SRRequest>> memoryMapForMinCPU = requestMap.get(minRequestedCPU);

        if (memoryMapForMinCPU == null || memoryMapForMinCPU.isEmpty()) {
            return false;
        }

        Integer minRequestedMemory = memoryMapForMinCPU.firstKey();
        LinkedList<SRRequest> minRequests = memoryMapForMinCPU.get(minRequestedMemory);

        if (minRequests == null || minRequests.isEmpty()) {
            System.err.println("Error: minRequests is not null but empty");
            return false;
        }

        if (minRequestedCPU> shadowResourceCPU || minRequestedMemory > shadowResourceMemory) {
            return false;
        }

        return true;
    }

    private List<SRRequest> scheduleRequests(HostSR hostSR) {
        // TODO: 装箱问题-价值最大化，目前只是考虑把最大的请求放进去，后续需要考虑时间因素
        int shadowResourceCPU = hostSR.getSRCpu();
        int shadowResourceMemory = hostSR.getSRMemory();

        List<SRRequest> scheduledSRRequests = new ArrayList<>();

        // Create a descending iterator for CPU
        Iterator<Integer> requestedCpuIterator = requestMap.descendingKeySet().iterator();
        while (requestedCpuIterator.hasNext()) {
            int requestCpu = requestedCpuIterator.next();

            if (requestCpu > shadowResourceCPU) {
                continue;
            }

            // Create a descending iterator for Memory
            TreeMap<Integer, LinkedList<SRRequest>> memoryMap = requestMap.get(requestCpu);
            Iterator<Integer> memoryIterator = memoryMap.descendingKeySet().iterator();
            while (memoryIterator.hasNext()) {
                int requestMemory = memoryIterator.next();

                if (requestMemory > shadowResourceMemory) {
                    continue;
                }

                LinkedList<SRRequest> srRequests = memoryMap.get(requestMemory);
                while (!srRequests.isEmpty()) {
                    if (shadowResourceCPU < requestCpu || shadowResourceMemory < requestMemory) {
                        break;
                    }

                    SRRequest srInstance = poll(requestCpu, requestMemory);
                    scheduledSRRequests.add(srInstance);

                    shadowResourceCPU -= requestCpu;
                    shadowResourceMemory -= requestMemory;
                }

                if (memoryMap.get(requestMemory).isEmpty()) {
                    memoryIterator.remove();
                }
            }

            if (memoryMap.isEmpty()) {
                requestedCpuIterator.remove();
            }

            if (shadowResourceCPU ==0 || shadowResourceMemory == 0) {
                break;
            }
        }

        return scheduledSRRequests;
    }
}
