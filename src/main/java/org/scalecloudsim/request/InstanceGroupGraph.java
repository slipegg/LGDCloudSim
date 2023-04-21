package org.scalecloudsim.request;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;

public interface InstanceGroupGraph extends RequestEntity{
    Logger LOGGER = LoggerFactory.getLogger(InstanceGroupGraph.class.getSimpleName());

    boolean getDirected();

     InstanceGroupGraph setDirected(boolean directed);

     InstanceGroupGraph addEdge(InstanceGroup src, InstanceGroup dst, double delay, long bw);

    InstanceGroupGraph addEdge(InstanceGroupEdge edge);

    int removeEdge(InstanceGroup src, InstanceGroup dst);

    InstanceGroupEdge getEdge(InstanceGroup src, InstanceGroup dst);

    Set<InstanceGroupEdge> getGraph();

    List<InstanceGroup> getDstList(InstanceGroup src);

    List<InstanceGroup> getSrcList(InstanceGroup dst);

    double getDelay(InstanceGroup src, InstanceGroup dst);

    double getBw(InstanceGroup src, InstanceGroup dst);

}
