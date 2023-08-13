package org.cpnsim.request;

import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
public class InstanceGroupEdgeSimple implements InstanceGroupEdge{
    InstanceGroup src;
    InstanceGroup dst;
    double minDelay;
    double requiredBw;

    public InstanceGroupEdgeSimple(InstanceGroup src, InstanceGroup dst, double minDelay, double requiredBw) {
        this.src = Objects.requireNonNull(src);
        this.dst = Objects.requireNonNull(dst);
        this.minDelay = minDelay;
        this.requiredBw = requiredBw;
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
