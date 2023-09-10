package org.cpnsim.record;

import org.cloudsimplus.core.CloudSim;
import org.cpnsim.datacenter.DatacenterSimple;
import org.cpnsim.request.*;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * @author Xinlei Wei
 * @date 2023/9/5 23:55
 */
public class SqlRecordNullTest {

    SqlRecordNull sqlRecordNull =new SqlRecordNull();

    @Test
    public void testGetDbPath(){
        String result= sqlRecordNull.getDbPath();
        assertNull(result);
    }

    @Test
    public void testSetDbPath(){
        sqlRecordNull.setDbPath("test");
        String result= sqlRecordNull.getDbPath();
        assertNull(result);
    }

    @Test
    public void testRecordUserRequestsSubmitInfo(){
        Map<String,Map<String ,Object>> map1= sqlRecordNull.queryUserRequestTable();

        UserRequest userRequest=new UserRequestSimple(1);
        userRequest.setBelongDatacenterId(1);
        userRequest.setSubmitTime(1.0);
        userRequest.setInstanceGroups(List.of(new InstanceGroupSimple(1)));
        List<UserRequest> userRequests=List.of(userRequest);
        sqlRecordNull.recordUserRequestsSubmitinfo(userRequests);

        Map<String,Map<String ,Object>> map2= sqlRecordNull.queryUserRequestTable();
        assertMapEquals(map1,map2);
    }

    @Test
    public void testRecordUserRequestFinishInfo(){
        Map<String,Map<String ,Object>> map1= sqlRecordNull.queryUserRequestTable();

        UserRequest userRequest=new UserRequestSimple(1);
        userRequest.setBelongDatacenterId(1);
        userRequest.setSubmitTime(1.0);
        userRequest.setInstanceGroups(List.of(new InstanceGroupSimple(1)));
        sqlRecordNull.recordUserRequestFinishInfo(userRequest);

        Map<String,Map<String ,Object>> map2= sqlRecordNull.queryUserRequestTable();
        assertMapEquals(map1,map2);
    }

    @Test
    public void testRecordInstanceGroupsReceivedInfo(){
        Map<String,Map<String ,Object>> map1= sqlRecordNull.queryInstanceGroupTable();

        InstanceGroup instanceGroup=new InstanceGroupSimple(1);
        instanceGroup.setUserRequest(new UserRequestSimple(1));
        instanceGroup.setRetryNum(2);
        instanceGroup.setReceiveDatacenter(new DatacenterSimple(new CloudSim(),1));
        instanceGroup.setReceivedTime(1.0);
        instanceGroup.setInstanceList(List.of(new InstanceSimple(1,1,2,1,2)));
        List<InstanceGroup> instanceGroups=List.of(instanceGroup);
        sqlRecordNull.recordInstanceGroupsReceivedInfo(instanceGroups);

        Map<String,Map<String ,Object>> map2= sqlRecordNull.queryInstanceGroupTable();
        assertMapEquals(map1,map2);
    }

    @Test
    public void testRecordInstanceGroupFinishInfo(){
        Map<String,Map<String ,Object>> map1= sqlRecordNull.queryInstanceGroupTable();

        InstanceGroup instanceGroup=new InstanceGroupSimple(1);
        instanceGroup.setUserRequest(new UserRequestSimple(1));
        instanceGroup.setRetryNum(2);
        instanceGroup.setReceiveDatacenter(new DatacenterSimple(new CloudSim(),1));
        instanceGroup.setReceivedTime(1.0);
        instanceGroup.setInstanceList(List.of(new InstanceSimple(1,1,2,1,2)));
        sqlRecordNull.recordInstanceGroupFinishInfo(instanceGroup);

        Map<String,Map<String ,Object>> map2= sqlRecordNull.queryInstanceGroupTable();
        assertMapEquals(map1,map2);
    }

    @Test
    public void testRecordInstanceGroupAllInfo(){
        Map<String,Map<String ,Object>> map1= sqlRecordNull.queryInstanceGroupTable();

        InstanceGroup instanceGroup=new InstanceGroupSimple(1);
        instanceGroup.setUserRequest(new UserRequestSimple(1));
        instanceGroup.setRetryNum(2);
        instanceGroup.setReceiveDatacenter(new DatacenterSimple(new CloudSim(),1));
        instanceGroup.setReceivedTime(1.0);
        instanceGroup.setInstanceList(List.of(new InstanceSimple(1,1,2,1,2)));
        sqlRecordNull.recordInstanceGroupFinishInfo(instanceGroup);

        Map<String,Map<String ,Object>> map2= sqlRecordNull.queryInstanceGroupTable();
        assertMapEquals(map1,map2);
    }

    @Test
    public void testRecordInstanceGroupGraphAllocateInfo(){
        Map<String,Map<String ,Object>> map1= sqlRecordNull.queryInstanceGroupGraphTable();

        sqlRecordNull.recordInstanceGroupGraphAllocateInfo(1,2,3,4,1.0,2.0);

        Map<String,Map<String ,Object>> map2= sqlRecordNull.queryInstanceGroupGraphTable();
        assertMapEquals(map1,map2);
    }

    @Test
    public void testRecordInstanceGroupGraphReleaseInfo(){
        Map<String,Map<String ,Object>> map1= sqlRecordNull.queryInstanceGroupGraphTable();

        sqlRecordNull.recordInstanceGroupGraphAllocateInfo(1,2,3,4,1.0,2.0);
        sqlRecordNull.recordInstanceGroupGraphReleaseInfo(1,3,5.0);

        Map<String,Map<String ,Object>> map2= sqlRecordNull.queryInstanceGroupGraphTable();
        assertMapEquals(map1,map2);
    }

    @Test
    public void testRecordInstancesCreateInfo(){
        Map<String,Map<String ,Object>> map1= sqlRecordNull.queryInstanceTable();

        Instance instance=new InstanceSimple(1,1,2,1,2);
        InstanceGroup instanceGroup=new InstanceGroupSimple(2);
        instanceGroup.setReceiveDatacenter(new DatacenterSimple(new CloudSim(),6));
        instance.setInstanceGroup(instanceGroup);
        instance.setUserRequest(new UserRequestSimple(3));
        instance.setLifeTime(4);
        instance.setRetryNum(5);
        instance.setHost(7);
        instance.setStartTime(8.0);
        Map<Integer,List<Instance>> instances=Map.of(1,List.of(instance));
        sqlRecordNull.recordInstancesCreateInfo(instances);

        Map<String,Map<String ,Object>> map2= sqlRecordNull.queryInstanceTable();
        assertMapEquals(map1,map2);
    }

    @Test
    public void testRecordInstancesFinishInfo(){
        Map<String,Map<String ,Object>> map1= sqlRecordNull.queryInstanceTable();

        Instance instance=new InstanceSimple(1,1,2,1,2);
        InstanceGroup instanceGroup=new InstanceGroupSimple(2);
        instanceGroup.setReceiveDatacenter(new DatacenterSimple(new CloudSim(),6));
        instance.setInstanceGroup(instanceGroup);
        instance.setUserRequest(new UserRequestSimple(3));
        List<Instance> instanceList=List.of(instance);
        Map<Integer,List<Instance>> instanceMap=Map.of(1,instanceList);
        sqlRecordNull.recordInstancesCreateInfo(instanceMap);

        instance.setFinishTime(9.0);
        sqlRecordNull.recordInstancesFinishInfo(instanceList);

        Map<String,Map<String ,Object>> map2= sqlRecordNull.queryInstanceTable();
        assertMapEquals(map1,map2);
    }

    @Test
    public void testRecordInstancesAllInfo(){
        Map<String,Map<String ,Object>> map1= sqlRecordNull.queryInstanceTable();

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
        sqlRecordNull.recordInstancesAllInfo(instances);

        Map<String,Map<String ,Object>> map2= sqlRecordNull.queryInstanceTable();
        assertMapEquals(map1,map2);
    }


    private void assertMapEquals(Map<String, Map<String,Object>> map1,Map<String, Map<String,Object>> map2){
        assertEquals(map1.size(),map2.size());
        for (String key :
                map1.keySet()) {
            assertEquals(map1.get(key),map2.get(key));
        }
    }
}
