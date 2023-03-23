package org.scalecloudsim.Instances;

import org.cloudsimplus.core.CloudSimTag;
import org.cloudsimplus.core.Simulation;
import org.scalecloudsim.users.User;

import java.util.List;

public class InstanceGroupNull implements InstanceGroup{
    @Override
    public void setId(long id) {

    }

    @Override
    public double getSubmissionDelay() {
        return -1;
    }

    @Override
    public void setSubmissionDelay(double submissionDelay) {

    }

    @Override
    public boolean isDelayed() {
        return false;
    }

    @Override
    public long getId() {
        return -1;
    }

    @Override
    public String getUid() {
        return "";
    }

    @Override
    public User getUser() {
        return User.NULL;
    }

    @Override
    public void setUser(User user) {

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
//    public InstanceGroup setDescription(String description) {
//        return this;
//    }

    @Override
    public List<Instance> getInstanceList() {
        return null;
    }

    @Override
    public InstanceGroup setInstanceList(List<Instance> instanceList) {
        return this;
    }

    @Override
    public int getGroupType() {
        return -1;
    }

    @Override
    public InstanceGroup setGroupType(int tag) {
        return this;
    }

//    @Override
//    public boolean isInterWaiting() {
//        return false;
//    }
//
//    @Override
//    public InstanceGroup setInterWaiting() {
//        return this;
//    }
//
//    @Override
//    public boolean isInterScheduling() {
//        return false;
//    }
//
//    @Override
//    public InstanceGroup setInterScheduling() {
//        return this;
//    }
//
//    @Override
//    public double getInterSchedulingStartTime() {
//        return -1;
//    }
//
//    @Override
//    public boolean isInterScheduledSuccess() {
//        return false;
//    }
//
//    @Override
//    public InstanceGroup setInterScheduledSuccess() {
//        return this;
//    }
//
//    @Override
//    public double getInterScheduledTime() {
//        return -1;
//    }
//
//    @Override
//    public boolean isInterScheduledFail() {
//        return false;
//    }
//
//    @Override
//    public InstanceGroup setInterScheduledFail() {
//        return this;
//    }
}
