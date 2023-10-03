package org.cpnsim.user;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.cloudsimplus.core.CloudSimEntity;
import org.cloudsimplus.core.CloudSimTag;
import org.cloudsimplus.core.Simulation;
import org.cloudsimplus.core.events.SimEvent;
import org.cpnsim.request.UserRequest;
import org.cpnsim.datacenter.Datacenter;
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
            for (Datacenter datacenter : (List<Datacenter>) dcList) {
                datacenterMap.put(datacenter.getId(), datacenter);
            }
            LOGGER.info("{}: {}: List of {} datacenters received.", getSimulation().clockStr(), getName(), dcList.size());
            if (userRequestManager.getNextSendTime() != Double.MAX_VALUE) {
                send(this, userRequestManager.getNextSendTime() - getSimulation().clock(), CloudSimTag.NEED_SEND_USER_REQUEST, null);
            }
            return;
        }

        LOGGER.error("The date type of "+evt+"is not List<Datacenter>");
    }

    private void sendUserRequest() {
        double nowTime = getSimulation().clock();
        Map<Integer, List<UserRequest>> userRequestMap = userRequestManager.generateOnceUserRequests();
        if (userRequestMap == null || userRequestMap.size() == 0) {
            LOGGER.info("{}: {}: No user request to send.", getSimulation().clockStr(), getName());
            return;
        }
        for (Map.Entry<Integer, List<UserRequest>> entry : userRequestMap.entrySet()) {
            int datacenterId = entry.getKey();
            List<UserRequest> userRequests = entry.getValue();
            if (userRequests.size() == 0)
                continue;
            Datacenter datacenter = datacenterMap.get(datacenterId);
            if (datacenter.isCentralizedInterSchedule()) {
                send(getSimulation().getCis(), 0, CloudSimTag.USER_REQUEST_SEND, userRequests);
            } else {
                send(datacenter, 0, CloudSimTag.USER_REQUEST_SEND, userRequests);
            }
            getSimulation().getSqlRecord().recordUserRequestsSubmitinfo(userRequests);
            LOGGER.info("{}: {}: Sending {} request to {}", getSimulation().clockStr(), getName(), userRequests.size(), datacenter.getName());
        }
        if (userRequestManager.getNextSendTime() != Double.MAX_VALUE) {
            send(this, userRequestManager.getNextSendTime() - nowTime, CloudSimTag.NEED_SEND_USER_REQUEST, null);
        }
    }
}
