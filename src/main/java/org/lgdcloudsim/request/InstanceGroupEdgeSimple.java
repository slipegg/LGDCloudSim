package org.lgdcloudsim.request;

import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

/**
 * InstanceGroupEdgeSimple is a simple implementation of the {@link InstanceGroupEdge} interface.
 *
 * @author Anonymous
 * @since LGDCloudSim 1.0
 */

@Getter
@Setter
public class InstanceGroupEdgeSimple implements InstanceGroupEdge{
    InstanceGroup src;

    InstanceGroup dst;

    double maxDelay;

    double requiredBw;

    /**
     * Construct an instance group edge with the source instance group, the destination instance group,
     * the maximum delay and the required bandwidth.
     *
     * @param src        the source instance group of the edge.
     * @param dst        the destination instance group of the edge.
     * @param maxDelay   the maximum delay of the edge.
     * @param requiredBw the required bandwidth of the edge.
     */
    public InstanceGroupEdgeSimple(InstanceGroup src, InstanceGroup dst, double maxDelay, double requiredBw) {
        this.src = Objects.requireNonNull(src);
        this.dst = Objects.requireNonNull(dst);
        this.maxDelay = maxDelay;
        this.requiredBw = requiredBw;
    }

    @Override
    public String toString() {
        return "InstanceGroupEdgeSimple{" +
                "src=" + src.getId() +
                ", dst=" + dst.getId() +
                ", maxDelay=" + maxDelay +
                ", requiredBw=" + requiredBw +
                '}';
    }
}
