package org.cpnsim.statemanager;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IsomorphicHostStateGenerator implements HostStateGenerator {
    int cpu;
    int ram;
    int storage;
    int bw;

    public IsomorphicHostStateGenerator(int cpu, int ram, int storage, int bw) {
        this.cpu = cpu;
        this.ram = ram;
        this.storage = storage;
        this.bw = bw;
    }

    public IsomorphicHostStateGenerator() {
        this.cpu = 124;
        this.ram = 1024;
        this.storage = 10240;
        this.bw = 1024;
    }


    @Override
    public int[] generateHostState() {
        int[] hostStates = new int[4];
        hostStates[0] = cpu;
        hostStates[1] = ram;
        hostStates[2] = storage;
        hostStates[3] = bw;
        return hostStates;
    }
}
