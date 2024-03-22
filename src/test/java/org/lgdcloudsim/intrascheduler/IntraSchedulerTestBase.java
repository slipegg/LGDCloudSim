package org.lgdcloudsim.intrascheduler;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.lgdcloudsim.core.Simulation;
import org.lgdcloudsim.core.SimulationNull;
import org.lgdcloudsim.datacenter.Datacenter;
import org.lgdcloudsim.datacenter.DatacenterNull;
import org.lgdcloudsim.request.Instance;
import org.lgdcloudsim.statemanager.*;
import org.junit.Before;
import org.junit.Test;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

/**
 * Base test for all implementations of {@link IntraScheduler}
 */
public abstract class IntraSchedulerTestBase<T extends IntraScheduler> {
    private T scheduler;

    private Simulation sim;
    private Datacenter dc;
    private StatesManager sm;
    private PartitionRangesManager prm;

    protected abstract T createInnerScheduler(int id, int firstPartitionId, int partitionNum);

    @Before
    public void setup() {
        scheduler = createInnerScheduler(0, 0, 2);
        // create a FakeDatacenter with a FakeSimulation
        sim = new FakeSimulation();
        dc = new FakeDatacenter(sim);
        dc.setIntraSchedulers(List.of(scheduler));
        // create a StatesManager of 4 hosts and link it with the FakeDatacenter.
        // synGap is set to Double.POSITIVE_INFINITY, so sync is disabled.
        // test for synchronization can be added later.
        prm = new PartitionRangesManager(Map.of(
                0, new int[] { 0, 1 },
                1, new int[] { 2, 3 }));
        sm = new StatesManagerSimple(4, prm, Double.POSITIVE_INFINITY);
        dc.setStatesManager(sm);
    }

    @Test
    public void testNotSuitable() {
//        // create a FakeSynState which declines every request
//        FakeSynState fakeSynState = new FakeSynState(prm);
//        fakeSynState.passSet = new HashSet<>();
//        // let scheduler schedule an Instance
//        List<Instance> instanceList = List.of(new InstanceSimple(12345, 1, 1, 1, 1));
//        Map<Integer, List<Instance>> result = scheduler.scheduleInstances(instanceList, fakeSynState);
//        // expect: result[-1] contains that instance
//        Set<Integer> failedInstanceIds = result.get(-1)
//                .stream()
//                .map(ChangeableId::getId)
//                .collect(Collectors.toSet());
//        assertEquals(Set.of(12345), failedInstanceIds);
//        return;
    }
}

/**
 * A fake {@link SynState}, with controllable output of
 * {@link SynState#isSuitable}
 */
class FakeSynState implements SynState {

    public List<Pair<Integer, Instance>> tmpResourceAllocationHistory;

    public List<Pair<Integer, Instance>> checkSuitableHistory;

    // private PartitionRangesManager partitionRangesManager;

    public Set<Integer> passSet;

    FakeSynState(PartitionRangesManager partitionRangesManager) {
        // this.partitionRangesManager = partitionRangesManager;
        this.checkSuitableHistory = new ArrayList<>();
        this.tmpResourceAllocationHistory = new ArrayList<>();
        this.passSet = null; // force manual initialization
    }

    @Override
    public HostState getHostState(int hostId) {
        return null;
    }

    /**
     * Whether a host is suitable for an instance depends on the controller's input.
     */
    public boolean isSuitable(int hostId, Instance instance) {
        checkSuitableHistory.add(Pair.of(hostId, instance));
        return passSet.contains(hostId);
    }

    /**
     * Do nothing but record its input.
     */
    public void allocateTmpResource(int hostId, Instance instance) {
        tmpResourceAllocationHistory.add(Pair.of(hostId, instance));
    }
}

/**
 * A fake {@link Datacenter}
 */
class FakeDatacenter extends DatacenterNull {
    @Getter
    Simulation simulation;

    @Getter
    List<IntraScheduler> intraSchedulers;

    @Getter
    StatesManager statesManager;

    FakeDatacenter(@NonNull Simulation simulation) {
        this.simulation = simulation;
    }

    public Datacenter setIntraSchedulers(List<IntraScheduler> intraSchedulers) {
        this.intraSchedulers = intraSchedulers;
        for (IntraScheduler intraScheduler : intraSchedulers) {
            intraScheduler.setDatacenter(this);
        }
        return this;
    }

    @Override
    public Datacenter setStatesManager(StatesManager statesManager) {
        this.statesManager = statesManager;
        statesManager.setDatacenter(this);
        return this;
    }
}

/**
 * A fake {@link Simulation}, with mutable {@link Simulation#clock()}
 */
class FakeSimulation extends SimulationNull {
    @Getter
    @Setter
    double clock;

    @Override
    public double clock() {
        return this.clock;
    }
}