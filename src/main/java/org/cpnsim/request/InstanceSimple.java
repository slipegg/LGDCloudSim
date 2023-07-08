package org.cpnsim.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InstanceSimple implements Instance {
    @Getter
    int id;
    @Getter
    UserRequest userRequest;
    int cpu;
    int ram;
    int storage;
    int bw;

    int lifeTime;
    InstanceGroup instanceGroup;
    int destHost;
    int host;
    double startTime;
    double finishTime;
    @Getter
    @Setter
    int retryNum;
    @Getter
    @Setter
    int retryMaxNum;
    @Getter
    @Setter
    int state;

    //还缺userRequest和instanceGroup等待后面进行设置
    public InstanceSimple(int id, int cpu, int ram, int storage, int bw) {
        this.id = id;

        this.cpu = cpu;
        this.ram = ram;
        this.storage = storage;
        this.bw = bw;

        this.lifeTime = -1;
        this.destHost = -1;
        this.retryMaxNum = 3;

        this.host = -1;
        this.startTime = -1;
        this.finishTime = -1;

        this.state = UserRequest.WAITING;
    }

    public InstanceSimple(int id, int cpu, int ram, int storage, int bw, int lifeTime) {
        this(id, cpu, ram, storage, bw);
        this.lifeTime = lifeTime;
    }

    @Override
    public void setId(int id) {
        this.id = id;
    }

    @Override
    public boolean isSetDestHost() {
        return destHost != -1;
    }

    @Override
    public Instance addRetryNum() {
        this.retryNum++;
        if (this.retryNum >= this.retryMaxNum) {
            this.state = UserRequest.FAILED;
        }
        return this;
    }

    @Override
    public boolean isFailed() {
        return this.state == UserRequest.FAILED;
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
                ", lifeTime=" + lifeTime +
                '}';
    }
}
