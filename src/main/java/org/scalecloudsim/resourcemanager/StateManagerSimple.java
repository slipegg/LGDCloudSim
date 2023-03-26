package org.scalecloudsim.resourcemanager;

import org.scalecloudsim.datacenters.Datacenter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class StateManagerSimple implements StateManager{//
    public Logger LOGGER = LoggerFactory.getLogger(StateManager.class.getSimpleName());
    long id;
    Datacenter datacenter;
    Map<Integer,PartitionStateManager> partitionStateManagerMap;
    List<PartitionRange> ranges;
    public StateManagerSimple(){
        partitionStateManagerMap=new HashMap<>();
    }
    public StateManagerSimple(List<PartitionRange> ranges){
        partitionStateManagerMap=new HashMap<>();
        setPartitionRange(ranges);
    }
    @Override
    public StateManager setPartitionRange(List<PartitionRange> ranges) {
        this.ranges=ranges;
        for(PartitionRange range:ranges){
            PartitionStateManager partitionStateManager=new PartitionStateManagerSimple(this,range);
            partitionStateManagerMap.put(range.getId(),partitionStateManager);
        }
        return this;
    }

    @Override
    public StateManager addPartitionWatch(int rangeId, double delay) {
        PartitionStateManager partitionStateManager=partitionStateManagerMap.get(rangeId);
        partitionStateManager.addDelayWatch(delay);
        return this;
    }

    @Override
    public StateManager addAllPartitionWatch(double delay) {
        partitionStateManagerMap.forEach((id,partitionStateManager)->{
            partitionStateManager.addDelayWatch(delay);
        });
        return this;
    }

    @Override
    public PartitionRange getPartitionRangeById(int id) {
        Optional<PartitionRange> partition= ranges.stream().filter(PartitionRange->PartitionRange.getId()==id).findFirst();
        if(partition.isPresent()){
            return partition.get();
        }
        else{
            LOGGER.info("There is not partitionRange id="+id);
            return null;
        }

    }

    @Override
    public PartitionStateManager getPartitionStateManager(int id) {
        return partitionStateManagerMap.get(id);
    }

    @Override
    public void setId(long id) {
        this.id=id;
    }

    @Override
    public Datacenter getDatacenter() {
        return datacenter;
    }

    @Override
    public void setDatacenter(Datacenter datacenter) {
        this.datacenter=datacenter;
    }

    @Override
    public long getId() {
        return id;
    }
}
