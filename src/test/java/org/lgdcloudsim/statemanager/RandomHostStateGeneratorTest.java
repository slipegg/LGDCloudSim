package org.lgdcloudsim.statemanager;

import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertTrue;

public class RandomHostStateGeneratorTest {
    @Test
    void testRandomHostStateGenerator() {
        int seed = 1;
        int minCpu = 10;
        int maxCpu = 20;
        int minRam = 30;
        int maxRam = 40;
        int minStorage = 50;
        int maxStorage = 60;
        int minBandwidth = 70;
        int maxBandwidth = 80;
        int minGpu = 0;
        int maxGpu = 16;
        String gpuType = "NVIDIA A100";
        RandomHostStateGenerator randomHostStateGenerator = new RandomHostStateGenerator(seed, minCpu, maxCpu, minRam, maxRam, minStorage, maxStorage, minBandwidth, maxBandwidth, minGpu, maxGpu, gpuType);
        for (int i = 0; i < 20; i++) {
            HostState randomState = randomHostStateGenerator.generateHostState();
            assertTrue(randomState.getStateArray()[0] >= minCpu && randomState.getStateArray()[0] <= maxCpu);
            assertTrue(randomState.getStateArray()[1] >= minRam && randomState.getStateArray()[1] <= maxRam);
            assertTrue(randomState.getStateArray()[2] >= minStorage && randomState.getStateArray()[2] <= maxStorage);
            assertTrue(randomState.getStateArray()[3] >= minBandwidth && randomState.getStateArray()[3] <= maxBandwidth);
            assertTrue(randomState.getStateArray()[4] >= minGpu && randomState.getStateArray()[4] <= maxGpu);
            assertTrue(randomState.getGpuType().equals(gpuType));
        }

    }
}
