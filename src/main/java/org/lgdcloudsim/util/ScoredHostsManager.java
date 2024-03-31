package org.lgdcloudsim.util;

import org.lgdcloudsim.datacenter.Datacenter;

import java.util.Comparator;
import java.util.Map;
import java.util.PriorityQueue;

/**
 * The manager that manages the scored hosts.
 * It contains the priority queue of the scored hosts and the score host history map.
 *
 * @author Anonymous
 * @since LGDCloudSim 1.0
 */
public class ScoredHostsManager {
    /**
     * The priority queue of the scored hosts.
     */
    PriorityQueue<ScoredHost> scoredHostPriorityQueue = new PriorityQueue<>(Comparator.comparingDouble(ScoredHost::getScore).reversed());

    /**
     * The score host history map.
     */
    Map<Datacenter, Map<Integer, Double>> scoreHostHistoryMap;

    /**
     * Construct the scored hosts manager with the score host history map.
     *
     * @param scoreHostHistoryMap the score host history map.
     */
    public ScoredHostsManager(Map<Datacenter, Map<Integer, Double>> scoreHostHistoryMap){
        this.scoreHostHistoryMap = scoreHostHistoryMap;
    }

    /**
     * Filter and score the host.
     * @param hostId the host id to be filtered and scored.
     * @param datacenter the data center where the host is from.
     * @param score the score of the host.
     */
    public void addScoredHost(int hostId, Datacenter datacenter,double score){
        scoredHostPriorityQueue.add(new ScoredHost(datacenter, hostId, score));
    }

    /**
     * Get the scored host number.
     * @return the scored host number.
     */
    public int getScoredHostNum(){
        return scoredHostPriorityQueue.size();
    }

    /**
     * Poll the best scored host.
     * @return the best scored host.
     */
    public ScoredHost pollBestScoreHost(){
        return scoredHostPriorityQueue.poll();
    }
}
