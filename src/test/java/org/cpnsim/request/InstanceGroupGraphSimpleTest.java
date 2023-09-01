package org.cpnsim.request;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class InstanceGroupGraphSimpleTest {
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
