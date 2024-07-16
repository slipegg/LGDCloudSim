package org.lgdcloudsim.network;

import java.util.Set;

import org.lgdcloudsim.core.SimEntity;
import org.lgdcloudsim.datacenter.Datacenter;
import org.lgdcloudsim.request.UserRequest;

/**
 * NetworkTopology is an interface for the network topology.
 * It contains the {@link AreaDelayManager}, {@link RegionDelayManager}, {@link DcBwManager} and {@link DelayDynamicModel}.
 *
 * @author Anonymous
 * @since LGDCloudSim 1.0
 */
public interface NetworkTopology {
    /**
     * The null network topology.
     */
    NetworkTopology NULL = new NetworkTopologyNull();

    /**
     * Get the delay between the source and the destination.
     * The entity of the source and the destination can be a data center or cloud manager{@link org.lgdcloudsim.core.CloudInformationService}.
     *
     * @param src the source entity.
     * @param dst the destination entity.
     * @return the delay between the source and the destination.
     */
    double getDelay(SimEntity src, SimEntity dst);

    /**
     * Set the delay dynamic model.
     *
     * @param delayDynamicModel the delay dynamic model.
     * @return the network topology.
     */
    NetworkTopology setDelayDynamicModel(DelayDynamicModel delayDynamicModel);

    /**
     * Get the dynamic delay between the source and the destination at the given time.
     *
     * @param src  the source entity.
     * @param dst  the destination entity.
     * @param time the time at which the dynamic delay is calculated.
     * @return the dynamic delay between the source and the destination at the given time.
     */
    double getDynamicDelay(SimEntity src, SimEntity dst, double time);

    /**
     * Get the bandwidth between the source and the destination.
     * The entity of the source and the destination must be a data center.
     *
     * @param src the source entity.
     * @param dst the destination entity.
     * @return the bandwidth between the source and the destination.
     */
    double getBw(SimEntity src, SimEntity dst);

    /**
     * Get the bandwidth between the source and the destination.
     * The entity of the source and the destination must be a data center.
     *
     * @param src the source data center id.
     * @param dst the destination data center id.
     * @return the bandwidth between the source and the destination.
     */
    double getBw(Integer src, Integer dst);

    /**
     * Get the unit price of the bandwidth between the source and the destination.
     * The entity of the source and the destination must be a data center.
     *
     * @param src the source data center id.
     * @param dst the destination data center id.
     * @return the unit price of the bandwidth between the source and the destination.
     */
    double getUnitPrice(Integer src, Integer dst);

    /**
     * Get the unit price of the bandwidth between the source and the destination.
     * The entity of the source and the destination must be a data center.
     *
     * @param src the source entity.
     * @param dst the destination entity.
     * @return the unit price of the bandwidth between the source and the destination.
     */
    double getUnitPrice(SimEntity src, SimEntity dst);

    /**
     * Allocate the bandwidth between the source and the destination.
     * The entity of the source and the destination must be a data center.
     *
     * @param src        the source entity.
     * @param dst        the destination entity.
     * @param allocateBw the bandwidth to be allocated.
     * @return true if the bandwidth is allocated successfully, false otherwise.
     */
    boolean allocateBw(SimEntity src, SimEntity dst, double allocateBw);

    /**
     * Release the bandwidth between the source and the destination.
     * The entity of the source and the destination must be a data center.
     *
     * @param src       the source entity.
     * @param dst       the destination entity.
     * @param releaseBw the bandwidth to be released.
     * @return the network topology.
     */
    NetworkTopology releaseBw(SimEntity src, SimEntity dst, double releaseBw);

    /**
     * Get the total cost of the network bandwidth.
     *
     * @return the total cost of the network bandwidth.
     */
    double getNetworkTCO();

    /**
     * Get the access latency between the user request and the data center.
     * The access latency is calculated based on the area where the user belongs and the region where the data center belongs.
     *
     * @param userRequest the user request.
     * @param datacenter  the data center.
     * @return the access latency between the user request and the data center.
     */
    double getAccessLatency(UserRequest userRequest, Datacenter datacenter);

    /**
     * Get the data center id list.
     *
     * @return the data center id list.
     */
    Set<Integer> getDcIdList();
}
