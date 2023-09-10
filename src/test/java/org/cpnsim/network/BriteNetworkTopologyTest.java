package org.cpnsim.network;

import org.cloudsimplus.core.CloudSim;
import org.cloudsimplus.core.Simulation;
import org.cloudsimplus.network.RandomDelayDynamicModel;
import org.cloudsimplus.network.topologies.BriteNetworkTopology;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.cpnsim.datacenter.Datacenter;
import org.cpnsim.datacenter.DatacenterSimple;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class BriteNetworkTopologyTest {

    final String NETWORK_TOPOLOGY_FILE = "src/test/resources/topology.brite";

    BriteNetworkTopology networkTopology;

    Datacenter dc0,dc1,dc2,dc3,dc4;

    @BeforeEach
    public void initializeNetworkTopology(){
        networkTopology = BriteNetworkTopology.getInstance(NETWORK_TOPOLOGY_FILE, true);
        Simulation scaleCloudsim = new CloudSim();
        dc0 = new DatacenterSimple(scaleCloudsim, 0);
        networkTopology.mapNode(dc0, 0);
        dc1 = new DatacenterSimple(scaleCloudsim, 1);
        networkTopology.mapNode(dc1, 1);
        dc2 = new DatacenterSimple(scaleCloudsim, 2);
        networkTopology.mapNode(dc2, 2);
        dc3 = new DatacenterSimple(scaleCloudsim, 3);
        networkTopology.mapNode(dc3, 3);
        dc4 = new DatacenterSimple(scaleCloudsim, 4);
        networkTopology.mapNode(dc4, 4);
    }

    @Test
    public void testNetworkTopologyBw(){
        double expectedBw04 = 10949.58;
        assertEquals(expectedBw04, networkTopology.getBw(dc0, dc4));
    }

    @Test
    public void testNetworkTopologyAllocateBw(){
        networkTopology.allocateBw(dc0, dc4, 100);
        double expectedBw04 = 10949.58 - 100;
        assertEquals(expectedBw04, networkTopology.getBw(dc0, dc4));
    }

    @Test
    public void testNetworkTopologyReleaseBw(){
        networkTopology.releaseBw(dc0, dc4, 100);
        double expectedBw04 = 10949.58 + 100;
        assertEquals(expectedBw04, networkTopology.getBw(dc0, dc4));
    }

    @Test
    public void testNetworkTopologyAccessLatency() {
        double accessLatency = networkTopology.getAcessLatency(dc0, dc2);
        double expectedAccessLatency = 2.05;
        assertEquals(expectedAccessLatency, accessLatency);
    }
}
