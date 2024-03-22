//package org.lgdcloudsim.request;
//
//import org.junit.jupiter.api.Test;
//import static org.junit.jupiter.api.Assertions.assertEquals;
//
//public class InstanceGroupGraphSimpleTest {
//}

package org.lgdcloudsim.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class InstanceGroupGraphSimpleTest {

    private InstanceGroupGraph instanceGroupGraph;

    @BeforeEach
    void setUp() {
        instanceGroupGraph = new InstanceGroupGraphSimple(true);
    }

    @Test
    void testConstructor() {
        assertNotNull(instanceGroupGraph);
        assertTrue(instanceGroupGraph.getDirected());
        assertTrue(instanceGroupGraph.isEmpty());
    }

    @Test
    void testAddEdge() {
        InstanceGroup src = new InstanceGroupSimple(1);
        InstanceGroup dst = new InstanceGroupSimple(2);
        instanceGroupGraph.addEdge(src, dst, 10.0, 100.0);
        assertFalse(instanceGroupGraph.isEmpty());
        assertEquals(1, instanceGroupGraph.getGraph().size());
    }

    @Test
    void testRemoveEdge() {
        InstanceGroup src = new InstanceGroupSimple(1);
        InstanceGroup dst = new InstanceGroupSimple(2);
        instanceGroupGraph.addEdge(src, dst, 10.0, 100.0);
        assertFalse(instanceGroupGraph.isEmpty());
        instanceGroupGraph.removeEdge(src, dst);
        assertTrue(instanceGroupGraph.isEmpty());
    }

    @Test
    void testGetEdge() {
        InstanceGroup src = new InstanceGroupSimple(1);
        InstanceGroup dst = new InstanceGroupSimple(2);
        instanceGroupGraph.addEdge(src, dst, 10.0, 100.0);
        InstanceGroupEdge edge = instanceGroupGraph.getEdge(src, dst);
        assertNotNull(edge);
        assertEquals(src, edge.getSrc());
        assertEquals(dst, edge.getDst());
        assertEquals(10.0, edge.getMaxDelay());
        assertEquals(100.0, edge.getRequiredBw());
    }

    @Test
    void testGetDstList() {
        InstanceGroup src = new InstanceGroupSimple(1);
        InstanceGroup dst1 = new InstanceGroupSimple(2);
        InstanceGroup dst2 = new InstanceGroupSimple(3);
        instanceGroupGraph.addEdge(src, dst1, 10.0, 100.0);
        instanceGroupGraph.addEdge(src, dst2, 20.0, 200.0);
        List<InstanceGroup> dstList = instanceGroupGraph.getDstList(src);
        assertNotNull(dstList);
        assertEquals(2, dstList.size());
        assertTrue(dstList.contains(dst1));
        assertTrue(dstList.contains(dst2));
    }

    @Test
    void testGetSrcList() {
        InstanceGroup src1 = new InstanceGroupSimple(1);
        InstanceGroup src2 = new InstanceGroupSimple(2);
        InstanceGroup dst = new InstanceGroupSimple(3);
        instanceGroupGraph.addEdge(src1, dst, 10.0, 100.0);
        instanceGroupGraph.addEdge(src2, dst, 20.0, 200.0);
        List<InstanceGroup> srcList = instanceGroupGraph.getSrcList(dst);
        assertNotNull(srcList);
        assertEquals(2, srcList.size());
        assertTrue(srcList.contains(src1));
        assertTrue(srcList.contains(src2));
    }

    @Test
    void testGetDelay() {
        InstanceGroup src = new InstanceGroupSimple(1);
        InstanceGroup dst = new InstanceGroupSimple(2);
        instanceGroupGraph.addEdge(src, dst, 10.0, 100.0);
        assertEquals(10.0, instanceGroupGraph.getDelay(src, dst));
    }

    @Test
    void testGetBw() {
        InstanceGroup src = new InstanceGroupSimple(1);
        InstanceGroup dst = new InstanceGroupSimple(2);
        instanceGroupGraph.addEdge(src, dst, 10.0, 100.0);
        assertEquals(100.0, instanceGroupGraph.getBw(src, dst));
    }

    @Test
    void testIsEdgeLinked() {
        InstanceGroup src = new InstanceGroupSimple(1);
        assertFalse(instanceGroupGraph.isEdgeLinked(src));
        InstanceGroup dst = new InstanceGroupSimple(2);
        instanceGroupGraph.addEdge(src, dst, 10.0, 100.0);
        assertTrue(instanceGroupGraph.isEdgeLinked(src));
        assertTrue(instanceGroupGraph.isEdgeLinked(dst));
    }

    @Test
    public void testDirectedEdges() {
        // create a directed graph
        InstanceGroupGraphSimple graph = new InstanceGroupGraphSimple(true);
        // create 3 InstanceGroup
        InstanceGroup ig1 = new InstanceGroupSimple(1);
        InstanceGroup ig2 = new InstanceGroupSimple(2);
        // add edge (1, 2) with delay == 10 and bw == 5
        graph.addEdge(ig1, ig2, 10, 5);

        // expect: edge (1, 2) with delay == 10 and bw == 5
        assertEquals(10, graph.getDelay(ig1, ig2));
        assertEquals(5, graph.getBw(ig1, ig2));

        // expect: unadded reverse edge (2, 1) with delay == Double.MAX and bw == 0
        assertEquals(Double.MAX_VALUE, graph.getDelay(ig2, ig1));
        assertEquals(0, graph.getBw(ig2, ig1));

        // add edge (2, 1) with delay == 100 and bw == 50
        graph.addEdge(ig2, ig1, 100, 50);

        // expect: edge (2, 1) with delay == 100 and bw == 50
        assertEquals(100, graph.getDelay(ig2, ig1));
        assertEquals(50, graph.getBw(ig2, ig1));

        // remove edge (1, 2)
        graph.removeEdge(ig1, ig2);
        // expect: edge (1, 2) with delay == Double.MAX and bw == 0
        assertEquals(Double.MAX_VALUE, graph.getDelay(ig1, ig2));
        // expect: reverse edge (2, 1) is not removed and has delay == 100 and bw == 50
        assertEquals(100, graph.getDelay(ig2, ig1));
        assertEquals(50, graph.getBw(ig2, ig1));

        // remove edge (2, 1)
        graph.removeEdge(ig2, ig1);
        // expect: edge (2, 1) with delay == Double.MAX and bw == 0
        assertEquals(Double.MAX_VALUE, graph.getDelay(ig2, ig1));
        assertEquals(0, graph.getBw(ig2, ig1));

    }

    @Test
    public void testUndirectedEdges() {
        // create a directed graph
        InstanceGroupGraphSimple graph = new InstanceGroupGraphSimple(false);
        // create 3 InstanceGroup
        InstanceGroup ig1 = new InstanceGroupSimple(1);
        InstanceGroup ig2 = new InstanceGroupSimple(2);
        // add edge (1, 2) with delay == 10 and bw == 5
        graph.addEdge(ig1, ig2, 10, 5);

        // expect: edge (1, 2) with delay == 10 and bw == 5
        assertEquals(10, graph.getDelay(ig1, ig2));
        assertEquals(5, graph.getBw(ig1, ig2));

        // expect: unadded reverse edge (2, 1) with delay == 10 and bw == 5
        assertEquals(10, graph.getDelay(ig2, ig1));
        assertEquals(5, graph.getBw(ig2, ig1));

        // remove edge (1, 2)
        graph.removeEdge(ig1, ig2);
        // expect: edge (1, 2) with delay == Double.MAX and bw == 0
        assertEquals(Double.MAX_VALUE, graph.getDelay(ig1, ig2));
        assertEquals(0, graph.getBw(ig1, ig2));

        // expect: reverse edge (1, 2) is also removed
        assertEquals(Double.MAX_VALUE, graph.getDelay(ig2, ig1));
        assertEquals(0, graph.getBw(ig2, ig1));
    }
}

