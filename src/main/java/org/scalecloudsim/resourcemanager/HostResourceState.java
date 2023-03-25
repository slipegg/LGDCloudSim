package org.scalecloudsim.resourcemanager;

public class HostResourceState {
    long ram;
    long bw;
    long storage;
    long cpu;

    public HostResourceState(long ram, long bw, long storage, long cpu) {
        this.ram = ram;
        this.bw = bw;
        this.storage = storage;
        this.cpu = cpu;
    }

    public void setState(long ram, long bw, long storage, long cpu) {
        this.ram = ram;
        this.bw = bw;
        this.storage = storage;
        this.cpu = cpu;
    }

    @Override
    public boolean equals(final Object obj) {
        return obj instanceof HostResourceState that &&
                that.ram == this.ram &&
                that.bw == this.bw &&
                that.storage == this.storage &&
                that.cpu == this.cpu;
    }
    @Override
    public int hashCode() {
        int res=Long.hashCode(ram);
        res=31*res+Long.hashCode(bw);
        res=31*res+Long.hashCode(storage);
        res=31*res+Long.hashCode(cpu);
        return res;
    }
    @Override
    public String toString() {
        return "HostResourceState{ram="+ram+",bw="+bw+",storage="+storage+",cpu="+cpu+"}";
    }

    public long getRam() {
        return ram;
    }

    public void setRam(long ram) {
        this.ram = ram;
    }

    public long getBw() {
        return bw;
    }

    public void setBw(long bw) {
        this.bw = bw;
    }

    public long getStorage() {
        return storage;
    }

    public void setStorage(long storage) {
        this.storage = storage;
    }

    public long getCpu() {
        return cpu;
    }

    public void setCpu(long cpu) {
        this.cpu = cpu;
    }
}
