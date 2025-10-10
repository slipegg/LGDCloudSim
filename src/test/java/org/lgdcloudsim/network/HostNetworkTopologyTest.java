package org.lgdcloudsim.network;

import org.junit.jupiter.api.Test;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class HostNetworkTopologyTest {

    @Test
    void testParsingAndLookup() throws IOException {
        ClosTopologyManager topo = new ClosTopologyManager("src/test/resources/hostTopoConfig.csv");

        // lookup several host ids
        ClosTopologyItem i0 = topo.getClosTopologyItemByHostId(0);
        assertNotNull(i0);
        assertEquals(1, i0.getDcId());
        assertEquals("G6", i0.getASW());
        assertEquals("P10", i0.getPSW());
        assertEquals("S14", i0.getDSW());

        ClosTopologyItem i150 = topo.getClosTopologyItemByHostId(150);
        assertNotNull(i150);
        assertEquals(1, i150.getDcId());
        assertEquals("G6", i150.getASW());
        assertEquals("P10", i150.getPSW());
        assertEquals("S15", i150.getDSW());

        ClosTopologyItem i250 = topo.getClosTopologyItemByHostId(250);
        assertNotNull(i250);
        assertEquals(1, i250.getDcId());
        assertEquals("G6", i250.getASW());
        assertEquals("P21", i250.getPSW());
        assertEquals("S21", i250.getDSW());

        ClosTopologyItem i450 = topo.getClosTopologyItemByHostId(450);
        assertNotNull(i450);
        assertEquals(2, i450.getDcId());
        assertEquals("G7", i450.getASW());
        assertEquals("P31", i450.getPSW());
        assertEquals("S32", i450.getDSW());

        // out of range
        assertNull(topo.getClosTopologyItemByHostId(9999));

        int[] g6 = topo.getHostIdRangeByClos(new ClosTopologyItem(1, "G6", "P10", "S14"));
        assertNotNull(g6);
        assertEquals(0, g6[0]);
        assertEquals(99, g6[1]);

        int[] g7 = topo.getHostIdRangeByClos(new ClosTopologyItem(2, "G7", "P31", "S32"));
        assertNotNull(g7);
        assertEquals(400, g7[0]);
        assertEquals(499, g7[1]);

        assertNull(topo.getHostIdRangeByClos(new ClosTopologyItem(0, "NOPE", "NOPE", "NOPE")));
    }
}
