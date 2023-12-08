package org.cpnsim.util;

import org.cpnsim.datacenter.Datacenter;
import org.cpnsim.request.InstanceGroup;
import org.cpnsim.statemanager.SimpleStateEasyObject;

import java.util.Comparator;
import java.util.Map;
import java.util.PriorityQueue;

public class ScoredDatacentersManager {
    PriorityQueue<ScoredDc> scoredDcPriorityQueue = new PriorityQueue<>(Comparator.comparingDouble(ScoredDc::getScore).reversed());
    Map<Datacenter, Double> scoreDcHistoryMap;

    public ScoredDatacentersManager(Map<Datacenter, Double> scoreDcHistoryMap){
        this.scoreDcHistoryMap = scoreDcHistoryMap;
    }

    public void filterAndScoreDc( Datacenter datacenter, double score){
        scoredDcPriorityQueue.add(new ScoredDc(datacenter, score));
    }


    public int getScoredDcNum(){
        return scoredDcPriorityQueue.size();
    }

    public ScoredDc pollBestScoreDc(){
        return scoredDcPriorityQueue.poll();
    }


}
