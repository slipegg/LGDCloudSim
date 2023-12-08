package org.cpnsim.network;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AreaDelayManagerTest {
    @Test
    public void testAreaDelayManager() {
        final String AREA_DELAY_FILE = "src/test/resources/areaDelay.csv";
        final String REGION_DELAY_FILE = "src/test/resources/regionDelay.csv";
        RegionDelayManager regionDelayManager = new RegionDelayManager(REGION_DELAY_FILE);
        AreaDelayManager areaDelayManager = new AreaDelayManager(AREA_DELAY_FILE, regionDelayManager);
        assertEquals(70.28, areaDelayManager.getDelay("shanghai", "asia-east1"));
        assertEquals(80.64, areaDelayManager.getDelay("beijing", "asia-northeast1"));
        assertEquals(70.28 + regionDelayManager.getDelay("asia-east1", "africa-south1"), areaDelayManager.getDelay("shanghai", "africa-south1"));
    }
}
