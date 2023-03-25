package org.scalecloudsim.resourcemanager;

import org.cloudsimplus.hosts.Host;

import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Map;
import java.util.TreeMap;

public class HostHistoryManagerSimple implements HostHistoryManager{
    public LinkedList<HostResourceStateHistory> history;
    double timeRange;
    Map<Double,HostResourceStateHistory> specialTimeStates;
    Host host;
    public HostHistoryManagerSimple(Host host,double timeRange){
        this.host=host;
        this.history=new LinkedList<>();
        this.specialTimeStates =new TreeMap<>();
        this.timeRange=timeRange;
    }

    public HostHistoryManagerSimple(){
        this.history=new LinkedList<>();
        this.specialTimeStates =new TreeMap<>();
        this.timeRange=0;
    }
    @Override
    public HostHistoryManager addHistory(HostResourceStateHistory hostResourceStateHistory){
        if(history.isEmpty()){
            history.addFirst(hostResourceStateHistory);
            if(specialTimeStates.containsKey(0.0))
            {
                specialTimeStates.put(0.0,hostResourceStateHistory);
            }
            return this;
        }
        if(hostResourceStateHistory.time<history.getFirst().time){//需要点名主机是哪个
            LOGGER.warn("A outdated HostResourceStateHistory of host has been updated");
            return this;
        }
        //维护双端队列
        //过时的就不需要保存
        while (hostResourceStateHistory.time-history.getLast().time>timeRange&&history.size()>1){
            history.removeLast();
        }
        // 同一时间在进行更新
        while (hostResourceStateHistory.time==history.getFirst().time){
            history.removeFirst();
        }
        history.addFirst(hostResourceStateHistory);
        //维护specialTimeStatus
        double nowtime=history.getFirst().time;
        ListIterator<HostResourceStateHistory> iterator =history.listIterator();
        HostResourceStateHistory nowState=null;
        HostResourceStateHistory lastState=null;
        for (Map.Entry<Double,HostResourceStateHistory> entry : specialTimeStates.entrySet()) {
            while (iterator.hasNext()){
                nowState=iterator.next();
                //刚好相等
                if(nowtime-entry.getKey()==nowState.time){
                    specialTimeStates.put(entry.getKey(),nowState);
                    break;
                }
                //夹在中间
                else if((lastState!=null&&nowtime-entry.getKey()<lastState.time)&&nowtime-entry.getKey()>nowState.time){
                    entry.getValue().setHistoryStatus(nowState);
                    break;
                }
                    lastState=nowState;
            }
        }
        return this;
    }

    @Override
    public HostHistoryManager setHistoryRange(double range) {
        return null;
    }

    @Override
    public double getHistoryRange() {
        return 0;
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
        return null;
    }
    @Override
    public HostHistoryManager addSpecialTimeHistoryWatch(double time) {
        if(!specialTimeStates.containsKey(time)){
            HostResourceStateHistory state=findSpecialStatus(time);
            specialTimeStates.put(time,state);
            if(time>timeRange){
                timeRange=time;
            }
        }
        return this;
    }

    HostResourceState getLastState(){
        return history.getFirst();
    }
    public LinkedList getHistory(){
        return history;
    }
    public Map getSpecialTimeStates(){
        return specialTimeStates;
    }
}
