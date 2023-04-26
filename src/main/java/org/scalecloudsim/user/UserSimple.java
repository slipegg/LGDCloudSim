package org.scalecloudsim.user;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.cloudsimplus.core.CloudSimEntity;
import org.cloudsimplus.core.CloudSimTag;
import org.cloudsimplus.core.Simulation;
import org.cloudsimplus.core.events.SimEvent;
import org.scalecloudsim.request.UserRequest;
import org.scalecloudsim.datacenter.Datacenter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;


public class UserSimple extends CloudSimEntity {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserSimple.class.getSimpleName());
    @Getter@Setter
    private List<Datacenter> datacenterList;
    @Getter@Setter
    double sendOnceInterval;
    @Getter@Setter
    UserRequestManager userRequestManager;
    @Getter
    @Setter
    boolean isSendLater = true;
    int sendCount = 2;
//    private Datacenter
    /**
     * Creates a new entity.
     *
     * @param simulation The CloudSimPlus instance that represents the simulation the Entity belongs to
     * @throws IllegalArgumentException when the entity name is invalid
     */
    public UserSimple(@NonNull Simulation simulation,double sendOnceInterval,UserRequestManager userRequestManager) {
        super(simulation);
        this.sendOnceInterval = sendOnceInterval;
        this.userRequestManager = userRequestManager;
    }

    @Override
    protected void startInternal() {
        LOGGER.info("{} is starting...", getName());
        schedule(getSimulation().getCis(), 0, CloudSimTag.DC_LIST_REQUEST);
    }

    @Override
    public void processEvent(SimEvent evt) {
        switch (evt.getTag()) {
            case CloudSimTag.DC_LIST_REQUEST -> processDatacenterListRequest(evt);
            case CloudSimTag.NEED_SEND_USER_REQUEST -> sendUserRequest();
            default -> LOGGER.warn("{} received an unknown event tag: {}", getName(), evt.getTag());
        }
    }
    private void processDatacenterListRequest(final SimEvent evt) {
        if(evt.getData() instanceof List dcList) {
            setDatacenterList(dcList);
            LOGGER.info("{}: {}: List of {} datacenters(s) received.", getSimulation().clockStr(), getName(), this.datacenterList.size());
            sendUserRequest();
            return;
        }

        LOGGER.error("The date type of "+evt+"is not List<Datacenter>");
    }
    private void sendUserRequest() {
        double nowTime = getSimulation().clock();
        Map<Integer, List<UserRequest>> userRequestMap = userRequestManager.generateOnceUserRequests();
        if (userRequestMap == null) {
            LOGGER.info("{}: {}: No user request to send.", getSimulation().clockStr(), getName());
            return;
        }
        for (Map.Entry<Integer, List<UserRequest>> entry : userRequestMap.entrySet()) {
            int datacenterId = entry.getKey();
            List<UserRequest> userRequests = entry.getValue();
            if (userRequests.size() == 0)
                continue;
            Datacenter datacenter = datacenterList.get(datacenterId);
            send(datacenter, nowTime, CloudSimTag.USER_REQUEST_SEND, userRequests);
            LOGGER.info("{}: {}: Sending {} request to {}", getSimulation().clockStr(), getName(), userRequests.size(), datacenter.getName());
        }
        send(this, userRequestManager.getNextSendTime() - nowTime, CloudSimTag.NEED_SEND_USER_REQUEST, null);
        LOGGER.info("{}: {} will send user request after {} seconds", getSimulation().clockStr(), getName(), sendOnceInterval);
    }

}
