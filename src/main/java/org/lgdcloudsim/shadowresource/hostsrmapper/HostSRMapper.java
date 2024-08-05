package org.lgdcloudsim.shadowresource.hostsrmapper;

import org.lgdcloudsim.shadowresource.requestmapper.SRRequest;

import java.util.List;

public interface HostSRMapper {
    HostSRMapper push(HostSR hostSR);

    HostSRMapper push(List<HostSR> hostSRs);

    HostSRMapper update(HostSR hostSR);

    HostSRMapper remove(int hostId);

    HostSR poll(int cpu, int memory);

    int schedule(SRRequest srRequest);

    int size();

    long HostSRCpuTotal();

    long HostSRMemoryTotal();

    List<Integer> getCPUList();

    List<Integer> getMemoryList(int cpu);
}
