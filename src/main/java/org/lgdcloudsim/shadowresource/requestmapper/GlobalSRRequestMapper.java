package org.lgdcloudsim.shadowresource.requestmapper;

import org.lgdcloudsim.core.Nameable;
import org.lgdcloudsim.core.Simulation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class GlobalSRRequestMapper {
    /**
     * the Logger.
     **/
    private Logger LOGGER = LoggerFactory.getLogger(PartitionSRRequestMapper.class.getSimpleName());

    private Simulation simulation;

    private LinkedList<SRRequest> srRequests;

    public GlobalSRRequestMapper(Simulation simulation) {
        super();
        this.simulation = simulation;
        this.srRequests = new LinkedList<>();
    }

    public GlobalSRRequestMapper addRequest(SRRequest srRequest) {
        srRequests.add(srRequest);
        return this;
    }

    public GlobalSRRequestMapper addRequest(List<SRRequest> srRequests) {
        this.srRequests.addAll(srRequests);
        return this;
    }

    public List<SRRequest> pop(int n) {
        List<SRRequest> result = new LinkedList<>();
        for (int i = 0; i < n; i++) {
            if (srRequests.isEmpty()) {
                break;
            }
            result.add(srRequests.poll());
        }
        return result;
    }

    public int getSize() {
        return srRequests.size();
    }

    public long getSRRequestCpuTotal() {
        return srRequests.stream().mapToLong(srRequest -> srRequest.getInstance().getCpu()).sum();
    }

    public long getSRRequestMemoryTotal() {
        return srRequests.stream().mapToLong(srRequest -> srRequest.getInstance().getRam()).sum();
    }

    public String getName() {
        return "GlobalSRRequestMapper";
    }
}
