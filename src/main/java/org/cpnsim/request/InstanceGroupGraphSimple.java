package org.cpnsim.request;

import lombok.Getter;
import lombok.Setter;

import java.util.*;

public class InstanceGroupGraphSimple implements InstanceGroupGraph {
    private int id;
    private UserRequest userRequest;
    private Set<InstanceGroupEdge> graph;
    @Getter
    @Setter
    boolean directed;

    public InstanceGroupGraphSimple(boolean directed) {
        this.directed = directed;
        graph = new HashSet<>();
    }

    @Override
    public boolean getDirected() {
        return false;
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
    public InstanceGroupGraph removeEdge(InstanceGroup src, InstanceGroup dst) {
        boolean removed = false;
        graph.removeIf(instanceGroupEdge -> (instanceGroupEdge.getSrc() == src && instanceGroupEdge.getDst() == dst)
                || (!directed && instanceGroupEdge.getSrc() == dst && instanceGroupEdge.getDst() == src));
        if (!removed) {
            LOGGER.info("There is no edge(src={},dst={}) to remove.", src, dst);
        }
        return this;
    }

    @Override
    public InstanceGroupEdge getEdge(InstanceGroup src, InstanceGroup dst) {
        for (InstanceGroupEdge instanceGroupEdge : graph) {
            if (instanceGroupEdge.getSrc() == src && instanceGroupEdge.getDst() == dst) {
                return instanceGroupEdge;
            }
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
        }
        return res;
    }

    @Override
    public double getDelay(InstanceGroup src, InstanceGroup dst) {
        for (InstanceGroupEdge instanceGroupEdge : graph) {
            if (instanceGroupEdge.getSrc() == src && instanceGroupEdge.getDst() == dst) {
                return instanceGroupEdge.getMinDelay();
            }
        }
        return Double.MAX_VALUE;
    }

    @Override
    public double getBw(InstanceGroup src, InstanceGroup dst) {
        for (InstanceGroupEdge instanceGroupEdge : graph) {
            if (instanceGroupEdge.getSrc() == src && instanceGroupEdge.getDst() == dst) {
                return instanceGroupEdge.getRequiredBw();
            }
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
