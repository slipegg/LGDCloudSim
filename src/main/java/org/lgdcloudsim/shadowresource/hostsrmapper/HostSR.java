package org.lgdcloudsim.shadowresource.hostsrmapper;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HostSR {
    private int hostId;
    private int SRCpu;
    private int SRMemory;
    private double SRLife;
    private int availableCpu;
    private int availableMemory;
    private int cpuCapacity;
    private int memoryCapacity;

    public HostSR(int hostId, int SRCpu, int SRMemory, double SRLife, int availableCpu, int availableMemory, int cpuCapacity, int memoryCapacity) {
        this.hostId = hostId;
        this.SRCpu = SRCpu;
        this.SRMemory = SRMemory;
        this.SRLife = SRLife;
        this.availableCpu = availableCpu;
        this.availableMemory = availableMemory;
        this.cpuCapacity = cpuCapacity;
        this.memoryCapacity = memoryCapacity;
    }

    public void addHostSR(HostSR hostSR) {
        this.SRCpu += hostSR.SRCpu;
        this.SRMemory += hostSR.SRMemory;
        this.availableCpu = hostSR.availableCpu;
        this.availableMemory = hostSR.availableMemory;
    }

    @Override
    public String toString(){
        return "HostSR[hostId=" + hostId + ", SRCpu=" + SRCpu + ", SRMemory=" + SRMemory + ", SRLife=" + SRLife + ", availableCpu=" + availableCpu + ", availableMemory=" + availableMemory + "]";
    }
}
