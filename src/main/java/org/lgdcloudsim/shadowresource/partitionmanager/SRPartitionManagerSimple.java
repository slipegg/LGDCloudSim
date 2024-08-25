package org.lgdcloudsim.shadowresource.partitionmanager;

import java.util.ArrayList;
import java.util.List;

import org.lgdcloudsim.core.Nameable;
import org.lgdcloudsim.shadowresource.hostsrmapper.HostSR;
import org.lgdcloudsim.shadowresource.hostsrmapper.HostSRMapper;
import org.lgdcloudsim.shadowresource.hostsrmapper.HostSRMapperSimple;
import org.lgdcloudsim.shadowresource.util.Queue;
import org.lgdcloudsim.shadowresource.util.QueueFifo;
import org.lgdcloudsim.shadowresource.util.SRRequestScheduledRes;
import org.lgdcloudsim.shadowresource.requestmapper.SRRequest;
import org.lgdcloudsim.shadowresource.requestmapper.SRRequestMapper;
import org.lgdcloudsim.shadowresource.requestmapper.SRRequestMapperSimple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.Getter;
import lombok.Setter;

public class SRPartitionManagerSimple  implements SRPartitionManager, Nameable{
    /**
     * the Logger.
     **/
    private Logger LOGGER = LoggerFactory.getLogger(SRPartitionManagerSimple.class.getSimpleName());

    @Getter
    private int partitionId;

    private Queue<SRRequest> srRequestQueue;
    
    private SRRequestMapper srRequestMapper;

    @Getter
    @Setter
    private boolean isSRRequestScheduleBusy;

    private Queue<HostSR> hostSRQueue;

    private HostSRMapper hostSRMapper;

    @Getter
    @Setter
    private boolean isHostSRScheduleBusy;

    public SRPartitionManagerSimple(int partitionId) {
        this.partitionId = partitionId;
        srRequestQueue = new QueueFifo<SRRequest>();
        hostSRQueue = new QueueFifo<HostSR>();
        srRequestMapper = new SRRequestMapperSimple();
        hostSRMapper = new HostSRMapperSimple();
        isSRRequestScheduleBusy = false;
        isHostSRScheduleBusy = false;
    }

    @Override
    public SRPartitionManagerSimple addToQueue(List<SRRequest> srRequests) {
        srRequestQueue.add(srRequests);
        return this;
    }

    @Override
    public SRPartitionManagerSimple addToQueue(HostSR hostSR) {
        hostSRQueue.add(hostSR);
        return this;
    }

    @Override
    public SRRequestScheduledRes scheduleForNewSRRequest(){
        double startTime = System.currentTimeMillis();
        
        List<SRRequest> scheduledRequests = new ArrayList<>();
        List<SRRequest> srRequests = srRequestQueue.getBatchItem();

        for (SRRequest srRequest : srRequests) {
            SRRequest scheduledSrRequest = scheduleForSRRequest(srRequest);
            if (scheduledSrRequest != null) {
                scheduledRequests.add(scheduledSrRequest);
            }else{
                srRequestMapper.push(srRequest);
            }
        }

        double endTime = System.currentTimeMillis();
        return new SRRequestScheduledRes(partitionId, scheduledRequests, endTime - startTime);
    }

    private SRRequest scheduleForSRRequest(SRRequest srRequest) {
        int id = hostSRMapper.schedule(srRequest);
        if (id == -1) {
            return null;
        }else {
            srRequest.getInstance().setExpectedScheduleHostId(id);
            return srRequest;
        }
    }

    @Override
    public int collectSR(HostSR hostSR) {
        hostSRQueue.add(hostSR);

        if (!isHostSRScheduleBusy) {
            setHostSRScheduleBusy(true);
            return NEED_START_SCHEDULE;
        } else {
            return ALREADY_IN_SCHEDULE;
        }
    }

    
    public SRRequestScheduledRes scheduleForNewHostSR(){
        double startTime = System.currentTimeMillis();

        List<SRRequest> scheduledRequests = new ArrayList<>();

        List<HostSR> hostSRs = hostSRQueue.getBatchItem();

        for (HostSR hostSR : hostSRs) {
            List<SRRequest> srRequests = srRequestMapper.schedule(hostSR);
            if (srRequests != null && !srRequests.isEmpty()) {
                // 将所有的SRRequest的ExpectedScheduleHostId设置为hostSR.getId()
                srRequests.forEach(srRequest -> srRequest.getInstance().setExpectedScheduleHostId(hostSR.getHostId()));
                hostSR.setSRCpu(hostSR.getSRCpu() - srRequests.stream().mapToInt(srRequest -> srRequest.getInstance().getCpu()).sum());
                hostSR.setSRMemory(hostSR.getSRMemory() - srRequests.stream().mapToInt(srRequest -> srRequest.getInstance().getRam()).sum());
            }
            if (hostSR.getSRCpu() > 0 || hostSR.getSRMemory() > 0) {
                hostSRMapper.push(hostSR);
            }

            scheduledRequests.addAll(srRequests);
        }

        double endTime = System.currentTimeMillis();
        return new SRRequestScheduledRes(partitionId, scheduledRequests, endTime - startTime);
    }

    public boolean isContinueSRRequestSchedule(int partitionId){
        return !srRequestQueue.isEmpty();
    }

    public boolean isContinueHostSRSchedule(int partitionId){
        return !hostSRQueue.isEmpty();
    }
    
    public long getTotalSRRequestedCpu() {
        List<SRRequest> srRequests = srRequestQueue.getList();
        // 计算SRRequest.getInstance().getCpu()的和
        return srRequestMapper.SRRequestCpuTotal() 
                + srRequests.stream()
                .mapToLong(request -> request.getInstance().getCpu())
                .sum();
    }

    public long getTotalHostSRCpu() {
        List<HostSR> hostSRs = hostSRQueue.getList();
        // 计算HostSR.getInstance().getCpu()的和
        return hostSRMapper.HostSRCpuTotal() 
                + hostSRs.stream()
                .mapToLong(HostSR::getSRCpu)
                .sum();
    }

    @Override
    public int getId() {
        return partitionId;
    }

    @Override
    public String getName() {
        return "PartitionManagerSimple"+partitionId;
    }

    @Override
    public void cancelHostSR(int hostId) {
        hostSRMapper.remove(hostId);
    }
    
}
