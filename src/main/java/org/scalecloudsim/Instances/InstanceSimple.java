package org.scalecloudsim.Instances;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

public class InstanceSimple implements Instance {
    int id;
    UserRequest userRequest;

    int cpu;
    int ram;
    int storage;
    int bw;

    double lifeTime;
    InstanceGroup instanceGroup;
    int destHost;
    int maxFailNum;

    int failNum;
    List<InstanceFailInfo> failedinfoList;
    int host;
    double startTime;
    double finishTime;
    int status;
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
        this.maxFailNum = 0;

        this.failNum = 0;
        this.failedinfoList = new ArrayList<>();
        this.host = -1;
        this.startTime = -1;
        this.finishTime = -1;

        this.status = 0;
    }

    public InstanceSimple(int id, int cpu, int ram, int storage, int bw, double lifeTime) {
        this.id = id;

        this.cpu = cpu;
        this.ram = ram;
        this.storage = storage;
        this.bw = bw;

        this.lifeTime = lifeTime;
        this.destHost = -1;
        this.maxFailNum = 0;

        this.failNum = 0;
        this.failedinfoList = new ArrayList<>();
        this.host = -1;
        this.startTime = -1;
        this.finishTime = -1;

        this.status = 0;
    }

    @Override
    public void setId(int id) {
        this.id = id;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public int getCpu() {
        return cpu;
    }

    @Override
    public int getRam() {
        return ram;
    }

    @Override
    public int getStorage() {
        return storage;
    }

    @Override
    public int getBw() {
        return bw;
    }

    @Override
    public Instance setCpu(int cpu) {
        this.cpu = cpu;
        return this;
    }

    @Override
    public Instance setRam(int ram) {
        this.ram = ram;
        return this;
    }

    @Override
    public Instance setStorage(int storage) {
        this.storage = storage;
        return this;
    }

    @Override
    public Instance setBw(int bw) {
        this.bw = bw;
        return this;
    }

    @Override
    public double getLifeTime() {
        return lifeTime;
    }

    @Override
    public Instance setLifeTime(double lifeTime) {
        this.lifeTime = lifeTime;
        return this;
    }

    @Override
    public InstanceGroup getInstanceGroup() {
        return instanceGroup;
    }

    @Override
    public Instance setInstanceGroup(InstanceGroup instanceGroup) {
        this.instanceGroup = instanceGroup;
        return this;
    }

    @Override
    public Instance setDestHost(int destHost) {
        this.destHost = destHost;
        return this;
    }

    @Override
    public int getDestHost() {
        return destHost;
    }

    @Override
    public boolean isSetDestHost() {
        return destHost != -1;
    }

    @Override
    public int getMaxFailNum() {
        return maxFailNum;
    }

    @Override
    public Instance setMaxFailNum(int maxFailNum) {
        this.maxFailNum = maxFailNum;
        return this;
    }

    @Override
    public int getFailNum() {
        return failNum;
    }

    @Override
    public Instance setFailNum(int failNum) {
        this.failNum = failNum;
        return this;
    }

    @Override
    public List<InstanceFailInfo> getFailedHosts() {
        return failedinfoList;
    }

    @Override
    public Instance addFailedInfo(InstanceFailInfo instanceFailInfo) {
        failedinfoList.add(instanceFailInfo);
        return this;
    }

    @Override
    public int getHost() {
        return host;
    }

    @Override
    public Instance setHost(int host) {
        this.host = host;
        return this;
    }

    @Override
    public double getStartTime() {
        return startTime;
    }

    @Override
    public Instance setStartTime(double startTime) {
        this.startTime = startTime;
        return this;
    }

    @Override
    public double getFinishTime() {
        return finishTime;
    }

    @Override
    public Instance setFinishTime(double finishTime) {
        this.finishTime = finishTime;
        return this;
    }

    @Override
    public int getStatus() {
        return status;
    }

    @Override
    public Instance setStatus(int status) {
        this.status = status;
        return this;
    }

    @Override
    public Instance addRetryNum() {
        this.retryNum++;
        if (this.retryNum >= this.retryMaxNum) {
            this.state = UserRequest.FAILED;
            this.getInstanceGroup().setState(UserRequest.FAILED);
            this.getUserRequest().setState(UserRequest.FAILED);
        }
        return this;
    }

    @Override
    public boolean isFailed() {
        return this.state == UserRequest.FAILED;
    }

    @Override
    public UserRequest getUserRequest() {
        return userRequest;
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
