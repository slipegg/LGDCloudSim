package org.cpnsim.statemanager;

import org.cpnsim.request.Instance;

public class HostState {
    public static int STATE_NUM = 4;
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

    public HostState(int[] state) {
        this.cpu = state[0];
        this.ram = state[1];
        this.storage = state[2];
        this.bw = state[3];
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

    public boolean isSuitable(Instance instance) {
        return cpu >= instance.getCpu() && ram >= instance.getRam() && storage >= instance.getStorage() && bw >= instance.getBw();
    }

    public void allocate(Instance instance) {
        cpu = cpu - instance.getCpu();
        ram = ram - instance.getRam();
        storage = storage - instance.getStorage();
        bw = bw - instance.getBw();
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
