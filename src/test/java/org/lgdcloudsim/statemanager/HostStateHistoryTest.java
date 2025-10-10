package org.lgdcloudsim.statemanager;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class HostStateHistoryTest {
    @Test
    void testHostStateHistory() {
        HostStateHistory hostStateHistory = new HostStateHistory(1, 2, 3, 4, 5, "NVIDIA A100", 7.2);
        HostState state = hostStateHistory.getHostState();
        int[] expectedState = {1, 2, 3, 4, 5};
        assertArrayEquals(expectedState, new int[]{state.getCpu(), state.getRam(), state.getStorage(), state.getBw(), state.getGpu()});

        double time = hostStateHistory.getTime();
        assertEquals(7.2, time);

    }
}
