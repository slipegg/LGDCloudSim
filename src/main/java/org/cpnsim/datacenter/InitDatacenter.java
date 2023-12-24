package org.cpnsim.datacenter;

import org.cpnsim.core.Factory;
import org.cpnsim.core.Simulation;
import org.cpnsim.intrascheduler.IntraScheduler;
import org.cpnsim.interscheduler.InterScheduler;
import org.cpnsim.interscheduler.InterSchedulerSimple;
import org.cpnsim.statemanager.*;
import org.slf4j.LoggerFactory;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.FileReader;
import java.util.*;

import org.slf4j.Logger;

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

    private static int intraSchedulerId = 0;

    /**
     * Initialize datacenters.
     **/
    public static void initDatacenters(Simulation cpnSim, Factory factory, String filePath) {
        InitDatacenter.cpnSim = cpnSim;
        InitDatacenter.factory = factory;
        JsonObject jsonObject = readJsonFile(filePath);

        if (jsonObject.containsKey("collaborations")){
            initMultiDatacenters(jsonObject);
        }else{
            initSingleDatacenter(jsonObject);
        }
    }

    private static void initMultiDatacenters(JsonObject jsonObject){
        CollaborationManager collaborationManager = new CollaborationManagerSimple(cpnSim);
        for (int i = 0; i < jsonObject.getJsonArray("collaborations").size(); i++) {
            JsonObject collaborationJson = jsonObject.getJsonArray("collaborations").getJsonObject(i);
            int collaborationId = collaborationJson.getInt("id");

            boolean isCenterSchedule = collaborationJson.containsKey("centerScheduler");
            int target = InterSchedulerSimple.NULL;
            boolean isSupportForward = false;
            if (isCenterSchedule) {
                target = getInterScheduleTarget(collaborationJson.getJsonObject("centerScheduler"));
            }
            if (target == InterSchedulerSimple.DC_TARGET) {
                isSupportForward = collaborationJson.getJsonObject("centerScheduler").getBoolean("isSupportForward");
            }
            for (int j = 0; j < collaborationJson.getJsonArray("datacenters").size(); j++) {
                JsonObject datacenterJson = collaborationJson.getJsonArray("datacenters").getJsonObject(j);
                Datacenter datacenter = getDatacenter(datacenterJson, collaborationId, isCenterSchedule, target, isSupportForward);
                collaborationManager.addDatacenter(datacenter, collaborationId);
            }

            if (isCenterSchedule) {
                JsonObject centerSchedulerJson = collaborationJson.getJsonObject("centerScheduler");
                InterScheduler interScheduler = initInterScheduler(centerSchedulerJson, collaborationId, collaborationManager);

                collaborationManager.addCenterScheduler(interScheduler);
            }

            if (isNeedInterSchedulerForDc(isCenterSchedule, target, isSupportForward)) {
                initInterSchedulers(collaborationJson.getJsonArray("datacenters"), collaborationId, collaborationManager);
            }
        }
    }

    private static void initSingleDatacenter(JsonObject jsonObject){
        cpnSim.setSingleDatacenterFlag(true);
        CollaborationManager collaborationManager = new CollaborationManagerSimple(cpnSim);
        int collaborationId = 0;
        Datacenter datacenter = getDatacenter(jsonObject, collaborationId, false, InterSchedulerSimple.NULL, false);
        collaborationManager.addDatacenter(datacenter, collaborationId);
    }

    private static void initInterSchedulers(JsonArray datacenters, int collaborationId, CollaborationManager collaborationManager) {
        for (int j = 0; j < datacenters.size(); j++) {
            JsonObject datacenterJson = datacenters.getJsonObject(j);
            JsonObject interSchedulerJson = datacenterJson.getJsonObject("interScheduler");
            if (interSchedulerJson == null) {
                throw new IllegalArgumentException("interScheduler should not be null");
            }

            InterScheduler interScheduler = initInterScheduler(interSchedulerJson, collaborationId, collaborationManager);

            int datacenterId = datacenterJson.getInt("id");
            Datacenter datacenter = collaborationManager.getDatacenterById(datacenterId);
            interScheduler.setDatacenter(datacenter);
            datacenter.setInterScheduler(interScheduler);
        }
    }

    private static InterScheduler initInterScheduler(JsonObject interSchedulerJson, int collaborationId, CollaborationManager collaborationManager) {
        String interSchedulerType = interSchedulerJson.getString("type");
        int target = getInterScheduleTarget(interSchedulerJson);
        boolean isSupportForward = false;
        if (target == InterSchedulerSimple.DC_TARGET) {
            isSupportForward = interSchedulerJson.getBoolean("isSupportForward");
        }
        InterScheduler interScheduler = factory.getInterScheduler(interSchedulerType, interSchedulerId++, cpnSim, collaborationId, target, isSupportForward);
        Object[] dcStateSynIntervalAndType = getDcStateSynIntervalAndType(interSchedulerJson, collaborationManager);
        interScheduler.setDcStateSynInterval((Map<Datacenter, Double>) dcStateSynIntervalAndType[0]);
        interScheduler.setDcStateSynType((Map<Datacenter, String>) dcStateSynIntervalAndType[1]);
        return interScheduler;
    }

    private static int getInterScheduleTarget(JsonObject centerSchedulerJson) {
        String targetStr = centerSchedulerJson.getString("target");
        return switchTarget(targetStr);
    }

    private static int switchTarget(String targetStr) {
        return switch (targetStr) {
            case "dc","datacenter" -> InterSchedulerSimple.DC_TARGET;
            case "host" -> InterSchedulerSimple.HOST_TARGET;
            case "mixed" -> InterSchedulerSimple.MIXED_TARGET;
            default -> throw new IllegalArgumentException("target should be dc, host or mixed");
        };
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
    private static Datacenter getDatacenter(JsonObject datacenterJson, int collaborationId, boolean isCenterSchedule, int target, boolean isSupportForward) {
        int id = datacenterJson.getInt("id");
        if (id == 0) {
            throw new IllegalArgumentException("0 is the id of CIS,Datacenter id should not be 0");
        }
        Datacenter datacenter = new DatacenterSimple(cpnSim, id);

        addRegionInfo(datacenter, datacenterJson);

        if(datacenterJson.containsKey("architecture")){
            datacenter.setArchitecture(datacenterJson.getString("architecture"));
        }

        StatesManager statesManager = getStatesManager(datacenterJson, isCenterSchedule, target);
        datacenter.setStatesManager(statesManager);

        JsonObject interSchedulerJson = datacenterJson.getJsonObject("interScheduler");
        if (isCenterSchedule) {
            datacenter.setCentralizedInterSchedule(true);
        }
        if (isNeedInterSchedulerForDc(isCenterSchedule, target, isSupportForward)) {
            InterScheduler interScheduler = factory.getInterScheduler(interSchedulerJson.getString("type"), interSchedulerId++, cpnSim, collaborationId, target, false);
            interScheduler.setDatacenter(datacenter);
            datacenter.setInterScheduler(interScheduler);
        }

        if (isNeedInnerSchedule(isCenterSchedule, target, isSupportForward)) {
            JsonObject loadBalanceJson = datacenterJson.getJsonObject("loadBalancer");
            LoadBalance loadBalance = factory.getLoadBalance(loadBalanceJson.getString("type"));
            datacenter.setLoadBalance(loadBalance);

            List<IntraScheduler> intraSchedulers = getIntraSchedulers(datacenterJson, statesManager.getPartitionRangesManager().getPartitionNum());
            datacenter.setIntraSchedulers(intraSchedulers);
        }

        JsonObject resourceAllocateSelectorJson = datacenterJson.getJsonObject("resourceAllocateSelector");
        ConflictHandler conflictHandler = factory.getResourceAllocateSelector(resourceAllocateSelectorJson.getString("type"));
        datacenter.setConflictHandler(conflictHandler);

        JsonObject unitPriceJson = datacenterJson.getJsonObject("resourceUnitPrice");
        setDatacenterResourceUnitPrice(datacenter, unitPriceJson);

        return datacenter;
    }

    private static void addRegionInfo(Datacenter datacenter, JsonObject datacenterJson) {
        if (datacenterJson.containsKey("region")) {
            String region = datacenterJson.getString("region");
            datacenter.setRegion(region);
        }
        if (datacenterJson.containsKey("location")) {
            JsonArray location = datacenterJson.getJsonArray("location");
            double x = location.getJsonNumber(0).doubleValue();
            double y = location.getJsonNumber(1).doubleValue();
            datacenter.setLocation(x, y);
        }
    }

    private static boolean isNeedInterSchedulerForDc(boolean isCenterSchedule, int target, boolean isSupportForward) {
        return (!cpnSim.isSingleDatacenterFlag())&&((isCenterSchedule && isSupportForward) || (!isCenterSchedule));
    }

    private static boolean isNeedInnerSchedule(boolean isCenterSchedule, int target, boolean isSupportForward) {
        return !((isCenterSchedule && target == InterSchedulerSimple.HOST_TARGET)
                || (!isCenterSchedule && target == InterSchedulerSimple.MIXED_TARGET)
                || (isCenterSchedule && target == InterSchedulerSimple.DC_TARGET && isSupportForward));
    }

    /**
     * From a {@link JsonObject} object to get all InnerSchedulers.
     *
     * @param datacenterJson a {@link JsonObject} object
     * @param partitionNum   the number of partitions
     * @return a list of {@link IntraScheduler} objects
     */
    private static List<IntraScheduler> getIntraSchedulers(JsonObject datacenterJson, int partitionNum) {
        List<IntraScheduler> intraSchedulers = new ArrayList<>();
        int firstPartitionId = 0;
        for (int k = 0; k < datacenterJson.getJsonArray("innerSchedulers").size(); k++) {
            JsonObject schedulerJson = datacenterJson.getJsonArray("innerSchedulers").getJsonObject(k);
            if(schedulerJson.containsKey("firstPartitionId")){
                firstPartitionId = schedulerJson.getInt("firstPartitionId");
            }else{
                LOGGER.info("InnerScheduler {} Missing firstPartitionId, defaults to 0", k);
            }
            IntraScheduler scheduler = factory.getIntraScheduler(schedulerJson.getString("type"), intraSchedulerId++, firstPartitionId, partitionNum);
            intraSchedulers.add(scheduler);
        }
        return intraSchedulers;
    }

    /**
     * From a {@link JsonObject} object to get a {@link StatesManager} object.
     *
     * @param datacenterJson a {@link JsonObject} object
     */
    private static StatesManager getStatesManager(JsonObject datacenterJson, boolean isCenterSchedule, int target) {
        int hostNum = datacenterJson.getInt("hostNum");
        PartitionRangesManager partitionRangesManager = getPartitionRangesManager(datacenterJson);
        double synchronizationGap;
        if (isCenterSchedule && target == InterSchedulerSimple.HOST_TARGET) {
            synchronizationGap = 0;
        } else {
            synchronizationGap = datacenterJson.getJsonNumber("synchronizationGap").doubleValue();
        }
//        StateManager stateManager = new StateManagerSimple(hostNum, cpnSim, partitionRangesManager, intraSchedulers);
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
        if (datacenterJson.containsKey("isPredict")) {
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
        } else {
            statesManager.setPredictable(false);
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
