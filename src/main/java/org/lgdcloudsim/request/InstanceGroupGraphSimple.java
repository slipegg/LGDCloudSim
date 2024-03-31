package org.lgdcloudsim.request;

import lombok.Getter;
import lombok.Setter;

import java.util.*;

/**
 * InstanceGroupGraphSimple is a simple implementation of the {@link InstanceGroupGraph} interface.
 *
 * @author Anonymous
 * @since LGDCloudSim 1.0
 */
public class InstanceGroupGraphSimple implements InstanceGroupGraph {
    private int id;

    private UserRequest userRequest;

    private Set<InstanceGroupEdge> graph;

    @Getter
    @Setter
    boolean directed;

    /**
     * Construct an instance group graph with the directed flag, but the graph is empty.
     *
     * @param directed the directed flag of the graph.
     */
    public InstanceGroupGraphSimple(boolean directed) {
        this.directed = directed;
        graph = new HashSet<>();
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
            graph.add(new InstanceGroupEdgeSimple(edge.getDst(), edge.getSrc(), edge.getMaxDelay(), edge.getRequiredBw()));
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
        LOGGER.info("There is no edge(src={},dst={}).", src, dst);
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
                return instanceGroupEdge.getMaxDelay();
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
    public boolean isEmpty() {
        return graph.isEmpty();
    }

    @Override
    public boolean isEdgeLinked(InstanceGroup instanceGroup) {
        if (graph.isEmpty()) {
            return false;
        } else {
            for (InstanceGroupEdge instanceGroupEdge : graph) {
                if (instanceGroupEdge.getSrc() == instanceGroup || instanceGroupEdge.getDst() == instanceGroup) {
                    return true;
                }
            }
            return false;
        }
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
        this.userRequest = userRequest;
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
