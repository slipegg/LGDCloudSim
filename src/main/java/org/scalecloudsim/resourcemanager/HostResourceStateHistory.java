package org.scalecloudsim.resourcemanager;

public class HostResourceStateHistory extends HostResourceState{
    double time;
    public HostResourceStateHistory(long ram, long bw, long storage, long cpu,double time){
        super(ram, bw, storage, cpu);
        this.time=time;
    }

    public double getTime() {
        return time;
    }

    public HostResourceStateHistory setTime(double time) {
        this.time = time;
        return this;
    }

    public HostResourceStateHistory setHistoryStatus(HostResourceStateHistory hostResourceStateHistory){
        setState(hostResourceStateHistory.ram, hostResourceStateHistory.bw, hostResourceStateHistory.storage,hostResourceStateHistory.cpu);
        setTime(hostResourceStateHistory.time);
        return this;
    }
    @Override
    public String toString() {
        return "HostResourceStateHistory{ram="+ram+",bw="+bw+",storage="+storage+",cpu="+cpu+",time="+time+"}";
    }
}
