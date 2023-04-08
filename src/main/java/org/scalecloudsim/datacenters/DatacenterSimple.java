package org.scalecloudsim.datacenters;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.cloudsimplus.core.CloudSimEntity;
import org.cloudsimplus.core.CloudSimTag;
import org.cloudsimplus.core.SimEntity;
import org.cloudsimplus.core.Simulation;
import org.cloudsimplus.core.events.SimEvent;
import org.cloudsimplus.network.topologies.NetworkTopology;
import org.scalecloudsim.Instances.Instance;
import org.scalecloudsim.Instances.InstanceGroup;
import org.scalecloudsim.Instances.UserRequest;
import org.scalecloudsim.statemanager.StateManager;
import org.scalecloudsim.statemanager.StateManagerSimple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class DatacenterSimple extends CloudSimEntity implements Datacenter {
    public Logger LOGGER = LoggerFactory.getLogger(DatacenterSimple.class.getSimpleName());
    private Set<Integer> collaborationIds;
    private GroupQueue groupQueue;
    @Getter
    private StateManager stateManager;
    @Getter
    private int hostNum;

    /**
     * Creates a new entity.
     *
     * @param simulation The CloudSimPlus instance that represents the simulation the Entity belongs to
     * @throws IllegalArgumentException when the entity name is invalid
     */
    public DatacenterSimple(@NonNull Simulation simulation) {
        super(simulation);
        this.collaborationIds = new HashSet<>();
        this.groupQueue = new GroupQueueFifo();
    }

    public DatacenterSimple(@NonNull Simulation simulation, int id) {
        this(simulation);
        this.setId(id);
    }

    public DatacenterSimple(@NonNull Simulation simulation, int id, int hostNum) {
        this(simulation);
        this.setId(id);
        this.stateManager = new StateManagerSimple(hostNum, simulation);
    }


    @Override
    public Datacenter addCollaborationId(int collaborationId) {
        if (collaborationIds.contains(collaborationId)) {
            LOGGER.warn("the datacenter(" + this + ") already belongs to the collaboration " + collaborationId);
        } else {
            collaborationIds.add(collaborationId);
        }
        return this;
    }

    @Override
    public Datacenter removeCollaborationId(int collaborationId) {
        if (!collaborationIds.contains(collaborationId)) {
            LOGGER.warn("the datacenter(" + this + ") does not belong to the collaboration " + collaborationId + " to be removed");
        } else {
            collaborationIds.remove(collaborationId);
        }
        return this;
    }

    @Override
    public Set<Integer> getCollaborationIds() {
        return collaborationIds;
    }

    @Override
    protected void startInternal() {
        LOGGER.info("{}: {} is starting...", getSimulation().clockStr(), getName());
        sendNow(getSimulation().getCis(), CloudSimTag.DC_REGISTRATION_REQUEST, this);
    }

    @Override
    public void processEvent(SimEvent evt) {
        switch (evt.getTag()) {
            case CloudSimTag.USER_REQUEST_SEND -> processUserRequestsSend(evt);
            case CloudSimTag.INTER_SCHEDULE -> processInterSchedule();
            case CloudSimTag.ASK_DC_REVIVE_GROUP -> processAskDcReviveGroup(evt);
            case CloudSimTag.RESPOND_DC_REVIVE_GROUP_ACCEPT -> processRespondDcReviveGroupAccept(evt);
            case CloudSimTag.RESPOND_DC_REVIVE_GROUP_REJECT -> processRespondDcReviveGroupReject(evt);
            default ->
                    LOGGER.warn("{}: {} received unknown event {}", getSimulation().clockStr(), getName(), evt.getTag());
        }
    }

    private void processRespondDcReviveGroupAccept(SimEvent evt) {

    }

    private void processRespondDcReviveGroupReject(SimEvent evt) {

    }

    private void processAskDcReviveGroup(SimEvent evt) {
        if (evt.getData() instanceof List<?> instanceGroups) {
            LOGGER.info("{}: {} received {} instance groups from {} to schedule.", getSimulation().clockStr(), getName(), instanceGroups.size(), evt.getSource().getName());
            Map<InstanceGroup, Boolean> reviveGroupResult = getReviveGroupResult((List<InstanceGroup>) instanceGroups);
            double costTime = instanceGroups.size() * 0.1;//TODO 为了模拟没有随机性，先设置为每一个亲和组调度花费0.1ms
            respondAskDcReviveGroup(evt.getSource(), reviveGroupResult, costTime);
        }
    }

    private Map<InstanceGroup, Boolean> getReviveGroupResult(List<InstanceGroup> instanceGroups) {
        //TODO 怎么判断是否接收，如果接收了怎么进行资源预留
        Map<InstanceGroup, Boolean> result = new HashMap<>();
        for (InstanceGroup instanceGroup : instanceGroups) {
            result.put(instanceGroup, true);
        }
        return result;
    }

    private void respondAskDcReviveGroup(SimEntity dst, Map<InstanceGroup, Boolean> reviveGroupResult, double costTime) {
        List<InstanceGroup> acceptedGroups = new ArrayList<>();
        List<InstanceGroup> rejectedGroups = new ArrayList<>();
        for (Map.Entry<InstanceGroup, Boolean> entry : reviveGroupResult.entrySet()) {
            if (entry.getValue()) {
                acceptedGroups.add(entry.getKey());
            } else {
                rejectedGroups.add(entry.getKey());
            }
        }
        LOGGER.info("{}: {} is responding {} accepted groups and {} rejected groups to {}.", getSimulation().clockStr(), getName(), acceptedGroups.size(), rejectedGroups.size(), dst.getName());
        sendBetweenDc(dst, costTime, CloudSimTag.RESPOND_DC_REVIVE_GROUP_ACCEPT, acceptedGroups);
        sendBetweenDc(dst, costTime, CloudSimTag.RESPOND_DC_REVIVE_GROUP_REJECT, rejectedGroups);
    }

    private void processUserRequestsSend(final SimEvent evt) {
        if (evt.getData() instanceof List<?> userRequests) {
            for (Object userRequest : userRequests) {
                if (userRequest instanceof UserRequest) {
                    groupQueue.addInstanceGroups((UserRequest) userRequest);
                }
            }
            LOGGER.info("{}: {} received {} user request.The size of InstanceGroup queue is {}.", getSimulation().clockStr(), getName(), userRequests.size(), groupQueue.getGroupNum());
        }
        sendNow(this, CloudSimTag.INTER_SCHEDULE);
    }

    private void processInterSchedule() {
        //得到本轮需要进行域间调度的亲和组
        List<InstanceGroup> instanceGroups = groupQueue.getInstanceGroups();
        LOGGER.info("{}: {} is processing inter schedule for {} instance groups.", getSimulation().clockStr(), getName(), instanceGroups.size());
        //得到其他数据中心的基础信息和资源抽样信息
        List<Datacenter> allDatacenters = getSimulation().getCollaborationManager().getDatacenters(this);
        NetworkTopology networkTopology = getSimulation().getNetworkTopology();
        //根据接入时延和资源抽样信息得到每个亲和组可调度的数据中心
//        double start = System.currentTimeMillis();
        Map<InstanceGroup, List<Datacenter>> instanceGroupAvaiableDatacenters = new HashMap<>();
        for (InstanceGroup instanceGroup : instanceGroups) {
            List<Datacenter> availableDatacenters = getAvaiableDatacenters(instanceGroup, new ArrayList<>(allDatacenters), networkTopology);
            if (availableDatacenters.size() > 0) {
                instanceGroupAvaiableDatacenters.put(instanceGroup, availableDatacenters);
            } else {
                //TODO 进入域间调度失败处理
                interScheduleFail(instanceGroup);
            }
        }
        filterDatacentersByNetworkTopology(instanceGroupAvaiableDatacenters, networkTopology);
//        double costTime = (System.currentTimeMillis() - start) / 10;//假设在集群调度器中的性能更强。只需要花费十分之一的时间
        double costTime = instanceGroups.size() * 0.1;//TODO 为了模拟没有随机性，先设置为每一个亲和组调度花费0.1ms
        LOGGER.info("{}: {} inter scheduling cost {} ms.", getSimulation().clockStr(), getName(), costTime);
        interScheduleByResult(instanceGroupAvaiableDatacenters, costTime);
        if (groupQueue.getGroupNum() > 0) {
            send(this, costTime, CloudSimTag.INTER_SCHEDULE, null);
        }
        //根据网络拓扑中的时延和宽带情况对整个一批的进行排序
        //进行域间调度
        /*
         * 0.得到同一个协作区的所有数据中心
         * 1.根据接入时延要求得到可调度的数据中心
         * 2.根据个数据中心的资源抽样信息得到可调度的数据中心
         * 3.在可调度的数据中心中根据网络时延和宽带情况，每个个亲和组都可能会得到多个的调度方案
         * 4.将亲和组发送给调度方案中的各个数据中心进行询问
         * 4.如果没有可以的调度方案就当做失败将其返回给亲和组队列等待下次调度
         * 5.各个数据中心接收亲和组调度请求，并进行决策，决定是否接收该亲和组，如果决定接收就为其预留资源，并返回结果信息
         * 6.原数据中心接收发送出去的各个亲和组的调度结果，进行最终决策，为其决定指定的数据中心，并发送信息释放其他数据中心的资源
         * 7.各个数据中心如果接收到释放资源的消息，就释放资源，如果接收到确认亲和组放置的信息就将其实例放入到域内实例调度请求队列中
         */
    }

    //TODO 如果前一个亲和组被可能被分配给多个数据中心，那么后一个亲和组在分配的时候应该如何更新资源状态。目前是不考虑
    private List<Datacenter> getAvaiableDatacenters(InstanceGroup instanceGroup, List<Datacenter> allDatacenters, NetworkTopology networkTopology) {
        //根据接入时延要求得到可调度的数据中心
        filterDatacentersByAccessLatency(instanceGroup, allDatacenters, networkTopology);
        //根据资源抽样信息得到可调度的数据中心
        filterDatacentersByResourceSample(instanceGroup, allDatacenters);
        //根据网络时延和宽带情况以及抽样信息得到最优的调度方案
        return allDatacenters;
    }

    private void filterDatacentersByAccessLatency(InstanceGroup instanceGroup, List<Datacenter> allDatacenters, NetworkTopology networkTopology) {
        allDatacenters.removeIf(datacenter -> instanceGroup.getAcessLatency() < networkTopology.getAcessLatency(this));
    }

    private void filterDatacentersByResourceSample(InstanceGroup instanceGroup, List<Datacenter> allDatacenters) {
        allDatacenters.removeIf(
                datacenter -> datacenter.getStateManager().getSimpleState().getCpuAvaiableSum() < instanceGroup.getCpuSum() ||
                        datacenter.getStateManager().getSimpleState().getRamAvaiableSum() < instanceGroup.getRamSum() ||
                        datacenter.getStateManager().getSimpleState().getStorageAvaiableSum() < instanceGroup.getStorageSum() ||
                        datacenter.getStateManager().getSimpleState().getBwAvaiableSum() < instanceGroup.getBwSum()
        );
        for (Datacenter datacenter : allDatacenters) {
            Map<Integer, Map<Integer, Integer>> instanceCpuRamNum = new HashMap<>();//记录一下所有Instance的cpu—ram的种类情况
            for (Instance instance : instanceGroup.getInstanceList()) {
                int allocateNum = instanceCpuRamNum.getOrDefault(instance.getCpu(), new HashMap<>()).getOrDefault(instance.getRam(), 0);
                if (datacenter.getStateManager().getSimpleState().getCpuRamSum(instance.getCpu(), instance.getRam()) - allocateNum <= 0) {
                    //如果该数据中心的资源不足以满足亲和组的资源需求，那么就将其从可调度的数据中心中移除
                    allDatacenters.remove(datacenter);
                    break;
                } else {
                    //如果该数据中心的资源可以满足亲和组的资源需求，那么就记录更新所有Instance的cpu—ram的种类情况
                    if (instanceCpuRamNum.containsKey(instance.getCpu())) {
                        Map<Integer, Integer> ramNumMap = instanceCpuRamNum.get(instance.getCpu());
                        if (ramNumMap.containsKey(instance.getRam())) {
                            ramNumMap.put(instance.getRam(), ramNumMap.get(instance.getRam()) + 1);
                        } else {
                            ramNumMap.put(instance.getRam(), 1);
                        }
                    } else {
                        Map<Integer, Integer> ramNumMap = new HashMap<>();
                        ramNumMap.put(instance.getRam(), 1);
                        instanceCpuRamNum.put(instance.getCpu(), ramNumMap);
                    }
                }
            }
        }
    }

    private void filterDatacentersByNetworkTopology(Map<InstanceGroup, List<Datacenter>> instanceGroupAvaiableDatacenters, NetworkTopology networkTopology) {
        //TODO 根据网络拓扑中的时延和宽带进行筛选得到最优的调度方案
    }

    //根据筛选情况进行调度
    private void interScheduleByResult(Map<InstanceGroup, List<Datacenter>> instanceGroupAvaiableDatacenters, double costTime) {
        Map<Datacenter, List<InstanceGroup>> sendMap = new HashMap<>();
        for (Map.Entry<InstanceGroup, List<Datacenter>> entry : instanceGroupAvaiableDatacenters.entrySet()) {
            InstanceGroup instanceGroup = entry.getKey();
            List<Datacenter> datacenters = entry.getValue();
            if (datacenters.size() == 0) {
                //如果没有可调度的数据中心，那么就将其返回给亲和组队列等待下次调度
                interScheduleFail(instanceGroup);
            } else {
                //如果有可调度的数据中心，那么就将其发送给可调度的数据中心
                for (Datacenter datacenter : datacenters) {
                    if (sendMap.containsKey(datacenter)) {
                        sendMap.get(datacenter).add(instanceGroup);
                    } else {
                        List<InstanceGroup> instanceGroups = new ArrayList<>();
                        instanceGroups.add(instanceGroup);
                        sendMap.put(datacenter, instanceGroups);
                    }
                }
            }
        }
        for (Map.Entry<Datacenter, List<InstanceGroup>> entry : sendMap.entrySet()) {
            Datacenter datacenter = entry.getKey();
            List<InstanceGroup> instanceGroups = entry.getValue();
            sendBetweenDc(datacenter, costTime, CloudSimTag.ASK_DC_REVIVE_GROUP, instanceGroups);
        }
    }

    private void interScheduleFail(InstanceGroup instanceGroup) {
        //TODO 如果亲和组调度失败，那么就将其返回给亲和组队列等待下次调度
    }

    @Override
    public int compareTo(SimEntity o) {
        return Comparator.comparing(SimEntity::getId).compare(this, o);
    }
}