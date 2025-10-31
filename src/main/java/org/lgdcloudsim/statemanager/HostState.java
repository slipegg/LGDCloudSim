package org.lgdcloudsim.statemanager;

import lombok.Getter;
import lombok.Setter;
import org.lgdcloudsim.request.Instance;

/**
 * A class to record the state of a host.
 * The state of a host is represented by four integers: cpu, ram, storage and bw.
 * The four integers are the amount of cpu, ram, storage and bw that are available on the host.
 *
 * @author Anonymous
 * @since LGDCloudSim 1.0
 */
@Getter
@Setter
public class HostState {
    /**
     * The number of state attributes
     * (For now, it is 5: cpu, ram, storage, bw and gpu)
     */
    public static int STATE_NUM = 5;
    /** The amount of cpu that is available on the host */
    int cpu;
    /** The amount of ram that is available on the host */
    int ram;
    /** The amount of storage that is available on the host */
    int storage;
    /** The amount of bw that is available on the host */
    int bw;
    /** The amount of gpu that is available on the host */
    int gpu;
    /** The type of gpu that is available on the host */
    String gpuType;

    /**
    * Create a HostState object with the given 4 resource.
    *
    * @param cpu the amount of cpu that is available on the host
    * @param ram the amount of ram that is available on the host
    * @param storage the amount of storage that is available on the host
    * @param bw the amount of bw that is available on the host
    * @param gpu the amount of gpu that is available on the host
    * @param gpuType the type of gpu that is available on the host
    * */
    public HostState(int cpu, int ram, int storage, int bw, int gpu, String gpuType ) {
        this.cpu = cpu;
        this.ram = ram;
        this.storage = storage;
        this.bw = bw;
        this.gpu = gpu;
        this.gpuType = gpuType;
    }

    /**
     * Create a HostState object with the given state.
     *
     * @param state the state of the host
     * */
    public HostState(int[] state, String gpuType) {
        this(state[0], state[1], state[2], state[3], state[4], gpuType);
    }

    /**
     * Get the state of the host.
     *
     * @return the state of the host. The state includes 4 integers: cpu, ram, storage and bw.
     * */
    public int[] getStateArray() {
        int[] state = new int[HostState.STATE_NUM];
        state[0] = cpu;
        state[1] = ram;
        state[2] = storage;
        state[3] = bw;
        state[4] = gpu;
        return state;
    }

    /**
     * Set the state of the host.
     *
     * @param hostState the state of the host. The state includes 4 integers: cpu, ram, storage and bw.
     * */
    public void setState(HostState hostState) {
        this.cpu = hostState.getCpu();
        this.ram = hostState.getRam();
        this.storage = hostState.getStorage();
        this.bw = hostState.getBw();
        this.gpu = hostState.getGpu();
        this.gpuType = hostState.getGpuType();
    }

    /**
     * Check whether the host is suitable for the instance.
     * The host is suitable for the instance if the host has enough cpu, ram, storage and bw.
     *
     * @param instance the instance to be checked
     * @return true if the host is suitable for the instance, false otherwise
     * */
    public boolean isSuitable(Instance instance) {
        if (!instance.getGpuType().isEmpty() && !instance.getGpuType().equals(gpuType)) {
            return false;
        }
        return cpu >= instance.getCpu() && ram >= instance.getRam() && storage >= instance.getStorage() && bw >= instance.getBw() && gpu >= instance.getGpu();
    }


    /**
     * Allocate the instance on the host.
     * The allocation will reduce the amount of cpu, ram, storage and bw on the host.
     *
     * @param instance the instance to be allocated
     * */
    public void allocate(Instance instance) {
        cpu = cpu - instance.getCpu();
        ram = ram - instance.getRam();
        storage = storage - instance.getStorage();
        bw = bw - instance.getBw();
        gpu = gpu - instance.getGpu();
    }

    public void release(Instance instance) {
        cpu = cpu + instance.getCpu();
        ram = ram + instance.getRam();
        storage = storage + instance.getStorage();
        bw = bw + instance.getBw();
        gpu = gpu + instance.getGpu();
    }

    public int getGPUScore() {
        return gpu*(gpu-1);
    }

    @Override
    public String toString() {
        return "HostState{" +
                "cpu=" + cpu +
                ", ram=" + ram +
                ", storage=" + storage +
                ", bw=" + bw +
                ", gpu=" + gpu +
                ", gpuType='" + gpuType + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof HostState) {
            HostState hostState = (HostState) obj;
            return cpu == hostState.getCpu() && ram == hostState.getRam() && storage == hostState.getStorage() && bw == hostState.getBw() && gpu == hostState.getGpu() && gpuType.equals(hostState.getGpuType());
        }
        return false;
    }
}
