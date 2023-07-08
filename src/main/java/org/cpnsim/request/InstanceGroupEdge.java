package org.cpnsim.request;

public interface InstanceGroupEdge {
    double getMinDelay();

    InstanceGroupEdge setMinDelay(double minDelay);

    double getRequiredBw();

    InstanceGroupEdge setRequiredBw(double requiredBw);

    InstanceGroup getSrc();

    InstanceGroupEdge setSrc(InstanceGroup source);

    InstanceGroup getDst();

    InstanceGroupEdge setDst(InstanceGroup destination);
}
