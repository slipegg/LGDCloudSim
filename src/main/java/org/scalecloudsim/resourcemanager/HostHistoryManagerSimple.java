package org.scalecloudsim.resourcemanager;

import org.cloudsimplus.hosts.Host;

import java.util.*;

public class HostHistoryManagerSimple implements HostHistoryManager{
    public LinkedList<HostResourceStateHistory> history;
    double timeRange;
    Map<Double,HostResourceStateHistory> delayStates;
    Map<Double,Integer> delayWatchNum;
    Host host;
    public HostHistoryManagerSimple(Host host,double timeRange){
        this.host=host;
        this.history=new LinkedList<>();
        this.delayStates =new TreeMap<>();
        this.delayWatchNum=new HashMap<>();
        this.timeRange=timeRange;
    }
    public HostHistoryManagerSimple(Host host){
        this.host=host;
        this.history=new LinkedList<>();
        this.delayStates =new TreeMap<>();
        this.delayWatchNum=new HashMap<>();
        this.timeRange=0;
    }

    public HostHistoryManagerSimple(){
        this.history=new LinkedList<>();
        this.delayStates =new TreeMap<>();
        this.delayWatchNum=new HashMap<>();
        this.timeRange=0;
    }

    @Override
    public HostHistoryManager updateHistory(double nowTime) {
        //维护双端队列
        removeOldHistory(nowTime);
        //维护delayStates
        updateDelayStates(nowTime);
        return this;
    }

    @Override
    public HostHistoryManager addHistory(HostResourceStateHistory hostResourceStateHistory){
        if(!history.isEmpty()&&hostResourceStateHistory.time<history.getFirst().time){//warn需要点名主机是哪个
            LOGGER.warn("A outdated HostResourceStateHistory of host has been sent to update");
            return this;
        }
        double nowTime=hostResourceStateHistory.getTime();
        //过时的就不需要保存
        removeOldHistory(nowTime);
        // 同一时间在进行更新的需要删除然后覆盖
        while (history.size()>0&&nowTime==history.getFirst().time){
            history.removeFirst();
        }
        //添加到队头
        history.addFirst(hostResourceStateHistory);
        //维护delayStates
        updateDelayStates(nowTime);
        return this;
    }

    //过时的就不需要保存在双端队列中
    private void removeOldHistory(double nowTime){
        int removeSum=0;
        Iterator<HostResourceStateHistory> lastiterator =history.descendingIterator();
        //找到小于等于nowTime-timeRange的记录的个数
        while(lastiterator.hasNext()){
            HostResourceStateHistory last=lastiterator.next();
            if(last.time<=nowTime-timeRange){
                removeSum++;
            }
            else{
                break;
            }
        }
        //额外保存小于等于nowTime-timeRange的最新的一个记录
        removeSum--;
        //删除掉不需要的记录
        while (removeSum>0){
            history.removeLast();
            removeSum--;
        }
    }
    //维护delayStates
    private void updateDelayStates(double nowTime){
        //这里因为效率问题，所以没有使用getSpecialTimeHostState方法，而是自己遍历，因为这样可以记录上一次遍历的位置，不用从头开始遍历，时间复杂度为O(m+n)
        int nowHistoryIndex=0;
        int historySize=history.size();
        HostResourceStateHistory nowState=null;
        HostResourceStateHistory lastState=null;
        for (Map.Entry<Double,HostResourceStateHistory> entry : delayStates.entrySet()) {
            double delayTime=entry.getKey();
            HostResourceStateHistory delayHostState=entry.getValue();
            while (nowHistoryIndex<historySize){
                nowState=history.get(nowHistoryIndex);
                //刚好相等
                if(nowTime-delayTime>=nowState.time){
                    delayHostState.setHistoryStatus(nowState);
                    break;
                }
                //夹在中间
                else if((nowHistoryIndex>0&&nowTime-delayTime<history.get(nowHistoryIndex-1).time)&&nowTime-delayTime>nowState.time){
                    delayHostState.setHistoryStatus(nowState);
                    break;
                }
                nowHistoryIndex++;
            }
        }
    }

    //得到特定时间的状态
    @Override
    public HostResourceStateHistory getSpecialTimeHostState(double time) {
        double nowTime=host.getSimulation().clock();
        if(time>nowTime){
//            LOGGER.warn("The time you want to get is in the future!");
            return new HostResourceStateHistory(-1,-1,-1,-1,-1);
        }
        else if(!history.isEmpty()&&time<history.getLast().time){
//            LOGGER.warn("The time you want to get is not recorded!");
            return new HostResourceStateHistory(-1,-1,-1,-1,-1);
        }
        else{//夹在中间
            ListIterator<HostResourceStateHistory> iterator =history.listIterator();
            HostResourceStateHistory prestate=null;
            while (iterator.hasNext()){
                HostResourceStateHistory nowstate=iterator.next();
                if(time>=nowstate.time){
                    return nowstate;
                }
                else if(nowstate.time<time&&prestate!=null&&prestate.time>time){
                    return prestate;
                }
                prestate=nowstate;
            }
            return new HostResourceStateHistory(-1,-1,-1,-1,-1);
        }
    }

    @Override
    public HostHistoryManager setHistoryRange(double range) {
        timeRange=range;
        return this;
    }

    @Override
    public double getHistoryRange() {
        return timeRange;
    }



//    private HostResourceStateHistory findDelayStatus(double delayTime){
//        double nowTime=host.getSimulation().clock();
//        if(nowTime-delayTime<history.getLast().time){
//            return new HostResourceStateHistory(-1,-1,-1,-1,-1);
//        }
//        ListIterator<HostResourceStateHistory> iterator =history.listIterator();
//        HostResourceStateHistory prestate=null;
//        while (iterator.hasNext()){
//            HostResourceStateHistory nowstate=iterator.next();
//            if(nowstate.time==delayTime){
//                return nowstate;
//            }
//            else{
//                if(prestate!=null&&prestate.time>delayTime&&nowstate.time<delayTime){
//                    return prestate;
//                }
//            }
//            prestate=nowstate;
//        }
//        return new HostResourceStateHistory(-1,-1,-1,-1,-1);
//    }
    @Override
    public HostResourceState addDelayWatch(double delayTime) {
        if(!delayStates.containsKey(delayTime)){
            HostResourceStateHistory state= getSpecialTimeHostState(host.getSimulation().clock()-delayTime);
            delayStates.put(delayTime,state);
            if(delayTime >timeRange){
                timeRange= delayTime;
            }
            delayWatchNum.put(delayTime,1);
            return state;
        }
        else{
            Integer num=delayWatchNum.get(delayTime);
            num+=1;
        }
        return delayStates.get(delayTime);
    }

    @Override
    public HostHistoryManager delDelayWatch(double delayTime) {
        if(!delayStates.containsKey(delayTime)){
            LOGGER.warn("There is not delayTime("+delayTime+") to delete watch.");
            return this;
        }
        Integer num=delayWatchNum.get(delayTime);
        num-=1;
        if(num==0){
            delayStates.remove(delayTime);
            delayWatchNum.remove(delayTime);
        }
        return this;
    }

    HostResourceState getLastState(){
        return history.getFirst();
    }
    public LinkedList getHistory(){
        return history;
    }
    public Map getDelayStates(){
        return delayStates;
    }
}
