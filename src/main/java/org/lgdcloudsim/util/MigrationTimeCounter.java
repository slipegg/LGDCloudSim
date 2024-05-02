package org.lgdcloudsim.util;

import org.lgdcloudsim.datacenter.Datacenter;
import org.lgdcloudsim.network.NetworkTopology;
import org.lgdcloudsim.request.Instance;

public class MigrationTimeCounter {
    /**
     * Get the migration time(ms) of the instance from the source datacenter to the target datacenter.
     *
     * @param instance
     * @param srcDatacenter
     * @param dstDatacenter
     * @return
     */
    static public double getMigrateTime(Instance instance, Datacenter srcDatacenter, Datacenter dstDatacenter, NetworkTopology networkTopology) {
        return instance.getImageSize() / networkTopology.getTransferDataBw(srcDatacenter, dstDatacenter) * 1000;
    }
}
