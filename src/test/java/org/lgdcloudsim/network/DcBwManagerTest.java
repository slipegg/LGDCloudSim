package org.lgdcloudsim.network;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DcBwManagerTest {
    @Test
    public void testDcBwManager() {
        final String DC_BW_FILE = "src/test/resources/DatacenterBwConfig.csv";
        DcBwManager dcBwManager = new DcBwManager(DC_BW_FILE);
        assertEquals(665940.63, dcBwManager.getBw(3, 4));
        assertEquals(665940.63, dcBwManager.getBw(4, 3));
        assertEquals(999999999.00, dcBwManager.getBw(4, 4));

        assertEquals(0.0, dcBwManager.getUnitPrice(1, 1));
        assertEquals(2, dcBwManager.getUnitPrice(1, 4));
        assertEquals(2, dcBwManager.getUnitPrice(4, 1));

        dcBwManager.allocateBw(4, 3, 100);
        assertEquals(665940.63, dcBwManager.getBw(3, 4));
        assertEquals(665840.63, dcBwManager.getBw(4, 3));

        dcBwManager.releaseBw(4, 3, 100);
        assertEquals(665940.63, dcBwManager.getBw(3, 4));
        assertEquals(665940.63, dcBwManager.getBw(4, 3));

        assertEquals(10 * 100, dcBwManager.getBwTCO());
    }
}
