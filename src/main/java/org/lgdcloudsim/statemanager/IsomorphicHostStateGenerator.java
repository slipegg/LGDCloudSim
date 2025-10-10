package org.lgdcloudsim.statemanager;

import lombok.Getter;
import lombok.Setter;

/**
 * A class to generate isomorphic host state.
 *
 * @author Anonymous
 * @since LGDCloudSim 1.0
 */

@Getter
@Setter
public class IsomorphicHostStateGenerator implements HostStateGenerator {
    /** the amount of cpu that is available on the host */
    int cpu;

    /** the amount of ram that is available on the host */
    int ram;

    /** the amount of storage that is available on the host */
    int storage;

    /** the amount of bw that is available on the host */
    int bw;

    /** the amount of gpu that is available on the host */
    int gpu;

    /** the type of gpu that is available on the host */
    String gpuType;

    /**
     * Create a IsomorphicHostStateGenerator object with the given 4 resource.
     *
     * @param cpu     the amount of cpu that is available on the host
     * @param ram     the amount of ram that is available on the host
     * @param storage the amount of storage that is available on the host
     * @param bw      the amount of bw that is available on the host
     */
    public IsomorphicHostStateGenerator(int cpu, int ram, int storage, int bw, int gpu, String gpuType) {
        this.cpu = cpu;
        this.ram = ram;
        this.storage = storage;
        this.bw = bw;
        this.gpu = gpu;
        this.gpuType = gpuType;
    }

    /**
     * Create a IsomorphicHostStateGenerator object with the default 4 resource.
     * */
    public IsomorphicHostStateGenerator() {
        this.cpu = 124;
        this.ram = 1024;
        this.storage = 10240;
        this.bw = 1024;
        this.gpu = 16;
        this.gpuType = "NVIDIA A100";
    }

    /**
     * Generate the isomorphic state of a host.
     *
     * @return the state of the host.The state includes 4 integers: cpu, ram, storage and bw.
     * */
    @Override
    public HostState generateHostState() {
        int[] hostStates = new int[HostState.STATE_NUM];
        hostStates[0] = cpu;
        hostStates[1] = ram;
        hostStates[2] = storage;
        hostStates[3] = bw;
        hostStates[4] = gpu;
        return new HostState(hostStates, gpuType);
    }
}
