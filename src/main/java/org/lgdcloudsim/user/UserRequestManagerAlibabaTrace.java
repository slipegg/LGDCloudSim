package org.lgdcloudsim.user;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.lgdcloudsim.request.*;

import java.io.File;
import java.io.FileReader;
import java.util.*;
import lombok.Setter;

@Setter
public class UserRequestManagerAlibabaTrace implements UserRequestManager {
    public static final String BATCH_TYPE = "batch";
    public static final String CONTAINER_TYPE = "container";
    private String batchFilePath;
    private String containerFilePath;
    private int datacenterId;
    private CSVFormat csvFormat;   
    private Iterator<CSVRecord> batchCsvIterator; 
    private Iterator<CSVRecord> containerCsvIterator; 
    private String model = "single";
    private int userRequestId = 0;
    private int instanceGroupId = 0;
    private int instanceId = 0;


    // System settings when using the dataset
    /* It represents how many rows the system will replay. 
     * If this number is greater than the number of rows in the csv file, 
     * it will start from the beginning and replay again until it reaches this number. 
     * If its value is -1, it means that all rows of the csv file will only be replayed once.
    */
    private int batchReplayMaxTime = -1;
    private int containerReplayMaxTime = -1;

    /*
     * It indicates whether we need to skip the beginning time when we replay the request of this csv, 
     * because for the container, most instances will be submitted at the 0th second.
     */
    private boolean isBatchReplySkipZero = false;
    private boolean isContainerReplySkipZero = false;

    // Now we only support real time from csv file
    private InstanceLifeModel batchLifeModel=InstanceLifeModel.REAL;
    private InstanceLifeModel containerLifeModel = InstanceLifeModel.REAL;
    
    /*
     * It represents the acceleration ratio of the sending time. 
     * If the value is 0.1, it means that the instance with startTime of 10 in the csv file will be submitted at 10*0.1=1s.
     */
    private double batchSubmitAccelerationRatio=1;
    private double containerSubmitAccelerationRatio=1;

    /*
     * The scale-up ratio of the resources required by the instance.
     */
    private double batchCpuScale=1;
    private double batchMemoryScale=1;
    private double containerCpuScale=1;
    private double containerMemoryScale=1;

    // The following variables are needed to use the above settings
    private CSVRecord batchNextCsvRecord;
    private CSVRecord containerNextCsvRecord;
    private double batchTime = 0;
    private double containerTime = 0;
    private int batchReplayNumTmp = 0;
    private int containerReplayNumTmp = 0;
    private double batchBaseTime = 0;
    private double containerBaseTime = 0;

    public UserRequestManagerAlibabaTrace(String batchFilePath, String containerFilePath, int datacenterId, int batchReplayMaxTime, int containerReplayMaxTime) {
        this.batchFilePath = batchFilePath;
        this.containerFilePath = containerFilePath;
        this.datacenterId = datacenterId;
        this.csvFormat = CSVFormat.Builder.create().setHeader().build();
        this.batchReplayMaxTime = batchReplayMaxTime;
        this.containerReplayMaxTime = containerReplayMaxTime;
        initBatchCSVIterator(false);
        initContainerCSVIterator(false);
    }

    @Override
    public Map<Integer, List<UserRequest>> generateOnceUserRequests() {
        switch (model) {
            case "single":
                return generateUserRequestsForSingleModel();
            default:
                return new HashMap<>();
        }
    }

    @Override
    public double getNextSendTime() {
        if (getBatchNextTime() < getContainerNextTime()) {
            return getBatchNextTime();
        } else {
            return getContainerNextTime();
        }
    }

    private void initBatchCSVIterator(boolean isSkipZero) {
        if (batchReplayMaxTime == -1) {
            batchReplayMaxTime = getCsvCount(batchFilePath);
        }
        if (batchReplayMaxTime == 0) {
            batchBaseTime = Double.MAX_VALUE;
            return;
        }
        File batchCsvFile = new File(batchFilePath);
        try {
            CSVParser batchCsvParser = new CSVParser(new FileReader(batchCsvFile), csvFormat);
            batchCsvIterator = batchCsvParser.iterator();
            while (batchCsvIterator.hasNext()) {
                CSVRecord record = batchCsvIterator.next();
                if (!isValidateData(record)) {
                    continue;
                }
                if (isSkipZero && Double.parseDouble(record.get("start_time")) == 0) {
                    continue;
                }
                batchNextCsvRecord = record;
                batchTime = Double.parseDouble(record.get("start_time"));
                break;
            }
            if (batchNextCsvRecord == null) {
                throw new Exception("No valid data in batch file");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initContainerCSVIterator(boolean isSkipZero) {
        if (containerReplayMaxTime == -1) {
            containerReplayMaxTime = getCsvCount(containerFilePath);
        }
        if (containerReplayMaxTime == 0) {
            containerBaseTime = Double.MAX_VALUE;
            return;
        }
        File containerCsvFile = new File(containerFilePath);
        try {
            CSVParser containerCsvParser = new CSVParser(new FileReader(containerCsvFile), csvFormat);
            containerCsvIterator = containerCsvParser.iterator();
            while (containerCsvIterator.hasNext()) {
                CSVRecord record = containerCsvIterator.next();
                if (!isValidateData(record)) {
                    continue;
                }
                if (isSkipZero && Double.parseDouble(record.get("start_time")) == 0) {
                    continue;
                }
                containerNextCsvRecord = record;
                containerTime = Double.parseDouble(record.get("start_time"));
                break;
            }
            if (containerNextCsvRecord == null) {
                throw new Exception("No valid data in container file");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int getCsvCount(String filePath){
        int size = 0;
        try {
            CSVParser csvParser = new CSVParser(new FileReader(filePath), csvFormat);
            List<CSVRecord> csvRecords = csvParser.getRecords();
            size = csvRecords.size();
            csvParser.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return size;
    }

    private Map<Integer, List<UserRequest>> generateUserRequestsForSingleModel(){
        Map<Integer, List<UserRequest>> userRequestSend = new HashMap<>();
        List<UserRequest> userRequests = new ArrayList<>();
        userRequestSend.put(datacenterId, userRequests);

        if (getBatchNextTime() < getContainerNextTime()) {
            addBatchUserRequest(batchTime, userRequests);
        } else if (getBatchNextTime() > getContainerNextTime()) {
            addContainerUserRequest(containerTime, userRequests);
        } else {
            addBatchUserRequest(batchTime, userRequests);
            addContainerUserRequest(containerTime, userRequests);
        }

        return userRequestSend;
    }

    private void addBatchUserRequest(double nowTime, List<UserRequest> userRequests) {
        userRequests.addAll(generateUserRequests(batchNextCsvRecord, BATCH_TYPE, getBatchNextTime()));
        batchReplayNumTmp++;
        while (batchCsvIterator.hasNext() && batchReplayNumTmp < batchReplayMaxTime) {
            batchNextCsvRecord = batchCsvIterator.next();
            if (!isValidateData(batchNextCsvRecord)) {
                continue;
            }
            if (!isTheSameTimeData(batchNextCsvRecord, nowTime)) {
                break;
            }
            userRequests.addAll(generateUserRequests(batchNextCsvRecord, BATCH_TYPE, getBatchNextTime()));
            batchReplayNumTmp++;
        }

        if (batchReplayNumTmp >= batchReplayMaxTime) {
            batchBaseTime = Double.MAX_VALUE;
        } else if (!batchCsvIterator.hasNext()&&isTheSameTimeData(batchNextCsvRecord, nowTime)){
            initBatchCSVIterator(isBatchReplySkipZero);
            batchBaseTime += nowTime;
        } else {
            batchTime = Double.parseDouble(batchNextCsvRecord.get("start_time"));
        }
    }

    private void addContainerUserRequest(double nowTime, List<UserRequest> userRequests) {
        userRequests.addAll(generateUserRequests(containerNextCsvRecord, CONTAINER_TYPE, getContainerNextTime()));
        containerReplayNumTmp++;
        while (containerCsvIterator.hasNext() && containerReplayNumTmp < containerReplayMaxTime) {
            containerNextCsvRecord = containerCsvIterator.next();
            if (!isValidateData(containerNextCsvRecord)) {
                continue;
            }
            if (!isTheSameTimeData(containerNextCsvRecord, nowTime)) {
                break;
            }
            userRequests.addAll(generateUserRequests(containerNextCsvRecord, CONTAINER_TYPE, getContainerNextTime()));
            containerReplayNumTmp++;
        }

        if (containerReplayNumTmp >= containerReplayMaxTime) {
            containerBaseTime = Double.MAX_VALUE;
        } else if (!containerCsvIterator.hasNext()&&isTheSameTimeData(containerNextCsvRecord, nowTime)){
            initContainerCSVIterator(isContainerReplySkipZero);
            containerBaseTime += nowTime;
        } else {
            containerTime = Double.parseDouble(containerNextCsvRecord.get("start_time"));
        }
    }

    private double getBatchNextTime() {
        if (batchBaseTime == Double.MAX_VALUE) {
            return Double.MAX_VALUE;
        }
        return (batchBaseTime + batchTime) * 1000 * batchSubmitAccelerationRatio;
    }

    private double getContainerNextTime() {
        if (containerBaseTime == Double.MAX_VALUE) {
            return Double.MAX_VALUE;
        }
        return (containerBaseTime + containerTime) * 1000 * containerSubmitAccelerationRatio;
    }
    
    private boolean isValidateData(CSVRecord record) {
        if (record.get("start_time") == null || record.get("runtime") == null || record.get("plan_cpu") == null || record.get("plan_mem") == null) {
            return false;
        }

        return true;
    }

    private boolean isTheSameTimeData(CSVRecord record, double nowTime) {
        if (nowTime == -1) {
            return true;
        }
        return nowTime == Double.parseDouble(record.get("start_time"));
    }

    private List<UserRequest> generateUserRequests(CSVRecord record, String type, double submitTime) {
        int runtime = Integer.parseInt(record.get("runtime"));
        double plan_cpu = Double.parseDouble(record.get("plan_cpu"));
        double plan_mem = Double.parseDouble(record.get("plan_mem"));
        int instance_num = (int)(Double.parseDouble(record.get("instance_num")));

        List<UserRequest> userRequests = new ArrayList<>();
        for(int i = 0; i < instance_num; i++) {
            Instance instance;
            if (type.equals(BATCH_TYPE)) {
                instance = new InstanceSimple(instanceId, (int)(plan_cpu * batchCpuScale), (int)(plan_mem * 100 * batchMemoryScale), 0, 0, runtime*1000);
            } else {
                instance = new InstanceSimple(instanceId, (int)(plan_cpu * containerCpuScale), (int)(plan_mem * 100 * containerMemoryScale), 0, 0, runtime*1000);
            }
            InstanceGroup instanceGroup = new InstanceGroupSimple(instanceGroupId, new ArrayList<>(Collections.singletonList(instance)));
            UserRequest userRequest = new UserRequestSimple(userRequestId, new ArrayList<>(Collections.singletonList(instanceGroup)), new InstanceGroupGraphSimple(false));
            userRequest.setSubmitTime(submitTime);
            userRequests.add(userRequest);

            instanceId++;
            instanceGroupId++;
            userRequestId++;
        }

        return userRequests;
    }
}
