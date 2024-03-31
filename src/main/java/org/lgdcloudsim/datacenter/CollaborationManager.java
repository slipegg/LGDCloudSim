package org.lgdcloudsim.datacenter;

import org.lgdcloudsim.interscheduler.InterScheduler;
import org.lgdcloudsim.queue.InstanceGroupQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A class to manager the collaboration zones of the datacenters.
 *
 * @author Anonymous
 * @since LGDCloudSim 1.0
 */
public interface CollaborationManager {
    /**
     * the Logger of the class.
     **/
    Logger LOGGER = LoggerFactory.getLogger(CollaborationManager.class.getSimpleName());

    /**
     * Add the datacenter to the collaboration with the given collaboration id.
     *
     * @param datacenter      the datacenter to be added to the collaboration
     * @param collaborationId the id of the collaboration
     */
    CollaborationManager addDatacenter(Datacenter datacenter, int collaborationId);

    /**
     * Add the datacenter to the collaboration with the given map.
     *
     * @param datacenters the collaboration map.
     */
    CollaborationManager addDatacenter(Map<Integer, Set<Datacenter>> datacenters);

    /**
     * Remove the datacenter from the collaboration with the given collaboration id.
     *
     * @param datacenter      the datacenter to be removed from the collaboration
     * @param collaborationId the id of the collaboration
     */
    CollaborationManager removeDatacenter(Datacenter datacenter, int collaborationId);

    /**
     * Remove the datacenter from all collaborations.
     *
     * @param datacenter the datacenter to be removed from the collaboration
     */
    CollaborationManager removeDatacenter(Datacenter datacenter);

    /**
     * Get other datacenters in the collaboration zones as this datacenter.
     *
     * @param datacenter the datacenter
     * @return the other datacenters in the same collaboration zones as this datacenter
     */
    List<Datacenter> getOtherDatacenters(Datacenter datacenter);

    /**
     * Get the datacenters in the collaboration with the given collaboration id.
     *
     * @param collaborationId the id of the collaboration
     * @return the datacenters in the collaboration with the given collaboration id
     */
    List<Datacenter> getDatacenters(int collaborationId);

    /**
     * Get the datacenters which are in the same collaboration zones as this datacenter.
     *
     * @param datacenter the datacenter
     * @return
     */
    List<Datacenter> getDatacenters(Datacenter datacenter);

    /**
     * Get the collaboration map.
     *
     * @return the collaboration map
     */
    Map<Integer, Set<Datacenter>> getCollaborationMap();

    /**
     * Get the collaboration ids.
     *
     * @return the collaboration ids
     */
    List<Integer> getCollaborationIds();

    /**
     * Get the interval time for periodic adjustment of collaboration zones.
     *
     * @return the interval time for periodic adjustment of collaboration zones
     */
    double getChangeCollaborationSynTime();

    /**
     * Set the interval time for periodic adjustment of collaboration zones.
     *
     * @param changeCollaborationSynTime the interval time for periodic adjustment of collaboration zones
     */
    CollaborationManager setChangeCollaborationSynTime(double changeCollaborationSynTime);

    /**
     * Get whether the collaboration zones is adjusted periodically.
     *
     * @return true if the collaboration zones is adjusted periodically, false otherwise
     */
    boolean getIsChangeCollaborationSyn();

    /**
     * Set whether the collaboration zones is adjusted periodically.
     *
     * @param isChangeCollaborationSyn true if the collaboration zones is adjusted periodically, false otherwise
     */
    CollaborationManager setIsChangeCollaborationSyn(boolean isChangeCollaborationSyn);

    /**
     * Strategies for realigning collaborative areas
     */
    CollaborationManager changeCollaboration();

    /**
     * Add the center scheduler to the collaboration with the given collaboration id.
     *
     * @param centerScheduler the center scheduler to be added to the collaboration
     * @return the collaboration manager
     */
    CollaborationManager addCenterScheduler(InterScheduler centerScheduler);

    /**
     * Get the center scheduler with the given collaboration id.
     *
     * @return the center scheduler with the given collaboration id
     */
    Map<Integer, InstanceGroupQueue> getCollaborationGroupQueueMap();

    /**
     * Get the map of the collaboration id and the center scheduler.
     * The key is the collaboration id, and the value is the center scheduler of the collaboration with the collaboration id.
     *
     * @return the map of the collaboration id and the center scheduler
     */
    Map<Integer, InterScheduler> getCollaborationCenterSchedulerMap();

    /**
     * Get the map of the collaboration id and the center scheduler busy status.
     * If the value is true, the center scheduler is busy, it can not start a new scheduling.
     * Otherwise, the center scheduler is idle.
     *
     * @return the map of the collaboration id and the center scheduler busy status
     */
    Map<Integer, Boolean> getCenterSchedulerBusyMap();

    /**
     * Get the data center by the given data center id.
     *
     * @param datacenterId the id of the data center
     */
    Datacenter getDatacenterById(int datacenterId);

    /**
     * Get the only collaboration id of the data center.
     *
     * @param datacenterId the id of the data center
     */
    int getOnlyCollaborationId(int datacenterId);
}
