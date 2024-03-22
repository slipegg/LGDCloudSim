package org.lgdcloudsim.network;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RandomDelayDynamicModelTest {
    @Test
    public void testRandomDelayDynamicModel() {
        RandomDelayDynamicModel randomDelayDynamicModel = new RandomDelayDynamicModel();
        assertEquals(randomDelayDynamicModel.getDynamicDelay(1, 4, 100, 33), randomDelayDynamicModel.getDynamicDelay(1, 4, 100, 33));
    }
}
