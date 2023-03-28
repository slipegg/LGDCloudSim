package org.scalecloudsim.resourcemanager;

import org.cloudsimplus.hosts.Host;
import org.scalecloudsim.datacenters.Datacenter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class PartitionStateManagerSimple implements PartitionStateManager{
    Logger LOGGER = LoggerFactory.getLogger(PartitionStateManagerSimple.class.getSimpleName());
    PartitionRange partitionRange;
    StateManager stateManager;
    Map<Double,List<HostResourceState>> partitionState;
    public PartitionStateManagerSimple(StateManager stateManager,PartitionRange partitionRange){
        this.stateManager=stateManager;
        setPartitionRange(partitionRange);
        partitionState=new HashMap<>();
    }
    @Override
    public PartitionStateManager setPartitionRange(PartitionRange partitionRange) {
        if(this.partitionRange!=partitionRange){
            LOGGER.info("the partition range has been set,we will change the partition range and the watch will be deleted");
            for(Map.Entry<Double,List<HostResourceState>> entry:partitionState.entrySet()){
                delDelayWatch(entry.getKey());
            }
        }
        this.partitionRange=partitionRange;
        return this;
    }

    @Override
    public PartitionRange getPartitionRange() {
        return partitionRange;
    }

    @Override
    public PartitionStateManager addDelayWatch(double delay) {
        List<HostResourceState> partitionDelayState=new ArrayList<>();
        partitionState.put(delay,partitionDelayState);
        //注意for循环需要优化
        List<Host> hosts=stateManager.getDatacenter().getHostList();
        for(int i=partitionRange.getStartIndex();i<= partitionRange.getEndIndex();i++){
            Host host=hosts.get(i);
            HostHistoryManager hostHistoryManager= host.getHostHistoryManager();
            HostResourceState hostResourceState = hostHistoryManager.addDelayWatch(delay);
            partitionDelayState.add(hostResourceState);
        }
        return this;
    }

    @Override
    public PartitionStateManager delDelayWatch(double delay) {
//        List<HostResourceState> partitionDelayState=new ArrayList<>();
//        partitionState.put(delay,partitionDelayState);
        //注意for循环需要优化
        List<Host> hosts=stateManager.getDatacenter().getHostList();
        for(int i=partitionRange.getStartIndex();i<= partitionRange.getEndIndex();i++){
            Host host=hosts.get(i);
            HostHistoryManager hostHistoryManager= host.getHostHistoryManager();
            hostHistoryManager.delDelayWatch(delay);
        }
        partitionState.remove(delay);
        return this;
    }

    @Override
    public PartitionStateManager delAllDelayWatch() {
        if(!partitionState.isEmpty())
        {
            Set<Double> keys=partitionState.keySet();
            while (!keys.isEmpty()){
                delDelayWatch(keys.iterator().next());
            }
        }
        return this;
    }

    @Override
    public List<HostResourceState> getPartitionDelayState(double delay) {
        if(!partitionState.containsKey(delay)){
            LOGGER.warn("The delay("+delay+") has not been watched.");
            return new ArrayList<>();
        }
        return partitionState.get(delay);
    }
}
