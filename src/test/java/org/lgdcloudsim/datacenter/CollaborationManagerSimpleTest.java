package org.lgdcloudsim.datacenter;

import org.lgdcloudsim.core.CloudSim;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CollaborationManagerSimpleTest {
    CloudSim cloudSim = new CloudSim();
    @Test
    public void testAddDatacenter() {
        Datacenter dc1 = new DatacenterSimple(cloudSim);
        CollaborationManager collaborationManager = new CollaborationManagerSimple(cloudSim);
        collaborationManager.addDatacenter(dc1, 0);
        Map<Integer, Set<Datacenter>> excepted = Map.of(0, Set.of(dc1));
        assertEquals(excepted, collaborationManager.getCollaborationMap());
        Set<Integer> exceptedSet = Set.of(0);
        assertEquals(exceptedSet, dc1.getCollaborationIds());
    }

    @Test
    public void testAddDatacenter2() {
        CollaborationManager collaborationManager = new CollaborationManagerSimple(cloudSim);
        Datacenter dc0 = new DatacenterSimple(cloudSim);
        collaborationManager.addDatacenter(dc0, 0);
        Datacenter dc1 = new DatacenterSimple(cloudSim);
        Datacenter dc2 = new DatacenterSimple(cloudSim);
        Map<Integer, Set<Datacenter>> collaborationMap = Map.of(0, Set.of(dc1), 1, Set.of(dc0, dc2));
        collaborationManager.addDatacenter(collaborationMap);
        Map<Integer, Set<Datacenter>> excepted = Map.of(0, Set.of(dc0, dc1), 1, Set.of(dc0, dc2));
        assertEquals(excepted, collaborationManager.getCollaborationMap());
        Set<Integer> exceptedSet = Set.of(0, 1);
        assertEquals(exceptedSet, dc0.getCollaborationIds());
        Set<Integer> exceptedSet1 = Set.of(0);
        assertEquals(exceptedSet1, dc1.getCollaborationIds());
        Set<Integer> exceptedSet2 = Set.of(1);
        assertEquals(exceptedSet2, dc2.getCollaborationIds());
    }

    @Test
    public void testGetOtherDatacenters() {
        CollaborationManager collaborationManager = new CollaborationManagerSimple(cloudSim);
        Datacenter dc0 = new DatacenterSimple(cloudSim);
        collaborationManager.addDatacenter(dc0, 0);
        Datacenter dc1 = new DatacenterSimple(cloudSim);
        Datacenter dc2 = new DatacenterSimple(cloudSim);
        Map<Integer, Set<Datacenter>> collaborationMap = Map.of(0, Set.of(dc1), 1, Set.of(dc0, dc2));
        collaborationManager.addDatacenter(collaborationMap);
        assertEquals(Set.of(dc1, dc2), unordered(collaborationManager.getOtherDatacenters(dc0)));
    }

    private <T> Map<Integer, Set<T>> ignoreEmptyValue(Map<Integer, Set<T>> m) {
        Map<Integer, Set<T>> result = new HashMap<>(m);
        result.entrySet().removeIf((e) -> e.getValue().isEmpty());
        return result;
    }

    @Test
    public void testRemoveDatacenter1() {
        // test for CollaborationManager removeDatacenter(Datacenter datacenter, int collaborationId)
        CollaborationManager collaborationManager = new CollaborationManagerSimple(cloudSim);
        Datacenter dc0 = new DatacenterSimple(cloudSim);
        Datacenter dc1 = new DatacenterSimple(cloudSim);

        // remove (dc0, 0) from empty collaborationManager
        // excepted: collaborationMap = Map.of();
        collaborationManager.removeDatacenter(dc0, 0);
        assertEquals(Map.of(), collaborationManager.getCollaborationMap());

        // add (dc0, 0) and (dc1, 0) to collaborationManager
        collaborationManager.addDatacenter(dc0, 0);
        collaborationManager.addDatacenter(dc1, 0);

        // remove (null, 0) from collaborationManager
        // excepted: collaborationMap = Map.of(0, Set.of(dc0, dc1));
        collaborationManager.removeDatacenter(null, 0);
        assertEquals(Map.of(0, Set.of(dc0, dc1)), ignoreEmptyValue(collaborationManager.getCollaborationMap()));

        // remove (dc1, 0) from collaborationManager
        // excepted: collaborationMap = Map.of(0, Set.of(dc0));
        collaborationManager.removeDatacenter(dc1, 0);
        assertEquals(Map.of(0, Set.of(dc0)), collaborationManager.getCollaborationMap());

        // remove (dc0, 0) from collaborationManager
        // excepted: collaborationMap = Map.of();
        collaborationManager.removeDatacenter(dc0, 0);
        assertEquals(Map.of(), ignoreEmptyValue(collaborationManager.getCollaborationMap()));
    }

    @Test
    public void testRemoveDatacenter2() {
        // test for CollaborationManager removeDatacenter(Datacenter datacenter)
        CollaborationManager collaborationManager = new CollaborationManagerSimple(cloudSim);
        Datacenter dc0 = new DatacenterSimple(cloudSim);

        // remove dc0 from empty collaborationManager
        // excepted: collaborationMap = Map.of();
        collaborationManager.removeDatacenter(dc0);
        assertEquals(Map.of(), collaborationManager.getCollaborationMap());
        
        // add (dc0, 0) and (dc0, 1) to collaborationManager
        // expected: collaborationMap = Map.of(0, Set.of(dc0), 1, Set.of(dc0));
        collaborationManager.addDatacenter(dc0, 0);
        collaborationManager.addDatacenter(dc0, 1);
        assertEquals(Map.of(0, Set.of(dc0), 1, Set.of(dc0)), collaborationManager.getCollaborationMap());

        // remove dc0 from collaborationManager
        // excepted: collaborationMap = Map.of();
        collaborationManager.removeDatacenter(dc0);
        assertEquals(Map.of(), ignoreEmptyValue(collaborationManager.getCollaborationMap()));
    }

    private <T> Set<T> unordered(List<T> list) {
        return new HashSet<>(list);
    }

    @Test
    public void testGetDatacenters1() {
        // test for List<Datacenter> getDatacenters(int collaborationId)
        CollaborationManager collaborationManager = new CollaborationManagerSimple(cloudSim);
        // expect: getDatacenters(null) = List.of();
        assertEquals(List.of(), collaborationManager.getDatacenters(null));
        // expect: getDatacenters(0) = List.of();
        assertEquals(List.of(), collaborationManager.getDatacenters(0));

        // add (dc0, 0), (dc1, 0), (dc0, 1), (dc2, 1) to collaborationManager
        // expect: getDatacenters(0) = List.of(dc0, dc1);
        Datacenter dc0 = new DatacenterSimple(cloudSim);
        Datacenter dc1 = new DatacenterSimple(cloudSim);
        Datacenter dc2 = new DatacenterSimple(cloudSim);
        Map<Integer, Set<Datacenter>> collaborationMap = Map.of(0, Set.of(dc0, dc1), 1, Set.of(dc0, dc2));
        collaborationManager.addDatacenter(collaborationMap);
        assertEquals(Set.of(dc0, dc1), unordered(collaborationManager.getDatacenters(0)));
    }

    @Test
    public void testGetDatacenters2() {
        // test for List<Datacenter> getDatacenters(Datacenter datacenter)
        CollaborationManager collaborationManager = new CollaborationManagerSimple(cloudSim);
        // expect: getDatacenters(null) = List.of();
        assertEquals(List.of(), collaborationManager.getDatacenters(null));
        Datacenter dc0 = new DatacenterSimple(cloudSim);
        // expect: getDatacenters(dc0) = List.of();
        assertEquals(List.of(), collaborationManager.getDatacenters(dc0));

        // add (dc0, 0), (dc1, 0), (dc0, 1), (dc2, 1) to collaborationManager
        // expect: getDatacenters(dc0) = List.of(dc0, dc1, dc2);
        Datacenter dc1 = new DatacenterSimple(cloudSim);
        Datacenter dc2 = new DatacenterSimple(cloudSim);
        Map<Integer, Set<Datacenter>> collaborationMap = Map.of(0, Set.of(dc0, dc1), 1, Set.of(dc0, dc2));
        collaborationManager.addDatacenter(collaborationMap);
        assertEquals(Set.of(dc0, dc1, dc2), unordered(collaborationManager.getDatacenters(dc0)));
    }
}
