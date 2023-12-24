package org.cpnsim.datacenter;

import org.cpnsim.interscheduler.InterScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A class to manager the collaboration of the datacenters.
 *
 * @author Jiawen Liu
 * @since CPNSim 1.0
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
     * Get the interval time for periodic adjustment of collaboration areas.
     *
     * @return the interval time for periodic adjustment of collaboration areas
     */
    double getChangeCollaborationSynTime();

    /**
     * Set the interval time for periodic adjustment of collaboration areas.
     *
     * @param changeCollaborationSynTime the interval time for periodic adjustment of collaboration areas
     */
    CollaborationManager setChangeCollaborationSynTime(double changeCollaborationSynTime);

    /**
     * Get whether the collaboration areas is adjusted periodically.
     *
     * @return true if the collaboration areas is adjusted periodically, false otherwise
     */
    boolean getIsChangeCollaborationSyn();

    /**
     * Set whether the collaboration areas is adjusted periodically.
     *
     * @param isChangeCollaborationSyn true if the collaboration areas is adjusted periodically, false otherwise
     */
    CollaborationManager setIsChangeCollaborationSyn(boolean isChangeCollaborationSyn);

    /**
     * Strategies for realigning collaborative areas
     */
    CollaborationManager changeCollaboration();

    CollaborationManager addCenterScheduler(InterScheduler centerScheduler);

    Map<Integer, InstanceGroupQueue> getCollaborationGroupQueueMap();

    Map<Integer, InterScheduler> getCollaborationCenterSchedulerMap();

    Map<Integer, Boolean> getCenterSchedulerBusyMap();

    Datacenter getDatacenterById(int datacenterId);

    int getOnlyCollaborationId(int datacenterId);
}
