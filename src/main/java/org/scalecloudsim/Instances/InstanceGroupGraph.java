package org.scalecloudsim.Instances;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public interface InstanceGroupGraph {
    Logger LOGGER = LoggerFactory.getLogger(InstanceGroupGraph.class.getSimpleName());
    InstanceGroupGraph NULL = new InstanceGroupGraphNull();
    public InstanceGroupGraph setDirected(boolean directed);

    public boolean getDirected();

    public InstanceGroupGraph addEdge(InstanceGroup src,InstanceGroup dst,double delay,long bw);

    public InstanceGroupGraph addEdge(InstanceGroupEdge edge);

    public int removeEdge(InstanceGroup src, InstanceGroup dst);

    public InstanceGroupEdge getEdge(InstanceGroup src,InstanceGroup dst);

    public List getGraph();

    public List getDstList(InstanceGroup src);

}
