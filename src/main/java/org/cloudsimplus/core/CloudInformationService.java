/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */
package org.cloudsimplus.core;

import lombok.Getter;
import lombok.NonNull;
import org.cloudsimplus.core.events.SimEvent;
import org.scalecloudsim.datacenter.Datacenter;
import org.scalecloudsim.request.Instance;
import org.scalecloudsim.request.InstanceGroup;
import org.scalecloudsim.request.InstanceGroupEdge;
import org.scalecloudsim.request.UserRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * A Cloud Information Service (CIS) is an entity that provides cloud resource
 * registration, indexing and discovery services. The Cloud datacenters tell their
 * readiness to process Cloudlets by registering themselves with this entity.
 * Other entities such as the broker can contact this class for
 * resource discovery service, which returns a list of registered resource.
 *
 * <p>
 * In summary, it acts like a yellow page service.
 * An instance of this class is automatically created by CloudSimPlus upon initialisation of the simulation.
 * </p>
 *
 * @author Manzur Murshed
 * @author Rajkumar Buyya
 * @since CloudSim Toolkit 1.0
 */
public class CloudInformationService extends CloudSimEntity {
    private static final Logger LOGGER = LoggerFactory.getLogger(CloudInformationService.class.getSimpleName());

    @Getter
    private final List<Datacenter> datacenterList;

    /**
     * Creates a new entity.
     *
     * @param simulation The CloudSimPlus instance that represents the simulation the Entity belongs to
     * @throws IllegalArgumentException when the entity name is invalid
     */
    public CloudInformationService(@NonNull Simulation simulation) {
        super(simulation);
        this.datacenterList = new ArrayList<>();
    }

    @Override
    protected void startInternal() {

    }

    @Override
    public void processEvent(SimEvent evt) {
        switch (evt.getTag()) {
//            case CloudSimTag.REGISTER_REGIONAL_CIS -> cisList.add((CloudInformationService) evt.getData());
//            case CloudSimTag.REQUEST_REGIONAL_CIS -> super.send(evt.getSource(), 0, evt.getTag(), cisList);
            case CloudSimTag.DC_REGISTRATION_REQUEST -> datacenterList.add((Datacenter) evt.getData());
            // A Broker is requesting a list of all datacenters.
            case CloudSimTag.DC_LIST_REQUEST -> super.send(evt.getSource(), 0, evt.getTag(), datacenterList);
            case CloudSimTag.USER_REQUEST_FAIL -> processUserRequestFail(evt);
        }
    }

    private void processUserRequestFail(SimEvent evt) {
        if (evt.getData() instanceof UserRequest userRequest) {
            if (userRequest.getState() == UserRequest.FAILED) {
                return;
            }
            LOGGER.warn("{}: The UserRequest{} has failed. Reason: {}", getSimulation().clockStr(), userRequest.getId(), userRequest.getFailReason());
            userRequest.setState(UserRequest.FAILED);
            userRequest.setFinishTime(getSimulation().clock());
            getSimulation().getSqlRecord().recordUserRequestFinishInfo(userRequest);
            //释放Bw资源
            List<InstanceGroupEdge> allocateEdges = userRequest.getAllocatedEdges();
            for (InstanceGroupEdge allocateEdge : allocateEdges) {
                double allocatedBw = allocateEdge.getRequiredBw();
                Datacenter src = allocateEdge.getSrc().getReceiveDatacenter();
                Datacenter dest = allocateEdge.getDst().getReceiveDatacenter();
                if (src != null && dest != null) {
                    getSimulation().getNetworkTopology().releaseBw(src, dest, allocatedBw);
                }
            }
            //释放主机资源,结束已经在运行的任务,并且记录未运行的instance
            for (InstanceGroup instanceGroup : userRequest.getInstanceGroups()) {
                instanceGroup.setState(UserRequest.FAILED);
                instanceGroup.setFinishTime(getSimulation().clock());
                if (instanceGroup.getState() == UserRequest.SCHEDULING) {
                    getSimulation().getSqlRecord().recordInstanceGroupFinishInfo(instanceGroup);
                } else {
                    getSimulation().getSqlRecord().recordInstanceGroupAllInfo(instanceGroup);
                }
                for (Instance instance : instanceGroup.getInstanceList()) {
                    if (instance.getState() == UserRequest.RUNNING) {
                        send(instance.getInstanceGroup().getReceiveDatacenter(), 0, CloudSimTag.END_INSTANCE_RUN, instance);
                    } else {
                        instance.setState(UserRequest.FAILED);
                        instance.setFinishTime(getSimulation().clock());
                        getSimulation().getSqlRecord().recordInstanceAllInfo(instance);
                    }
                }
            }
        }
    }
}