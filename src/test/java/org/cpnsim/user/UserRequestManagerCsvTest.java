//package org.cpnsim.user;
//
//import org.cpnsim.request.UserRequest;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.BeforeAll;
//
//import java.lang.reflect.Field;
//import java.util.List;
//import java.util.Map;
//
//import static org.junit.jupiter.api.Assertions.assertArrayEquals;
//import static org.junit.jupiter.api.Assertions.assertEquals;
//
//public class UserRequestManagerCsvTest {
//    UserRequestManagerCsv userRequestManager = new UserRequestManagerCsv("src/test/resources/generateRequestParament.csv");
//    private static Field[] fields;
//
//    @BeforeAll
//    public static void setUpClass() {
//        fields = UserRequestManagerCsv.class.getDeclaredFields();
//        for (Field field : fields) {
//            field.setAccessible(true);
//        }
//    }
//
//    @Test
//    public void testGetValue() {
//        try {
//            double[] FromDcPercent = (double[]) getFieldValue("FromDcPercent");
//            assertArrayEquals(new double[]{1.0}, FromDcPercent);
//            int RequestPerNumMin = (int) getFieldValue("RequestPerNumMin");
//            assertEquals(4, RequestPerNumMin);
//            int RequestPerNumMax = (int) getFieldValue("RequestPerNumMax");
//            assertEquals(4, RequestPerNumMax);
//            int RequestTimeIntervalMin = (int) getFieldValue("RequestTimeIntervalMin");
//            assertEquals(100, RequestTimeIntervalMin);
//            int RequestTimeIntervalMax = (int) getFieldValue("RequestTimeIntervalMax");
//            assertEquals(100, RequestTimeIntervalMax);
//            int RequestTimes = (int) getFieldValue("RequestTimes");
//            assertEquals(10, RequestTimes);
//            int RequestGroupNumMin = (int) getFieldValue("RequestGroupNumMin");
//            assertEquals(1, RequestGroupNumMin);
//            int RequestGroupNumMax = (int) getFieldValue("RequestGroupNumMax");
//            assertEquals(1, RequestGroupNumMax);
//            int GroupInstanceNumMin = (int) getFieldValue("GroupInstanceNumMin");
//            assertEquals(1, GroupInstanceNumMin);
//            int GroupInstanceNumMax = (int) getFieldValue("GroupInstanceNumMax");
//            assertEquals(1, GroupInstanceNumMax);
//            double GroupAccessDelayPercent = (double) getFieldValue("GroupAccessDelayPercent");
//            assertEquals(0, GroupAccessDelayPercent);
//            int GroupAccessDelayMin = (int) getFieldValue("GroupAccessDelayMin");
//            assertEquals(20, GroupAccessDelayMin);
//            int GroupAccessDelayMax = (int) getFieldValue("GroupAccessDelayMax");
//            assertEquals(30, GroupAccessDelayMax);
//            double GroupEdgePercent = (double) getFieldValue("GroupEdgePercent");
//            assertEquals(0, GroupEdgePercent);
//            int GroupEdgeIsDirected = (int) getFieldValue("GroupEdgeIsDirected");
//            assertEquals(0, GroupEdgeIsDirected);
//            double GroupBwPercent = (double) getFieldValue("GroupBwPercent");
//            assertEquals(1, GroupBwPercent);
//            int GroupBwMin = (int) getFieldValue("GroupBwMin");
//            assertEquals(10, GroupBwMin);
//            int GroupBwMax = (int) getFieldValue("GroupBwMax");
//            assertEquals(200, GroupBwMax);
//            double GroupDelayPercent = (double) getFieldValue("GroupDelayPercent");
//            assertEquals(1, GroupDelayPercent);
//            int GroupDelayMin = (int) getFieldValue("GroupDelayMin");
//            assertEquals(100, GroupDelayMin);
//            int GroupDelayMax = (int) getFieldValue("GroupDelayMax");
//            assertEquals(1000, GroupDelayMax);
//            int GroupRetryTimesMin = (int) getFieldValue("GroupRetryTimesMin");
//            assertEquals(3, GroupRetryTimesMin);
//            int GroupRetryTimesMax = (int) getFieldValue("GroupRetryTimesMax");
//            assertEquals(3, GroupRetryTimesMax);
//            int InstanceCpuNumMin = (int) getFieldValue("InstanceCpuNumMin");
//            assertEquals(1, InstanceCpuNumMin);
//            int InstanceCpuNumMax = (int) getFieldValue("InstanceCpuNumMax");
//            assertEquals(1, InstanceCpuNumMax);
//            int InstanceRamNumMin = (int) getFieldValue("InstanceRamNumMin");
//            assertEquals(1, InstanceRamNumMin);
//            int InstanceRamNumMax = (int) getFieldValue("InstanceRamNumMax");
//            assertEquals(1, InstanceRamNumMax);
//            int InstanceStorageNumMin = (int) getFieldValue("InstanceStorageNumMin");
//            assertEquals(1, InstanceStorageNumMin);
//            int InstanceStorageNumMax = (int) getFieldValue("InstanceStorageNumMax");
//            assertEquals(1, InstanceStorageNumMax);
//            int InstanceBwNumMin = (int) getFieldValue("InstanceBwNumMin");
//            assertEquals(1, InstanceBwNumMin);
//            int InstanceBwNumMax = (int) getFieldValue("InstanceBwNumMax");
//            assertEquals(1, InstanceBwNumMax);
//            int InstanceLifeTimeMin = (int) getFieldValue("InstanceLifeTimeMin");
//            assertEquals(495, InstanceLifeTimeMin);
//            int InstanceLifeTimeMax = (int) getFieldValue("InstanceLifeTimeMax");
//            assertEquals(495, InstanceLifeTimeMax);
//            int InstanceRetryTimesMin = (int) getFieldValue("InstanceRetryTimesMin");
//            assertEquals(3, InstanceRetryTimesMin);
//            int InstanceRetryTimesMax = (int) getFieldValue("InstanceRetryTimesMax");
//            assertEquals(3, InstanceRetryTimesMax);
//            System.out.println(RequestPerNumMin);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    @Test
//    public void testGenerateOnceUserRequests() {
//        Map<Integer, List<UserRequest>> userRequests = userRequestManager.generateOnceUserRequests();
//        int[] exceptDstDataCenterIds = {0};
//        int[] actualDstDataCenterIds = userRequests.keySet().stream().mapToInt(Integer::intValue).toArray();
//        assertArrayEquals(exceptDstDataCenterIds, actualDstDataCenterIds);
//        //TODO 继续测试对比其他的属性
//    }
//
//    private Object getFieldValue(String fieldName) throws IllegalAccessException {
//        for (Field field : fields) {
//            if (field.getName().equals(fieldName)) {
//                return field.get(userRequestManager);
//            }
//        }
//        return null;
//    }
//}
