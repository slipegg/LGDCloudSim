package org.lgdcloudsim.request;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;

/**
 * Each user request has a InstanceGroupGraph, which represents the relationship between instance groups.
 * Each InstanceGroupGraph is composed of a set of InstanceGroupEdges.
 *
 * @author Anonymous
 * @since LGDCloudSim 1.0
 */
public interface InstanceGroupGraph extends RequestEntity{
    /**
     * The logger of the class.
     */
    Logger LOGGER = LoggerFactory.getLogger(InstanceGroupGraph.class.getSimpleName());

    /**
     * Get whether the graph is directed.
     * If the graph is not directed, the edge from instance group A to instance group B is equivalent to the edge from instance group B to instance group A.
     *
     * @return whether the graph is directed.
     */
    boolean getDirected();

    /**
     * Set whether the graph is directed.
     * @param directed whether the graph is directed.
     * @return the instance group graph itself.
     */
     InstanceGroupGraph setDirected(boolean directed);

    /**
     * Add an edge to the graph.
     * If the graph is not directed, the edge from instance group A to instance group B
     * and the edge from instance group B to instance group A can be added to the graph only once.
     * @param src the source instance group of the edge.
     * @param dst the destination instance group of the edge.
     * @param delay the maximum delay of the edge.
     * @param bw the required bandwidth of the edge.
     * @return the instance group graph itself.
     */
    InstanceGroupGraph addEdge(InstanceGroup src, InstanceGroup dst, double delay, double bw);

    /**
     * Add an edge to the graph.
     * @param edge the edge to be added to the graph.
     * @return the instance group graph itself.
     */
    InstanceGroupGraph addEdge(InstanceGroupEdge edge);

    /**
     * Remove an edge from the graph.
     * @param src the source instance group of the edge.
     * @param dst the destination instance group of the edge.
     * @return the instance group graph itself.
     */
    InstanceGroupGraph removeEdge(InstanceGroup src, InstanceGroup dst);

    /**
     * Get the edge between the source instance group and the destination instance group.
     * @param src the source instance group of the edge.
     * @param dst the destination instance group of the edge.
     * @return the edge between the source instance group and the destination instance group.
     */
    InstanceGroupEdge getEdge(InstanceGroup src, InstanceGroup dst);

    /**
     * Get the set of edges in the graph.
     * @return the set of edges in the graph.
     */
    Set<InstanceGroupEdge> getGraph();

    /**
     * Get the list of instance groups that the source instance group can reach.
     * @param src the source instance group.
     * @return the list of instance groups that the source instance group can reach.
     */
    List<InstanceGroup> getDstList(InstanceGroup src);

    /**
     * Get the list of instance groups that can reach the destination instance group.
     * @param dst the destination instance group.
     * @return the list of instance groups that can reach the destination instance group.
     */
    List<InstanceGroup> getSrcList(InstanceGroup dst);

    /**
     * Get the delay between the source instance group and the destination instance group.
     * @param src the source instance group.
     * @param dst the destination instance group.
     * @return the delay between the source instance group and the destination instance group.
     */
    double getDelay(InstanceGroup src, InstanceGroup dst);

    /**
     * Get the bandwidth between the source instance group and the destination instance group.
     * @param src the source instance group.
     * @param dst the destination instance group.
     * @return the bandwidth between the source instance group and the destination instance group.
     */
    double getBw(InstanceGroup src, InstanceGroup dst);

    /**
     * Get whether the graph is empty.
     * If the graph is empty, the graph does not contain any edges.
     * @return whether the graph is empty.
     */
    boolean isEmpty();

    /**
     * Get whether the instance group is linked to other instance groups.
     * @param instanceGroup the instance group to be checked.
     * @return whether the instance group is linked to other instance groups.
     */
    boolean isEdgeLinked(InstanceGroup instanceGroup);
}
