package org.lgdcloudsim.datacenter;

import org.lgdcloudsim.conflicthandler.ConflictHandler;
import org.lgdcloudsim.core.SimEntity;
import org.lgdcloudsim.intrascheduler.IntraScheduler;
import org.lgdcloudsim.interscheduler.InterScheduler;
import org.lgdcloudsim.loadbalancer.LoadBalancer;
import org.lgdcloudsim.queue.InstanceQueue;
import org.lgdcloudsim.request.Instance;
import org.lgdcloudsim.request.InstanceGroup;
import org.lgdcloudsim.statemanager.StatesManager;

import java.awt.geom.Point2D;
import java.util.List;
import java.util.Set;

/**
 * An interface to be implemented by each class that represents a datacenter.
 * It extends {@link SimEntity} and {@link DatacenterPrice}.
 * The mainly components of a datacenter are the {@link InterScheduler}, {@link LoadBalancer}, {@link IntraScheduler}, {@link ConflictHandler}, and {@link StatesManager}.
 *
 * @author Anonymous
 * @since LGDCloudSim 1.0
 */
public interface Datacenter extends SimEntity, DatacenterPrice {
    /**
     * the NULL datacenter.
     **/
    public static final Datacenter NULL = new DatacenterNull();

    /** Get the collaborationIds to which the datacenter belongs. **/
    Set<Integer> getCollaborationIds();

    /**
     * Set the load balancer that is used to distribute the instances to the inter-schedulers.
     */
    Datacenter setInterLoadBalancer(LoadBalancer<InstanceGroup, InterScheduler> interLoadBalancer);

    /**
     * Set the {@link InterScheduler}s.
     */
    Datacenter setInterSchedulers(List<InterScheduler> interSchedulers);

    /**
     * Set the {@link IntraScheduler}s.
     **/
    Datacenter setIntraSchedulers(List<IntraScheduler> intraSchedulers);

    /**
     * Get the {@link InterScheduler}s.
     **/
    List<IntraScheduler> getIntraSchedulers();

    /**
     * Set the {@link LoadBalancer}.
     **/
    Datacenter setIntraLoadBalancer(LoadBalancer<Instance, IntraScheduler> intraLoadBalancer);

    /**
     * Set the {@link ConflictHandler}.
     **/
    Datacenter setConflictHandler(ConflictHandler conflictHandler);

    /**
     * Get the {@link ConflictHandler}.
     **/
    ConflictHandler getConflictHandler();

    /**
     * Set the {@link StatesManager}.
     **/
    Datacenter setStatesManager(StatesManager statesManager);

    /**
     * Get the {@link StatesManager}.
     **/
    StatesManager getStatesManager();

    /**
     * Set whether the inter-architecture is centralized.
     * If there is a centralized inter-scheduler in the datacenter, the inter-architecture is centralized.
     *
     * @param centralizedInterScheduleFlag whether the inter-architecture is centralized.
     * @return the datacenter.
     */
    Datacenter setCentralizedInterScheduleFlag(boolean centralizedInterScheduleFlag);

    /**
     * Get whether the inter-architecture is centralized.
     **/
    boolean isCentralizedInterSchedule();

    /**
     * Get the estimated TCO of the instance group.
     * @param instanceGroup the instance group.
     * @return the estimated TCO.
     */
    double getEstimatedTCO(InstanceGroup instanceGroup);

    /**
     * Get the instance queue of the datacenter.
     * The instance queue is used to store the instances which are waiting loadbalancer to distribute them to the intra-schedulers.
     * @return the estimated TCO.
     */
    InstanceQueue getInstanceQueue();

    /**
     * Get the region of the datacenter.
     * @return the region of the datacenter.
     */
    String getRegion();

    /**
     * Set the region of the datacenter.
     * @param region the region of the datacenter.
     * @return the datacenter.
     */
    Datacenter setRegion(String region);

    /**
     * Get the location of the datacenter.
     * @return the location of the datacenter.
     */
    Point2D getLocation();

    /**
     * Set the location of the datacenter.
     * @param latitude the latitude of the datacenter.
     * @param longitude the longitude of the datacenter.
     * @return the datacenter.
     */
    Datacenter setLocation(double latitude, double longitude);

    /**
     * Get the architecture of the datacenter.
     * @return the architecture of the datacenter.
     */
    String getArchitecture();

    /**
     * Set the architecture of the datacenter.
     * @param architecture the architecture of the datacenter.
     * @return the datacenter.
     */
    Datacenter setArchitecture(String architecture);

    /**
     * Get the host number of the datacenter.
     * @return the host number of the datacenter.
     */
    long getHostNum();

    /**
     * Get the total CPU of the datacenter.
     * @return the total CPU of the datacenter.
     */
    long getCpu();

    /**
     * Get the total memory of the datacenter.
     * @return the total memory of the datacenter.
     */
    long getRam();

    /**
     * Get the total storage of the datacenter.
     * @return the total storage of the datacenter.
     */
    long getStorage();

    /**
     * Get the total bandwidth of the datacenter.
     * @return the total bandwidth of the datacenter.
     */
    long getBw();

    /**
     * Get the price per CPU of the datacenter.
     * @return the price per CPU of the datacenter.
     */
    double getPricePerCPU();

    /**
     * Get the price per memory of the datacenter.
     * @return the price per memory of the datacenter.
     */
    double getPricePerRAM();

    /**
     * Get the price per storage of the datacenter.
     * @return the price per storage of the datacenter.
     */
    double getPricePerStorage();

    /**
     * Get the price per bandwidth of the datacenter.
     * @return the price per bandwidth of the datacenter.
     */
    double getPricePerBW();

    /**
     * Get the price per rack of the datacenter.
     * @return the price per rack of the datacenter.
     */
    double getPricePerRack();

    /**
     * Get the price per host of the datacenter.
     * @return the price per host of the datacenter.
     */
    double getHostPerRack();
}
