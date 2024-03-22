package org.lgdcloudsim.network;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RegionDelayManagerTest {
    @Test
    public void testRegionDelayManager() {
        final String REGION_DELAY_FILE = "src/test/resources/regionDelay.csv";
        RegionDelayManager regionDelayManager = new RegionDelayManager(REGION_DELAY_FILE);
        assertEquals(135.6483086680762, regionDelayManager.getAverageDelay());
        assertEquals(225.0, regionDelayManager.getDelay("africa-south1", "asia-east1"));
        assertEquals(20.4, regionDelayManager.getDelay("us-west4", "us-west4"));
    }
}
