package org.cpnsim.datacenter;

import org.cloudsimplus.core.CloudSim;
import org.cloudsimplus.core.Simulation;
import org.cpnsim.innerscheduler.InnerScheduleResult;
import org.cpnsim.innerscheduler.InnerScheduler;
import org.cpnsim.innerscheduler.InnerSchedulerSimple;
import org.cpnsim.request.Instance;
import org.cpnsim.request.InstanceSimple;
import org.cpnsim.request.UserRequest;
import org.cpnsim.request.UserRequestSimple;
import org.cpnsim.statemanager.PartitionRangesManager;
import org.cpnsim.statemanager.StatesManager;
import org.cpnsim.statemanager.StatesManagerSimple;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class ResourceAllocateSelectorSimpleTest {
    @Test
    void testResourceAllocateSelectorSimple() {
        int hostNum = 20;
        Map<Integer, int[]> ranges = new HashMap<>();
        ranges.put(0, new int[]{0, hostNum - 1});
        PartitionRangesManager partitionRangesManager = new PartitionRangesManager(ranges);
        Simulation simulation = new CloudSim();
        Datacenter datacenter = new DatacenterSimple(simulation);
        StatesManager statesManager = new StatesManagerSimple(hostNum, partitionRangesManager, 0);
        statesManager.initHostStates(10, 10, 10, 10, 0, 20);
        datacenter.setStatesManager(statesManager);
        Map<Integer, Double> partitionDelay = Map.of(1, 0.0);
        InnerScheduler innerScheduler = new InnerSchedulerSimple(partitionDelay);

        List<InnerScheduleResult> innerScheduleResults = new ArrayList<>();
        InnerScheduleResult innerScheduleResult = new InnerScheduleResult(innerScheduler);
        innerScheduleResults.add(innerScheduleResult);
        UserRequest userRequest = new UserRequestSimple(0);
        Instance instance = new InstanceSimple(0, 4, 4, 4, 4);
        instance.setUserRequest(userRequest);
        innerScheduleResult.setScheduleResult(Map.of(0, List.of(instance, instance, instance)));

        ResourceAllocateSelector resourceAllocateSelector = new ResourceAllocateSelectorSimple();
        resourceAllocateSelector.setDatacenter(datacenter);

        Map<Integer, List<Instance>> res = resourceAllocateSelector.selectResourceAllocate(innerScheduleResults);
        Map<Integer, List<Instance>> exceptedRes = Map.of(0, List.of(instance, instance), -1, List.of(instance));
        assertEquals(exceptedRes, res);
    }
}
