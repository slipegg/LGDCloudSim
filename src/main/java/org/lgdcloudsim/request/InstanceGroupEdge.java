package org.lgdcloudsim.request;

/**
 * An interface representing the edges of a connection between groups of instances.
 * This connection can represent the bandwidth that needs to be leased between the instance groups in the affinity request
 * and the connection delay between the instance groups.
 *
 * @author Anonymous
 * @since LGDCloudSim 1.0
 */
public interface InstanceGroupEdge {
    /**
     * Get the source instance group of the edge.
     *
     * @return the source instance group of the edge.
     */
    InstanceGroup getSrc();

    /**
     * Set the source instance group of the edge.
     * @param source the source instance group of the edge.
     * @return the instance group edge itself.
     */
    InstanceGroupEdge setSrc(InstanceGroup source);

    /**
     * Get the destination instance group of the edge.
     * @return the destination instance group of the edge.
     */
    InstanceGroup getDst();

    /**
     * Set the destination instance group of the edge.
     * @param destination the destination instance group of the edge.
     * @return the instance group edge itself.
     */
    InstanceGroupEdge setDst(InstanceGroup destination);

    /**
     * Get the maximum delay of the edge.
     * The connection delay between the data center scheduled to the src instance group
     * and the data center scheduled to the dst instance group
     * needs to be less than this maximum connection delay
     *
     * @return the maximum delay of the edge.
     */
    double getMaxDelay();

    /**
     * Set the maximum delay of the edge.
     *
     * @param maxDelay the maximum delay of the edge.
     * @return the instance group edge itself.
     */
    InstanceGroupEdge setMaxDelay(double maxDelay);

    /**
     * Get the required bandwidth of the edge.
     * The bandwidth between the data center to which the src instance group is scheduled
     * and the data center to which the dst instance group is scheduled
     * will be occupied by the required number of Mbps.
     *
     * @return the required bandwidth of the edge.
     */
    double getRequiredBw();

    /**
     * Set the required bandwidth of the edge.
     *
     * @param requiredBw the required bandwidth of the edge.
     * @return the instance group edge itself.
     */
    InstanceGroupEdge setRequiredBw(double requiredBw);
}
