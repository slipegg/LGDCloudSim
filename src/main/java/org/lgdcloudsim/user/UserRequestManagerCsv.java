package org.lgdcloudsim.user;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.lgdcloudsim.request.*;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * UserRequestManagerCsv is a class that implements the @{@link UserRequestManager} interface.
 * It can randomly generate user requests from a csv file.
 * <p>The csv file should contain the following parameters:</p>
 * <ul>
 *     <li>DcDistribution: area distribution of user requests received by the data center.
 *     For example: {"1":{"shanghai":0.4,"beijing":0.1},"2":{"beijing":0.5}}.
 *     It means that 40% of users are from Shanghai and sent to data center 1, 10% of users are from Beijing and sent to data center 1,
 *     and 50% of users are from Beijing and sent to data center 2.</li>
 *     <li>RequestPerNumMin: the minimum number of requests per time.</li>
 *     <li>RequestPerNumMax: the maximum number of requests per time.</li>
 *     <li>RequestTimeIntervalMin: the minimum time interval (ms) between two requests.</li>
 *     <li>RequestTimeIntervalMax: the maximum time interval (ms) between two requests.</li>
 *     <li>RequestTimes: the number of times to send requests.</li>
 *     <li>ScheduleDelayLimitMin: the minimum schedule delay limit (ms) of the requests.</li>
 *     <li>ScheduleDelayLimitMax: the maximum schedule delay limit (ms) of the requests.</li>
 *     <li>RequestGroupNumMin: the minimum number of instance groups in a request.</li>
 *     <li>RequestGroupNumMax: the maximum number of instance groups in a request.</li>
 *     <li>GroupInstanceNumMin: the minimum number of instances in an instance group.</li>
 *     <li>GroupInstanceNumMax: the maximum number of instances in an instance group.</li>
 *     <li>GroupAccessDelayPercent: the percentage of instance groups that have access delay. (Range: [0,1]) </li>
 *     <li>GroupAccessDelayMin: the minimum access delay (ms) of the instance groups.</li>
 *     <li>GroupAccessDelayMax: the maximum access delay (ms) of the instance groups.</li>
 *     <li>GroupEdgePercent: the percentage that an instance group has edge constraints with other instance groups. (Range: [0,1]) </li>
 *     <li>GroupEdgeIsDirected: whether the edges between instance groups are directed. (1: true, 0: false) </li>
 *     <li>GroupBwPercent: the percentage that an edge constraint contains a bandwidth constraint. (Range: [0,1]) </li>
 *     <li>GroupBwMin: the minimum bandwidth requirement in the edge constraints.</li>
 *     <li>GroupBwMax: the maximum bandwidth requirement in the edge constraints.</li>
 *     <li>GroupDelayPercent: the percentage that an edge constraint contains a link delay constraint (ms). (Range: [0,1]) </li>
 *     <li>GroupDelayMin: the minimum link delay (ms) requirement in the edge constraints.</li>
 *     <li>GroupDelayMax: the maximum link delay (ms) requirement in the edge constraints.</li>
 *     <li>GroupRetryTimesMin: the minimum retry times of the instance groups.</li>
 *     <li>GroupRetryTimesMax: the maximum retry times of the instance groups.</li>
 *     <li>InstanceCpuNumMin: the minimum number of cpu core in an instance.</li>
 *     <li>InstanceCpuNumMax: the maximum number of cpu core in an instance.</li>
 *     <li>InstanceRamNumMin: the minimum number (GB) of ram in an instance.</li>
 *     <li>InstanceRamNumMax: the maximum number (GB) of ram in an instance.</li>
 *     <li>InstanceStorageNumMin: the minimum number (GB) of storage in an instance.</li>
 *     <li>InstanceStorageNumMax: the maximum number of (GB) storage in an instance.</li>
 *     <li>InstanceBwNumMin: the minimum number (Mbps) of bw in an instance.</li>
 *     <li>InstanceBwNumMax: the maximum number (Mbps) of bw in an instance.</li>
 *     <li>InstanceLifeTimeMin: the minimum lifecycle (ms) of an instance.
 *     When InstanceLifeTimeMin==-1 and InstanceLifeTimeMax==-1, the instance life cycle is ♾️. </li>
 *     <li>InstanceLifeTimeMax: the maximum lifecycle (ms) of an instance.
 *     When InstanceLifeTimeMin==-1 and InstanceLifeTimeMax==-1, the instance life cycle is ♾️. </li>
 *     <li>InstanceRetryTimesMin: the minimum retry times of an instance.</li>
 *     <li>InstanceRetryTimesMax: the maximum retry times of an instance.</li>
 * </ul>
 *
 * @author Anonymous
 * @since LGDCloudSim 1.0
 */
public class UserRequestManagerCsv implements UserRequestManager {
    /**
     * The csv file name.
     */
    private final String fileName;

    /**
     * The area distribution of user requests received by the data center.
     */
    private TreeMap<Integer, TreeMap<String, Double>> DcAreaDistribution = new TreeMap<>();

    /**
     * The sum of the area distribution of user requests.
     */
    private double DcAreaDistributionSum = 0;

    /**
     * The minimum number of requests per time.
     */
    private int RequestPerNumMin = -2;

    /**
     * The maximum number of requests per time.
     */
    private int RequestPerNumMax = -2;
    /**
     * The number of requests per time.
     * If you don't want to use random values, you can initialize it to use fixed values
     */
    private int RequestPerNum = -2;

    /**
     * The minimum time interval (ms) between two requests.
     */
    private int RequestTimeIntervalMin = -2;

    /**
     * The maximum time interval (ms) between two requests.
     */
    private int RequestTimeIntervalMax = -2;

    /**
     * The time interval (ms) between two requests.
     * If you don't want to use random values, you can initialize it to use fixed values
     */
    private int RequestTimeInterval = -2;

    /**
     * The number of times to send requests.
     */
    private int RequestTimes = -2;

    /**
     * The minimum schedule delay limit (ms) of the requests.
     */
    private double ScheduleDelayLimitMin = -2;

    /**
     * The maximum schedule delay limit (ms) of the requests.
     */
    private double ScheduleDelayLimitMax = -2;

    /**
     * The schedule delay limit (ms) of the requests.
     * If you don't want to use random values, you can initialize it to use fixed values
     */
    private double ScheduleDelayLimit = -2;

    /**
     * The minimum number of instance groups in a request.
     */
    private int RequestGroupNumMin = -2;

    /**
     * The maximum number of instance groups in a request.
     */
    private int RequestGroupNumMax = -2;

    /**
     * The number of instance groups in a request.
     * If you don't want to use random values, you can initialize it to use fixed values
     */
    private int RequestGroupNum = -2;

    /**
     * The minimum number of instances in an instance group.
     */
    private int GroupInstanceNumMin = -2;

    /**
     * The maximum number of instances in an instance group.
     */
    private int GroupInstanceNumMax = -2;

    /**
     * The number of instances in an instance group.
     * If you don't want to use random values, you can initialize it to use fixed values
     */
    private int GroupInstanceNum = -2;

    /**
     * The percentage of instance groups that have access delay.
     */
    private double GroupAccessDelayPercent = -2;

    /**
     * The minimum access delay (ms) of the instance groups.
     */
    private int GroupAccessDelayMin = -2;

    /**
     * The maximum access delay (ms) of the instance groups.
     */
    private int GroupAccessDelayMax = -2;

    /**
     * The access delay (ms) of the instance groups.
     * If you don't want to use random values, you can initialize it to use fixed values
     */
    private int GroupAccessDelay = -2;

    /**
     * The percentage that an instance group has edge constraints with other instance groups.
     */
    private double GroupEdgePercent = -2;

    /**
     * Whether the edges between instance groups are directed.
     */
    private int GroupEdgeIsDirected = -2;

    /**
     * The percentage that an edge constraint contains a bandwidth constraint.
     */
    private double GroupBwPercent = -2;

    /**
     * The minimum bandwidth requirement in the edge constraints.
     */
    private int GroupBwMin = -2;

    /**
     * The maximum bandwidth requirement in the edge constraints.
     */
    private int GroupBwMax = -2;

    /**
     * The bandwidth requirement in the edge constraints.
     * If you don't want to use random values, you can initialize it to use fixed values
     */
    private int GroupBw = -2;

    /**
     * The percentage that an edge constraint contains a link delay constraint (ms).
     */
    private double GroupDelayPercent = -2;

    /**
     * The minimum link delay (ms) requirement in the edge constraints.
     */
    private int GroupDelayMin = -2;

    /**
     * The maximum link delay (ms) requirement in the edge constraints.
     */
    private int GroupDelayMax = -2;

    /**
     * The link delay (ms) requirement in the edge constraints.
     * If you don't want to use random values, you can initialize it to use fixed values
     */
    private int GroupDelay = -2;

    /**
     * The minimum retry times of the instance groups.
     */
    private int GroupRetryTimesMin = -2;

    /**
     * The maximum retry times of the instance groups.
     */
    private int GroupRetryTimesMax = -2;

    /**
     * The retry times of the instance groups.
     * If you don't want to use random values, you can initialize it to use fixed values
     */
    private int GroupRetryTimes = -2;

    /**
     * The minimum number of cpu core in an instance.
     */
    private int InstanceCpuNumMin = -2;

    /**
     * The maximum number of cpu core in an instance.
     */
    private int InstanceCpuNumMax = -2;

    /**
     * The number of cpu core in an instance.
     * If you don't want to use random values, you can initialize it to use fixed values
     */
    private int InstanceCpuNum = -2;

    /**
     * The minimum number (GB) of ram in an instance.
     */
    private int InstanceRamNumMin = -2;

    /**
     * The maximum number (GB) of ram in an instance.
     */
    private int InstanceRamNumMax = -2;

    /**
     * The number (GB) of ram in an instance.
     * If you don't want to use random values, you can initialize it to use fixed values
     */
    private int InstanceRamNum = -2;

    /**
     * The minimum number (GB) of storage in an instance.
     */
    private int InstanceStorageNumMin = -2;

    /**
     * The maximum number of (GB) storage in an instance.
     */
    private int InstanceStorageNumMax = -2;

    /**
     * The number (GB) of storage in an instance.
     * If you don't want to use random values, you can initialize it to use fixed values
     */
    private int InstanceStorageNum = -2;

    /**
     * The minimum number (Mbps) of bw in an instance.
     */
    private int InstanceBwNumMin = -2;

    /**
     * The maximum number (Mbps) of bw in an instance.
     */
    private int InstanceBwNumMax = -2;

    /**
     * The number (Mbps) of bw in an instance.
     * If you don't want to use random values, you can initialize it to use fixed values
     */
    private int InstanceBwNum = -2;

    /**
     * The minimum lifecycle (ms) of an instance.
     */
    private int InstanceLifeTimeMin = -2;

    /**
     * The maximum lifecycle (ms) of an instance.
     */
    private int InstanceLifeTimeMax = -2;

    /**
     * The lifecycle (ms) of an instance.
     * If you don't want to use random values, you can initialize it to use fixed values
     */
    private int InstanceLifeTime = -2;

    /**
     * The minimum retry times of an instance.
     */
    private int InstanceRetryTimesMin = -2;

    /**
     * The maximum retry times of an instance.
     */
    private int InstanceRetryTimesMax = -2;

    /**
     * The retry times of an instance.
     * If you don't want to use random values, you can initialize it to use fixed values
     */
    private int InstanceRetryTimes = -2;

    /**
     * The id of the next instance.
     */
    private static int instanceId = 0;

    /**
     * The id of the next instance group.
     */
    private static int instanceGroupId = 0;

    /**
     * The id of the next user request.
     */
    private static int userRequestId = 0;

    /**
     * The random object.
     */
    private final Random random;

    /**
     * The next send time.
     */
    @Getter
    private double nextSendTime = 0;

    /**
     * The number of times the user requests have been sent.
     * If the number of times the user requests have been sent exceeds the RequestTimes, the user requests will not be sent again.
     */
    private int sendTimes = 0;

    /**
     * Construct a user request manager with the csv file name.
     *
     * @param fileName the csv file name.
     */
    public UserRequestManagerCsv(String fileName) {
        random = new Random();
        this.fileName = fileName;

        // Pass the path to the CSV file to the File object
        File csvFile = new File(this.fileName);

        // create the CSVFormat object, specify the format of the CSV file
        CSVFormat csvFormat = CSVFormat.DEFAULT.withFirstRecordAsHeader();

        try (CSVParser csvParser = new CSVParser(new FileReader(csvFile), csvFormat)) {
            for (CSVRecord csvRecord : csvParser) {
                String title = csvRecord.get(0);
                switch (title) {
                    case "DcDistribution" -> stringToDistributionMap(csvRecord.get(1));
                    case "RequestPerNumMin" -> this.RequestPerNumMin = Integer.parseInt(csvRecord.get(1));
                    case "RequestPerNumMax" -> this.RequestPerNumMax = Integer.parseInt(csvRecord.get(1));
                    case "RequestPerNum" -> this.RequestPerNum = Integer.parseInt(csvRecord.get(1));
                    case "RequestTimeIntervalMin" -> this.RequestTimeIntervalMin = Integer.parseInt(csvRecord.get(1));
                    case "RequestTimeIntervalMax" -> this.RequestTimeIntervalMax = Integer.parseInt(csvRecord.get(1));
                    case "RequestTimeInterval" -> this.RequestTimeInterval = Integer.parseInt(csvRecord.get(1));
                    case "RequestTimes" -> this.RequestTimes = Integer.parseInt(csvRecord.get(1));
                    case "ScheduleDelayLimitMin" -> this.ScheduleDelayLimitMin = Double.parseDouble(csvRecord.get(1));
                    case "ScheduleDelayLimitMax" -> this.ScheduleDelayLimitMax = Double.parseDouble(csvRecord.get(1));
                    case "ScheduleDelayLimit" -> this.ScheduleDelayLimit = Double.parseDouble(csvRecord.get(1));
                    case "RequestGroupNumMin" -> this.RequestGroupNumMin = Integer.parseInt(csvRecord.get(1));
                    case "RequestGroupNumMax" -> this.RequestGroupNumMax = Integer.parseInt(csvRecord.get(1));
                    case "RequestGroupNum" -> this.RequestGroupNum = Integer.parseInt(csvRecord.get(1));
                    case "GroupInstanceNumMin" -> this.GroupInstanceNumMin = Integer.parseInt(csvRecord.get(1));
                    case "GroupInstanceNumMax" -> this.GroupInstanceNumMax = Integer.parseInt(csvRecord.get(1));
                    case "GroupInstanceNum" -> this.GroupInstanceNum = Integer.parseInt(csvRecord.get(1));
                    case "GroupAccessDelayPercent" ->
                            this.GroupAccessDelayPercent = Double.parseDouble(csvRecord.get(1));
                    case "GroupAccessDelayMin" -> this.GroupAccessDelayMin = Integer.parseInt(csvRecord.get(1));
                    case "GroupAccessDelayMax" -> this.GroupAccessDelayMax = Integer.parseInt(csvRecord.get(1));
                    case "GroupAccessDelay" -> this.GroupAccessDelay = Integer.parseInt(csvRecord.get(1));
                    case "GroupEdgePercent" -> this.GroupEdgePercent = Double.parseDouble(csvRecord.get(1));
                    case "GroupEdgeIsDirected" -> this.GroupEdgeIsDirected = Integer.parseInt(csvRecord.get(1));
                    case "GroupBwPercent" -> this.GroupBwPercent = Double.parseDouble(csvRecord.get(1));
                    case "GroupBwMin" -> this.GroupBwMin = Integer.parseInt(csvRecord.get(1));
                    case "GroupBwMax" -> this.GroupBwMax = Integer.parseInt(csvRecord.get(1));
                    case "GroupBw" -> this.GroupBw = Integer.parseInt(csvRecord.get(1));
                    case "GroupDelayPercent" -> this.GroupDelayPercent = Double.parseDouble(csvRecord.get(1));
                    case "GroupDelayMin" -> this.GroupDelayMin = Integer.parseInt(csvRecord.get(1));
                    case "GroupDelayMax" -> this.GroupDelayMax = Integer.parseInt(csvRecord.get(1));
                    case "GroupDelay" -> this.GroupDelay = Integer.parseInt(csvRecord.get(1));
                    case "GroupRetryTimesMin" -> this.GroupRetryTimesMin = Integer.parseInt(csvRecord.get(1));
                    case "GroupRetryTimesMax" -> this.GroupRetryTimesMax = Integer.parseInt(csvRecord.get(1));
                    case "GroupRetryTimes" -> this.GroupRetryTimes = Integer.parseInt(csvRecord.get(1));
                    case "InstanceCpuNumMin" -> this.InstanceCpuNumMin = Integer.parseInt(csvRecord.get(1));
                    case "InstanceCpuNumMax" -> this.InstanceCpuNumMax = Integer.parseInt(csvRecord.get(1));
                    case "InstanceCpuNum" -> this.InstanceCpuNum = Integer.parseInt(csvRecord.get(1));
                    case "InstanceRamNumMin" -> this.InstanceRamNumMin = Integer.parseInt(csvRecord.get(1));
                    case "InstanceRamNumMax" -> this.InstanceRamNumMax = Integer.parseInt(csvRecord.get(1));
                    case "InstanceRamNum" -> this.InstanceRamNum = Integer.parseInt(csvRecord.get(1));
                    case "InstanceStorageNumMin" -> this.InstanceStorageNumMin = Integer.parseInt(csvRecord.get(1));
                    case "InstanceStorageNumMax" -> this.InstanceStorageNumMax = Integer.parseInt(csvRecord.get(1));
                    case "InstanceStorageNum" -> this.InstanceStorageNum = Integer.parseInt(csvRecord.get(1));
                    case "InstanceBwNumMin" -> this.InstanceBwNumMin = Integer.parseInt(csvRecord.get(1));
                    case "InstanceBwNumMax" -> this.InstanceBwNumMax = Integer.parseInt(csvRecord.get(1));
                    case "InstanceBwNum" -> this.InstanceBwNum = Integer.parseInt(csvRecord.get(1));
                    case "InstanceLifeTimeMin" -> this.InstanceLifeTimeMin = Integer.parseInt(csvRecord.get(1));
                    case "InstanceLifeTimeMax" -> this.InstanceLifeTimeMax = Integer.parseInt(csvRecord.get(1));
                    case "InstanceLifeTime" -> this.InstanceLifeTime = Integer.parseInt(csvRecord.get(1));
                    case "InstanceRetryTimesMin" -> this.InstanceRetryTimesMin = Integer.parseInt(csvRecord.get(1));
                    case "InstanceRetryTimesMax" -> this.InstanceRetryTimesMax = Integer.parseInt(csvRecord.get(1));
                    case "InstanceRetryTimes" -> this.InstanceRetryTimes = Integer.parseInt(csvRecord.get(1));
                    default -> {
                        LOGGER.warn("The parameter name {} is not correct, please check the parameter name in the csv file", title);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Generate a batch of user requests after initialization via csv file.
     *
     * @return a map of user requests, the key is the data center id which the user requests are sent to, and the value is the list of user requests.
     */
    @Override
    public Map<Integer, List<UserRequest>> generateOnceUserRequests() {
        Map<Integer, List<UserRequest>> userRequestsMap = new HashMap<>();
        if (sendTimes >= RequestTimes) {
            return userRequestsMap;
        }

        List<UserRequest> userRequests = new ArrayList<>();
        int requestNum = getExpectedValue(RequestPerNum, RequestPerNumMin, RequestPerNumMax);
        for (int i = 0; i < requestNum; i++) {
            UserRequest userRequest = generateAUserRequest();
            int belongDatacenterId = -1;
            String belongArea = null;

            double randomDouble = random.nextDouble(DcAreaDistributionSum);
            for (Map.Entry<Integer, TreeMap<String, Double>> entry : DcAreaDistribution.entrySet()) {
                for (Map.Entry<String, Double> innerEntry : entry.getValue().entrySet()) {
                    if (randomDouble < innerEntry.getValue()) {
                        belongDatacenterId = entry.getKey();
                        belongArea = innerEntry.getKey();
                        break;
                    }
                }
                if (belongDatacenterId != -1) {
                    break;
                }
            }

            userRequest.setBelongDatacenterId(belongDatacenterId);
            userRequest.setArea(belongArea);
            userRequest.setSubmitTime(nextSendTime);

            double scheduleDelayLimit = getExpectedValue(ScheduleDelayLimit, ScheduleDelayLimitMin, ScheduleDelayLimitMax);
            userRequest.setScheduleDelayLimit(scheduleDelayLimit);

            userRequests.add(userRequest);
            if (!userRequestsMap.containsKey(belongDatacenterId)) {
                userRequestsMap.put(belongDatacenterId, new ArrayList<>());
            }
            userRequestsMap.get(belongDatacenterId).add(userRequest);
        }

        double nextSendInterval = getExpectedValue(RequestTimeInterval, RequestTimeIntervalMin, RequestTimeIntervalMax);
        nextSendTime += BigDecimal.valueOf(nextSendInterval).setScale(3, RoundingMode.HALF_UP).doubleValue();
        sendTimes += 1;
        return userRequestsMap;
    }

    /**
     * Generate an instance.
     *
     * @return an instance.
     */
    private Instance generateAnInstance() {
        int cpuNum = getExpectedValue(InstanceCpuNum, InstanceCpuNumMin, InstanceCpuNumMax);

        int ramNum = getExpectedValue(InstanceRamNum, InstanceRamNumMin, InstanceRamNumMax);

        int storageNum = getExpectedValue(InstanceStorageNum, InstanceStorageNumMin, InstanceStorageNumMax);

        int bwNum = getExpectedValue(InstanceBwNum, InstanceBwNumMin, InstanceBwNumMax);

        int lifeTime = generateInstanceLifeTime();
        Instance instance = new InstanceSimple(instanceId++, cpuNum, ramNum, storageNum, bwNum, lifeTime);

        int retryTimes = getExpectedValue(InstanceRetryTimes, InstanceRetryTimesMin, InstanceRetryTimesMax);
        instance.setRetryMaxNum(retryTimes);

        return instance;
    }

    /**
     * Generate the lifecycle of an instance.
     *
     * @return the lifecycle of an instance.
     */
    private int generateInstanceLifeTime() {
        if (InstanceLifeTimeMax == -1 || InstanceLifeTimeMin == -1) {
            return -1;
        }
        if (InstanceLifeTime != -2) {
            return InstanceLifeTime;
        }

        int lifeGap = InstanceLifeTimeMax - InstanceLifeTimeMin;
        int granularity = 10;
        int lifeSmallGapNum = lifeGap / granularity;

        return random.nextInt(lifeSmallGapNum + 1) * granularity + InstanceLifeTimeMin;
    }

    /**
     * Generate an instance group.
     *
     * @return an instance group.
     */
    private InstanceGroup generateAnInstanceGroup() {
        int instanceNum = getExpectedValue(GroupInstanceNum, GroupInstanceNumMin, GroupInstanceNumMax);
        List<Instance> instanceList = new ArrayList<>();
        for (int i = 0; i < instanceNum; i++) {
            instanceList.add(generateAnInstance());
        }
        InstanceGroup instanceGroup = new InstanceGroupSimple(instanceGroupId++, instanceList);
        if (GroupAccessDelayPercent != 0 && GroupAccessDelayPercent != -2 && random.nextDouble() < GroupAccessDelayPercent) {
            double accessLatency = getExpectedValue(GroupAccessDelay, GroupAccessDelayMin, GroupAccessDelayMax);
            instanceGroup.setAccessLatency(accessLatency);
        }

        int retryTimes = Math.max(1, getExpectedValue(GroupRetryTimes, GroupRetryTimesMin, GroupRetryTimesMax));
        instanceGroup.setRetryMaxNum(retryTimes);
        return instanceGroup;
    }

    /**
     * Generate an instance group graph.
     *
     * @param instanceGroups the instance groups.
     * @return an instance group graph.
     */
    private InstanceGroupGraph generateAnInstanceGroupGraph(List<InstanceGroup> instanceGroups) {
        InstanceGroupGraph instanceGroupGraph = new InstanceGroupGraphSimple(false);
        if (GroupEdgePercent == 0 || GroupEdgePercent == -2 || instanceGroups.size() <= 1) {
            return instanceGroupGraph;
        }

        instanceGroupGraph.setDirected(GroupEdgeIsDirected == 1);
        for (int i = 0; i < instanceGroups.size(); i++) {
            int j = (GroupEdgeIsDirected == 1) ? 0 : i + 1;
            for (; j < instanceGroups.size(); j++) {
                if (random.nextDouble() < GroupEdgePercent && j != i) {
                    double bw = 0;
                    if (random.nextDouble() < GroupBwPercent) {
                        bw = getExpectedValue(GroupBw, GroupBwMin, GroupBwMax);
                        bw = BigDecimal.valueOf(bw).setScale(2, RoundingMode.HALF_UP).doubleValue();
                    }
                    double delay = 0;
                    if (random.nextDouble() < GroupDelayPercent) {
                        delay = getExpectedValue(GroupDelay, GroupDelayMin, GroupDelayMax);
                        delay = BigDecimal.valueOf(delay).setScale(2, RoundingMode.HALF_UP).doubleValue();
                    }

                    instanceGroupGraph.addEdge(instanceGroups.get(i), instanceGroups.get(j), delay, bw);
                }
            }
        }
        return instanceGroupGraph;
    }

    /**
     * Generate a user request.
     *
     * @return a user request.
     */
    private UserRequest generateAUserRequest() {
        int groupNum = getExpectedValue(RequestGroupNum, RequestGroupNumMin, RequestGroupNumMax);
        List<InstanceGroup> instanceGroups = new ArrayList<>();
        for (int i = 0; i < groupNum; i++) {
            instanceGroups.add(generateAnInstanceGroup());
        }
        InstanceGroupGraph instanceGroupGraph = generateAnInstanceGroupGraph(instanceGroups);
        return new UserRequestSimple(userRequestId++, instanceGroups, instanceGroupGraph);
    }

    //    private <T extends Number & Comparable<T>> T getValue(T fixed, T min, T max) {
//        if (fixed.compareTo(min) != -2) {
//            return fixed;
//        } else {
//            // Generate a random value between min and max (inclusive)
//            return (T) Integer.valueOf(random.nextInt(max.intValue() - min.intValue() + 1) + min.intValue());
//        }
//    }
    private int getExpectedValue(int fixed, int min, int max) {
        if (fixed != -2) {
            return fixed;
        } else {
            // Generate a random value between min and max (inclusive)
            return random.nextInt(max - min + 1) + min;
        }
    }

    private double getExpectedValue(double fixed, double min, double max) {
        if (fixed != -2) {
            return fixed;
        } else if (min == -2 && max == -2) {
            // Generate a random value between min and max (inclusive)
            return random.nextDouble() * (max - min) + min;
        } else {
            return -1;
        }
    }

    /**
     * Convert the JSON string to a distribution map.
     *
     * @param jsonString the JSON string.
     * @throws IOException if the JSON string is not correct.
     */
    private void stringToDistributionMap(String jsonString) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(jsonString);
        for (Iterator<Map.Entry<String, JsonNode>> it = rootNode.fields(); it.hasNext(); ) {
            Map.Entry<String, JsonNode> outerEntry = it.next();
            int outerKey = Integer.parseInt(outerEntry.getKey());
            JsonNode innerNode = outerEntry.getValue();

            // 如果是 JSON 对象
            TreeMap<String, Double> innerMap = new TreeMap<>();
            for (Iterator<Map.Entry<String, JsonNode>> innerIt = innerNode.fields(); innerIt.hasNext(); ) {
                Map.Entry<String, JsonNode> entry = innerIt.next();
                String innerKey = entry.getKey();
                double innerValue = entry.getValue().asDouble();
                DcAreaDistributionSum += innerValue;
                innerMap.put(innerKey, DcAreaDistributionSum);
            }

            DcAreaDistribution.put(outerKey, innerMap);
        }
    }
}
