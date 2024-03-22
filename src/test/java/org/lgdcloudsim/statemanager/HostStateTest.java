package org.lgdcloudsim.statemanager;

import org.lgdcloudsim.request.Instance;
import org.lgdcloudsim.request.InstanceSimple;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class HostStateTest {
    @Test
    void testHostState() {
        HostState hostState = new HostState(1, 2, 3, 4);
        int[] state = hostState.getStateArray();
        int[] expectedState = {1, 2, 3, 4};
        for (int i = 0; i < 4; i++) {
            assertEquals(expectedState[i], state[i]);
        }

        int[] stateArray = hostState.getStateArray();
        assertArrayEquals(expectedState, stateArray);

        Instance instance0 = new InstanceSimple(0, 1, 2, 3, 4);
        boolean isSuitable0 = hostState.isSuitable(instance0);
        assertEquals(true, isSuitable0);
        Instance instance1 = new InstanceSimple(0, 2, 2, 3, 4);
        boolean isSuitable1 = hostState.isSuitable(instance1);
        assertEquals(false, isSuitable1);

        hostState.allocate(instance0);
        int[] expectedState1 = {0, 0, 0, 0};
        int[] state1 = hostState.getStateArray();
        assertArrayEquals(expectedState1, state1);
    }
}
