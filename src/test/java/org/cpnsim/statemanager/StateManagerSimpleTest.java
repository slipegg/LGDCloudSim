package org.cpnsim.statemanager;

import org.cloudsimplus.core.CloudSim;
import org.cloudsimplus.core.Simulation;
import org.cpnsim.datacenter.Datacenter;
import org.cpnsim.datacenter.DatacenterSimple;
import org.cpnsim.request.Instance;
import org.cpnsim.request.InstanceSimple;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class StateManagerSimpleTest {
    @Test
    void testStateManagerSimple() {
        int hostNum = 20;
        Map<Integer, int[]> ranges = new HashMap<>();
        ranges.put(0, new int[]{0, hostNum - 1});
        PartitionRangesManager partitionRangesManager = new PartitionRangesManager(ranges);
        Simulation simulation = new CloudSim();
        Datacenter datacenter = new DatacenterSimple(simulation);
        StatesManager statesManager = new StatesManagerSimple(hostNum, partitionRangesManager, 0);
        statesManager.setDatacenter(datacenter);
        statesManager.initHostStates(10, 10, 10, 10, 0, 20);
        HostState nowHostState0 = statesManager.getNowHostState(0);
        HostState exceptedHostState0 = new HostState(10, 10, 10, 10);
        assertEquals(exceptedHostState0, nowHostState0);

        Instance instance0 = new InstanceSimple(0, 2, 2, 2, 2);
        statesManager.allocate(0, instance0);
        HostState nowHostState1 = statesManager.getNowHostState(0);
        HostState exceptedHostState1 = new HostState(8, 8, 8, 8);
        assertEquals(exceptedHostState1, nowHostState1);

        statesManager.release(0, instance0);
        HostState nowHostState2 = statesManager.getNowHostState(0);
        HostState exceptedHostState2 = new HostState(10, 10, 10, 10);
        assertEquals(exceptedHostState2, nowHostState2);
    }
}
