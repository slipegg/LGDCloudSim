package org.scalecloudsim.datacenters;

import lombok.NonNull;
import org.cloudsimplus.core.CloudSimEntity;
import org.cloudsimplus.core.CloudSimTag;
import org.cloudsimplus.core.SimEntity;
import org.cloudsimplus.core.Simulation;
import org.cloudsimplus.core.events.SimEvent;
import org.scalecloudsim.Instances.InstanceGroup;
import org.scalecloudsim.Instances.UserRequest;
import org.scalecloudsim.statemanager.StateManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DatacenterSimple extends CloudSimEntity implements Datacenter{
    public Logger LOGGER = LoggerFactory.getLogger(DatacenterSimple.class.getSimpleName());
    private Set<Integer> collaborationIds;
    private GroupQueue groupQueue;
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
    public DatacenterSimple(@NonNull Simulation simulation,int id) {
        this(simulation);
        this.setId(id);
    }
    @Override
    public Datacenter setStateManager(StateManager stateManager) {
        return null;
    }

    @Override
    public StateManager getStateManager() {
        return null;
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
            default ->
                    LOGGER.warn("{}: {} received unknown event {}", getSimulation().clockStr(), getName(), evt.getTag());
        }
    }

    private void processUserRequestsSend(final SimEvent evt) {
        if(evt.getData() instanceof List<?> userRequests) {
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
        List<InstanceGroup> instanceGroups = groupQueue.getInstanceGroups();
        //得到其他数据中心的基础信息和资源抽样信息

        //进行域间调度
        /*
         * 0.得到同一个协作区的所有数据中心
         * 1.根据接入时延要求得到可调度的数据中心
         * 2.根据资源抽样信息得到可调度的数据中心
         * 3.根据网络时延和宽带情况以及抽样信息得到最优的调度方案
         * 4.如果亲和组只有一个调度方案直接发送过去继续调度
         * 4.如果没有可以的调度方案就将其返回给亲和组队列
         * 4.一个亲和组有多个可调度的数据中心，询问数据中心能否进行调度
         */
    }

    @Override
    public int compareTo(SimEntity o) {
        return Comparator.comparing(SimEntity::getId).compare(this, o);
    }
}