package org.scalecloudsim.datacenters;

import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

//还有一些测试没有写完，但是这个类的功能已经实现了
public class CollaborationManagerSimpleTest {
    @Test
    public void testAddDatacenter() {
        Datacenter dc1 = new DatacenterSimple();
        CollaborationManager collaborationManager = new CollaborationManagerSimple();
        collaborationManager.addDatacenter(dc1, 0);
        Map<Integer, Set<Datacenter>> excepted = Map.of(0, Set.of(dc1));
        assertEquals(excepted, collaborationManager.getCollaborationMap());
        Set<Integer> exceptedSet = Set.of(0);
        assertEquals(exceptedSet, dc1.getCollaborationIds());
    }

    @Test
    public void testAddDatacenter2() {
        CollaborationManager collaborationManager = new CollaborationManagerSimple();
        Datacenter dc0 = new DatacenterSimple();
        collaborationManager.addDatacenter(dc0, 0);
        Datacenter dc1 = new DatacenterSimple();
        Datacenter dc2 = new DatacenterSimple();
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
        CollaborationManager collaborationManager = new CollaborationManagerSimple();
        Datacenter dc0 = new DatacenterSimple();
        collaborationManager.addDatacenter(dc0, 0);
        Datacenter dc1 = new DatacenterSimple();
        Datacenter dc2 = new DatacenterSimple();
        Map<Integer, Set<Datacenter>> collaborationMap = Map.of(0, Set.of(dc1), 1, Set.of(dc0, dc2));
        collaborationManager.addDatacenter(collaborationMap);
        Set<Datacenter> excepted = Set.of(dc1);
        assertEquals(excepted, collaborationManager.getOtherDatacenters(dc0, 0));
        Set<Datacenter> excepted1 = Set.of(dc1, dc2);
        assertEquals(excepted1, collaborationManager.getOtherDatacenters(dc0));
    }
}
