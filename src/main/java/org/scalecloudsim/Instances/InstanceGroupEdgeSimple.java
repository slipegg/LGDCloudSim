package org.scalecloudsim.Instances;

import java.util.Objects;

public class InstanceGroupEdgeSimple implements InstanceGroupEdge{
    InstanceGroup src;
    InstanceGroup dst;
    double minDelay;
    long requiredBw;

    public InstanceGroupEdgeSimple(InstanceGroup src, InstanceGroup dst, double minDelay, long requiredBw) {
        this.src = Objects.requireNonNull(src);
        this.dst = Objects.requireNonNull(dst);
        this.minDelay = minDelay;
        this.requiredBw = requiredBw;
    }


    @Override
    public double getMinDelay() {
        return minDelay;
    }

    @Override
    public InstanceGroupEdge setMinDelay(double minDelay) {
        this.minDelay = minDelay;
        return this;
    }

    @Override
    public double getRequiredBw() {
        return requiredBw;
    }

    @Override
    public InstanceGroupEdge setRequiredBw(double requiredBw) {
        this.requiredBw = (long) requiredBw;
        return this;
    }

    @Override
    public InstanceGroup getSrc() {
        return src;
    }

    @Override
    public InstanceGroupEdge setSrc(InstanceGroup source) {
        this.src = source;
        return this;
    }

    @Override
    public InstanceGroup getDst() {
        return dst;
    }

    @Override
    public InstanceGroupEdge setDst(InstanceGroup destination) {
        this.dst = destination;
        return this;
    }
    @Override
    public String toString() {
        return "InstanceGroupEdgeSimple{" +
                "src=" + src.getId() +
                ", dst=" + dst.getId() +
                ", minDelay=" + minDelay +
                ", requiredBw=" + requiredBw +
                '}';
    }
}
