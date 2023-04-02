package org.scalecloudsim.statemanager;

public class HostState {
    static int STATE_NUM = 4;
    int cpu;
    int ram;
    int storage;
    int bw;

    public HostState(int cpu, int ram, int storage, int bw) {
        this.cpu = cpu;
        this.ram = ram;
        this.storage = storage;
        this.bw = bw;
    }

    public int[] getStateArray() {
        int[] state = new int[4];
        state[0] = cpu;
        state[1] = ram;
        state[2] = storage;
        state[3] = bw;
        return state;
    }

    public int getRam() {
        return ram;
    }

    public void setRam(int ram) {
        this.ram = ram;
    }

    public int getStorage() {
        return storage;
    }

    public void setStorage(int storage) {
        this.storage = storage;
    }

    public int getBw() {
        return bw;
    }

    public void setBw(int bw) {
        this.bw = bw;
    }

    public int getCpu() {
        return cpu;
    }

    public void setCpu(int cpu) {
        this.cpu = cpu;
    }

    public void setState(HostState hostState) {
        this.cpu = hostState.getCpu();
        this.ram = hostState.getRam();
        this.storage = hostState.getStorage();
        this.bw = hostState.getBw();
    }

    @Override
    public String toString() {
        return "HostState{" +
                "cpu=" + cpu +
                ", ram=" + ram +
                ", storage=" + storage +
                ", bw=" + bw +
                '}';
    }
}
