package org.cpnsim.record;

import org.cloudsimplus.core.CloudSim;
import org.cpnsim.datacenter.DatacenterSimple;
import org.cpnsim.request.*;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author 魏鑫磊
 * @date 2023/9/2 10:01
 */
public class SqlRecordSimpleTest {

    SqlRecordSimple sqlRecordSimple=new SqlRecordSimple();

    @AfterEach
    public void closeConnection(){
        sqlRecordSimple.close();
    }

    @Test
    public void testQueryUserRequestSubmitinfo(){
        UserRequest userRequest=new UserRequestSimple(1);
        userRequest.setBelongDatacenterId(1);
        userRequest.setSubmitTime(1.0);
        userRequest.setInstanceGroups(List.of(new InstanceGroupSimple(1)));
        List<UserRequest> userRequests=List.of(userRequest);

        sqlRecordSimple.recordUserRequestsSubmitinfo(userRequests);
        Map<String, Map<String, Object>> mapMap=sqlRecordSimple.queryUserRequestTable();
        Map<String,Object> map=mapMap.get("1");
        assertEquals(1,map.get("belongDc"));
        assertEquals(1.0,map.get("submitTime"));
        assertEquals(1,map.get("instanceGroupNum"));
        assertEquals(0,map.get("successInstanceGroupNum"));
    }

    @Test
    public void testRecordUserRequestFinishInfo(){
        UserRequest userRequest=new UserRequestSimple(1);
        userRequest.setInstanceGroups(List.of(new InstanceGroupSimple(1)));
        List<UserRequest> userRequests=List.of(userRequest);
        sqlRecordSimple.recordUserRequestsSubmitinfo(userRequests);

        userRequest.setFinishTime(1.0);
        userRequest.setState(2);
        userRequest.setFailReason("test");
        sqlRecordSimple.recordUserRequestFinishInfo(userRequest);

        Map<String, Map<String, Object>> mapMap=sqlRecordSimple.queryUserRequestTable();
        Map<String,Object> map=mapMap.get("1");
        assertEquals(1.0,map.get("finishTime"));
        assertEquals("SUCCESS",map.get("state"));
        assertEquals("test",map.get("failReason"));
    }

    @Test
    public void testRecordInstanceGroupsReceivedInfo(){
        InstanceGroup instanceGroup=new InstanceGroupSimple(1);
        instanceGroup.setUserRequest(new UserRequestSimple(1));
        instanceGroup.setRetryNum(2);
        instanceGroup.setReceiveDatacenter(new DatacenterSimple(new CloudSim(),1));
        instanceGroup.setReceivedTime(1.0);
        instanceGroup.setInstanceList(List.of(new InstanceSimple(1,1,2,1,2)));
        List<InstanceGroup> instanceGroups=List.of(instanceGroup);

        sqlRecordSimple.recordInstanceGroupsReceivedInfo(instanceGroups);
        Map<String, Map<String, Object>> mapMap=sqlRecordSimple.queryInstanceGroupTable();
        Map<String,Object> map=mapMap.get("1");
        assertEquals(1,map.get("userRequestId"));
        assertEquals(2,map.get("retryTimes"));
        assertEquals(1,map.get("receivedDc"));
        assertEquals(1.0,map.get("receivedTime"));
        assertEquals(1,map.get("instanceNum"));
        assertEquals(0,map.get("successInstanceNum"));
    }

    @Test
    public void testRecordInstanceGroupFinishInfo(){
        InstanceGroup instanceGroup=new InstanceGroupSimple(1);
        instanceGroup.setUserRequest(new UserRequestSimple(1));
        instanceGroup.setReceiveDatacenter(new DatacenterSimple(new CloudSim(),1));
        instanceGroup.setInstanceList(List.of(new InstanceSimple(1,1,2,1,2)));
        List<InstanceGroup> instanceGroups=List.of(instanceGroup);
        sqlRecordSimple.recordInstanceGroupsReceivedInfo(instanceGroups);

        instanceGroup.setFinishTime(1.0);
        sqlRecordSimple.recordInstanceGroupFinishInfo(instanceGroup);

        Map<String, Map<String, Object>> mapMap=sqlRecordSimple.queryInstanceGroupTable();
        Map<String,Object> map=mapMap.get("1");
        assertEquals(1.0,map.get("finishTime"));
    }

    @Test
    public void testRecordInstanceGroupGraphAllocateInfo(){
        sqlRecordSimple.recordInstanceGroupGraphAllocateInfo(1,2,3,4,1.0,2.0);
        Map<String, Map<String, Object>> mapMap=sqlRecordSimple.queryInstanceGroupGraphTable();
        Map<String,Object> map=mapMap.get("1-3");
        assertEquals(1,map.get("srcDcId"));
        assertEquals(2,map.get("srcInstanceGroupId"));
        assertEquals(3,map.get("dstDcId"));
        assertEquals(4,map.get("dstInstanceGroupId"));
        assertEquals(1.0,map.get("bw"));
        assertEquals(2.0,map.get("startTime"));
    }

    @Test
    public void testRecordInstanceGroupGraphReleaseInfo(){
        sqlRecordSimple.recordInstanceGroupGraphAllocateInfo(1,2,3,4,1.0,2.0);
        sqlRecordSimple.recordInstanceGroupGraphReleaseInfo(1,3,5.0);

        Map<String, Map<String, Object>> mapMap=sqlRecordSimple.queryInstanceGroupGraphTable();
        Map<String,Object> map=mapMap.get("1-3");
        assertEquals(5.0,map.get("finishTime"));
    }

    @Test
    public void testRecordInstanceGroupAllInfo(){
        InstanceGroup instanceGroup=new InstanceGroupSimple(1);
        instanceGroup.setUserRequest(new UserRequestSimple(1));
        instanceGroup.setRetryNum(2);
        instanceGroup.setReceiveDatacenter(new DatacenterSimple(new CloudSim(),1));
        instanceGroup.setReceivedTime(1.0);
        instanceGroup.setFinishTime(2.0);
        instanceGroup.setInstanceList(List.of(new InstanceSimple(1,1,2,1,2)));

        sqlRecordSimple.recordInstanceGroupAllInfo(instanceGroup);
        Map<String, Map<String, Object>> mapMap=sqlRecordSimple.queryInstanceGroupTable();
        Map<String,Object> map=mapMap.get("1");
        assertEquals(1,map.get("userRequestId"));
        assertEquals(2,map.get("retryTimes"));
        assertEquals(1,map.get("receivedDc"));
        assertEquals(1.0,map.get("receivedTime"));
        assertEquals(2.0,map.get("finishTime"));
        assertEquals(1,map.get("instanceNum"));
        assertEquals(0,map.get("successInstanceNum"));
    }

    @Test
    public void testRecordInstanceCreateInfo(){
        Instance instance=new InstanceSimple(1,1,2,1,2);
        InstanceGroup instanceGroup=new InstanceGroupSimple(2);
        instanceGroup.setReceiveDatacenter(new DatacenterSimple(new CloudSim(),6));
        instance.setInstanceGroup(instanceGroup);
        instance.setUserRequest(new UserRequestSimple(3));
        instance.setLifeTime(4);
        instance.setRetryNum(5);
        instance.setHost(7);
        instance.setStartTime(8.0);
        sqlRecordSimple.recordInstanceCreateInfo(instance);

        Map<String, Map<String, Object>> mapMap=sqlRecordSimple.queryInstanceTable();
        Map<String,Object> map=mapMap.get("1");
        assertEquals(2,map.get("instanceGroupId"));
        assertEquals(3,map.get("userRequestId"));
        assertEquals(1,map.get("cpu"));
        assertEquals(2,map.get("ram"));
        assertEquals(1,map.get("storage"));
        assertEquals(2,map.get("bw"));
        assertEquals(4,map.get("lifeTime"));
        assertEquals(5,map.get("retryTimes"));
        assertEquals(6,map.get("datacenter"));
        assertEquals(7,map.get("host"));
        assertEquals(8.0,map.get("startTime"));
    }

    @Test
    public void testRecordInstancesCreateInfo(){
        Instance instance=new InstanceSimple(1,1,2,1,2);
        InstanceGroup instanceGroup=new InstanceGroupSimple(2);
        instanceGroup.setReceiveDatacenter(new DatacenterSimple(new CloudSim(),6));
        instance.setInstanceGroup(instanceGroup);
        instance.setUserRequest(new UserRequestSimple(3));
        instance.setLifeTime(4);
        instance.setRetryNum(5);
        instance.setHost(7);
        instance.setStartTime(8.0);
        instance.setState(UserRequest.RUNNING);
        List<Instance> instances=List.of(instance);
        Map<Integer,List<Instance>> instancesMap=new HashMap<>();
        instancesMap.put(1,instances);
        sqlRecordSimple.recordInstancesCreateInfo(instancesMap);

        Map<String, Map<String, Object>> mapMap=sqlRecordSimple.queryInstanceTable();
        Map<String,Object> map=mapMap.get("1");
        assertEquals(2,map.get("instanceGroupId"));
        assertEquals(3,map.get("userRequestId"));
        assertEquals(1,map.get("cpu"));
        assertEquals(2,map.get("ram"));
        assertEquals(1,map.get("storage"));
        assertEquals(2,map.get("bw"));
        assertEquals(4,map.get("lifeTime"));
        assertEquals(5,map.get("retryTimes"));
        assertEquals(6,map.get("datacenter"));
        assertEquals(7,map.get("host"));
        assertEquals(8.0,map.get("startTime"));
    }

    @Test
    public void testRecordInstanceFinishInfo(){
        Instance instance=new InstanceSimple(1,1,2,1,2);
        InstanceGroup instanceGroup=new InstanceGroupSimple(2);
        instanceGroup.setReceiveDatacenter(new DatacenterSimple(new CloudSim(),6));
        instance.setInstanceGroup(instanceGroup);
        instance.setUserRequest(new UserRequestSimple(3));
        sqlRecordSimple.recordInstanceCreateInfo(instance);

        instance.setFinishTime(9.0);
        sqlRecordSimple.recordInstanceFinishInfo(instance);

        Map<String, Map<String, Object>> mapMap=sqlRecordSimple.queryInstanceTable();
        Map<String,Object> map=mapMap.get("1");
        assertEquals(9.0,map.get("finishTime"));
    }

    @Test
    public void testRecordInstancesFinishInfo(){
        Instance instance=new InstanceSimple(1,1,2,1,2);
        InstanceGroup instanceGroup=new InstanceGroupSimple(2);
        instanceGroup.setReceiveDatacenter(new DatacenterSimple(new CloudSim(),6));
        instance.setInstanceGroup(instanceGroup);
        instance.setUserRequest(new UserRequestSimple(3));
        sqlRecordSimple.recordInstanceCreateInfo(instance);

        instance.setFinishTime(9.0);
        sqlRecordSimple.recordInstancesFinishInfo(List.of(instance));

        Map<String, Map<String, Object>> mapMap=sqlRecordSimple.queryInstanceTable();
        Map<String,Object> map=mapMap.get("1");
        assertEquals(9.0,map.get("finishTime"));
    }

    @Test
    public void testRecordInstancesAllInfo(){
        Instance instance=new InstanceSimple(1,1,2,1,2);
        InstanceGroup instanceGroup=new InstanceGroupSimple(2);
        instanceGroup.setReceiveDatacenter(new DatacenterSimple(new CloudSim(),6));
        instance.setInstanceGroup(instanceGroup);
        instance.setUserRequest(new UserRequestSimple(3));
        instance.setLifeTime(4);
        instance.setRetryNum(5);
        instance.setHost(7);
        instance.setStartTime(8.0);
        instance.setFinishTime(9.0);
        List<Instance> instances=List.of(instance);
        sqlRecordSimple.recordInstancesAllInfo(instances);

        Map<String, Map<String, Object>> mapMap=sqlRecordSimple.queryInstanceTable();
        Map<String,Object> map=mapMap.get("1");
        assertEquals(2,map.get("instanceGroupId"));
        assertEquals(3,map.get("userRequestId"));
        assertEquals(1,map.get("cpu"));
        assertEquals(2,map.get("ram"));
        assertEquals(1,map.get("storage"));
        assertEquals(2,map.get("bw"));
        assertEquals(4,map.get("lifeTime"));
        assertEquals(5,map.get("retryTimes"));
        assertEquals(6,map.get("datacenter"));
        assertEquals(7,map.get("host"));
        assertEquals(8.0,map.get("startTime"));
        assertEquals(9.0,map.get("finishTime"));
    }

    @Test
    public void testRecordInstanceAllInfo(){
        Instance instance=new InstanceSimple(1,1,2,1,2);
        InstanceGroup instanceGroup=new InstanceGroupSimple(2);
        instanceGroup.setReceiveDatacenter(new DatacenterSimple(new CloudSim(),6));
        instance.setInstanceGroup(instanceGroup);
        instance.setUserRequest(new UserRequestSimple(3));
        instance.setLifeTime(4);
        instance.setRetryNum(5);
        instance.setHost(7);
        instance.setStartTime(8.0);
        instance.setFinishTime(9.0);
        sqlRecordSimple.recordInstancesAllInfo(instance);

        Map<String, Map<String, Object>> mapMap=sqlRecordSimple.queryInstanceTable();
        Map<String,Object> map=mapMap.get("1");
        assertEquals(2,map.get("instanceGroupId"));
        assertEquals(3,map.get("userRequestId"));
        assertEquals(1,map.get("cpu"));
        assertEquals(2,map.get("ram"));
        assertEquals(1,map.get("storage"));
        assertEquals(2,map.get("bw"));
        assertEquals(4,map.get("lifeTime"));
        assertEquals(5,map.get("retryTimes"));
        assertEquals(6,map.get("datacenter"));
        assertEquals(7,map.get("host"));
        assertEquals(8.0,map.get("startTime"));
        assertEquals(9.0,map.get("finishTime"));
    }
}
