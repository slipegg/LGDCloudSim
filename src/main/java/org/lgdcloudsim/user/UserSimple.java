package org.lgdcloudsim.user;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import org.lgdcloudsim.core.CloudActionTags;
import org.lgdcloudsim.core.CloudSimEntity;
import org.lgdcloudsim.core.Simulation;
import org.lgdcloudsim.core.events.SimEvent;
import org.lgdcloudsim.request.UserRequest;
import org.lgdcloudsim.datacenter.Datacenter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class UserSimple extends CloudSimEntity {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserSimple.class.getSimpleName());

    private Map<Integer, Datacenter> datacenterMap = new HashMap<>();

    private UserRequestManager userRequestManager;

    public UserSimple(@NonNull Simulation simulation, UserRequestManager userRequestManager) {
        super(simulation);
        this.userRequestManager = userRequestManager;
    }

    @Override
    protected void startInternal() {
        LOGGER.info("{}: {} is starting...", getSimulation().clockStr(), getName());
        schedule(getSimulation().getCis(), 0, CloudActionTags.DC_LIST_REQUEST);
    }

    @Override
    public void processEvent(SimEvent evt) {
        switch (evt.getTag()) {
            case DC_LIST_REQUEST -> processDatacenterListRequest(evt);
            case NEED_SEND_USER_REQUEST -> sendUserRequest();
            default -> LOGGER.warn("{} received an unknown event tag: {}", getName(), evt.getTag());
        }
    }

    private void processDatacenterListRequest(final SimEvent evt) {
        if(evt.getData() instanceof List dcList) {
            for (Datacenter datacenter : (List<Datacenter>) dcList) {
                datacenterMap.put(datacenter.getId(), datacenter);
            }
            LOGGER.info("{}: {}: List of {} datacenters received.", getSimulation().clockStr(), getName(), dcList.size());
            if (userRequestManager.getNextSendTime() != Double.MAX_VALUE) {
                send(this, userRequestManager.getNextSendTime() - getSimulation().clock(), CloudActionTags.NEED_SEND_USER_REQUEST, null);
            }
            return;
        }

        LOGGER.error("The date type of "+evt+"is not List<Datacenter>");
    }

    private void sendUserRequest() {
        double nowTime = getSimulation().clock();
        Map<Integer, List<UserRequest>> userRequestMap = userRequestManager.generateOnceUserRequests();
        if (userRequestMap == null || userRequestMap.isEmpty()) {
            LOGGER.info("{}: {}: No user request to send.", getSimulation().clockStr(), getName());
            return;
        }

        for (Map.Entry<Integer, List<UserRequest>> entry : userRequestMap.entrySet()) {
            int datacenterId = entry.getKey();
            List<UserRequest> userRequests = entry.getValue();
            if (userRequests.isEmpty())
                continue;

            Datacenter datacenter = datacenterMap.get(datacenterId);
            if (datacenter.isCentralizedInterSchedule()) {
                send(getSimulation().getCis(), 0, CloudActionTags.USER_REQUEST_SEND, userRequests);
                LOGGER.info("{}: {}: Sending {} request to {}", getSimulation().clockStr(), getName(), userRequests.size(), getSimulation().getCis().getName());
            } else {
                send(datacenter, 0, CloudActionTags.USER_REQUEST_SEND, userRequests);
                LOGGER.info("{}: {}: Sending {} request to {}", getSimulation().clockStr(), getName(), userRequests.size(), datacenter.getName());
            }
            getSimulation().getSqlRecord().recordUserRequestsSubmitInfo(userRequests);
        }
        if (userRequestManager.getNextSendTime() != Double.MAX_VALUE) {
            send(this, userRequestManager.getNextSendTime() - nowTime, CloudActionTags.NEED_SEND_USER_REQUEST, null);
        }
    }
}
