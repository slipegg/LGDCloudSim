package org.cpnsim.interscheduler;

import org.cloudsimplus.core.CloudSim;
import org.cloudsimplus.core.Simulation;
import org.cloudsimplus.network.topologies.BriteNetworkTopology;
import org.cloudsimplus.network.topologies.NetworkTopology;
import org.cpnsim.datacenter.Datacenter;
import org.cpnsim.datacenter.DatacenterSimple;
import org.cpnsim.request.*;
import org.cpnsim.statemanager.PartitionRangesManager;
import org.cpnsim.statemanager.StatesManager;
import org.cpnsim.statemanager.StatesManagerSimple;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author 魏鑫磊
 * @date 2023/7/13 10:44
 */
public class InterSchedulerSimpleTest {

    @Test
    public void testNewScheduleInterSchedulerSimpleNoParam(){
        InterScheduler interScheduler=new InterSchedulerSimple();
        assertEquals(0,interScheduler.getId());
    }

    @Test
    public void testNewScheduleInterSchedulerSimpleParams(){
        Simulation simulation=new CloudSim();
        Datacenter datacenter=new DatacenterSimple(simulation);
        int id=1;
        InterScheduler interScheduler=new InterSchedulerSimple(id,datacenter);
        assertEquals(id,interScheduler.getId());
        assertSame(datacenter,interScheduler.getDatacenter());
    }

    @Test
    public void testSetId(){
        int id=1;
        InterScheduler interScheduler=new InterSchedulerSimple();
        interScheduler.setId(id);
        assertEquals(id,interScheduler.getId());
    }

    @Test
    public void testSetDatacenter(){
        InterScheduler interScheduler=new InterSchedulerSimple();
        Simulation simulation=new CloudSim();
        Datacenter datacenter=new DatacenterSimple(simulation);
        interScheduler.setDatacenter(datacenter);
        assertSame(datacenter,interScheduler.getDatacenter());
    }

    @Test
    public void testIsDirected(){
        InterScheduler interScheduler=new InterSchedulerSimple();
        assertFalse(interScheduler.isDirectedSend());
    }

    @Test
    public void testDecideReceiveGroupResult(){
        List<InstanceGroup> instanceGroups=new ArrayList<>();
        List<Integer> userRequests= Arrays.asList(
                UserRequest.WAITING,
                UserRequest.FAILED,
                UserRequest.SCHEDULING,
                UserRequest.SUCCESS,
                UserRequest.RUNNING);
        userRequests.forEach(state->{
            UserRequest userRequest=new UserRequestSimple(0);
            userRequest.setState(state);
            InstanceGroup instanceGroup=new InstanceGroupSimple(state);
            instanceGroup.setUserRequest(userRequest);
            instanceGroups.add(instanceGroup);
        });

        UserRequest failedRequest=new UserRequestSimple(0);
        failedRequest.setState(UserRequest.FAILED);
        InstanceGroup instanceGroup=new InstanceGroupSimple(128);
        instanceGroup.setUserRequest(failedRequest);
        instanceGroups.add(instanceGroup);

        InterScheduler interScheduler=new InterSchedulerSimple();
        Map<InstanceGroup, Boolean> result=interScheduler.decideReciveGroupResult(instanceGroups);
        assertEquals(4,result.size());
        assertFalse(result.containsKey(failedRequest));
    }

    @Test
    public void testDecideTargetDatacenter(){
        Simulation simulation=new CloudSim();
        Datacenter datacenter1=new DatacenterSimple(simulation);
        Datacenter datacenter2=new DatacenterSimple(simulation);
        Datacenter datacenter3=new DatacenterSimple(simulation);
        Datacenter datacenter4=new DatacenterSimple(simulation);

        InstanceGroup instanceGroup1=new InstanceGroupSimple(1);
        InstanceGroup instanceGroup2=new InstanceGroupSimple(2);
        InstanceGroup instanceGroup3=new InstanceGroupSimple(3);

        List<InstanceGroup> instanceGroups= Arrays.asList(
                instanceGroup1,instanceGroup2,instanceGroup3
        );

        Map<Datacenter,Integer> datacenterIntegerMap1=Map.of(
                datacenter1,1,
                datacenter2,0
        );
        Map<Datacenter,Integer> datacenterIntegerMap2=Map.of(
                datacenter2,1,
                datacenter3,0,
                datacenter4,1
        );
        Map<Datacenter,Integer> datacenterIntegerMap3=Map.of(
                datacenter1,0,
                datacenter4,0
        );

        Map<InstanceGroup,Map<Datacenter,Integer>> instanceGroupMapMap=Map.of(
                instanceGroup1,datacenterIntegerMap1,
                instanceGroup2,datacenterIntegerMap2,
                instanceGroup3,datacenterIntegerMap3
        );

        InterScheduler interScheduler=new InterSchedulerSimple();
        Map<InstanceGroup,Datacenter> result= interScheduler.decideTargetDatacenter(instanceGroupMapMap,instanceGroups);

        assertEquals(3,result.size());

        assertSame(datacenter1,result.get(instanceGroup1));
        assertTrue(
                result.get(instanceGroup2)==datacenter2
                        ||
                        result.get(instanceGroup2)==datacenter4
        );
        assertNull(result.get(instanceGroup3));
    }

    @Test
    public void testFilterDatacentersByAccessLatency() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
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
        InstanceGroup instanceGroup=new InstanceGroupSimple(0);
        instanceGroup.setAccessLatency(3);
        ArrayList<Datacenter> datacenters = new ArrayList<Datacenter>(Arrays.asList(dc1, dc2, dc3, dc4));
        Method method =InterSchedulerSimple.class.getDeclaredMethod("filterDatacentersByAccessLatency",InstanceGroup.class,List.class, NetworkTopology.class);
        method.setAccessible(true);
        InterSchedulerSimple interScheduler=new InterSchedulerSimple();
        interScheduler.setDatacenter(dc0);
        method.invoke(interScheduler,instanceGroup,datacenters,networkTopology);
        assertEquals(3,datacenters.size());
    }

    @Test
    public void testFilterDatacentersByResourceSample() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        CloudSim cloudSim=new CloudSim();
        Datacenter datacenter=new DatacenterSimple(cloudSim);
        Map<Integer, int[]> ranges=new HashMap<>();
        ranges.put(1,new int[]{0,2});
        PartitionRangesManager partitionRangesManager=new PartitionRangesManager(ranges);
        StatesManager statesManager=new StatesManagerSimple(1,partitionRangesManager,0);
        datacenter.setStatesManager(statesManager);
        ArrayList<Datacenter> allDatacenters=new ArrayList<Datacenter>(List.of(datacenter));

        InstanceGroup instanceGroup=new InstanceGroupSimple(1);
        Instance instance=new InstanceSimple(1,1,2,1,2);
        List<Instance> instances=List.of(instance);
        instanceGroup.setInstanceList(instances);

        Method method =InterSchedulerSimple.class.getDeclaredMethod("filterDatacentersByResourceSample",InstanceGroup.class,List.class);
        method.setAccessible(true);
        InterSchedulerSimple interScheduler=new InterSchedulerSimple();
        method.invoke(interScheduler,instanceGroup,allDatacenters);

        assertArrayEquals(new Datacenter[]{},allDatacenters.toArray());
    }
}
