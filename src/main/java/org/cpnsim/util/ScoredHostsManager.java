package org.cpnsim.util;

import org.cpnsim.datacenter.Datacenter;

import java.util.Comparator;
import java.util.Map;
import java.util.PriorityQueue;

public class ScoredHostsManager {
    PriorityQueue<ScoredHost> scoredHostPriorityQueue = new PriorityQueue<>(Comparator.comparingDouble(ScoredHost::getScore).reversed());
    Map<Datacenter, Map<Integer, Double>> scoreHostHistoryMap;

    public ScoredHostsManager(Map<Datacenter, Map<Integer, Double>> scoreHostHistoryMap){
        this.scoreHostHistoryMap = scoreHostHistoryMap;
    }

    public void addScoredHost(int hostId, Datacenter datacenter,double score){
        scoredHostPriorityQueue.add(new ScoredHost(datacenter, hostId, score));
    }

    public int getScoredHostNum(){
        return scoredHostPriorityQueue.size();
    }

    public ScoredHost pollBestScoreHost(){
        return scoredHostPriorityQueue.poll();
    }
}
