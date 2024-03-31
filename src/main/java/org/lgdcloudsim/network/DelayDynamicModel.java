package org.lgdcloudsim.network;

/**
 * DelayDynamicModel is an interface for the dynamic delay model.
 * Dynamic network delay is mainly used for information transfer between data centers or between data centers and cloud managers.
 * Note that it is necessary to ensure that the dynamic network delay between the same location at the same time is the same.
 *
 * @author Anonymous
 * @since LGDCloudSim 1.0
 */
public interface DelayDynamicModel {
    /**
     * Get the dynamic delay between the source and the destination at the given time.
     *
     * @param srcId the id of the source.
     * @param dstId the id of the destination.
     * @param delay the static delay between the source and the destination.
     * @param time  the time at which the dynamic delay is calculated.
     * @return the dynamic delay between the source and the destination at the given time.
     */
    double getDynamicDelay(int srcId, int dstId, double delay, double time);
}
