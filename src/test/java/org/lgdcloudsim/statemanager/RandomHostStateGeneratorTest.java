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
        RandomHostStateGenerator randomHostStateGenerator = new RandomHostStateGenerator(seed, minCpu, maxCpu, minRam, maxRam, minStorage, maxStorage, minBandwidth, maxBandwidth);
        for (int i = 0; i < 20; i++) {
            int[] randomState = randomHostStateGenerator.generateHostState();
            assertTrue(randomState[0] >= minCpu && randomState[0] <= maxCpu);
            assertTrue(randomState[1] >= minRam && randomState[1] <= maxRam);
            assertTrue(randomState[2] >= minStorage && randomState[2] <= maxStorage);
            assertTrue(randomState[3] >= minBandwidth && randomState[3] <= maxBandwidth);
        }

    }
}
