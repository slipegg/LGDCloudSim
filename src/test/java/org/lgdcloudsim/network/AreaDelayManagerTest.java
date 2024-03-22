package org.lgdcloudsim.network;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AreaDelayManagerTest {
    @Test
    public void testAreaDelayManager() {
        final String AREA_DELAY_FILE = "src/test/resources/areaDelay.csv";
        final String REGION_DELAY_FILE = "src/test/resources/regionDelay.csv";
        RegionDelayManager regionDelayManager = new RegionDelayManager(REGION_DELAY_FILE);
        AreaDelayManager areaDelayManager = new AreaDelayManager(AREA_DELAY_FILE, regionDelayManager);
        assertEquals(176.93, areaDelayManager.getDelay("Angola", "africa-south1"));
        assertEquals(80.45, areaDelayManager.getDelay("China", "asia-east1"));
        System.out.println(areaDelayManager.getDelay("United States", "us-east1"));
        System.out.println(areaDelayManager.getDelay("United States", "northamerica-northeast1"));
        System.out.println(areaDelayManager.getDelay("United States", "us-west1"));
    }
}
