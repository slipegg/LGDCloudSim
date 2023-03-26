package org.scalecloudsim.Instances;

import org.cloudsimplus.core.Simulation;
import org.cloudsimplus.core.UniquelyIdentifiable;
import org.cloudsimplus.hosts.Host;
import org.cloudsimplus.resources.Bandwidth;
import org.scalecloudsim.users.User;

public class InstanceSimple implements Instance{
    long id;
    double submissiondelay;
    User user;
    double submissionTime;
    double lifeTime;
    InstanceGroup instanceGroup;
    Host host;
    long ram;
    long bw;
    long storage;
    long cpu;

    public InstanceSimple(long id,long ram, long bw, long storage, long cpu) {
        this.id=id;
        this.ram = ram;
        this.bw = bw;
        this.storage = storage;
        this.cpu = cpu;
        this.lifeTime=-1;
    }
    public InstanceSimple(long ram, long bw, long storage, long cpu) {
        this.id=-1;
        this.ram = ram;
        this.bw = bw;
        this.storage = storage;
        this.cpu = cpu;
        this.lifeTime=-1;
    }
    public InstanceSimple(long ram, long bw, long storage, long cpu,double lifeTime) {
        this.id=-1;
        this.ram = ram;
        this.bw = bw;
        this.storage = storage;
        this.cpu = cpu;
        this.lifeTime=lifeTime;
    }

    //    public InstanceSimple(long ram,long bw,long storage,)
    @Override
    public void setId(long id) {
        this.id=id;
    }

    @Override
    public double getSubmissionDelay() {
        return submissiondelay;
    }

    @Override
    public void setSubmissionDelay(double submissionDelay) {
        this.submissiondelay=submissionDelay;
    }

    @Override
    public boolean isDelayed() {
        return submissiondelay>0;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public String getUid() {
        return UniquelyIdentifiable.getUid(user.getId(),this.getId());
    }

    @Override
    public User getUser() {
        return user;
    }

    @Override
    public void setUser(User user) {
        this.user=user;
    }

    @Override
    public Simulation getSimulation() {
        return null;
    }

    @Override
    public double getSubmittedTime() {
        return submissionTime;
    }

    @Override
    public boolean isSubmitted() {
        return submissionTime!=-1;
    }

    @Override
    public void setSubmittedTime(double time) {
        submissionTime=time;
    }

    @Override
    public double getLifeTime() {
        return lifeTime;
    }

    @Override
    public Instance setLifeTime(double lifeTime) {
        this.lifeTime=lifeTime;
        return this;
    }

    @Override
    public InstanceGroup getInstanceGroup() {
        return instanceGroup;
    }

    @Override
    public Instance setInstanceGroup(InstanceGroup instanceGroup) {
        this.instanceGroup=instanceGroup;
        return this;
    }

    @Override
    public Instance setHost(Host host) {
        this.host=host;
        return this;
    }

    @Override
    public Host getHost() {
        return host;
    }

    @Override
    public long getBw() {
        return bw;
    }

    @Override
    public long getRam() {
        return ram;
    }

    @Override
    public long getStorage() {
        return storage;
    }

    @Override
    public long getCpu() {
        return cpu;
    }

    @Override
    public Instance setBw(long bw) {
        this.bw=bw;
        return this;
    }

    @Override
    public Instance setRam(long ram) {
        this.ram=ram;
        return this;
    }

    @Override
    public Instance setStorage(long storage) {
        this.storage=storage;
        return this;
    }

    @Override
    public Instance setCpu(long cpu) {
        this.cpu=cpu;
        return this;
    }
}
