package org.scalecloudsim.statemanager;

import org.cloudsimplus.core.CloudSim;
import org.cloudsimplus.core.Simulation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class IsomorphicHostStateGeneratorTest {
    @Test
    public void testIsomorphicHostStateGenerator() {
        Simulation sacleCloudSim = new CloudSim();
        int hostNum = 5_000;
        StateManager stateManager = new StateManagerSimple(hostNum, sacleCloudSim);
        HostStateGenerator isomorphicHostStateGenerator = new IsomorphicHostStateGenerator(124, 512, 10240, 1024);
        stateManager.initHostStates(isomorphicHostStateGenerator);
        int[] hostStates = stateManager.getHostStates();
        assertEquals(124, hostStates[0]);
        assertEquals(512, hostStates[1]);
        assertEquals(10240, hostStates[2]);
        assertEquals(1024, hostStates[3]);
        assertEquals(124, hostStates[hostNum * 4 - 4]);
        assertEquals(512, hostStates[hostNum * 4 - 3]);
        assertEquals(10240, hostStates[hostNum * 4 - 2]);
        assertEquals(1024, hostStates[hostNum * 4 - 1]);
    }
}
