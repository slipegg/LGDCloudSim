package org.lgdcloudsim.core;

import org.lgdcloudsim.datacenter.Datacenter;

/**
 * An interface to be implemented by classes that have a reference to a {@link Datacenter} object.
 * It is used by components that exist within the data center.
 *
 * @author Anonymous
 * @since LGDCloudSim
 */
public interface DatacenterEntity {
    /**
     * Gets the Datacenter where the component belongs to.
     *
     * @return the Datacenter object
     */
    Datacenter getDatacenter();

    /**
     * Sets the Datacenter where the component belongs to.
     * @param datacenter the Datacenter component to set
     */
    void setDatacenter(Datacenter datacenter);
}
