package org.lgdcloudsim.request;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * UserRequestSimple is a simple implementation of the {@link UserRequest} interface.
 *
 * @version 1.0
 * @since LGDCloudSim 1.0
 */

@Getter
@Setter
public class UserRequestSimple implements UserRequest {
    @Getter
    private int id;

    private int belongDatacenterId;

    @Getter
    private List<InstanceGroup> instanceGroups;

    @Getter
    private InstanceGroupGraph instanceGroupGraph;

    private String area;

    private double submitTime;

    private double finishTime;

    private int state;

    @Getter
    private String failReason;

    @Getter
    private List<InstanceGroupEdge> allocatedEdges;

    private int successGroupNum;

    private double scheduleDelayLimit;

    /**
     * Construct a user request with the id.
     * Note that it is empty and the state is waiting.
     *
     * @param id the id of the user request.
     */
    public UserRequestSimple(int id) {
        this.id = id;
        this.state = UserRequest.WAITING;
        this.finishTime = -1;
        this.failReason = "";
        this.successGroupNum = 0;
        this.allocatedEdges = new ArrayList<>();
        this.scheduleDelayLimit = -1;
    }

    /**
     * Construct a user request with the id, the instance groups and the instance group graph.
     * Note that the state is waiting.
     * @param id the id of the user request.
     * @param instanceGroups the instance groups of the user request.
     * @param instanceGroupGraph the instance group graph of the user request.
     */
    public UserRequestSimple(int id, List<InstanceGroup> instanceGroups, InstanceGroupGraph instanceGroupGraph) {
        this(id);
        setInstanceGroups(instanceGroups);
        this.instanceGroupGraph = instanceGroupGraph;
    }

    /**
     * Construct a user request with the id, the instance groups, the instance group graph and the area.
     *
     * @param id                 the id of the user request.
     * @param instanceGroups     the instance groups of the user request.
     * @param instanceGroupGraph the instance group graph of the user request.
     * @param area               the area of the user request.
     */
    public UserRequestSimple(int id, List<InstanceGroup> instanceGroups, InstanceGroupGraph instanceGroupGraph, String area) {
        this(id, instanceGroups, instanceGroupGraph);
        this.area = area;
    }

    @Override
    public void setId(int id) {
        this.id = id;
    }

    @Override
    public UserRequest setInstanceGroups(List<InstanceGroup> instanceGroups) {
        this.instanceGroups=instanceGroups;
        for(InstanceGroup instanceGroup:instanceGroups){
            instanceGroup.setUserRequest(this);
            for (Instance instance : instanceGroup.getInstances()) {
                instance.setInstanceGroup(instanceGroup);
            }
        }
        return this;
    }

    @Override
    public UserRequest setInstanceGroupGraph(InstanceGroupGraph instanceGroupGraph) {
        this.instanceGroupGraph = instanceGroupGraph;
        this.instanceGroupGraph.setUserRequest(this);
        return this;
    }

    @Override
    public UserRequest addSuccessGroupNum() {
        successGroupNum++;
        if (successGroupNum == instanceGroups.size()) {
            state = UserRequest.SUCCESS;
        }
        return this;
    }

    @Override
    public UserRequest addAllocatedEdge(InstanceGroupEdge edge) {
        allocatedEdges.add(edge);
        return this;
    }

    @Override
    public UserRequest delAllocatedEdge(InstanceGroupEdge edge) {
        allocatedEdges.remove(edge);
        return this;
    }

    @Override
    public UserRequest addFailReason(String failReason) {
        if (this.failReason.equals("")) {
            this.failReason = failReason;
        } else {
            this.failReason = this.failReason + "-" + failReason;
        }
        return this;
    }

    @Override
    public String toString() {
        return "UserRequestSimple [id=" + id + ", submitTime=" + submitTime + ", instanceGroups=" + instanceGroups
                + ", instanceGroupGraph=" + instanceGroupGraph + "]";
    }
}
