package org.lgdcloudsim.datacenter;

import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertEquals;

public class ConflictHandlerSimpleTest {
    @Test
    void testResourceAllocateSelectorSimple() {
//        int hostNum = 20;
//        Map<Integer, int[]> ranges = new HashMap<>();
//        ranges.put(0, new int[]{0, hostNum - 1});
//        PartitionRangesManager partitionRangesManager = new PartitionRangesManager(ranges);
//        Simulation simulation = new CloudSim();
//        Datacenter datacenter = new DatacenterSimple(simulation);
//        StatesManager statesManager = new StatesManagerSimple(hostNum, partitionRangesManager, 0);
//        statesManager.initHostStates(10, 10, 10, 10, 0, 20);
//        datacenter.setStatesManager(statesManager);
//        Map<Integer, Double> partitionDelay = Map.of(1, 0.0);
//        InnerScheduler innerScheduler = new InnerSchedulerSimple(partitionDelay);
//
//        List<InnerScheduleResult> innerScheduleResults = new ArrayList<>();
//        InnerScheduleResult innerScheduleResult = new InnerScheduleResult(innerScheduler);
//        innerScheduleResults.add(innerScheduleResult);
//        UserRequest userRequest = new UserRequestSimple(0);
//        Instance instance = new InstanceSimple(0, 4, 4, 4, 4);
//        instance.setUserRequest(userRequest);
//        innerScheduleResult.setScheduleResult(Map.of(0, List.of(instance, instance, instance)));
//
//        ResourceAllocateSelector resourceAllocateSelector = new ResourceAllocateSelectorSimple();
//        resourceAllocateSelector.setDatacenter(datacenter);
//
//        ResourceAllocateResult res = resourceAllocateSelector.selectResourceAllocate(innerScheduleResults);
//        Map<Integer, List<Instance>> exceptedSuccessRes = Map.of(0, List.of(instance, instance));
//        Map<InnerScheduler, List<Instance>> exceptedFailRes = Map.of(innerScheduler, List.of(instance));
//        Assertions.assertEquals(exceptedSuccessRes, res.getSuccessRes());
//        Assertions.assertEquals(exceptedFailRes, res.getFailRes());
    }
}
