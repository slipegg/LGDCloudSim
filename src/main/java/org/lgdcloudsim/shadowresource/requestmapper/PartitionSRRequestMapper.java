package org.lgdcloudsim.shadowresource.requestmapper;

import lombok.Getter;
import lombok.Setter;
import org.lgdcloudsim.core.Nameable;
import org.lgdcloudsim.core.Simulation;
import org.lgdcloudsim.request.Instance;
import org.lgdcloudsim.shadowresource.hostsrmapper.HostSR;
import org.lgdcloudsim.shadowresource.hostsrmapper.PartitionHostSRMapper;
import org.lgdcloudsim.shadowresource.util.Queue;
import org.lgdcloudsim.shadowresource.util.QueueFifo;
import org.lgdcloudsim.shadowresource.util.SRScheduleRes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class PartitionSRRequestMapper implements Nameable{
    /**
     * the Logger.
     **/
    private Logger LOGGER = LoggerFactory.getLogger(PartitionSRRequestMapper.class.getSimpleName());

    private int partitionId;

    private Simulation simulation;

    SRRequestMapper srRequestMapper;

    SRRequestMapCoordinator mapCoordinator;

    @Setter
    PartitionHostSRMapper partitionHostSRMapper;

    Random random ;

    Queue<SRRequest> srRequestQueue;

    @Getter
    @Setter
    private boolean isBusy;

    public PartitionSRRequestMapper( SRRequestMapCoordinator mapCoordinator, Simulation simulation, int partitionId) {
        super();
        this.simulation = simulation;
        this.partitionId = partitionId;
        this.mapCoordinator = mapCoordinator;
        srRequestMapper = new SRRequestMapperSimple();
        random = new Random();
        srRequestQueue = new QueueFifo<>();
        this.isBusy = false;
    }

//    public int receiveRequest(SRRequest srRequest) {
//        int hostId = partitionHostSRMapperSimple.scheduleForSRRequest(srRequest);
//        if (hostId == -1) {
//            srRequestMapper.push(srRequest);
//        }
//        return hostId;
//    }

    public PartitionSRRequestMapper addSRRequest(SRRequest srRequest) {
        srRequestMapper.push(srRequest);
        return this;
    }

    public PartitionSRRequestMapper addSRRequestToQueue(List<SRRequest> srRequests) {
        srRequestQueue.add(srRequests);
        return this;
    }

    public List<Instance> scheduleSRRequests() {
        for(Integer cpu : srRequestMapper.getCPUList()){
            for(Integer memory : srRequestMapper.getMemoryList(cpu)){
                List<SRRequest> srRequests = srRequestMapper.get(cpu);
            }
        }
    }

    public List<SRRequest> scheduleForHostSR(HostSR hostSR) {
        List<SRRequest> srRequests = srRequestMapper.schedule(hostSR);
        return srRequests;
    }

    public int getSize() {
        return srRequestMapper.size();
    }

    public long getSRRequestCpuTotal() {
        return srRequestMapper.SRRequestCpuTotal();
    }

    public long getSRRequestMemoryTotal() {
        return srRequestMapper.SRRequestMemoryTotal();
    }

    public boolean isEmpty(){
        return srRequestMapper.size() == 0;
    }
    
    public boolean isQueueEmpty(){
        return srRequestQueue.isEmpty();
    }

    public List<SRRequest> popRandom(long popCpuSum, long popMemorySum){
        List<SRRequest> srRequests = new ArrayList<>();
        while (popCpuSum > 0 && popMemorySum > 0){
            List<Integer> cpus = new ArrayList<>(srRequestMapper.getCPUList());
            int randomCpu = cpus.get(random.nextInt(cpus.size()));
            List<Integer> memories = new ArrayList<>(srRequestMapper.getMemoryList(randomCpu));
            int randomMemory = memories.get(random.nextInt(memories.size()));
            SRRequest srRequest = srRequestMapper.poll(randomCpu, randomMemory);
            srRequests.add(srRequest);
            popCpuSum -= randomCpu;
            popMemorySum -= randomMemory;
        }
        return srRequests;
    }

    public SRScheduleRes scheduleBySRRequestSend(List<SRRequest> srRequests) {
        SRScheduleRes srScheduleRes = partitionHostSRMapper.scheduleForSRRequest(srRequests);
        return srScheduleRes;
    }

    public List<SRRequest> scheduleForNewSRRequests(){
        List<SRRequest> scheduledRequests = new ArrayList<>();

        List<SRRequest> srRequests = srRequestQueue.getBatchItem();
        if (srRequests.isEmpty()) {
            return scheduledRequests;
        }

        for (SRRequest srRequest : srRequests) {
            SRRequest scheduledSrRequest = partitionHostSRMapper.scheduleForSRRequest(srRequest);
            if (scheduledSrRequest != null) {
                scheduledRequests.add(scheduledSrRequest);
            }else{
                srRequestMapper.push(srRequest);
            }
        }

        return scheduledRequests;
    }

    @Override
    public int getId() {
        return partitionId;
    }

    @Override
    public String getName() {
        return "PartitionSRRequestMapper-part-" + partitionId;
    }
}
