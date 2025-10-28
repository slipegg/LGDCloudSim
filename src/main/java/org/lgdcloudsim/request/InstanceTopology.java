package org.lgdcloudsim.request;

import java.util.List;

public interface InstanceTopology {
    public static final String P2P = "P2P";
    public static final String All2All = "All2All";
    Instance getMaxScoreInstance();

    List<Instance> getLinkedInstances(Instance instance);

    List<Instance> getAllInstances();

    String getType();

    String toString();
}
