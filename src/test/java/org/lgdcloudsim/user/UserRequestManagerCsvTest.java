package org.lgdcloudsim.user;

import org.lgdcloudsim.request.Instance;
import org.lgdcloudsim.request.InstanceGroup;
import org.lgdcloudsim.request.UserRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.*;

public class UserRequestManagerCsvTest {
    UserRequestManagerCsv userRequestManager = new UserRequestManagerCsv("src/test/resources/generateRequestParameter.csv");
    private static Field[] fields;

    @BeforeAll
    public static void setUpClass() {
        fields = UserRequestManagerCsv.class.getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
        }
    }

    @Test
    public void testGetValue() {
        try {
            TreeMap<Integer, TreeMap<String, Double>> DcAreaDistribution = (TreeMap<Integer, TreeMap<String, Double>>) getFieldValue("DcAreaDistribution");
            TreeMap<Integer, TreeMap<String, Double>> exceptDcAreaDistribution = creatExceptDcAreaDistribution();
            assertEquals(exceptDcAreaDistribution, DcAreaDistribution);
            int RequestPerNumMin = (int) getFieldValue("RequestPerNumMin");
            assertEquals(10, RequestPerNumMin);
            int RequestPerNumMax = (int) getFieldValue("RequestPerNumMax");
            assertEquals(20, RequestPerNumMax);
            int RequestTimeIntervalMin = (int) getFieldValue("RequestTimeIntervalMin");
            assertEquals(100, RequestTimeIntervalMin);
            int RequestTimeIntervalMax = (int) getFieldValue("RequestTimeIntervalMax");
            assertEquals(200, RequestTimeIntervalMax);
            int RequestTimes = (int) getFieldValue("RequestTimes");
            assertEquals(10, RequestTimes);
            double ScheduleDelayLimitMin = (double) getFieldValue("ScheduleDelayLimitMin");
            assertEquals(1000.0, ScheduleDelayLimitMin);
            double ScheduleDelayLimitMax = (double) getFieldValue("ScheduleDelayLimitMax");
            assertEquals(2000.0, ScheduleDelayLimitMax);
            int RequestGroupNumMin = (int) getFieldValue("RequestGroupNumMin");
            assertEquals(3, RequestGroupNumMin);
            int RequestGroupNumMax = (int) getFieldValue("RequestGroupNumMax");
            assertEquals(5, RequestGroupNumMax);
            int GroupInstanceNumMin = (int) getFieldValue("GroupInstanceNumMin");
            assertEquals(10, GroupInstanceNumMin);
            int GroupInstanceNumMax = (int) getFieldValue("GroupInstanceNumMax");
            assertEquals(20, GroupInstanceNumMax);
            double GroupAccessDelayPercent = (double) getFieldValue("GroupAccessDelayPercent");
            assertEquals(0.3, GroupAccessDelayPercent);
            int GroupAccessDelayMin = (int) getFieldValue("GroupAccessDelayMin");
            assertEquals(20, GroupAccessDelayMin);
            int GroupAccessDelayMax = (int) getFieldValue("GroupAccessDelayMax");
            assertEquals(30, GroupAccessDelayMax);
            double GroupEdgePercent = (double) getFieldValue("GroupEdgePercent");
            assertEquals(0.5, GroupEdgePercent);
            int GroupEdgeIsDirected = (int) getFieldValue("GroupEdgeIsDirected");
            assertEquals(0, GroupEdgeIsDirected);
            double GroupBwPercent = (double) getFieldValue("GroupBwPercent");
            assertEquals(1.0, GroupBwPercent);
            int GroupBwMin = (int) getFieldValue("GroupBwMin");
            assertEquals(10, GroupBwMin);
            int GroupBwMax = (int) getFieldValue("GroupBwMax");
            assertEquals(200, GroupBwMax);
            double GroupDelayPercent = (double) getFieldValue("GroupDelayPercent");
            assertEquals(0.5, GroupDelayPercent);
            int GroupDelayMin = (int) getFieldValue("GroupDelayMin");
            assertEquals(100, GroupDelayMin);
            int GroupDelayMax = (int) getFieldValue("GroupDelayMax");
            assertEquals(1000, GroupDelayMax);
            int GroupRetryTimesMin = (int) getFieldValue("GroupRetryTimesMin");
            assertEquals(1, GroupRetryTimesMin);
            int GroupRetryTimesMax = (int) getFieldValue("GroupRetryTimesMax");
            assertEquals(3, GroupRetryTimesMax);
            int InstanceCpuNumMin = (int) getFieldValue("InstanceCpuNumMin");
            assertEquals(4, InstanceCpuNumMin);
            int InstanceCpuNumMax = (int) getFieldValue("InstanceCpuNumMax");
            assertEquals(12, InstanceCpuNumMax);
            int InstanceRamNumMin = (int) getFieldValue("InstanceRamNumMin");
            assertEquals(8, InstanceRamNumMin);
            int InstanceRamNumMax = (int) getFieldValue("InstanceRamNumMax");
            assertEquals(24, InstanceRamNumMax);
            int InstanceStorageNumMin = (int) getFieldValue("InstanceStorageNumMin");
            assertEquals(50, InstanceStorageNumMin);
            int InstanceStorageNumMax = (int) getFieldValue("InstanceStorageNumMax");
            assertEquals(150, InstanceStorageNumMax);
            int InstanceBwNumMin = (int) getFieldValue("InstanceBwNumMin");
            assertEquals(25, InstanceBwNumMin);
            int InstanceBwNumMax = (int) getFieldValue("InstanceBwNumMax");
            assertEquals(75, InstanceBwNumMax);
            int InstanceLifeTimeMin = (int) getFieldValue("InstanceLifeTimeMin");
            assertEquals(1000, InstanceLifeTimeMin);
            int InstanceLifeTimeMax = (int) getFieldValue("InstanceLifeTimeMax");
            assertEquals(9000, InstanceLifeTimeMax);
            int InstanceRetryTimesMin = (int) getFieldValue("InstanceRetryTimesMin");
            assertEquals(3, InstanceRetryTimesMin);
            int InstanceRetryTimesMax = (int) getFieldValue("InstanceRetryTimesMax");
            assertEquals(5, InstanceRetryTimesMax);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static TreeMap<Integer, TreeMap<String, Double>> creatExceptDcAreaDistribution() {
        TreeMap<Integer, TreeMap<String, Double>> exceptDcAreaDistribution = new TreeMap<>();
        TreeMap<String, Double> areaDistribution1 = new TreeMap<>();
        areaDistribution1.put("shanghai", 0.4);
        areaDistribution1.put("beijing", 0.5);
        TreeMap<String, Double> areaDistribution2 = new TreeMap<>();
        areaDistribution2.put("beijing", 1.0);
        exceptDcAreaDistribution.put(1, areaDistribution1);
        exceptDcAreaDistribution.put(2, areaDistribution2);
        return exceptDcAreaDistribution;
    }

    /**
     * Parament Name,Parament
     * FromDcPercent,"{""1"":{""shanghai"":0.4,""beijing"":0.1},""2"":{""beijing"":0.5}}"
     * RequestPerNumMin,10
     * RequestPerNumMax,20
     * RequestTimeIntervalMin,100
     * RequestTimeIntervalMax,200
     * RequestTimes,10
     * ScheduleDelayLimitMin,1000
     * ScheduleDelayLimitMax,2000
     * RequestGroupNumMin,3
     * RequestGroupNumMax,5
     * GroupInstanceNumMin,10
     * GroupInstanceNumMax,20
     * GroupAccessDelayPercent,0.3
     * GroupAccessDelayMin,20
     * GroupAccessDelayMax,30
     * GroupEdgePercent,0.5
     * GroupEdgeIsDirected,0
     * GroupBwPercent,1
     * GroupBwMin,10
     * GroupBwMax,200
     * GroupDelayPercent,0.5
     * GroupDelayMin,100
     * GroupDelayMax,1000
     * GroupRetryTimesMin,1
     * GroupRetryTimesMax,3
     * InstanceCpuNumMin,4
     * InstanceCpuNumMax,12
     * InstanceRamNumMin,8
     * InstanceRamNumMax,24
     * InstanceStorageNumMin,50
     * InstanceStorageNumMax,150
     * InstanceBwNumMin,25
     * InstanceBwNumMax,75
     * InstanceLifeTimeMin,1000
     * InstanceLifeTimeMax,9000
     * InstanceRetryTimesMin,3
     * InstanceRetryTimesMax,5
     */
    @Test
    public void testGenerateOnceUserRequests() {
        Map<Integer, List<UserRequest>> userRequests = userRequestManager.generateOnceUserRequests();

        int[] exceptDstDataCenterIds = {1, 2};
        int[] actualDstDataCenterIds = userRequests.keySet().stream().mapToInt(Integer::intValue).toArray();
        assertTrue(isArrayContainArray(exceptDstDataCenterIds, actualDstDataCenterIds));

        int actualUserRequestNum = userRequests.get(actualDstDataCenterIds[0]).size();
        if (actualDstDataCenterIds.length == 2) {
            actualUserRequestNum += userRequests.get(actualDstDataCenterIds[1]).size();
        }
        assertTrue(actualUserRequestNum >= 10 && actualUserRequestNum <= 20);

        double nextSendTime = userRequestManager.getNextSendTime();
        assertTrue(nextSendTime >= 100.0 && nextSendTime <= 200.0);

        UserRequest userRequest = userRequests.get(actualDstDataCenterIds[0]).get(0);

        double scheduleDelayLimit = userRequest.getScheduleDelayLimit();
        assertTrue(scheduleDelayLimit >= 1000.0 && scheduleDelayLimit <= 2000.0);

        int requestGroupNum = userRequest.getInstanceGroups().size();
        assertTrue(requestGroupNum >= 3 && requestGroupNum <= 5);

        int groupInstanceNum = userRequest.getInstanceGroups().get(0).getInstances().size();
        assertTrue(groupInstanceNum >= 10 && groupInstanceNum <= 20);

        InstanceGroup instanceGroup = userRequest.getInstanceGroups().get(0);
        int instanceGroupRetryTimes = instanceGroup.getRetryMaxNum();
        assertTrue(instanceGroupRetryTimes >= 1 && instanceGroupRetryTimes <= 3);

        Instance instance = userRequest.getInstanceGroups().get(0).getInstances().get(0);

        int cpu = instance.getCpu();
        assertTrue(cpu >= 4 && cpu <= 12);

        int ram = instance.getRam();
        assertTrue(ram >= 8 && ram <= 24);

        int storage = instance.getStorage();
        assertTrue(storage >= 50 && storage <= 150);

        int bw = instance.getBw();
        assertTrue(bw >= 25 && bw <= 75);

        int lifeTime = instance.getLifecycle();
        assertTrue(lifeTime >= 1000 && lifeTime <= 9000);

        int instanceRetryTimes = instance.getRetryMaxNum();
        assertTrue(instanceRetryTimes >= 3 && instanceRetryTimes <= 5);
    }

    private Object getFieldValue(String fieldName) throws IllegalAccessException {
        for (Field field : fields) {
            if (field.getName().equals(fieldName)) {
                return field.get(userRequestManager);
            }
        }
        return null;
    }

    private boolean isArrayContainArray(int[] array, int[] subArray) {
        if (array.length < subArray.length) {
            return false;
        }

        for (int i = 0; i <= array.length - subArray.length; i++) {
            int j;
            for (j = 0; j < subArray.length; j++) {
                if (array[i + j] != subArray[j]) {
                    break;
                }
            }
            if (j == subArray.length) {
                return true;
            }
        }
        return false;
    }
}
