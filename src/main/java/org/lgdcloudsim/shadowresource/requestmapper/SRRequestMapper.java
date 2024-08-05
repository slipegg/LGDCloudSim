package org.lgdcloudsim.shadowresource.requestmapper;

import org.lgdcloudsim.shadowresource.hostsrmapper.HostSR;

import java.util.List;

public interface SRRequestMapper {
    SRRequestMapper push(SRRequest srRequest);

    SRRequestMapper push(List<SRRequest> srRequests);

    SRRequest poll(int cpu, int memory);

    List<SRRequest> schedule(HostSR hostSR);

    int size();

    long SRRequestCpuTotal();

    long SRRequestMemoryTotal();

    List<Integer> getCPUList();

    List<Integer> getMemoryList(int cpu);

    List<SRRequest> getSRRequests(int cpu, int memory);
}
