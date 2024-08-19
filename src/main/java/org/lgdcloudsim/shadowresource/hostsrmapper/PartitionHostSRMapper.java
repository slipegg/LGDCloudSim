package org.lgdcloudsim.shadowresource.hostsrmapper;

import lombok.Getter;
import lombok.Setter;
import org.lgdcloudsim.core.Nameable;
import org.lgdcloudsim.core.Simulation;
import org.lgdcloudsim.request.Instance;
import org.lgdcloudsim.shadowresource.requestmapper.PartitionSRRequestMapper;
import org.lgdcloudsim.shadowresource.requestmapper.SRRequest;
import org.lgdcloudsim.shadowresource.util.Queue;
import org.lgdcloudsim.shadowresource.util.QueueFifo;
import org.lgdcloudsim.shadowresource.util.SRScheduleRes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class PartitionHostSRMapper implements Nameable {
    /**
     * the Logger.
     **/
    private Logger LOGGER = LoggerFactory.getLogger(PartitionSRRequestMapper.class.getSimpleName());

    private int partitionId;

    private Simulation simulation;

    private HostSRMapper hostSRMapper;

    private Queue<HostSR> hostSRQueue;

    @Getter
    @Setter
    private boolean isBusy;

    @Setter
    PartitionSRRequestMapper partitionSRRequestMapper;

    public PartitionHostSRMapper(HostSRMapper hostSRMapper, Simulation simulation, int partitionId) {
        super();
        this.simulation = simulation;
        this.partitionId = partitionId;
        this.hostSRMapper = hostSRMapper;
        this.hostSRQueue = new QueueFifo<HostSR>();
        this.isBusy = false;
    }
    
    public PartitionHostSRMapper receiveHostSR(List<HostSR> hostSR) {
        hostSRQueue.add(hostSR);

        return this;
    }

    public List<SRRequest> scheduleForNewHostSR(){
        List<SRRequest> scheduledRequests = new ArrayList<>();

        List<HostSR> hostSRs = hostSRQueue.getBatchItem();
        if (hostSRs.isEmpty()) {
            return scheduledRequests;
        }

        for (HostSR hostSR : hostSRs) {
            List<SRRequest> srRequests = partitionSRRequestMapper.scheduleForHostSR(hostSR);
            if (srRequests != null && !srRequests.isEmpty()) {
                hostSR.setSRCpu(hostSR.getSRCpu() - srRequests.stream().mapToInt(srRequest -> srRequest.getInstance().getCpu()).sum());
                hostSR.setSRMemory(hostSR.getSRMemory() - srRequests.stream().mapToInt(srRequest -> srRequest.getInstance().getRam()).sum());
            }
            if (hostSR.getSRCpu() > 0 || hostSR.getSRMemory() > 0) {
                hostSRMapper.push(hostSR);
            }

            scheduledRequests.addAll(srRequests);
        }

        return scheduledRequests;
    }

    public PartitionHostSRMapper receiveHostSR(HostSR hostSR) {
        hostSRQueue.add(hostSR);

        return this;
        // List<Instance> instances = partitionSRRequestMapper.scheduleForHostSR(hostSR);
        // if (instances != null && !instances.isEmpty()) {
        //     hostSR.setSRCpu(hostSR.getSRCpu() - instances.stream().mapToInt(Instance::getCpu).sum());
        //     hostSR.setSRMemory(hostSR.getSRMemory() - instances.stream().mapToInt(Instance::getRam).sum());
        // }
        // hostSRMapper.push(hostSR);

        // return instances;
    }

    public SRRequest scheduleForSRRequest(SRRequest srRequest) {
        int id = hostSRMapper.schedule(srRequest);
        if (id == -1) {
            return null;
        }else {
            srRequest.getInstance().setExpectedScheduleHostId(id);
            return srRequest;
        }
    }

    public SRScheduleRes scheduleForSRRequest(List<SRRequest> srRequests) {
        SRScheduleRes srScheduleRes = new SRScheduleRes();
        for(SRRequest srRequest : srRequests) {
            Instance instance = scheduleForSRRequest(srRequest);
            if (instance != null) {
                srScheduleRes.addSuccess(srRequest);
            }else {
                srScheduleRes.addNoScheduled(srRequest);
            }
        }

        return srScheduleRes;
    }

    public boolean isQueueEmpty(){
        return hostSRQueue.isEmpty();
    }

    @Override
    public int getId() {
        return partitionId;
    }

    @Override
    public String getName() {
        return "PartitionSRMapperSimple-" + partitionId;
    }
}
