package org.lgdcloudsim.request;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * InstanceSimple is a simple implementation of the {@link Instance} interface.
 *
 * @author Anonymous
 * @since LGDCloudSim 1.0
 */

@Getter
@Setter
public class InstanceSimple implements Instance {
    /**
     * The id of the instance. Each instance has a unique id.
     */
    @Getter
    @NonNull
    int id;

    /**
     * The user request to which the instance belongs.
     */
    @Getter
    @NonNull
    UserRequest userRequest;

    InstanceGroup instanceGroup;

    int cpu;

    int ram;

    int storage;

    int bw;

    int lifecycle;

    int destHostId;

    int host;

    double startTime;

    double finishTime;

    int retryNum;

    int retryMaxNum;

    int state;

    List<Integer> retryHostIds;

    int expectedScheduleHostId;

    double intraScheduleEndTime;

    /**
     * Create an instance with the specified id, CPU, memory, storage, and bandwidth.
     * The lifecycle of the instance is set to -1 by default, which means the instance will not be terminated automatically.
     * Later, we must set the userRequest and the instanceGroup to which the instance belongs.
     *
     * @param id      the id of the instance
     * @param cpu     the CPU required by the instance
     * @param ram     the memory required by the instance
     * @param storage the storage required by the instance
     * @param bw      the bandwidth required by the instance
     */
    public InstanceSimple(int id, int cpu, int ram, int storage, int bw) {
        this.id = id;

        this.cpu = cpu;
        this.ram = ram;
        this.storage = storage;
        this.bw = bw;
        this.lifecycle = -1;

        this.destHostId = -1;
        this.retryMaxNum = 0;

        this.host = -1;
        this.expectedScheduleHostId = -1;
        this.startTime = -1;
        this.finishTime = -1;

        this.state = UserRequest.WAITING;
    }

    /**
     * Create an instance with the specified id, CPU, memory, storage, bandwidth, and lifecycle.
     * Later, we must set the userRequest and the instanceGroup to which the instance belongs.
     *
     * @param id        the id of the instance
     * @param cpu       the CPU required by the instance
     * @param ram       the memory required by the instance
     * @param storage   the storage required by the instance
     * @param bw        the bandwidth required by the instance
     * @param lifecycle the lifecycle of the instance
     */
    public InstanceSimple(int id, int cpu, int ram, int storage, int bw, int lifecycle) {
        this(id, cpu, ram, storage, bw);
        this.lifecycle = lifecycle;
    }

    @Override
    public void setId(int id) {
        this.id = id;
    }

    @Override
    public boolean isSetDestHost() {
        return destHostId != -1;
    }

    @Override
    public Instance addRetryNum() {
        this.retryNum++;
        if (this.retryNum > this.retryMaxNum) {
            this.state = UserRequest.FAILED;
        }
        return this;
    }

    @Override
    public boolean isFailed() {
        return this.state == UserRequest.FAILED;
    }

    @Override
    public Instance addRetryHostId(int hostId) {
        if (this.retryHostIds == null) {
            this.retryHostIds = new ArrayList<>();
        }
        this.retryHostIds.add(hostId);
        return this;
    }

    @Override
    public List<Integer> getRetryHostIds() {
        return retryHostIds;
    }

    @Override
    public void setUserRequest(UserRequest userRequest) {
        this.userRequest = userRequest;
    }

    @Override
    public String toString() {
        return "InstanceSimple{" +
                "id=" + id +
                ", cpu=" + cpu +
                ", ram=" + ram +
                ", storage=" + storage +
                ", bw=" + bw +
                ", lifecycle=" + lifecycle +
                '}';
    }

    @Override
    public Instance setIntraScheduleEndTime(double intraScheduleEndTime) {
        this.intraScheduleEndTime = intraScheduleEndTime;
        return this;
    }
}
