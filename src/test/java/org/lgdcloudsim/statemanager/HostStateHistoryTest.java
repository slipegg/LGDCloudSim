package org.lgdcloudsim.statemanager;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class HostStateHistoryTest {
    @Test
    void testHostStateHistory() {
        HostStateHistory hostStateHistory = new HostStateHistory(1, 2, 3, 4, 7.2);
        int[] state = hostStateHistory.getStateArray();
        int[] expectedState = {1, 2, 3, 4};
        for (int i = 0; i < 4; i++) {
            assertEquals(expectedState[i], state[i]);
        }

        int[] stateArray = hostStateHistory.getStateArray();
        assertArrayEquals(expectedState, stateArray);

        double time = hostStateHistory.getTime();
        assertEquals(7.2, time);

    }
}
