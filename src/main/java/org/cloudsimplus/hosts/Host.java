package org.cloudsimplus.hosts;

import org.cloudsimplus.core.ChangeableId;
import org.cloudsimplus.core.DatacenterEntity;
import org.cloudsimplus.core.Identifiable;
import org.cloudsimplus.core.Simulation;
import org.scalecloudsim.Instances.Instance;
import org.scalecloudsim.datacenters.Datacenter;
import org.scalecloudsim.resourcemanager.HostHistoryManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface Host extends DatacenterEntity {
    Logger LOGGER = LoggerFactory.getLogger(Host.class.getSimpleName());
    HostHistoryManager getHostHistoryManager();
    HostSuitability createInstance(Instance instance);
    Host updateState();
    /**
     * An attribute that implements the Null Object Design Pattern for {@link Host}
     * objects.
     */
    Host NULL = new HostNull();

    Simulation getSimulation();
    Host setSimulation(Simulation simulation);

    /**
     * Gets the Datacenter where the host is placed.
     *
     * @return the data center of the host
     */

    /**
     * Sets the Datacenter where the host is placed.
     *
     * @param datacenter the new data center to move the host
     */


    /**
     * Checks if the host is suitable for a Vm
     * (if it has enough resources to attend the Vm)
     * and the Host is not failed.
     *
     * @param vm the Vm to check
     * @return true if is suitable for Vm, false otherwise
     * @see #getSuitabilityFor(Vm)
     */
//    boolean isSuitableForVm(Vm vm);
}