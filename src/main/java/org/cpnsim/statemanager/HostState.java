package org.cpnsim.statemanager;

import lombok.Getter;
import lombok.Setter;
import org.cpnsim.request.Instance;

@Getter
@Setter
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
        this(state[0], state[1], state[2], state[3]);
    }

    public int[] getStateArray() {
        int[] state = new int[4];
        state[0] = cpu;
        state[1] = ram;
        state[2] = storage;
        state[3] = bw;
        return state;
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
