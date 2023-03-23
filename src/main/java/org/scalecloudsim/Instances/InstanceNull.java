package org.scalecloudsim.Instances;

import org.cloudsimplus.core.UserEntity;
import org.cloudsimplus.hosts.Host;
import org.cloudsimplus.core.Simulation;
import org.scalecloudsim.users.User;

class InstanceNull implements Instance{
    @Override public void setId(long id) {/**/}
    @Override public long getId() {
        return -1;
    }
    @Override public double getSubmissionDelay() {
        return 0;
    }
    @Override public boolean isDelayed() { return false; }
    @Override public void setSubmissionDelay(double submissionDelay) {/**/}


    @Override
    public String getUid() {
        return "";
    }

    @Override
    public User getUser() {
        return User.NULL;
    }

    @Override
    public void setUser(User User) {

    }

    @Override
    public Simulation getSimulation() {
        return Simulation.NULL;
    }

    @Override
    public double getSubmittedTime() {
        return -1;
    }

    @Override
    public boolean isSubmitted() {
        return false;
    }

    @Override
    public void setSubmittedTime(double time) {
    }

//    @Override
//    public String getDescription() {
//        return "";
//    }
//
//    @Override
//    public Instance setDescription(String description) {
//        return this;
//    }

    @Override
    public double getLifeTime() {
        return -1;
    }

    @Override
    public Instance setLifeTime() {
        return this;
    }

    @Override
    public InstanceGroup getInstanceGroup() {
        return InstanceGroup.NULL;
    }

    @Override
    public Instance setInstanceGroup() {
        return this;
    }

    @Override
    public Host getHost() {
        return Host.NULL;
    }

    @Override
    public long getBw() {
        return -1;
    }

    @Override
    public long getRam() {
        return -1;
    }

    @Override
    public long getStorage() {
        return -1;
    }

    @Override
    public long getCpu() {
        return -1;
    }

    @Override
    public Instance setBw() {
        return this;
    }

    @Override
    public Instance setRam() {
        return this;
    }

    @Override
    public Instance setStorage() {
        return this;
    }

    @Override
    public Instance setCpu() {
        return this;
    }

//    @Override
//    public boolean isWaitInterSchedule() {
//        return false;
//    }
//
//    @Override
//    public Instance setWaitInterSchedule() {
//        return null;
//    }
//
//    @Override
//    public boolean isInnerWaiting() {
//        return false;
//    }
//
//    @Override
//    public Instance setInnerWaiting() {
//        return this;
//    }
//
//    @Override
//    public boolean isInnerScheduling() {
//        return false;
//    }
//
//    @Override
//    public Instance setInnerScheduling() {
//        return this;
//    }
//
//    @Override
//    public boolean isCreated() {
//        return false;
//    }
//
//    @Override
//    public Instance setCreated() {
//        return this;
//    }
//
//    @Override
//    public boolean isFailed() {
//        return false;
//    }
//
//    @Override
//    public void setFailed(boolean failed) {
//
//    }
//
//    @Override
//    public boolean isWorking() {
//        return false;
//    }
//
//    @Override
//    public Instance setWorking() {
//        return this;
//    }
//
//    @Override
//    public boolean isFinish() {
//        return false;
//    }
//
//    @Override
//    public Instance setFinish() {
//        return this;
//    }
//
//    @Override
//    public double getStartTime() {
//        return 0;
//    }
//
//    @Override
//    public Instance setStartTime(double startTime) {
//        return this;
//    }
//
//    @Override
//    public double getFinishTime() {
//        return -1;
//    }
//
//    @Override
//    public Instance setFinishTime(double startTime) {
//        return this;
//    }
}
