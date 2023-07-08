package org.cpnsim.request;

import java.util.*;

//TODO 考虑用数组的方式来存储
public class InstanceGroupGraphSimple implements InstanceGroupGraph{
    int id;
    UserRequest userRequest;

    Set<InstanceGroupEdge> graph;

    boolean directed;

    public InstanceGroupGraphSimple(boolean directed) {
        this.directed = directed;
        graph = new HashSet<>();
    }

    @Override
    public InstanceGroupGraph setDirected(boolean directed) {
        this.directed=directed;
        return this;
    }

    @Override
    public boolean getDirected() {
        return directed;
    }

    @Override
    public InstanceGroupGraph addEdge(InstanceGroup src, InstanceGroup dst, double delay, double bw) {
        addEdge(new InstanceGroupEdgeSimple(src, dst, delay, bw));
        return this;
    }

    @Override
    public InstanceGroupGraph addEdge(InstanceGroupEdge edge) {
        graph.add(Objects.requireNonNull(edge));
        if (!directed) {
            graph.add(new InstanceGroupEdgeSimple(edge.getDst(), edge.getSrc(), edge.getMinDelay(), edge.getRequiredBw()));
        }
        return this;
    }

    @Override
    public int removeEdge(InstanceGroup src, InstanceGroup dst) {

        LOGGER.info("There is no edge(src={},dst={}) to remove.",src,dst);
        return -1;
    }

    @Override
    public InstanceGroupEdge getEdge(InstanceGroup src, InstanceGroup dst) {
        for (InstanceGroupEdge instanceGroupEdge : graph) {
            if (instanceGroupEdge.getSrc() == src && instanceGroupEdge.getDst() == dst) {
                return instanceGroupEdge;
            }
//            if (!directed) {
//                if (instanceGroupEdge.getSrc() == dst && instanceGroupEdge.getDst() == src) {
//                    return instanceGroupEdge;
//                }
//            }
        }
        LOGGER.info("There is no edge(src={},dst={}).",src,dst);
        return null;
    }

    @Override
    public Set<InstanceGroupEdge> getGraph() {
        return graph;
    }

    @Override
    public List<InstanceGroup> getDstList(InstanceGroup src) {
        List<InstanceGroup> res = new ArrayList<>();
        for (InstanceGroupEdge instanceGroupEdge : graph) {
            if (instanceGroupEdge.getSrc() == src) {
                res.add(instanceGroupEdge.getDst());
            }
//            if (!this.directed && (instanceGroupEdge.getDst() == src)) {
//                res.add(instanceGroupEdge.getSrc());
//            }
        }
        return res;
    }

    @Override
    public List<InstanceGroup> getSrcList(InstanceGroup dst) {
        List<InstanceGroup> res = new ArrayList<>();
        for (InstanceGroupEdge instanceGroupEdge : graph) {
            if (instanceGroupEdge.getDst() == dst) {
                res.add(instanceGroupEdge.getSrc());
            }
//            if (!this.directed && (instanceGroupEdge.getSrc() == dst)) {
//                res.add(instanceGroupEdge.getDst());
//            }
        }
        return res;
    }

    @Override
    public double getDelay(InstanceGroup src, InstanceGroup dst) {
        for (InstanceGroupEdge instanceGroupEdge : graph) {
            if (instanceGroupEdge.getSrc() == src && instanceGroupEdge.getDst() == dst) {
                return instanceGroupEdge.getMinDelay();
            }
//            if (!directed) {
//                if (instanceGroupEdge.getSrc() == dst && instanceGroupEdge.getDst() == src) {
//                    return instanceGroupEdge.getMinDelay();
//                }
//            }
        }
        return Double.MAX_VALUE;
    }

    @Override
    public double getBw(InstanceGroup src, InstanceGroup dst) {
        for (InstanceGroupEdge instanceGroupEdge : graph) {
            if (instanceGroupEdge.getSrc() == src && instanceGroupEdge.getDst() == dst) {
                return instanceGroupEdge.getRequiredBw();
            }
//            if (!directed) {
//                if (instanceGroupEdge.getSrc() == dst && instanceGroupEdge.getDst() == src) {
//                    return instanceGroupEdge.getRequiredBw();
//                }
//            }
        }
        return 0;
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
    public UserRequest getUserRequest() {
        return userRequest;
    }

    @Override
    public void setUserRequest(UserRequest userRequest) {
        this.userRequest= userRequest;
    }

    @Override
    public String toString() {
        return "InstanceGroupGraphSimple{" +
                "id=" + id +
                ", graph=" + graph +
                ", directed=" + directed +
                '}';
    }
}
