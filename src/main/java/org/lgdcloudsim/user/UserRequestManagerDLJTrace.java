package org.lgdcloudsim.user;

import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.lgdcloudsim.request.Instance;
import org.lgdcloudsim.request.InstanceGroup;
import org.lgdcloudsim.request.InstanceGroupGraphSimple;
import org.lgdcloudsim.request.InstanceGroupSimple;
import org.lgdcloudsim.request.InstanceSimple;
import org.lgdcloudsim.request.TrainingStrategy;
import org.lgdcloudsim.request.UserRequest;
import org.lgdcloudsim.request.UserRequestSimple;

public class UserRequestManagerDLJTrace implements UserRequestManager {
    private final Map<Integer, String> dcRequestTracePathMap;
    private double currentTime = 0;
    private Map<Integer, List<CSVRecord>> dcRecordsMap = new HashMap<>();
    private Map<Integer, Integer> dcCurrentIndexMap = new HashMap<>();
    private int requestIdCounter = 0;
    private int instanceIdCounter = 0;
    private int instanceGroupIdCounter = 0;
    private double cachedNextTime = Double.MAX_VALUE;

    public UserRequestManagerDLJTrace(Map<Integer, String> dcRequestTracePathMap) {
        this.dcRequestTracePathMap = dcRequestTracePathMap;
        // Load CSV files for each datacenter
        for (Map.Entry<Integer, String> entry : dcRequestTracePathMap.entrySet()) {
            int dcId = entry.getKey();
            String path = entry.getValue();
            List<CSVRecord> records = new ArrayList<>();
            try (Reader reader = Files.newBufferedReader(Paths.get(path));
                 CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {
                for (CSVRecord record : csvParser) {
                    records.add(record);
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to load CSV file for datacenter " + dcId + ": " + path, e);
            }
            dcRecordsMap.put(dcId, records);
            dcCurrentIndexMap.put(dcId, 0);
        }
        // Initialize cachedNextTime
        cachedNextTime = Double.MAX_VALUE;
        for (Integer dcId : dcRequestTracePathMap.keySet()) {
            List<CSVRecord> records = dcRecordsMap.get(dcId);
            if (!records.isEmpty()) {
                double submitTime = Double.parseDouble(records.get(0).get("submit_time_sec"))*1000;
                if (submitTime < cachedNextTime) {
                    cachedNextTime = submitTime;
                }
            }
        }
    }

    private UserRequest createUserRequestFromRecord(CSVRecord record) {
        int instance_replicate  = Integer.parseInt(record.get("instance_replicate"));
        int instance_gpu = Integer.parseInt(record.get("gpu_num"));
        int instance_cpu = Integer.parseInt(record.get("cpu_num"));
        int instance_memory = Integer.parseInt(record.get("memory"));
        int instance_running_time = Integer.parseInt(record.get("running_time_sec"))*1000;
        String model = record.get("model");
        String strategy_type = record.get("strategy_type");
        int dp_dim = Integer.parseInt(record.get("dp_dim"));
        int pp_dim = Integer.parseInt(record.get("pp_dim"));
        int rdma = Integer.parseInt(record.get("rdma"));

        List<Instance> instanceList = new ArrayList<>();
        int rank0Index = instanceIdCounter;
        for (int i = 0; i < instance_replicate; i++) {
            Instance instance = new InstanceSimple(instanceIdCounter++, instance_cpu, instance_memory, 0, 0, instance_gpu, "NVIDIA A100", instance_running_time);
            instance.setRetryMaxNum(3);
            instanceList.add(instance);
        }
        InstanceGroup instanceGroup = new InstanceGroupSimple(instanceGroupIdCounter++, instanceList);
        if (instanceList.size() <= 1 ||strategy_type.isEmpty() || strategy_type.equals(TrainingStrategy.TYPE_NONE)) {
            // No training strategy
        } else {
            instanceGroup.setTrainingStrategy(new TrainingStrategy(strategy_type, dp_dim, pp_dim, rank0Index, instanceList));
        }
        UserRequest userRequest = new UserRequestSimple(requestIdCounter++, new ArrayList<>(Collections.singletonList(instanceGroup)), new InstanceGroupGraphSimple(false));
        userRequest.setSubmitTime(Double.parseDouble(record.get("submit_time_sec"))*1000);

        return userRequest;
    }

    @Override
    public Map<Integer, List<UserRequest>> generateOnceUserRequests() {
        Map<Integer, List<UserRequest>> result = new HashMap<>();

        if (cachedNextTime == Double.MAX_VALUE) {
            // No more requests
            return result;
        }

        // Update current time
        currentTime = cachedNextTime;

        // Collect requests at the current time
        for (Integer dcId : dcRequestTracePathMap.keySet()) {
            List<CSVRecord> records = dcRecordsMap.get(dcId);
            int index = dcCurrentIndexMap.get(dcId);
            List<UserRequest> requests = new ArrayList<>();
            while (index < records.size()) {
                CSVRecord record = records.get(index);
                double submitTime = Double.parseDouble(record.get("submit_time_sec"))*1000;
                if (submitTime == currentTime) {
                    // Create UserRequest (simplified for now)
                    UserRequest userRequest = createUserRequestFromRecord(record);
                    userRequest.setBelongDatacenterId(dcId);
                    userRequest.setArea("United States");
                    userRequest.setSubmitTime(submitTime);
                    requests.add(userRequest);
                    index++;
                } else {
                    break;
                }
            }
            dcCurrentIndexMap.put(dcId, index);
            if (!requests.isEmpty()) {
                result.put(dcId, requests);
            }
        }

        // Recalculate cachedNextTime
        double newNextTime = Double.MAX_VALUE;
        for (Integer dcId : dcRequestTracePathMap.keySet()) {
            List<CSVRecord> records = dcRecordsMap.get(dcId);
            int index = dcCurrentIndexMap.get(dcId);
            if (index < records.size()) {
                double submitTime = Double.parseDouble(records.get(index).get("submit_time_sec"))*1000;
                if (submitTime >= currentTime && submitTime < newNextTime) {
                    newNextTime = submitTime;
                }
            }
        }
        cachedNextTime = newNextTime;

        return result;
    }

    @Override
    public double getNextSendTime() {
        return cachedNextTime;
    }
}
