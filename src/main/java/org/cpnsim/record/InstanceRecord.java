package org.cpnsim.record;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InstanceRecord {
    private int id;
    private int instanceGroupId;
    private int userRequestId;
    private int cpu;
    private int ram;
    private int storage;
    private int bw;
    private int dataCenterId;
    private int hostId;
    private int lifeTime;
    private double startTime;
    private double finishTime;

    public InstanceRecord(int id, int instanceGroupId, int userRequestId, int cpu, int ram, int storage, int bw, int dataCenterId, int hostId, int lifeTime, double startTime) {
        this.id = id;
        this.instanceGroupId = instanceGroupId;
        this.userRequestId = userRequestId;
        this.cpu = cpu;
        this.ram = ram;
        this.storage = storage;
        this.bw = bw;
        this.dataCenterId = dataCenterId;
        this.hostId = hostId;
        this.lifeTime = lifeTime;
        this.startTime = startTime;
    }
}