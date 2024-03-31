package org.lgdcloudsim.user;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.lgdcloudsim.request.*;
import org.lgdcloudsim.util.GoogleTraceRequestFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * UserRequestManagerGoogleTrace is an implementation of the {@link UserRequestManager} interface.
 * It is responsible for generating user requests from the<a href="https://github.com/google/cluster-data">the Google cluster trace</a>
 * We have organized the 10,000 user request data in each cluster data into csv files.
 * The contents contained in the csv file are: time,user,collection_id,instance_index,cpus,ram,collection_type
 * Those with the same user form a user request, and those with the same collection_id form an instance group in the user request,
 * and each instance_index represents an instance.
 * Since the values of CPU and Ram have been normalized, you need to set the maximum value yourself to make the CPU and Ram a reasonable size.
 * The storage and bandwidth values are not provided by the Google cluster trace, so we need to randomly generate them ourselves.
 * It is recommended to generate some smaller values so as not to affect scheduling.
 * The life cycle of the instance also needs to be set by yourself.
 *
 * @author Anonymous
 * @since LGDCloudSim 1.0
 */
public class UserRequestManagerGoogleTrace implements UserRequestManager {
    /**
     * The id of the next user request.
     */
    private static int userRequestId = 0;

    /**
     * The id of the next instance group.
     */
    private static int instanceGroupId = 0;

    /**
     * The id of the next instance.
     */
    private static int instanceId = 0;

    /**
     * A class to record the instance information, including the cpu, ram and type of the instance in the Google trace csv file.
     */
    @Getter
    @Setter
    public class InstanceGoogleTrace {
        /**
         * The number of cpu cores of the instance.
         */
        private double cpu;
        /**
         * The ram  of the instance.
         */
        private double ram;

        /**
         * The type of the instance.
         */
        boolean type;

        /**
         * Construct an InstanceGoogleTrace with the cpu, ram and type of the instance.
         *
         * @param cpu  the number of cpu cores of the instance.
         * @param ram  the ram of the instance.
         * @param type the type of the instance.
         */
        public InstanceGoogleTrace(double cpu, double ram, boolean type) {
            this.cpu = cpu;
            this.ram = ram;
            this.type = type;
        }
    }

    /**
     * A class used to parse CSV file content and generate user requests.
     */
    public class UserRequestGoogleTrace {
        /**
         * The submission time of the user request.
         */
        private double submitTime;

        /**
         * The username of the user request.
         */
        private String userName;

        /**
         * A map to record the user request information, including the collection id and the instance index.
         */
        private Map<Integer, Map<Integer, InstanceGoogleTrace>> userRequestMap = new HashMap<Integer, Map<Integer, InstanceGoogleTrace>>();

        /**
         * A flag to record whether the user request is the first to be added.
         */
        private boolean isFirstAdd = true;

        /**
         * Add a record to the user request.
         * @param csvRecord the record of the csv file.
         */
        public void addRecord(CSVRecord csvRecord) {
            //The entries within each user request in CSV have been sorted by time,
            // so the first one is the earliest time, which is used as the submission time
            if (isFirstAdd) {
                userName = csvRecord.get(1);
                submitTime = Double.parseDouble(csvRecord.get(0)) / 1000;
                isFirstAdd = false;
            }
            int collectionId = (int) (Long.parseLong(csvRecord.get(2)) % 1000000000);
            int instanceIndex = Integer.parseInt(csvRecord.get(3));
            double cpu = Double.parseDouble(csvRecord.get(4));
            double ram = Double.parseDouble(csvRecord.get(5));
            boolean type = csvRecord.get(6).equals("0");
            Map<Integer, InstanceGoogleTrace> instanceGoogleTraceMap = (Map<Integer, InstanceGoogleTrace>) userRequestMap.get(collectionId);
            if (instanceGoogleTraceMap == null) {
                instanceGoogleTraceMap = new HashMap<>();
                userRequestMap.put(collectionId, instanceGoogleTraceMap);
            }
            InstanceGoogleTrace instanceGoogleTrace = new InstanceGoogleTrace(cpu, ram, type);
            instanceGoogleTraceMap.put(instanceIndex, instanceGoogleTrace);
        }

        /**
         * Generate a user request.
         * @param datacenterId the id of the data center to which the user request is sent.
         * @return the user request.
         */
        public UserRequest generateUserRequest(int datacenterId) {
            List<InstanceGroup> instanceGroups = new ArrayList<>();
            for (Map.Entry<Integer, Map<Integer, InstanceGoogleTrace>> entry : userRequestMap.entrySet()) {
                Map<Integer, InstanceGoogleTrace> instanceGoogleTraceMap = entry.getValue();
                InstanceGroup instanceGroup = generateAnInstanceGroup(instanceGoogleTraceMap);
                instanceGroups.add(instanceGroup);
            }

            InstanceGroupGraph instanceGroupGraph;
            if (edgePercentage != 0) {
                instanceGroupGraph = generateAnInstanceGroupGraph(instanceGroups);
            } else {
                instanceGroupGraph = new InstanceGroupGraphSimple(false);
            }

            UserRequest userRequest = new UserRequestSimple(UserRequestManagerGoogleTrace.userRequestId++, instanceGroups, instanceGroupGraph);
            userRequest.setSubmitTime(submitTime + lastSubmitTimeMap.get(datacenterId));
            userRequest.setBelongDatacenterId(datacenterId);
            userRequest.setArea(googleTraceRequestFiles.get(datacenterId).getArea());
            return userRequest;
        }

        /**
         * Generate an instance.
         *
         * @param instanceGoogleTrace the instance information in the Google trace csv file.
         * @return the instance.
         */
        private Instance generateAnInstance(InstanceGoogleTrace instanceGoogleTrace) {
            int lifeTime;
            if (lifeTimeMean == -1) {
                lifeTime = -1;
            } else {
                lifeTime = Math.max(1, (((int) (random.nextGaussian()) / 10 * 10 * lifeTimeStd + lifeTimeMean)));
            }
            Instance instance = new InstanceSimple(UserRequestManagerGoogleTrace.instanceId++, (int) (instanceGoogleTrace.cpu * maxCpuCapacity), (int) (instanceGoogleTrace.ram * maxRamCapacity), storageCapacity, bwCapacity, lifeTime);
            instance.setRetryMaxNum(instanceRetryTimes);
            return instance;
        }

        /**
         * Generate an instance group.
         *
         * @param instanceGoogleTraceMap the instance information in the Google trace csv file.
         * @return the instance group.
         */
        private InstanceGroup generateAnInstanceGroup(Map<Integer, InstanceGoogleTrace> instanceGoogleTraceMap) {
            List<Instance> instances = new ArrayList<>();
            for (Map.Entry<Integer, InstanceGoogleTrace> entry : instanceGoogleTraceMap.entrySet()) {
                InstanceGoogleTrace instanceGoogleTrace = entry.getValue();
                Instance instance = generateAnInstance(instanceGoogleTrace);
                instances.add(instance);
            }
            InstanceGroup instanceGroup = new InstanceGroupSimple(UserRequestManagerGoogleTrace.instanceGroupId++, instances);
            instanceGroup.setRetryMaxNum(instanceGroupRetryTimes);
            double accessLatencyFlag = random.nextDouble();
            if (accessLatencyFlag <= accessLatencyPercentage) {
                double accessLatency = Math.max(0, random.nextGaussian() * accessLatencyStd + accessLatencyMean);
                instanceGroup.setAccessLatency(accessLatency);
            }
            return instanceGroup;
        }

        /**
         * Generate an instance group graph.
         * @param instanceGroups the instance groups.
         * @return the instance group graph.
         */
        private InstanceGroupGraph generateAnInstanceGroupGraph(List<InstanceGroup> instanceGroups) {
            InstanceGroupGraph instanceGroupGraph = new InstanceGroupGraphSimple(false);
            instanceGroupGraph.setDirected(isEdgeDirected);
            for (int i = 0; i < instanceGroups.size(); i++) {
                int j = (isEdgeDirected) ? 0 : i + 1;
                for (; j < instanceGroups.size(); j++) {
                    if (random.nextDouble() < edgePercentage && j != i) {
                        double bw = Math.max(0, random.nextGaussian() * edgeBwStd + edgeBwMean);
                        bw = BigDecimal.valueOf(bw).setScale(2, RoundingMode.HALF_UP).doubleValue();
                        double delay = Math.max(0, random.nextGaussian() * edgeDelayStd + edgeDelayMean);
                        instanceGroupGraph.addEdge(instanceGroups.get(i), instanceGroups.get(j), delay, bw);
                    }
                }
            }
            return instanceGroupGraph;
        }

    }

    /**
     * Logger for this class.
     */
    private Logger LOGGER = LoggerFactory.getLogger(UserRequestManagerGoogleTrace.class.getSimpleName());

    /**
     * A random number generator.
     */
    private Random random = new Random(0);

    /**
     * The maximum capacity of the cpu.
     */
    int maxCpuCapacity;

    /**
     * The maximum capacity of the ram.
     */
    int maxRamCapacity;

    /**
     * The storage capacity.
     */
    int storageCapacity;

    /**
     * The bandwidth capacity.
     */
    int bwCapacity;

    /**
     * The mean of the lifetime.
     */
    int lifeTimeMean;

    /**
     * The standard deviation of the lifetime.
     */
    int lifeTimeStd;

    /**
     * The number of times to retry the instance group.
     */
    int instanceGroupRetryTimes;

    /**
     * The number of times to retry the instance.
     */
    int instanceRetryTimes;

    /**
     * The percentage of instance groups that have access latency.
     */
    double accessLatencyPercentage;

    /**
     * The mean of the access latency.
     */
    double accessLatencyMean;

    /**
     * The standard deviation of the access latency.
     */
    double accessLatencyStd;

    /**
     * Whether the edges between instance groups are directed.
     */
    boolean isEdgeDirected;

    /**
     * The percentage that an instance group has edge constraints with other instance groups.
     */
    double edgePercentage;

    /**
     * The mean of the edge delay.
     */
    double edgeDelayMean;

    /**
     * The standard deviation of the edge delay.
     */
    double edgeDelayStd;

    /**
     * The mean of the edge bandwidth.
     */
    double edgeBwMean;

    /**
     * The standard deviation of the edge bandwidth.
     */
    double edgeBwStd;

    /**
     * The CSV format.
     */
    CSVFormat csvFormat;

    /**
     * A mapping of file indices to GoogleTraceRequestFile objects.
     */
    Map<Integer, GoogleTraceRequestFile> googleTraceRequestFiles;

    /**
     * A mapping of file indices to CSV records.
     */
    Map<Integer, CSVRecord> csvRecordMap = new HashMap<>();

    /**
     * A mapping of file indices to CSV record iterators.
     */
    Map<Integer, Iterator<CSVRecord>> csvIteratorMap = new HashMap<>();

    /**
     * A mapping of file indices to lists of the latest user requests.
     */
    Map<Integer, List<UserRequest>> latestUserRequestMap = new HashMap<>();

    /**
     * A mapping of file indices to row numbers.
     */
    Map<Integer, Integer> rowMap = new HashMap<>();

    /**
     * A mapping of file indices to the last submit time.
     */
    Map<Integer, Double> lastSubmitTimeMap = new HashMap<>();

    /**
     * Construct a user request manager. It only generates ordinary user requests without network constraints.
     * @param googleTraceRequestFiles the mapping of file indices to GoogleTraceRequestFile objects.
     * @param maxCpuCapacity the maximum capacity of the cpu.
     * @param maxRamCapacity the maximum capacity of the ram.
     * @param storageCapacity the storage capacity.
     * @param bwCapacity the bandwidth capacity.
     * @param lifeTimeMean the mean of the lifetime.
     * @param lifeTimeStd the standard deviation of the lifetime.
     * @param instanceGroupRetryTimes the number of times to retry the instance group.
     * @param instanceRetryTimes the number of times to retry the instance.
     */
    public UserRequestManagerGoogleTrace(Map<Integer, GoogleTraceRequestFile> googleTraceRequestFiles, int maxCpuCapacity, int maxRamCapacity, int storageCapacity, int bwCapacity, int lifeTimeMean, int lifeTimeStd, int instanceGroupRetryTimes, int instanceRetryTimes) {
        this.maxCpuCapacity = maxCpuCapacity;
        this.maxRamCapacity = maxRamCapacity;
        this.storageCapacity = storageCapacity;
        this.bwCapacity = bwCapacity;
        this.googleTraceRequestFiles = googleTraceRequestFiles;
        this.lifeTimeMean = lifeTimeMean;
        this.lifeTimeStd = lifeTimeStd;
        this.instanceGroupRetryTimes = instanceGroupRetryTimes;
        this.instanceRetryTimes = instanceRetryTimes;
        this.accessLatencyPercentage = 0;
        this.edgePercentage = 0;
        this.csvFormat = CSVFormat.DEFAULT.withFirstRecordAsHeader();
        initCsvRecordMap();
    }

    /**
     * Construct a user request manager. It generates user requests with network constraints.
     *
     * @param googleTraceRequestFiles the mapping of file indices to GoogleTraceRequestFile objects.
     * @param maxCpuCapacity the maximum capacity of the CPU.
     * @param maxRamCapacity the maximum capacity of the RAM.
     * @param storageCapacity the storage capacity.
     * @param bwCapacity the bandwidth capacity.
     * @param lifeTimeMean the mean of the lifetime.
     * @param lifeTimeStd the standard deviation of the lifetime.
     * @param instanceGroupRetryTimes the number of times to retry the instance group.
     * @param instanceRetryTimes the number of times to retry the instance.
     * @param accessLatencyPercentage the percentage of instance groups that have access latency.
     * @param accessLatencyMean the mean of the access latency.
     * @param accessLatencyStd the standard deviation of the access latency.
     * @param isEdgeDirected whether the edges between instance groups are directed.
     * @param edgePercentage the percentage that an instance group has edge constraints with other instance groups.
     * @param edgeDelayMean the mean of the edge delay.
     * @param edgeDelayStd the standard deviation of the edge delay.
     * @param edgeBwMean the mean of the edge bandwidth.
     * @param edgeBwStd the standard deviation of the edge bandwidth.
     */
    public UserRequestManagerGoogleTrace(Map<Integer, GoogleTraceRequestFile> googleTraceRequestFiles, int maxCpuCapacity, int maxRamCapacity, int storageCapacity, int bwCapacity, int lifeTimeMean, int lifeTimeStd, int instanceGroupRetryTimes, int instanceRetryTimes,
                                         double accessLatencyPercentage, double accessLatencyMean, double accessLatencyStd,
                                         boolean isEdgeDirected, double edgePercentage, double edgeDelayMean, double edgeDelayStd, double edgeBwMean, double edgeBwStd) {
        this.maxCpuCapacity = maxCpuCapacity;
        this.maxRamCapacity = maxRamCapacity;
        this.storageCapacity = storageCapacity;
        this.bwCapacity = bwCapacity;
        this.googleTraceRequestFiles = googleTraceRequestFiles;
        this.lifeTimeMean = lifeTimeMean;
        this.lifeTimeStd = lifeTimeStd;
        this.instanceGroupRetryTimes = instanceGroupRetryTimes;
        this.instanceRetryTimes = instanceRetryTimes;
        this.accessLatencyPercentage = accessLatencyPercentage;
        this.accessLatencyMean = accessLatencyMean;
        this.accessLatencyStd = accessLatencyStd;
        this.isEdgeDirected = isEdgeDirected;
        this.edgePercentage = edgePercentage;
        this.edgeDelayMean = edgeDelayMean;
        this.edgeDelayStd = edgeDelayStd;
        this.edgeBwMean = edgeBwMean;
        this.edgeBwStd = edgeBwStd;
        this.csvFormat = CSVFormat.DEFAULT.withFirstRecordAsHeader();
        initCsvRecordMap();
    }

    /**
     * Initialize the csv record map.
     */
    private void initCsvRecordMap() {
        for (Map.Entry<Integer, GoogleTraceRequestFile> googleTraceRequestFile : googleTraceRequestFiles.entrySet()) {
            Integer belongDatacenterId = googleTraceRequestFile.getKey();
            rowMap.put(belongDatacenterId, 0);
            lastSubmitTimeMap.put(belongDatacenterId, 0.0);
            initCsvRecordMap(belongDatacenterId);
            generateUserRequests(belongDatacenterId);
        }
    }

    /**
     * Initialize the csv record map.
     * @param belongDatacenterId the id of the data center to which the csv file belongs.
     */
    private void initCsvRecordMap(Integer belongDatacenterId) {
        GoogleTraceRequestFile googleTraceRequestFile = googleTraceRequestFiles.get(belongDatacenterId);
        String filePath = googleTraceRequestFile.getFilePath();
        File csvFile = new File(filePath);

        try {
            CSVParser csvParser = new CSVParser(new FileReader(csvFile), csvFormat);
            Iterator<CSVRecord> csvIterator = csvParser.iterator();
            csvIteratorMap.put(belongDatacenterId, csvIterator);

            Iterator<CSVRecord> csvIteratorNext = csvParser.iterator();
            if (csvIteratorNext.hasNext()) {
                csvRecordMap.put(belongDatacenterId, csvIteratorNext.next());
            } else {
                csvRecordMap.put(belongDatacenterId, null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Generate user requests.
     * It keeps reading entries in the CSV until the next entry has a different submission time than the previously read user request
     * or until a sufficient number of entries have been read.
     * @param datacenterId the id of the data center to which the user requests are sent.
     */
    private void generateUserRequests(int datacenterId) {
        Iterator<CSVRecord> csvIterator = csvIteratorMap.get(datacenterId);
        UserRequestGoogleTrace userRequestGoogleTrace = new UserRequestGoogleTrace();
        while (csvRecordMap.get(datacenterId) != null) {
            CSVRecord csvRecord = csvRecordMap.get(datacenterId);
            userRequestGoogleTrace.addRecord(csvRecord);
            rowMap.put(datacenterId, rowMap.get(datacenterId) + 1);
            if (rowMap.get(datacenterId) > googleTraceRequestFiles.get(datacenterId).getRowNum()) {
                addUserRequestToMap(datacenterId, userRequestGoogleTrace);
                csvRecordMap.put(datacenterId, null);
                break;
            } else {
                if (csvIterator.hasNext()) {
                    CSVRecord csvRecordNext = csvIterator.next();
                    csvRecordMap.put(datacenterId, csvRecordNext);
                    if (!Objects.equals(csvRecordNext.get(1), csvRecord.get(1))) {
                        UserRequest googleUserRequest = addUserRequestToMap(datacenterId, userRequestGoogleTrace);
                        double nextRequestSubmitTime = Double.parseDouble(csvRecordNext.get(0)) + lastSubmitTimeMap.get(datacenterId);
                        if (nextRequestSubmitTime == googleUserRequest.getSubmitTime()) {
                            userRequestGoogleTrace = new UserRequestGoogleTrace();
                        } else {
                            break;
                        }
                    }
                } else {
                    UserRequest googleUserRequest = addUserRequestToMap(datacenterId, userRequestGoogleTrace);
                    initCsvRecordMap(datacenterId);
                    lastSubmitTimeMap.put(datacenterId, googleUserRequest.getSubmitTime());
                    break;
                }
            }
        }
    }

    /**
     * Add a user request to the latestUserRequestMap.
     * @param datacenterId the id of the data center to which the user requests are sent.
     * @param userRequestGoogleTrace the user request to be added.
     * @return the user request.
     */
    private UserRequest addUserRequestToMap(int datacenterId, UserRequestGoogleTrace userRequestGoogleTrace) {
        latestUserRequestMap.computeIfAbsent(datacenterId, k -> new ArrayList<>());
        UserRequest userRequest = userRequestGoogleTrace.generateUserRequest(datacenterId);
        latestUserRequestMap.get(datacenterId).add(userRequest);
        return userRequest;
    }

    /**
     * Get the latest submit time.
     * @return the latest submit time.
     */
    private double getLatestSubmitTime() {
        double submitTime = Double.MAX_VALUE;
        for (List<UserRequest> userRequests : latestUserRequestMap.values()) {
            if (userRequests != null && userRequests.get(0).getSubmitTime() < submitTime) {
                submitTime = userRequests.get(0).getSubmitTime();
            }
        }
        return submitTime;
    }

    @Override
    public Map<Integer, List<UserRequest>> generateOnceUserRequests() {
        Map<Integer, List<UserRequest>> sendUserRequestMap = new HashMap<>();
        double submitTime = getLatestSubmitTime();

        for (Map.Entry<Integer, List<UserRequest>> entry : latestUserRequestMap.entrySet()) {
            Integer datacenterId = entry.getKey();
            List<UserRequest> userRequest = entry.getValue();
            if (userRequest != null && userRequest.get(0).getSubmitTime() == submitTime) {
                sendUserRequestMap.put(datacenterId, userRequest);
                latestUserRequestMap.put(datacenterId, null);
                generateUserRequests(datacenterId);
            }
        }
        return sendUserRequestMap;
    }

    @Override
    public double getNextSendTime() {
        return getLatestSubmitTime();
    }
}
