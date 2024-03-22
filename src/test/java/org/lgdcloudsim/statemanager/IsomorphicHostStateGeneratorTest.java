package org.lgdcloudsim.statemanager;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

public class IsomorphicHostStateGeneratorTest {
    @Test
    void testIsomorphicHostStateGenerator() {
        IsomorphicHostStateGenerator isomorphicHostStateGenerator = new IsomorphicHostStateGenerator(2, 3, 4, 5);
        int[] expectedState = {2, 3, 4, 5};
        int[] isomorphicState0 = isomorphicHostStateGenerator.generateHostState();
        assertArrayEquals(expectedState, isomorphicState0);
        int[] isomorphicState1 = isomorphicHostStateGenerator.generateHostState();
        assertArrayEquals(expectedState, isomorphicState1);
    }
}
