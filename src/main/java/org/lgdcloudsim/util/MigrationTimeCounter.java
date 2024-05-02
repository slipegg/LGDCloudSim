package org.lgdcloudsim.util;

import org.lgdcloudsim.datacenter.Datacenter;
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
    static public double getMigrateTime(Instance instance, Datacenter srcDatacenter, Datacenter dstDatacenter) {
        return 5;
    }
}
