package org.lgdcloudsim.util;

import org.lgdcloudsim.datacenter.Datacenter;

import java.util.Comparator;
import java.util.Map;
import java.util.PriorityQueue;

/**
 * The manager that manages the scored data centers.
 * It contains the priority queue of the scored data centers and the score data center history map.
 *
 * @author Anonymous
 * @since LGDCloudSim 1.0
 */
public class ScoredDatacentersManager {
    /**
     * The priority queue of the scored data centers.
     */
    PriorityQueue<ScoredDc> scoredDcPriorityQueue = new PriorityQueue<>(Comparator.comparingDouble(ScoredDc::getScore).reversed());

    /**
     * The score data center history map.
     */
    Map<Datacenter, Double> scoreDcHistoryMap;

    /**
     * Construct the scored data centers manager with the score data center history map.
     *
     * @param scoreDcHistoryMap the score data center history map.
     */
    public ScoredDatacentersManager(Map<Datacenter, Double> scoreDcHistoryMap){
        this.scoreDcHistoryMap = scoreDcHistoryMap;
    }

    /**
     * Filter and score the data center.
     * @param datacenter the data center to be filtered and scored.
     * @param score the score of the data center.
     */
    public void filterAndScoreDc( Datacenter datacenter, double score){
        scoredDcPriorityQueue.add(new ScoredDc(datacenter, score));
    }

    /**
     * Get the scored data center number.
     * @return the scored data center number.
     */
    public int getScoredDcNum(){
        return scoredDcPriorityQueue.size();
    }

    /**
     * Poll the best scored data center.
     * @return the best scored data center.
     */
    public ScoredDc pollBestScoreDc(){
        return scoredDcPriorityQueue.poll();
    }
}
