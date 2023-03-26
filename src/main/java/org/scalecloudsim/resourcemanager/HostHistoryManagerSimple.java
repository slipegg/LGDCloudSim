package org.scalecloudsim.resourcemanager;

import org.cloudsimplus.hosts.Host;
import org.cloudsimplus.hosts.HostSimple;

import java.util.*;

public class HostHistoryManagerSimple implements HostHistoryManager{
    public LinkedList<HostResourceStateHistory> history;
    double timeRange;
    Map<Double,HostResourceStateHistory> delayStates;
    Host host;
    public HostHistoryManagerSimple(Host host,double timeRange){
        this.host=host;
        this.history=new LinkedList<>();
        this.delayStates =new TreeMap<>();
        this.timeRange=timeRange;
    }
    public HostHistoryManagerSimple(Host host){
        this.host=host;
        this.history=new LinkedList<>();
        this.delayStates =new TreeMap<>();
        this.timeRange=0;
    }

    public HostHistoryManagerSimple(){
        this.history=new LinkedList<>();
        this.delayStates =new TreeMap<>();
        this.timeRange=0;
    }

//    public HostHistoryManager recordHistory(){
//        HostResourceStateHistory hostResourceStateHistory=new HostResourceStateHistory(host.ge)
//    }

    @Override
    public HostHistoryManager updateHistory(double nowTime) {
        //维护双端队列
        maintainHistory(nowTime);
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
        //维护双端队列
        maintainHistory(nowTime);
        //添加到队头
        history.addFirst(hostResourceStateHistory);
        //维护delayStates
        updateDelayStates(nowTime);
        return this;
    }

    private void maintainHistory(double nowTime){
        //过时的就不需要保存
        Iterator<HostResourceStateHistory> lastiterator =history.descendingIterator();
        HostResourceStateHistory last;
        if(lastiterator.hasNext()){
            lastiterator.next();
        }
        int removeSum=0;
        while (lastiterator.hasNext()){
            HostResourceStateHistory secondLast=lastiterator.next();
            if(secondLast.time<=nowTime-timeRange){
                removeSum++;
            }
            else{
                break;
            }
        }
        while (removeSum!=0){
            history.removeLast();
            removeSum--;
        }
        // 同一时间在进行更新
        while (history.size()>0&&nowTime==history.getFirst().time){
            history.removeFirst();
        }
    }

    private void updateDelayStates(double nowTime){
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
    @Override
    public HostResourceState getSpecialTimeHostState(double delay) {
        if(!delayStates.containsKey(delay)){
            LOGGER.warn("This delay time("+delay+") has not been added to watch!Please use addDelayWatch(delay time)");
            return null;
        }
        else{
        return delayStates.get(delay);
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

    private HostResourceStateHistory findSpecialStatus(double time){
        ListIterator<HostResourceStateHistory> iterator =history.listIterator();
        HostResourceStateHistory prestate=null;
        while (iterator.hasNext()){
            HostResourceStateHistory nowstate=iterator.next();
            if(nowstate.time==time){
                return nowstate;
            }
            else{
                if(prestate!=null&&prestate.time>time&&nowstate.time<time){
                    return prestate;
                }
            }
            prestate=nowstate;
        }
        return new HostResourceStateHistory(-1,-1,-1,-1,-1);
    }
    @Override
    public HostResourceState addDelayWatch(double delayTime) {
        if(!delayStates.containsKey(delayTime)){
            HostResourceStateHistory state=findSpecialStatus(delayTime);
            delayStates.put(delayTime,state);
            if(delayTime >timeRange){
                timeRange= delayTime;
            }
            return state;
        }
        return delayStates.get(delayTime);
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
