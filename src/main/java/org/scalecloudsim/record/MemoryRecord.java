package org.scalecloudsim.record;

import lombok.Getter;

public class MemoryRecord {
    @Getter
    private static long maxUsedMemory;
    private static Runtime runtime;
    public static void recordMemory(){
        runtime = Runtime.getRuntime();
        maxUsedMemory = Math.max(maxUsedMemory, runtime.totalMemory()-runtime.freeMemory());
    }
}
