package org.scalecloudsim.Instances;

import java.util.List;

public class InstanceGroupGraphNull implements InstanceGroupGraph{
    @Override
    public InstanceGroupGraph setDirected(boolean directed) {
        return this;
    }

    @Override
    public boolean getDirected() {
        return false;
    }

    @Override
    public InstanceGroupGraph addEdge(InstanceGroup src, InstanceGroup dst, double delay, long bw) {
        return this;
    }

    @Override
    public InstanceGroupGraph addEdge(InstanceGroupEdge edge) {
        return this;
    }

    @Override
    public int removeEdge(InstanceGroup src, InstanceGroup dst) {
        return -1;
    }

    @Override
    public InstanceGroupEdge getEdge(InstanceGroup src, InstanceGroup dst) {
        return null;
    }

    @Override
    public List getGraph() {
        return null;
    }

    @Override
    public List getDstList(InstanceGroup src) {
        return null;
    }
}
