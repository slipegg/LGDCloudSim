package org.lgdcloudsim.datacenter;

import org.lgdcloudsim.conflicthandler.ConflictHandler;
import org.lgdcloudsim.core.Factory;
import org.lgdcloudsim.core.Simulation;
import org.lgdcloudsim.intrascheduler.IntraScheduler;
import org.lgdcloudsim.interscheduler.InterScheduler;
import org.lgdcloudsim.interscheduler.InterSchedulerSimple;
import org.lgdcloudsim.loadbalancer.LoadBalancer;
import org.lgdcloudsim.request.Instance;
import org.lgdcloudsim.request.InstanceGroup;
import org.lgdcloudsim.statemanager.*;
import org.slf4j.LoggerFactory;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.FileReader;
import java.util.*;

import org.slf4j.Logger;

/**
 * A class to initialize datacenters from a json file.
 * A simple example of the json file is as follows, the iter-architecture is centralized-two stage:
 * {
 * "collaborations": // A list of collaboration zones
 * [
 * {
 * "id": 1, // The id of the collaboration zone
 * "interLoadBalancer": {// The load balancer that is used to distribute the instanceGroups to the center inter-schedulers
 * "type": "round"
 * },
 * "centerSchedulers": // The center inter-scheduler of the cloud administrator in the collaboration zone. If there is no center inter-scheduler in the cloud administrator, this parameter can be omitted.
 * {
 * "type": "simple", // The type of the center inter-scheduler. Inter-schedulers with different scheduling algorithms need to be registered in the {@link Factory}.
 * // If you want to set multiple inter-schedulers, you can set the type to an array of strings, such as ["simple", "simple"].
 * "target": "dc", // The target of the center inter-scheduler.
 * // The value can be dc, host or mixed. If the value is dc, the center inter-scheduler only needs to distribute the instanceGroups to the datacenters.
 * // If the value is host, the center inter-scheduler needs to schedule all instances in instanceGroups to the hosts in the datacenter.
 * // If the value is mixed, the center inter-scheduler can distribute some instanceGroups to the datacenters and schedule some instances to the hosts in the datacenter.
 * // Note: now the value of target can only be dc or host, the mixed target is only used for the inter-scheduler of the datacenter.
 * "isSupportForward": true // When the target is dc, the value of isSupportForward needs to be set.
 * // If the value is false, after the instance group is distributed to each data center,
 * // the instance group cannot be forwarded. If it is true, it can be forwarded again.
 * "dcStateSynInfo":
 * [
 * {
 * "dcId": 1, // The id of the datacenter that needs to synchronize the state
 * "synInterval": 1500, // The interval of state synchronization, in milliseconds
 * "synStateType": "easySimple" // The type of state synchronization. It determines the status that the scheduler can obtain. These types need to be registered in the {@link StatesManager}.
 * },
 * ...
 * ]
 * }
 * "datacenters": // A list of datacenters in the collaboration zone
 * [
 * {
 * "id": 1, // The id of the datacenter, Note: the id of the datacenter cannot be 0, because 0 is the id of the cloud administrator. And the id of the datacenter should be unique.
 * "region": "us-east1", // The region of the datacenter. It's mostly about network.
 * "hostNum": 100, // The number of hosts in the datacenter
 * "partitions": // The partition information of the datacenter
 * [
 * length: 50, // The length of the partition
 * length: 50, // The length of the partition
 * ]
 * "hostStates": // The state information of the hosts in the datacenter
 * [
 * {
 * "cpu": 100, // The number of CPU cores of the host
 * "ram": 100, // The size of the RAM of the host, in GB
 * "storage": 1000, // The size of the storage of the host, in GB
 * "bw": 100, // The bandwidth of the host, in Mbps
 * "startId": 0, // The start id of the host state
 * "length": 50 // The number of hosts with the same state
 * }
 * ...
 * ]
 * "synchronizationGap": 1000, // The interval of state synchronization, in milliseconds
 * "intraSchedulers": // The intra-schedulers of the datacenter
 * [{
 * "firstPartitionId": 0, // The first partition id of the intra-scheduler to synchronize the state. If it is not set, the default value is 0.
 * // The status synchronization method in the data center is performed by zone,
 * //and the intra-scheduler will synchronize the status of each partition in turn.
 * "type": "simple", // The type of the intra-scheduler. Intra-schedulers with different scheduling algorithms need to be registered in the {@link Factory}.
 * }],
 * "intraLoadBalancer": // The load balancer of the datacenter. When there are intra-schedulers in the datacenter, the load balancer is needed.
 * {
 * "type": "batch" // The type of the load balancer. Load balancers with different scheduling algorithms need to be registered in the {@link Factory}.
 * },
 * "resourceAllocateSelector": // The resource allocation selector of the datacenter
 * {
 * "type": "simple" // The type of the resource allocation selector. Resource allocation selectors with different scheduling algorithms need to be registered in the {@link Factory}.
 * }
 * },
 * ...
 * ]
 * },
 * ...
 * ]
 * }
 * If you want to set centralized-one-stage scheduling architecture, you need to use center inter-scheduler in collaboration zone and set the target to host.
 * If you want to set centralized-two-stage scheduling architecture, you need to use center inter-scheduler in collaboration zone and set the target to dc and to set the isSupportForward of the center inter-scheduler to false and set intra-scheduler for each datacenter.
 * If you want to set distributed-two-stage scheduling architecture, you must not set center inter-scheduler in collaboration zone and set the inter-scheduler for each datacenter and set the target to mixed.
 * If you want to set mixed-two-stage scheduling architecture, you need to use center inter-scheduler in collaboration zone and set the target to dc and to set the isSupportForward of the center inter-scheduler to true and set inter-scheduler for each datacenter.
 * <p>
 * When the research scenario has only one data center and does not require additional inter-data center scheduling components,
 * you can only define the data center information in json, as follows:
 * {
 * "id": 1, // The id of the datacenter, Note: the id of the datacenter cannot be 0, because 0 is the id of the cloud administrator. And the id of the datacenter should be unique.
 * "region": "us-east1", // The region of the datacenter. It's mostly about network.
 * ...// Other information for data centers which is similar to above.
 * }
 *
 * @author Anonymous
 * @since LGDCloudSim 1.0
 */
public class InitDatacenter {
    private static Logger LOGGER = LoggerFactory.getLogger(InitDatacenter.class.getSimpleName());
    /**
     * The {@link Simulation} object.
     **/
    private static Simulation LGDCloudSim;

    /**
     * The {@link Factory} object.
     **/
    private static Factory factory;

    /**
     * The id of the inter-scheduler.
     */
    private static int interSchedulerId = 0;

    /**
     * The id of the intra-scheduler.
     */
    private static int intraSchedulerId = 0;

    /**
     * The id of the datacenter.
     */
    private static int datacenterId = 1;

    /**
     * Initialize datacenters with the simulation and factory and the path of the json file.
     *
     * @param LGDCloudSim the {@link Simulation} object
     * @param factory     the {@link Factory} object
     * @param filePath    the path of the json file
     **/
    public static void initDatacenters(Simulation LGDCloudSim, Factory factory, String filePath) {
        InitDatacenter.LGDCloudSim = LGDCloudSim;
        InitDatacenter.factory = factory;
        JsonObject jsonObject = readJsonFile(filePath);

        if (jsonObject.containsKey("collaborations")) {
            initMultiDatacenters(jsonObject);
        } else {
            initSingleDatacenter(jsonObject);
        }
    }

    /**
     * Initialize multiple datacenters scenario.
     *
     * @param jsonObject the json object of multiple datacenters scenario
     */
    private static void initMultiDatacenters(JsonObject jsonObject) {
        CollaborationManager collaborationManager = new CollaborationManagerSimple(LGDCloudSim);

        initDatacentersWithoutInterSchedulers(jsonObject, collaborationManager);
        addInterSchedulers(jsonObject, collaborationManager);
    }

    /**
     * Initialize the datacenters without the center inter-schedulers.
     *
     * @param jsonObject           the json object of the collaboration zones
     * @param collaborationManager the {@link CollaborationManager} object
     */
    private static void initDatacentersWithoutInterSchedulers(JsonObject jsonObject, CollaborationManager collaborationManager) {
        for (int i = 0; i < jsonObject.getJsonArray("collaborations").size(); i++) {
            JsonObject collaborationJson = jsonObject.getJsonArray("collaborations").getJsonObject(i);
            int collaborationId = collaborationJson.getInt("id");

            boolean isCenterSchedule = collaborationJson.containsKey("centerSchedulers");
            int target = InterSchedulerSimple.NULL;
            boolean isSupportForward = false;
            if (isCenterSchedule) {
                target = getInterScheduleTarget(collaborationJson.getJsonObject("centerSchedulers"));
            }
            if (target == InterSchedulerSimple.DC_TARGET) {
                isSupportForward = collaborationJson.getJsonObject("centerSchedulers").getBoolean("isSupportForward");
            }

            for (int j = 0; j < collaborationJson.getJsonArray("datacenters").size(); j++) {
                JsonObject datacenterJson = collaborationJson.getJsonArray("datacenters").getJsonObject(j);
                Datacenter datacenter = getDatacenter(datacenterJson, collaborationId, isCenterSchedule, target, isSupportForward);
                collaborationManager.addDatacenter(datacenter, collaborationId);
            }
        }
    }

    /**
     * Add the center inter-schedulers to the collaboration zones.
     *
     * @param jsonObject           the json object of the collaboration zones
     * @param collaborationManager the {@link CollaborationManager} object
     */
    private static void addInterSchedulers(JsonObject jsonObject, CollaborationManager collaborationManager) {
        for (int i = 0; i < jsonObject.getJsonArray("collaborations").size(); i++) {
            JsonObject collaborationJson = jsonObject.getJsonArray("collaborations").getJsonObject(i);
            int collaborationId = collaborationJson.getInt("id");

            boolean isCenterSchedule = collaborationJson.containsKey("centerSchedulers");
            int target = InterSchedulerSimple.NULL;
            boolean isSupportForward = false;
            if (isCenterSchedule) {
                target = getInterScheduleTarget(collaborationJson.getJsonObject("centerSchedulers"));
            }
            if (target == InterSchedulerSimple.DC_TARGET) {
                isSupportForward = collaborationJson.getJsonObject("centerSchedulers").getBoolean("isSupportForward");
            }

            if (isCenterSchedule) {
                JsonObject loadBalanceJson = collaborationJson.getJsonObject("interLoadBalancer");
                LoadBalancer<InstanceGroup, InterScheduler> loadBalancer = factory.getLoadBalance(loadBalanceJson.getString("type"));
                collaborationManager.addInterLoadBalancer(collaborationId, loadBalancer);

                JsonObject centerSchedulerJson = collaborationJson.getJsonObject("centerSchedulers");
                List<InterScheduler> interSchedulers = initInterSchedulers(centerSchedulerJson, collaborationId, collaborationManager, null);
                collaborationManager.addCenterSchedulers(collaborationId, interSchedulers);
            }

            if (isNeedInterSchedulerForDc(isCenterSchedule, target, isSupportForward)) {
                initInterSchedulersForDcs(collaborationJson.getJsonArray("datacenters"), collaborationId, collaborationManager);
            }
        }
    }

    /**
     * Initialize multiple inter-schedulers.
     *
     * @param interSchedulerJson   the json object of the inter-scheduler
     * @param collaborationId      the id of the collaboration zone
     * @param collaborationManager the {@link CollaborationManager} object
     * @return a list of {@link InterScheduler} objects
     */
    private static List<InterScheduler> initInterSchedulers(JsonObject interSchedulerJson, int collaborationId, CollaborationManager collaborationManager, Datacenter datacenter) {
        List<InterScheduler> interSchedulers = new ArrayList<>();
        JsonObject.ValueType valueType = interSchedulerJson.get("type").getValueType();
        String type;
        if (valueType == JsonObject.ValueType.ARRAY) {
            JsonArray interSchedulerTypesJson = interSchedulerJson.getJsonArray("type");
            for (int i = 0; i < interSchedulerTypesJson.size(); i++) {
                type = interSchedulerTypesJson.getString(i);
                InterScheduler interScheduler = initInterScheduler(type, interSchedulerJson, collaborationId, collaborationManager);
                if (datacenter != null) {
                    interScheduler.setDatacenter(datacenter);
                }
                interSchedulers.add(interScheduler);
            }
        } else {
            type = interSchedulerJson.getString("type");
            InterScheduler interScheduler = initInterScheduler(type, interSchedulerJson, collaborationId, collaborationManager);
            if (datacenter != null) {
                interScheduler.setDatacenter(datacenter);
            }
            interSchedulers.add(interScheduler);
        }

        return interSchedulers;
    }

    /**
     * Initialize single datacenter scenario.
     *
     * @param jsonObject the json object of single datacenter scenario
     */
    private static void initSingleDatacenter(JsonObject jsonObject) {
        LGDCloudSim.setSingleDatacenterFlag(true);
        CollaborationManager collaborationManager = new CollaborationManagerSimple(LGDCloudSim);
        int collaborationId = 0;
        Datacenter datacenter = getDatacenter(jsonObject, collaborationId, false, InterSchedulerSimple.NULL, false);
        collaborationManager.addDatacenter(datacenter, collaborationId);
    }

    /**
     * Initialize the inter-schedulers of the datacenters in the collaboration zone.
     *
     * @param datacenters          the json array of datacenters
     * @param collaborationId      the id of the collaboration zone
     * @param collaborationManager the {@link CollaborationManager} object
     */
    private static void initInterSchedulersForDcs(JsonArray datacenters, int collaborationId, CollaborationManager collaborationManager) {
        for (int j = 0; j < datacenters.size(); j++) {
            JsonObject datacenterJson = datacenters.getJsonObject(j);
            JsonObject interSchedulerJson = datacenterJson.getJsonObject("interSchedulers");
            if (interSchedulerJson == null) {
                throw new IllegalArgumentException("interScheduler should not be null");
            }
            int datacenterId = datacenterJson.getInt("id");
            Datacenter datacenter = collaborationManager.getDatacenterById(datacenterId);

            JsonObject loadBalanceJson = datacenterJson.getJsonObject("interLoadBalancer");
            LoadBalancer<InstanceGroup, InterScheduler> loadBalancer = factory.getLoadBalance(loadBalanceJson.getString("type"));
            datacenter.setInterLoadBalancer(loadBalancer);

            List<InterScheduler> interSchedulers = initInterSchedulers(interSchedulerJson, collaborationId, collaborationManager, datacenter);

            datacenter.setInterSchedulers(interSchedulers);
        }
    }

    /**
     * Initialize the inter-scheduler.
     *
     * @param type                the type of the inter-scheduler
     * @param interSchedulerJson   the json object of the inter-scheduler
     * @param collaborationId      the id of the collaboration zone
     * @param collaborationManager the {@link CollaborationManager} object
     * @return the {@link InterScheduler} object
     */
    private static InterScheduler initInterScheduler(String type, JsonObject interSchedulerJson, int collaborationId, CollaborationManager collaborationManager) {
        int target = getInterScheduleTarget(interSchedulerJson);
        boolean isSupportForward = false;
        if (target == InterSchedulerSimple.DC_TARGET) {
            isSupportForward = interSchedulerJson.getBoolean("isSupportForward");
        }

        InterScheduler interScheduler = factory.getInterScheduler(type, interSchedulerId++, LGDCloudSim, collaborationId, target, isSupportForward);

        Object[] dcStateSynIntervalAndType = getDcStateSynIntervalAndType(interSchedulerJson, collaborationManager);
        interScheduler.setDcStateSynInterval((Map<Datacenter, Double>) dcStateSynIntervalAndType[0]);
        interScheduler.setDcStateSynType((Map<Datacenter, String>) dcStateSynIntervalAndType[1]);
        return interScheduler;
    }

    /**
     * Get the target of the inter-scheduler from the json object.
     *
     * @param centerSchedulerJson the json object of the center inter-scheduler
     * @return the target of the inter-scheduler
     */
    private static int getInterScheduleTarget(JsonObject centerSchedulerJson) {
        String targetStr = centerSchedulerJson.getString("target");
        return switchTarget(targetStr);
    }

    /**
     * Get the target of the inter-scheduler.
     * It can be dc, host or mixed.
     *
     * @param targetStr the target string
     * @return the target of the inter-scheduler
     */
    private static int switchTarget(String targetStr) {
        return switch (targetStr) {
            case "dc", "datacenter" -> InterSchedulerSimple.DC_TARGET;
            case "host" -> InterSchedulerSimple.HOST_TARGET;
            case "mixed" -> InterSchedulerSimple.MIXED_TARGET;
            default -> throw new IllegalArgumentException("target should be dc, host or mixed");
        };
    }

    /**
     * Get the interval and type of state synchronization of the datacenter for inter-scheduler from the json object.
     *
     * @param dcStateSynInfoJson   the json object of the state synchronization information of the datacenter
     * @param collaborationManager the {@link CollaborationManager} object
     * @return an array of the interval and type of state synchronization
     */
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
        int id = 0;
        if (datacenterJson.containsKey("id")) {
            id = datacenterJson.getInt("id");
            datacenterId = id;
        } else {
            id = ++datacenterId;
        }

        if (id == 0) {
            throw new IllegalArgumentException("0 is the id of CIS,Datacenter id should not be 0");
        }
        Datacenter datacenter = new DatacenterSimple(LGDCloudSim, id);

        addRegionInfo(datacenter, datacenterJson);

        if (datacenterJson.containsKey("name")) {
            datacenter.setName(datacenterJson.getString("name"));
        }

        if (datacenterJson.containsKey("architecture")) {
            datacenter.setArchitecture(datacenterJson.getString("architecture"));
        }

        StatesManager statesManager = getStatesManager(datacenterJson, isCenterSchedule, target);
        datacenter.setStatesManager(statesManager);

        if (isCenterSchedule) {
            datacenter.setCentralizedInterScheduleFlag(true);
        }
        if (isNeedIntraScheduler(isCenterSchedule, target, isSupportForward)) {
            JsonObject loadBalanceJson = datacenterJson.getJsonObject("intraLoadBalancer");
            LoadBalancer<Instance, IntraScheduler> loadBalancer = factory.getLoadBalance(loadBalanceJson.getString("type"));
            datacenter.setIntraLoadBalancer(loadBalancer);

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

    /**
     * Add region information and location information to the datacenter.
     *
     * @param datacenter     the {@link Datacenter} object
     * @param datacenterJson the json object of the datacenter
     */
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

    /**
     * Get whether the inter-scheduler is needed for the datacenter.
     * It is decided by the scheduling architecture.
     *
     * @param isCenterSchedule whether the center inter-scheduler is needed
     * @param target           the target of the center inter-scheduler
     * @param isSupportForward whether the center inter-scheduler supports forwarding
     * @return whether the inter-scheduler is needed for the datacenter
     */
    private static boolean isNeedInterSchedulerForDc(boolean isCenterSchedule, int target, boolean isSupportForward) {
        return (!LGDCloudSim.isSingleDatacenterFlag()) && ((isCenterSchedule && isSupportForward) || (!isCenterSchedule));
    }

    /**
     * Get whether the intra-scheduler is needed for the datacenter.
     * It is decided by the scheduling architecture.
     *
     * @param isCenterSchedule whether the center inter-scheduler is needed
     * @param target           the target of the center inter-scheduler
     * @param isSupportForward whether the center inter-scheduler supports forwarding
     * @return whether the intra-scheduler is needed for the datacenter
     */
    private static boolean isNeedIntraScheduler(boolean isCenterSchedule, int target, boolean isSupportForward) {
        return !((isCenterSchedule && target == InterSchedulerSimple.HOST_TARGET)
                || (!isCenterSchedule && target == InterSchedulerSimple.MIXED_TARGET)
                || (isCenterSchedule && target == InterSchedulerSimple.DC_TARGET && isSupportForward));
    }

    /**
     * From a {@link JsonObject} object to get all IntraSchedulers.
     *
     * @param datacenterJson a {@link JsonObject} object
     * @param partitionNum   the number of partitions
     * @return a list of {@link IntraScheduler} objects
     */
    private static List<IntraScheduler> getIntraSchedulers(JsonObject datacenterJson, int partitionNum) {
        List<IntraScheduler> intraSchedulers = new ArrayList<>();
        int firstSynPartitionId = 0;
        for (int k = 0; k < datacenterJson.getJsonArray("intraSchedulers").size(); k++) {
            JsonObject schedulerJson = datacenterJson.getJsonArray("intraSchedulers").getJsonObject(k);
            if (schedulerJson.containsKey("firstPartitionId")) {
                firstSynPartitionId = schedulerJson.getInt("firstPartitionId");
            } else {
                LOGGER.info("IntraScheduler {} Missing firstPartitionId, defaults to 0", k);
            }
            IntraScheduler scheduler = factory.getIntraScheduler(schedulerJson.getString("type"), intraSchedulerId++, firstSynPartitionId, partitionNum);
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
        double synchronizationGap = 0;
        if (isCenterSchedule && target == InterSchedulerSimple.HOST_TARGET) {
            synchronizationGap = 0;
        } else {
            if (datacenterJson.containsKey("synchronizationGap")) {
                synchronizationGap = datacenterJson.getJsonNumber("synchronizationGap").doubleValue();
            }
        }
        int[] maxCpuRam = getMaxCpuRam(datacenterJson);

        int heartbeatInterval = 0;
        if (datacenterJson.containsKey("heartbeatInterval")) {
            heartbeatInterval = datacenterJson.getInt("heartbeatInterval", 0);
        }

        StatesManager statesManager = new StatesManagerSimple(hostNum, partitionRangesManager, synchronizationGap, heartbeatInterval, maxCpuRam[0], maxCpuRam[1]);

        setPrediction(statesManager, datacenterJson);

        initHostState(statesManager, datacenterJson);

        return statesManager;
    }

    /**
     * Get the maximum CPU and RAM of the hosts in the datacenter.
     *
     * @param datacenterJson a {@link JsonObject} object
     * @return an array of the maximum CPU and RAM
     */
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

    /**
     * Set the resource unit price of the datacenter.
     * It includes the unit price of CPU, RAM, rack, storage and bandwidth.
     * The bandwidth billing type has two types: used and fixed.
     * If the bandwidth billing type is used, the bandwidth utilization should be set.
     * It means the bandwidth price is calculated based on the actual amount of network data used.
     * If the bandwidth billing type is fixed, the bandwidth utilization should not be set.
     * It means the bandwidth price is calculated based on the actual bandwidth speed used.
     *
     * @param datacenter    the {@link Datacenter} object
     * @param unitPriceJson the json object of the resource unit price
     */
    private static void setDatacenterResourceUnitPrice(Datacenter datacenter, JsonObject unitPriceJson) {
        if (unitPriceJson == null) {
            return;
        }
        if (unitPriceJson.containsKey("pricePerCpuPerSec")) {
            double unitCpuPrice = unitPriceJson.getJsonNumber("pricePerCpuPerSec").doubleValue();
            datacenter.setPricePerCpuPerSec(unitCpuPrice);
        }
        if (unitPriceJson.containsKey("pricePerCpu")) {
            double unitCpuPrice = unitPriceJson.getJsonNumber("pricePerCpu").doubleValue();
            datacenter.setPricePerCpu(unitCpuPrice);
        }
        if (unitPriceJson.containsKey("pricePerRamPerSec")) {
            double unitRamPrice = unitPriceJson.getJsonNumber("pricePerRamPerSec").doubleValue();
            datacenter.setPricePerRamPerSec(unitRamPrice);
        }
        if (unitPriceJson.containsKey("pricePerRam")) {
            double unitRamPrice = unitPriceJson.getJsonNumber("pricePerRam").doubleValue();
            datacenter.setPricePerRam(unitRamPrice);
        }
        if (unitPriceJson.containsKey("pricePerStoragePerSec")) {
            double unitStoragePrice = unitPriceJson.getJsonNumber("pricePerStoragePerSec").doubleValue();
            datacenter.setPricePerStoragePerSec(unitStoragePrice);
        }
        if (unitPriceJson.containsKey("pricePerStorage")) {
            double unitStoragePrice = unitPriceJson.getJsonNumber("pricePerStorage").doubleValue();
            datacenter.setPricePerStorage(unitStoragePrice);
        }
        if (unitPriceJson.containsKey("pricePerBwPerSec")) {
            double unitBwPrice = unitPriceJson.getJsonNumber("pricePerBwPerSec").doubleValue();
            datacenter.setPricePerBwPerSec(unitBwPrice);
        }
        if (unitPriceJson.containsKey("pricePerBw")) {
            double unitBwPrice = unitPriceJson.getJsonNumber("pricePerBw").doubleValue();
            datacenter.setPricePerBw(unitBwPrice);
        }
        if (unitPriceJson.containsKey("pricePerRack")) {
            double unitRackPrice = unitPriceJson.getJsonNumber("pricePerRack").doubleValue();
            datacenter.setUnitRackPrice(unitRackPrice);
        }
        if (unitPriceJson.containsKey("HostNumPerRack")) {
            double hostNumPeerRack = unitPriceJson.getJsonNumber("HostNumPerRack").doubleValue();
            datacenter.setHostNumPerRack(hostNumPeerRack);
        }
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
    }
}
