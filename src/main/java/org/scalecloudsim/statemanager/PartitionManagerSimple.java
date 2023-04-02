package org.scalecloudsim.statemanager;

import org.cloudsimplus.core.Simulation;

import java.util.*;

public class PartitionManagerSimple implements PartitionManager{
    TreeSet<Double> delayWatchs;//记录需要监控的时延，也用来维护需要监控的最大长度
    Map<Double,Map<Integer,HostStateHistory>> watchTable;//一张表，记录需要监控的时延对应的历史状态，如果没有记录，就说明在记录的时间范围内其主机的状态都与大表相同
    Map<Double,Integer> delayWatchNum;//记录需要监控的时延的关联的个数
    StateManager stateManager;

    //一些冗余变量，用来提高可读性
    int paritionId;
    TreeMap<Integer,LinkedList<HostStateHistory>> hostHistoryMaps;//记录所有的历史状态，是一个对StateManager的引用
    Simulation simulation;
    double nowTime;//记录当前时刻，当做一个全局变量

    public PartitionManagerSimple(StateManager stateManager,int paritionId) {
        this.stateManager = stateManager;
        this.simulation =stateManager.getSimulation();
        this.hostHistoryMaps=stateManager.getHostHistoryMaps();
        this.paritionId=paritionId;
        this.delayWatchs = new TreeSet<>();
        this.watchTable = new TreeMap<>();
        this.delayWatchNum = new TreeMap<>();
    }

    @Override
    public PartitionManager addDelayWatch(double delay) {
        if(!watchTable.containsKey(delay)){
            Map<Integer,HostStateHistory> watchDate = new HashMap<>();
            watchTable.put(delay,watchDate);
            delayWatchNum.put(delay,1);
            delayWatchs.add(delay);
            updateWatchTable(delay);
        }
        else{
            int num=delayWatchNum.get(delay);
            delayWatchNum.put(delay,num+1);
        }
        return this;
    }

    private void updateWatchTable(double delay){
        nowTime=simulation.clock();
        Map<Integer,HostStateHistory> watchDate = watchTable.get(delay);
        int[] range=stateManager.getPartitionRangesManager().getRange(paritionId);
        Map<Integer,LinkedList<HostStateHistory>> hostStateHistoryMap=hostHistoryMaps.subMap(range[0],range[1]+1);
        for (Integer hostId : hostStateHistoryMap.keySet()) {
            HostStateHistory hostStateHistory=stateManager.getHostStateHistory(hostId,nowTime-delay);
            if(hostStateHistory!=null){
                watchDate.put(hostId,hostStateHistory);
            }
        }
    }

    @Override
    public PartitionManager delDelayWatch(double delay) {
        int delayNum=delayWatchNum.get(delay);
        delayNum--;
        if (delayNum==0){
            watchTable.remove(delay);
            delayWatchs.remove(delay);
            delayWatchNum.remove(delay);
        }
        else{
            delayWatchNum.put(delay,delayNum);
        }
        return this;
    }

    @Override
    public PartitionManager delAllDelayWatch() {
        delayWatchs.clear();
        delayWatchNum.clear();
        watchTable.clear();;
        return this;
    }

    @Override
    public PartitionManager addHostHistory(int hostId,HostStateHistory hostStateHistory) {
        nowTime=simulation.clock();
        if(!hostHistoryMaps.containsKey(hostId)){
            LinkedList<HostStateHistory> hostHistory=new LinkedList<>();
            hostHistoryMaps.put(hostId,hostHistory);
        }
        LinkedList<HostStateHistory> hostHistory=hostHistoryMaps.get(hostId);
        hostHistory.addFirst(hostStateHistory);
        removeOldHistory(nowTime,hostId);//增加时，lastTime=nowTime
        updateWatchTable(nowTime,hostId);
        return this;
    }

    @Override
    public PartitionManager updateHostHistory(double lastTime, int hostId) {
        nowTime=simulation.clock();
        removeOldHistory(lastTime,hostId);
        updateWatchTable(lastTime,hostId);
        return this;
    }

    //过时的就不需要保存在双端队列中
    private void removeOldHistory(double lastTime,int hostId){
        int removeSum=0;
        LinkedList<HostStateHistory> history=hostHistoryMaps.get(hostId);
        Iterator<HostStateHistory> lastiterator =history.descendingIterator();
        //找到小于等于nowTime-timeRange的记录的个数
        while(lastiterator.hasNext()){
            HostStateHistory last=lastiterator.next();
            if(last.time<=nowTime-delayWatchs.last()){
                removeSum++;
            }
            else{
                break;
            }
        }
        //还需要和大表的那一个节点进行比较
        if(lastTime<=nowTime-delayWatchs.last()){
            removeSum++;
        }
        //额外保存小于等于nowTime-delayWatchs.last()的最新的一个记录
        removeSum--;
        //删除掉不需要的记录
        while (removeSum>0){
            history.removeLast();
            removeSum--;
        }
    }

    //维护watchTable
    private void updateWatchTable(double lastTime, int hostId){
        //这里因为效率问题，所以没有使用getSpecialTimeHostState方法，而是自己遍历，因为这样可以记录上一次遍历的位置，不用从头开始遍历，时间复杂度为O(m+n)
        LinkedList<HostStateHistory> history=hostHistoryMaps.get(hostId);
        int nowHistoryIndex=0;
        int historySize=history.size();
        HostStateHistory nowState=null;
        HostStateHistory lastState=null;
        for (Map.Entry<Double,Map<Integer,HostStateHistory>> entry : watchTable.entrySet()) {
            double delayTime=entry.getKey();
            Map<Integer,HostStateHistory> hostStateHistoryMap=entry.getValue();
            if(nowTime-delayTime>=lastTime){
                hostStateHistoryMap.remove(hostId);//这个已经对应到大表的值了，所以可以删除
            }
            else{
                while (nowHistoryIndex<historySize){
                    nowState=history.get(nowHistoryIndex);
                    //刚好相等
                    if(nowTime-delayTime>=nowState.time){
                        hostStateHistoryMap.put(hostId,nowState);//记录过去的历史状态
                        break;
                    }
                    nowHistoryIndex++;
                }
            }
        }
    }

    @Override
    public Map<Integer, HostStateHistory> getDelayPartitionState(double delay) {
        return watchTable.get(delay);
    }
}
