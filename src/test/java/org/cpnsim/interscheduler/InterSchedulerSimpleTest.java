package org.cpnsim.interscheduler;

import org.cloudsimplus.core.CloudSim;
import org.cloudsimplus.core.Simulation;
import org.cpnsim.datacenter.Datacenter;
import org.cpnsim.datacenter.DatacenterSimple;
import org.cpnsim.request.InstanceGroup;
import org.cpnsim.request.InstanceGroupSimple;
import org.cpnsim.request.UserRequest;
import org.cpnsim.request.UserRequestSimple;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

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
            UserRequest userRequest=new UserRequestSimple();
            userRequest.setState(state);
            InstanceGroup instanceGroup=new InstanceGroupSimple(state);
            instanceGroup.setUserRequest(userRequest);
            instanceGroups.add(instanceGroup);
        });

        UserRequest failedRequest=new UserRequestSimple();
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
}
