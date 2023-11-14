package org.cpnsim.datacenter;

import org.cloudsimplus.core.Factory;
import org.cloudsimplus.core.Simulation;
import org.cpnsim.innerscheduler.InnerScheduler;
import org.cpnsim.interscheduler.InterScheduler;
import org.cpnsim.statemanager.*;
import org.slf4j.LoggerFactory;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.FileReader;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A class to initialize datacenters.
 * All methods are static.
 *
 * @author Jiawen Liu
 * @since CPNSim 1.0
 */
public class InitDatacenter {
    private static Logger LOGGER = LoggerFactory.getLogger(InitDatacenter.class.getSimpleName());
    /**
     * The {@link Simulation} object.
     **/
    private static Simulation cpnSim;

    /**
     * The {@link Factory} object.
     **/
    private static Factory factory;

    private static int interSchedulerId = 0;

    private static int innerSchedulerId = 0;

    /**
     * Initialize datacenters.
     **/
    public static void initDatacenters(Simulation cpnSim, Factory factory, String filePath) {
        InitDatacenter.cpnSim = cpnSim;
        InitDatacenter.factory = factory;
        JsonObject jsonObject = readJsonFile(filePath);

        CollaborationManager collaborationManager = new CollaborationManagerSimple(cpnSim);
        for (int i = 0; i < jsonObject.getJsonArray("collaborations").size(); i++) {
            JsonObject collaborationJson = jsonObject.getJsonArray("collaborations").getJsonObject(i);
            int collaborationId = collaborationJson.getInt("id");

            boolean isCenterSchedule = collaborationJson.containsKey("centerScheduler");
            boolean isDcTarget = collaborationJson.getJsonObject("centerScheduler").getBoolean("isDcTarget");
            for (int j = 0; j < collaborationJson.getJsonArray("datacenters").size(); j++) {
                JsonObject datacenterJson = collaborationJson.getJsonArray("datacenters").getJsonObject(j);
                Datacenter datacenter = getDatacenter(datacenterJson, collaborationId, isCenterSchedule, isDcTarget);
                collaborationManager.addDatacenter(datacenter, collaborationId);
            }

            if (isCenterSchedule) {
                initCenterScheduler(collaborationJson.getJsonObject("centerScheduler"), collaborationId, collaborationManager);
            }
        }
    }

    private static void initCenterScheduler(JsonObject centerSchedulerJson, int collaborationId, CollaborationManager collaborationManager) {
        String centerSchedulerType = centerSchedulerJson.getString("type");
        boolean isDcTarget = centerSchedulerJson.getBoolean("isDcTarget");
        boolean isSupportForward = false;
        if (isDcTarget) {
            isSupportForward = centerSchedulerJson.getBoolean("isSupportForward");
        }
        InterScheduler interScheduler = factory.getInterScheduler(centerSchedulerType, interSchedulerId++, cpnSim, collaborationId, isDcTarget, isSupportForward);

        Object[] dcStateSynIntervalAndType = getDcStateSynIntervalAndType(centerSchedulerJson, collaborationManager);
        interScheduler.setDcStateSynInterval((Map<Datacenter, Double>) dcStateSynIntervalAndType[0]);
        interScheduler.setDcStateSynType((Map<Datacenter, String>) dcStateSynIntervalAndType[1]);

        collaborationManager.addCenterScheduler(interScheduler);
    }

    private static Object[] getDcStateSynIntervalAndType(JsonObject dcStateSynInfoJson, CollaborationManager collaborationManager) {
        Map<Datacenter, Double> dcStateSynInterval = new HashMap<>();
        Map<Datacenter, String> dcStateSynType = new HashMap<>();
        for (int i = 0; i < dcStateSynInfoJson.getJsonArray("dcStateSynInfo").size(); i++) {
            JsonObject synDcStateInfo = dcStateSynInfoJson.getJsonArray("dcStateSynInfo").getJsonObject(i);
            int dcId = synDcStateInfo.getInt("dcId");
            double interval = synDcStateInfo.getJsonNumber("synInterval").doubleValue();
            String synStateType = synDcStateInfo.getString("synStateType");
            Datacenter datacenter = collaborationManager.getDatacenterById(dcId);

            if (datacenter == Datacenter.NULL) {
                throw new IllegalArgumentException("Datacenter id " + dcId + " in dcStateSynInfo is not found");
            }
            dcStateSynInterval.put(datacenter, interval);
            dcStateSynType.put(datacenter, synStateType);
        }
        return new Object[]{dcStateSynInterval, dcStateSynType};
    }

    /**
     * Read a json file.
     *
     * @param filePath the path of the json file
     * @return a {@link JsonObject} object
     */
    private static JsonObject readJsonFile(String filePath) {
        JsonObject jsonObject = null;
        try (FileReader fileReader = new FileReader(filePath); JsonReader reader = Json.createReader(fileReader)) {
            jsonObject = reader.readObject();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    /**
     * From a {@link JsonObject} object, get a {@link StatesManager} object.
     *
     * @param datacenterJson a {@link JsonObject} object
     * @return a {@link StatesManager} object
     */
    private static Datacenter getDatacenter(JsonObject datacenterJson, int collaborationId, boolean isCenterSchedule, boolean isDcTarget) {
        int id = datacenterJson.getInt("id");
        if (id == 0) {
            LOGGER.error("0 is the id of CIS,Datacenter id should not be 0");
            System.exit(1);
        }
        Datacenter datacenter = new DatacenterSimple(cpnSim, id);

        StatesManager statesManager = getStatesManager(datacenterJson, isCenterSchedule, isDcTarget);
        datacenter.setStatesManager(statesManager);

        JsonObject interSchedulerJson = datacenterJson.getJsonObject("interScheduler");
        if (isCenterSchedule) {
            datacenter.setCentralizedInterSchedule(true);
        } else {
            InterScheduler interScheduler = factory.getInterScheduler(interSchedulerJson.getString("type"), interSchedulerId++, cpnSim, collaborationId, false, false);
            interScheduler.setDatacenter(datacenter);
            if (interSchedulerJson.containsKey("isDirectSend")) {
                interScheduler.setDirectedSend(interSchedulerJson.getBoolean("isDirectSend"));
            }
            datacenter.setInterScheduler(interScheduler);
        }


        if (!(isCenterSchedule && !isDcTarget)) {
            JsonObject loadBalanceJson = datacenterJson.getJsonObject("loadBalancer");
            LoadBalance loadBalance = factory.getLoadBalance(loadBalanceJson.getString("type"));
            datacenter.setLoadBalance(loadBalance);

            List<InnerScheduler> innerSchedulers = getInnerSchedulers(datacenterJson, statesManager.getPartitionRangesManager().getPartitionNum());
            datacenter.setInnerSchedulers(innerSchedulers);
        }

        JsonObject resourceAllocateSelectorJson = datacenterJson.getJsonObject("resourceAllocateSelector");
        ResourceAllocateSelector resourceAllocateSelector = factory.getResourceAllocateSelector(resourceAllocateSelectorJson.getString("type"));
        datacenter.setResourceAllocateSelector(resourceAllocateSelector);

        JsonObject unitPriceJson = datacenterJson.getJsonObject("resourceUnitPrice");
        setDatacenterResourceUnitPrice(datacenter, unitPriceJson);

        return datacenter;
    }

    /**
     * From a {@link JsonObject} object to get all InnerSchedulers.
     *
     * @param datacenterJson a {@link JsonObject} object
     * @param partitionNum   the number of partitions
     * @return a list of {@link InnerScheduler} objects
     */
    private static List<InnerScheduler> getInnerSchedulers(JsonObject datacenterJson, int partitionNum) {
        List<InnerScheduler> innerSchedulers = new ArrayList<>();
        for (int k = 0; k < datacenterJson.getJsonArray("innerSchedulers").size(); k++) {
            JsonObject schedulerJson = datacenterJson.getJsonArray("innerSchedulers").getJsonObject(k);
            int firstPartitionId = schedulerJson.getInt("firstPartitionIndex");
            InnerScheduler scheduler = factory.getInnerScheduler(schedulerJson.getString("type"), innerSchedulerId++, firstPartitionId, partitionNum);
            innerSchedulers.add(scheduler);
        }
        return innerSchedulers;
    }

    /**
     * From a {@link JsonObject} object to get a {@link StatesManager} object.
     *
     * @param datacenterJson a {@link JsonObject} object
     */
    private static StatesManager getStatesManager(JsonObject datacenterJson, boolean isCenterSchedule, boolean isDcTarget) {
        int hostNum = datacenterJson.getInt("hostNum");
        PartitionRangesManager partitionRangesManager = getPartitionRangesManager(datacenterJson);
        double synchronizationGap;
        if (isCenterSchedule && !isDcTarget) {
            synchronizationGap = 0;
        } else {
            synchronizationGap = datacenterJson.getJsonNumber("synchronizationGap").doubleValue();
        }
//        StateManager stateManager = new StateManagerSimple(hostNum, cpnSim, partitionRangesManager, innerSchedulers);
        int[] maxCpuRam = getMaxCpuRam(datacenterJson);
        StatesManager statesManager = new StatesManagerSimple(hostNum, partitionRangesManager, synchronizationGap, maxCpuRam[0], maxCpuRam[1]);

        setPrediction(statesManager, datacenterJson);

        initHostState(statesManager, datacenterJson);

        return statesManager;
    }

    private static int[] getMaxCpuRam(JsonObject datacenterJson) {
        int maxCpu = 0;
        int maxRam = 0;
        for (int k = 0; k < datacenterJson.getJsonArray("hostStates").size(); k++) {
            JsonObject hostStateJson = datacenterJson.getJsonArray("hostStates").getJsonObject(k);
            int cpu = hostStateJson.getInt("cpu");
            int ram = hostStateJson.getInt("ram");
            if (cpu > maxCpu) {
                maxCpu = cpu;
            }
            if (ram > maxRam) {
                maxRam = ram;
            }
        }
        return new int[]{maxCpu, maxRam};
    }

    /**
     * From a {@link JsonObject} object to get a {@link PartitionRangesManager} object.
     *
     * @param datacenterJson a {@link JsonObject} object
     */
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

    /**
     * Set the prediction.
     *
     * @param statesManager  a {@link StatesManager} object
     * @param datacenterJson a {@link JsonObject} object
     */
    private static void setPrediction(StatesManager statesManager, JsonObject datacenterJson) {
        boolean isPredict = datacenterJson.getBoolean("isPredict");
        statesManager.setPredictable(isPredict);
        if (isPredict) {
            JsonObject predictionJson = datacenterJson.getJsonObject("prediction");
            PredictionManager predictionManager = factory.getPredictionManager(predictionJson.getString("type"));
            statesManager.setPredictionManager(predictionManager);
            int predictRecordNum = predictionJson.getInt("predictRecordNum");
            if (predictRecordNum <= 0) {
                LOGGER.error("predictRecordNum must be greater than 0");
                System.exit(-1);
            }
            statesManager.setPredictRecordNum(predictRecordNum);
        }
    }

    /**
     * Initialize the host state.
     *
     * @param statesManager  a {@link StatesManager} object
     * @param datacenterJson a {@link JsonObject} object
     */
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

    private static void setDatacenterResourceUnitPrice(Datacenter datacenter, JsonObject unitPriceJson) {
        if (unitPriceJson == null) {
            return;
        }
        double unitCpuPrice = unitPriceJson.getJsonNumber("cpu").doubleValue();
        double unitRamPrice = unitPriceJson.getJsonNumber("ram").doubleValue();
        double unitRackPrice = unitPriceJson.getJsonNumber("rack").doubleValue();
        double unitStoragePrice = unitPriceJson.getJsonNumber("storage").doubleValue();
        double unitBwPrice = unitPriceJson.getJsonNumber("bw").doubleValue();
        if (unitPriceJson.containsKey("bwBillingType")) {
            String bwBillingType = unitPriceJson.getString("bwBillingType");
            if (!Objects.equals(bwBillingType, "used") && !Objects.equals(bwBillingType, "fixed")) {
                LOGGER.error("bwBillingType should be either used or fixed");
                System.exit(1);
            } else {
                datacenter.setBwBillingType(bwBillingType);
                if (bwBillingType.equals("used")) {
                    if (unitPriceJson.containsKey("bwUtilization")) {
                        double bwUtilization = unitPriceJson.getJsonNumber("bwUtilization").doubleValue();
                        datacenter.setBwUtilization(bwUtilization);
                    } else {
                        LOGGER.error("A double type bwUtilization should be set when bwBillingType is used");
                        System.exit(1);
                    }
                }

            }
        }
        datacenter.setUnitCpuPrice(unitCpuPrice);
        datacenter.setUnitRamPrice(unitRamPrice);
        datacenter.setUnitRackPrice(unitRackPrice);
        datacenter.setUnitStoragePrice(unitStoragePrice);
        datacenter.setUnitBwPrice(unitBwPrice);
    }
}
