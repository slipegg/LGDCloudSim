package org.example;

import ch.qos.logback.classic.Level;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.mutable.MutableInt;
import org.cloudsimplus.core.CloudSim;
import org.cloudsimplus.core.CloudSimTag;
import org.cloudsimplus.core.Simulation;
import org.cloudsimplus.core.events.CloudSimEvent;
import org.cloudsimplus.network.topologies.BriteNetworkTopology;
import org.cloudsimplus.network.topologies.NetworkTopology;
import org.cloudsimplus.util.Log;
import org.scalecloudsim.Instances.UserRequest;
import org.scalecloudsim.datacenters.CollaborationManager;
import org.scalecloudsim.datacenters.CollaborationManagerSimple;
import org.scalecloudsim.datacenters.Datacenter;
import org.scalecloudsim.datacenters.DatacenterSimple;
import org.scalecloudsim.statemanager.HostStateGenerator;
import org.scalecloudsim.statemanager.IsomorphicHostStateGenerator;
import org.scalecloudsim.statemanager.SimpleState;
import org.scalecloudsim.statemanager.SimpleStateSimple;
import org.scalecloudsim.users.User;
import org.scalecloudsim.users.UserRequestManager;
import org.scalecloudsim.users.UserRequestManagerSimple;
import org.scalecloudsim.users.UserSimple;

import java.util.ArrayList;
import java.util.List;

public class test {
    Simulation scaleCloudSim;
    Datacenter dc1;
    Datacenter dc2;
    Datacenter dc3;
    CollaborationManager collaborationManager;
    UserSimple user;
    UserRequestManager userRequestManager;
    String NETWORK_TOPOLOGY_FILE = "topology.brite";
    int hostNum = 5_000;

    public static void main(String[] args) {
        test test = new test();

    }

    private test() {
        Log.setLevel(Level.INFO);
        scaleCloudSim = new CloudSim();
        initUser();
        initDatacenter();
        initNetwork();
        scaleCloudSim.start();
    }

    private void initUser() {
        userRequestManager = new UserRequestManagerSimple();
        user = new UserSimple(scaleCloudSim, 1, userRequestManager);
    }

    private void initDatacenter() {
        HostStateGenerator hostStateGenerator = new IsomorphicHostStateGenerator();

        dc1 = new DatacenterSimple(scaleCloudSim, 1, hostNum);
        dc1.getStateManager().initHostStates(hostStateGenerator);
        dc2 = new DatacenterSimple(scaleCloudSim, 2, hostNum);
        dc2.getStateManager().initHostStates(hostStateGenerator);
        dc3 = new DatacenterSimple(scaleCloudSim, 3, hostNum);
        dc3.getStateManager().initHostStates(hostStateGenerator);

        collaborationManager = new CollaborationManagerSimple(scaleCloudSim);
        collaborationManager.addDatacenter(dc1, 0);
        collaborationManager.addDatacenter(dc2, 0);
        collaborationManager.addDatacenter(dc3, 0);
    }

    private void initNetwork() {
        BriteNetworkTopology networkTopology = BriteNetworkTopology.getInstance(NETWORK_TOPOLOGY_FILE);
        scaleCloudSim.setNetworkTopology(networkTopology);
        networkTopology.mapNode(dc1, 0);
        networkTopology.mapNode(dc2, 1);
        networkTopology.mapNode(dc3, 2);
    }
}
