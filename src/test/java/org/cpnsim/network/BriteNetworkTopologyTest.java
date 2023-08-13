package org.cpnsim.network;

import org.cloudsimplus.core.CloudSim;
import org.cloudsimplus.core.Simulation;
import org.cloudsimplus.network.RandomDelayDynamicModel;
import org.cloudsimplus.network.topologies.BriteNetworkTopology;
import org.junit.jupiter.api.Test;
import org.cpnsim.datacenter.Datacenter;
import org.cpnsim.datacenter.DatacenterSimple;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class BriteNetworkTopologyTest {
    @Test
    public void testBriteNetworkTopology() {
        final String NETWORK_TOPOLOGY_FILE = "src/test/resources/topology.brite";
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
        double expectedBw04 = 10949.58;
        assertEquals(expectedBw04, networkTopology.getBw(dc0, dc4));
        networkTopology.allocateBw(dc0, dc4, 100);
        expectedBw04 -= 100;
        assertEquals(expectedBw04, networkTopology.getBw(dc0, dc4));
        networkTopology.releaseBw(dc0, dc4, 100);
        expectedBw04 += 100;
        assertEquals(expectedBw04, networkTopology.getBw(dc0, dc4));

        double accessLatency = networkTopology.getAcessLatency(dc0, dc2);
        double expectedAccessLatency = 2.05;
        assertEquals(expectedAccessLatency, accessLatency);
    }
}
