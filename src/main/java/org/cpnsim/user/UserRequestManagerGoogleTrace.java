package org.cpnsim.user;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.math3.analysis.function.Max;
import org.cpnsim.request.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.util.*;

public class UserRequestManagerGoogleTrace implements UserRequestManager {
    private static int userRequestId = 0;
    private static int instanceGroupId = 0;
    private static int instanceId = 0;

    @Getter
    @Setter
    public class InstanceGoogleTrace {
        private double cpu;
        private double ram;
        boolean type;

        public InstanceGoogleTrace(double cpu, double ram, boolean type) {
            this.cpu = cpu;
            this.ram = ram;
            this.type = type;
        }
    }

    public class UserRequestGoogleTrace {
        private double submitTime;
        private String userName;
        private Map<Integer, Map<Integer, InstanceGoogleTrace>> userRequestMap = new HashMap<Integer, Map<Integer, InstanceGoogleTrace>>();
        private boolean isFirstAdd = true;

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

        public UserRequest generateUserRequest(int datacenterId) {
            List<InstanceGroup> instanceGroups = new ArrayList<>();
            for (Map.Entry<Integer, Map<Integer, InstanceGoogleTrace>> entry : userRequestMap.entrySet()) {
                Integer collectionId = entry.getKey();
                Map<Integer, InstanceGoogleTrace> instanceGoogleTraceMap = entry.getValue();
                List<Instance> instances = new ArrayList<>();
                for (Map.Entry<Integer, InstanceGoogleTrace> entry1 : instanceGoogleTraceMap.entrySet()) {
                    Integer instanceIndex = entry1.getKey();
                    InstanceGoogleTrace instanceGoogleTrace = entry1.getValue();
                    int lifeTime = (Math.max((int) (random.nextGaussian() * lifeTimeStd + lifeTimeMean), 5000));
                    Instance instance = new InstanceSimple(UserRequestManagerGoogleTrace.instanceId++, (int) (instanceGoogleTrace.cpu * maxCpuCapacity), (int) (instanceGoogleTrace.ram * maxRamCapacity), storageCapacity, bwCapacity, lifeTime);
                    instances.add(instance);
                }
                InstanceGroup instanceGroup = new InstanceGroupSimple(UserRequestManagerGoogleTrace.instanceGroupId++, instances);
                double accessLatencyFlag = random.nextDouble();
                if (accessLatencyFlag <= accessLatencyPercentage) {
                    instanceGroup.setAccessLatency(0);
                }
                instanceGroups.add(instanceGroup);
            }
            UserRequest userRequest = new UserRequestSimple(UserRequestManagerGoogleTrace.userRequestId++, instanceGroups, new InstanceGroupGraphSimple(false));
            userRequest.setSubmitTime(submitTime);
            userRequest.setBelongDatacenterId(datacenterId);
            return userRequest;
        }
    }

    private Logger LOGGER = LoggerFactory.getLogger(UserRequestManagerGoogleTrace.class.getSimpleName());
    private Random random = new Random(0);
    int maxCpuCapacity;
    int maxRamCapacity;
    int storageCapacity;
    int bwCapacity;
    int lifeTimeMean;
    int lifeTimeStd;
    double accessLatencyPercentage;
    CSVFormat csvFormat;
    Map<String, Integer> datacenterFileMap;
    Map<Integer, CSVRecord> csvRecordMap = new HashMap<>();
    Map<Integer, Iterator<CSVRecord>> csvIteratorMap = new HashMap<>();
    Map<Integer, List<UserRequest>> latestUserRequestMap = new HashMap<>();

    public UserRequestManagerGoogleTrace(Map<String, Integer> datacenterFileMap, int maxCpuCapacity, int maxRamCapacity, int storageCapacity, int bwCapacity, int lifeTimeMean, int lifeTimeStd, double accessLatencyPercentage) {
        this.maxCpuCapacity = maxCpuCapacity;
        this.maxRamCapacity = maxRamCapacity;
        this.storageCapacity = storageCapacity;
        this.bwCapacity = bwCapacity;
        this.datacenterFileMap = datacenterFileMap;
        this.lifeTimeMean = lifeTimeMean;
        this.lifeTimeStd = lifeTimeStd;
        this.accessLatencyPercentage = accessLatencyPercentage;
        this.csvFormat = CSVFormat.DEFAULT.withFirstRecordAsHeader();
        for (Map.Entry<String, Integer> entry : datacenterFileMap.entrySet()) {
            String fileName = entry.getKey();
            Integer datacenterId = entry.getValue();
            File csvFile = new File(fileName);
            try {
                CSVParser csvParser = new CSVParser(new FileReader(csvFile), csvFormat);
                Iterator<CSVRecord> csvIterator = csvParser.iterator();
                csvIteratorMap.put(datacenterId, csvIterator);

                Iterator<CSVRecord> csvIteratorNext = csvParser.iterator();
                if (csvIteratorNext.hasNext()) {
                    csvRecordMap.put(datacenterId, csvIteratorNext.next());
                } else {
                    csvRecordMap.put(datacenterId, null);
                }
                generateUserRequests(datacenterId);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void generateUserRequests(int datacenterId) {
        Iterator<CSVRecord> csvIterator = csvIteratorMap.get(datacenterId);
        UserRequestGoogleTrace userRequestGoogleTrace = new UserRequestGoogleTrace();
        while (csvRecordMap.get(datacenterId) != null) {
            CSVRecord csvRecord = csvRecordMap.get(datacenterId);
            userRequestGoogleTrace.addRecord(csvRecord);
            if (csvIterator.hasNext()) {
                CSVRecord csvRecordNext = csvIterator.next();
                csvRecordMap.put(datacenterId, csvRecordNext);
                if (!Objects.equals(csvRecordNext.get(1), csvRecord.get(1))) {
                    UserRequest googleUserRequest = addUserRequestToMap(datacenterId, userRequestGoogleTrace);
                    double nextRequestSubmitTime = Double.parseDouble(csvRecordNext.get(0));
                    if (nextRequestSubmitTime == googleUserRequest.getSubmitTime()) {
                        userRequestGoogleTrace = new UserRequestGoogleTrace();
                    } else {
                        break;
                    }
                }
            } else {
                addUserRequestToMap(datacenterId, userRequestGoogleTrace);
                csvRecordMap.put(datacenterId, null);
            }
        }
    }

    private UserRequest addUserRequestToMap(int datacenterId, UserRequestGoogleTrace userRequestGoogleTrace) {
        latestUserRequestMap.computeIfAbsent(datacenterId, k -> new ArrayList<>());
        UserRequest userRequest = userRequestGoogleTrace.generateUserRequest(datacenterId);
        latestUserRequestMap.get(datacenterId).add(userRequest);
        return userRequest;
    }

    private double getLatestSubmitTime() {
        double submitTime = Double.MAX_VALUE;
        for (List<UserRequest> userRequests : latestUserRequestMap.values()) {
            if (userRequests != null && userRequests.get(0).getSubmitTime() < submitTime) {
                submitTime = userRequests.get(0).getSubmitTime();
            }
        }
        return submitTime;
    }

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

    public double getNextSendTime() {
        return getLatestSubmitTime();
    }
}
