package org.lgdcloudsim.record;

import lombok.Getter;

/**
 * MemoryRecord records the maximum used memory during the simulation.
 *
 * @author Anonymous
 * @since LGDCloudSim 1.0
 */
public class MemoryRecord {
    /**
     * The maximum used memory during the simulation.
     * The unit is byte.
     */
    @Getter
    private static long maxUsedMemory;

    /**
     * The runtime object.
     * It records the total memory and the free memory of the JVM.
     */
    private static Runtime runtime;

    /**
     * Record the memory usage, and update the maxUsedMemory if necessary.
     */
    public static void recordMemory(){
        runtime = Runtime.getRuntime();
        maxUsedMemory = Math.max(maxUsedMemory, runtime.totalMemory()-runtime.freeMemory());
    }
}
