package org.scalecloudsim.datacenter;

import org.cloudsimplus.core.Factory;
import org.cloudsimplus.core.Simulation;
import org.scalecloudsim.innerscheduler.InnerScheduler;
import org.scalecloudsim.interscheduler.InterScheduler;
import org.scalecloudsim.statemanager.*;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InitDatacenter {
    private static Simulation scaleCloudSim;
    private static Factory factory;

    public static void initDatacenters(Simulation scaleCloudSim, Factory factory, String filePath) {
        InitDatacenter.scaleCloudSim = scaleCloudSim;
        InitDatacenter.factory = factory;
        JsonObject jsonObject = readJsonFile(filePath);

        CollaborationManager collaborationManager = new CollaborationManagerSimple(scaleCloudSim);
        for (int i = 0; i < jsonObject.getJsonArray("collaborations").size(); i++) {
            JsonObject collaborationJson = jsonObject.getJsonArray("collaborations").getJsonObject(i);
            for (int j = 0; j < collaborationJson.getJsonArray("datacenters").size(); j++) {
                JsonObject datacenterJson = collaborationJson.getJsonArray("datacenters").getJsonObject(j);
                Datacenter datacenter = getDatacenter(datacenterJson);
                collaborationManager.addDatacenter(datacenter, collaborationJson.getInt("id"));
            }
        }
    }

    private static JsonObject readJsonFile(String filePath) {
        JsonObject jsonObject = null;
        try (FileReader fileReader = new FileReader(filePath); JsonReader reader = Json.createReader(fileReader)) {
            jsonObject = reader.readObject();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    private static Datacenter getDatacenter(JsonObject datacenterJson) {
        Datacenter datacenter = new DatacenterSimple(scaleCloudSim, datacenterJson.getInt("id"));

        StatesManager statesManager = getStatesManager(datacenterJson);
        datacenter.setStatesManager(statesManager);

        List<InnerScheduler> innerSchedulers = getInnerSchedulers(datacenterJson, statesManager.getPartitionRangesManager().getPartitionNum());
        datacenter.setInnerSchedulers(innerSchedulers);

        JsonObject interSchedulerJson = datacenterJson.getJsonObject("interScheduler");
        InterScheduler interScheduler = factory.getInterScheduler(interSchedulerJson.getString("type"));
        datacenter.setInterScheduler(interScheduler);

        JsonObject loadBalanceJson = datacenterJson.getJsonObject("loadBalancer");
        LoadBalance loadBalance = factory.getLoadBalance(loadBalanceJson.getString("type"));
        datacenter.setLoadBalance(loadBalance);

        JsonObject resourceAllocateSelectorJson = datacenterJson.getJsonObject("resourceAllocateSelector");
        ResourceAllocateSelector resourceAllocateSelector = factory.getResourceAllocateSelector(resourceAllocateSelectorJson.getString("type"));
        datacenter.setResourceAllocateSelector(resourceAllocateSelector);

        return datacenter;
    }

    private static List<InnerScheduler> getInnerSchedulers(JsonObject datacenterJson, int partitionNum) {
        List<InnerScheduler> innerSchedulers = new ArrayList<>();
        int innerSchdeuleId = 0;
        for (int k = 0; k < datacenterJson.getJsonArray("innerSchedulers").size(); k++) {
            JsonObject schedulerJson = datacenterJson.getJsonArray("innerSchedulers").getJsonObject(k);
            int firstPartitionId = schedulerJson.getInt("firstPartitionIndex");
            InnerScheduler scheduler = factory.getInnerScheduler(schedulerJson.getString("type"), innerSchdeuleId, firstPartitionId, partitionNum);
            innerSchedulers.add(scheduler);
            innerSchdeuleId++;
        }
        return innerSchedulers;
    }

    private static StatesManager getStatesManager(JsonObject datacenterJson) {
        int hostNum = datacenterJson.getInt("hostNum");
        PartitionRangesManager partitionRangesManager = getPartitionRangesManager(datacenterJson);
        double synchronizationGap = datacenterJson.getJsonNumber("synchronizationGap").doubleValue();
//        StateManager stateManager = new StateManagerSimple(hostNum, scaleCloudSim, partitionRangesManager, innerSchedulers);
        StatesManager statesManager = new StatesManagerSimple(hostNum, partitionRangesManager, synchronizationGap);

        setPrediction(statesManager, datacenterJson);

        initHostState(statesManager, datacenterJson);

        return statesManager;
    }

    private static PartitionRangesManager getPartitionRangesManager(JsonObject datacenterJson) {
        int startId = 0;
        int partitionId = 0;
        Map<Integer, int[]> ranges = new HashMap<>();
        for (int k = 0; k < datacenterJson.getJsonArray("partitions").size(); k++) {
            JsonObject partition = datacenterJson.getJsonArray("partitions").getJsonObject(k);
//            partitionRangesManager.addRange(partition.getInt("id"), startId, partition.getInt("length"));
            ranges.put(partitionId, new int[]{startId, startId + partition.getInt("length") - 1});
            startId += partition.getInt("length");
            partitionId++;
        }
        PartitionRangesManager partitionRangesManager = new PartitionRangesManager(ranges);
        return partitionRangesManager;
    }

    private static void setPrediction(StatesManager statesManager, JsonObject datacenterJson) {
        JsonObject predictionJson = datacenterJson.getJsonObject("prediction");
        PredictionManager predictionManager = factory.getPredictionManager(predictionJson.getString("type"));
        statesManager.setPredictionManager(predictionManager);
        boolean isPredict = datacenterJson.getBoolean("isPredict");
        statesManager.setPredictable(isPredict);
    }

    private static void initHostState(StatesManager statesManager, JsonObject datacenterJson) {
        int startId = 0;
        for (int k = 0; k < datacenterJson.getJsonArray("hostStates").size(); k++) {
            JsonObject hostStateJson = datacenterJson.getJsonArray("hostStates").getJsonObject(k);
            int cpu = hostStateJson.getInt("cpu");
            int ram = hostStateJson.getInt("ram");
            int storage = hostStateJson.getInt("storage");
            int bw = hostStateJson.getInt("bw");
            int length = hostStateJson.getInt("length");
            statesManager.initHostStates(cpu, ram, storage, bw, startId, length);
            startId += length;
        }
    }
}
