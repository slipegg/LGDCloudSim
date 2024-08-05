package org.lgdcloudsim.shadowresource.hostsrmapper;

import lombok.Setter;
import org.lgdcloudsim.core.Nameable;
import org.lgdcloudsim.core.Simulation;
import org.lgdcloudsim.request.Instance;
import org.lgdcloudsim.shadowresource.requestmapper.PartitionSRRequestMapper;
import org.lgdcloudsim.shadowresource.requestmapper.SRRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class PartitionHostSRMapperSimple implements Nameable {
    /**
     * the Logger.
     **/
    private Logger LOGGER = LoggerFactory.getLogger(PartitionSRRequestMapper.class.getSimpleName());

    private int partitionId;

    private Simulation simulation;

    HostSRMapper hostSRMapper;

    @Setter
    PartitionSRRequestMapper partitionSRRequestMapper;

    public PartitionHostSRMapperSimple(HostSRMapper hostSRMapper, Simulation simulation, int partitionId) {
        super();
        this.simulation = simulation;
        this.partitionId = partitionId;
        this.hostSRMapper = hostSRMapper;
    }

    public List<Instance> receiveHostSR(HostSR hostSR) {
        List<Instance> instances = partitionSRRequestMapper.scheduleForHostSR(hostSR);
        if (instances != null && !instances.isEmpty()) {
            hostSR.setSRCpu(hostSR.getSRCpu() - instances.stream().mapToInt(Instance::getCpu).sum());
            hostSR.setSRMemory(hostSR.getSRMemory() - instances.stream().mapToInt(Instance::getRam).sum());
        }
        hostSRMapper.push(hostSR);

        return instances;
    }

    public Instance scheduleForSRRequest(SRRequest srRequest) {
        int id = hostSRMapper.schedule(srRequest);
        return srRequest.getInstance().setExpectedScheduleHostId(id);
    }

    public List<Instance> scheduleForSRRequests(List<SRRequest> srRequests) {
        List<Instance> res = new ArrayList<>();
        for(SRRequest srRequest : srRequests) {
            res.add(scheduleForSRRequest(srRequest));
        }

        return res;
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
