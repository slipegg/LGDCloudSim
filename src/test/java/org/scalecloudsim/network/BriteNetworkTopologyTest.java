package org.scalecloudsim.network;

import org.cloudsimplus.core.CloudSim;
import org.cloudsimplus.core.Simulation;
import org.cloudsimplus.network.RandomDelayDynamicModel;
import org.cloudsimplus.network.topologies.BriteNetworkTopology;
import org.junit.jupiter.api.Test;
import org.scalecloudsim.datacenters.Datacenter;
import org.scalecloudsim.datacenters.DatacenterSimple;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class BriteNetworkTopologyTest {
    @Test
    public void testBriteNetworkTopology() {
        final String NETWORK_TOPOLOGY_FILE = "topology.brite";
        BriteNetworkTopology networkTopology = BriteNetworkTopology.getInstance(NETWORK_TOPOLOGY_FILE, true);
        Simulation scaleCloudsim = new CloudSim();
        Datacenter dc0 = new DatacenterSimple(scaleCloudsim, 0);
        networkTopology.mapNode(dc0, 0);
        Datacenter dc1 = new DatacenterSimple(scaleCloudsim, 1);
        networkTopology.mapNode(dc1, 1);
        Datacenter dc2 = new DatacenterSimple(scaleCloudsim, 2);
        networkTopology.mapNode(dc2, 2);
        Datacenter dc3 = new DatacenterSimple(scaleCloudsim, 3);
        networkTopology.mapNode(dc3, 3);
        Datacenter dc4 = new DatacenterSimple(scaleCloudsim, 4);
        networkTopology.mapNode(dc4, 4);
        System.out.println(networkTopology.getGraph());
        double expectedDelay02 = 7.0;
        assertEquals(expectedDelay02, networkTopology.getDelay(dc0, dc2));
        System.out.println(networkTopology.getBw(dc1, dc4));
        double expectedBw14 = 1200000.00;
        assertEquals(expectedBw14, networkTopology.getBw(dc1, dc4));
        networkTopology.allocateBw(dc1, dc4, 100);
        expectedBw14 -= 100;
        assertEquals(expectedBw14, networkTopology.getBw(dc1, dc4));
        networkTopology.releaseBw(dc1, dc4, 100);
        expectedBw14 += 100;
        assertEquals(expectedBw14, networkTopology.getBw(dc1, dc4));

        networkTopology.setDelayDynamicModel(new RandomDelayDynamicModel());
        assertNotEquals(expectedDelay02, networkTopology.getDynamicDelay(dc0, dc2, 1));
        System.out.println(networkTopology.getDynamicDelay(dc0, dc2, 1));
    }
}
