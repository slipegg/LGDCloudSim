package org.lgdcloudsim.statemanager;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

public class IsomorphicHostStateGeneratorTest {
    @Test
    void testIsomorphicHostStateGenerator() {
        IsomorphicHostStateGenerator isomorphicHostStateGenerator = new IsomorphicHostStateGenerator(2, 3, 4, 5, 8, "NVIDIA A100");
        HostState expectedState = new HostState(new int[]{2, 3, 4, 5, 8}, "NVIDIA A100");
        HostState isomorphicState0 = isomorphicHostStateGenerator.generateHostState();
        assertArrayEquals(expectedState.getStateArray(), isomorphicState0.getStateArray());
        HostState isomorphicState1 = isomorphicHostStateGenerator.generateHostState();
        assertArrayEquals(expectedState.getStateArray(), isomorphicState1.getStateArray());
    }
}
