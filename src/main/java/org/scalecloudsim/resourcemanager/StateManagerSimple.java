package org.scalecloudsim.resourcemanager;

import org.scalecloudsim.datacenters.Datacenter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static org.apache.commons.lang3.ArraySorter.sort;
//TODO: 需要支持动态调整，包括两种：1.分区范围不变，调整分区监听的时延。2.撤销原有分区，建立新分区。
//目前的思路是在host中统计每个special time关联的partition的个数。

public class StateManagerSimple implements StateManager{//
    //the logger of the class
    public Logger LOGGER = LoggerFactory.getLogger(StateManager.class.getSimpleName());
    //the id of the state manager
    long id;
    Datacenter datacenter;
    Map<Integer,PartitionStateManager> partitionStateManagerMap;
    List<PartitionRange> ranges;
    public StateManagerSimple(){
        partitionStateManagerMap=new HashMap<>();
    }
    public StateManagerSimple(List<PartitionRange> ranges){
        partitionStateManagerMap=new HashMap<>();
        setPartitionRanges(ranges);
    }


    /**
     * set the partition ranges if the ranges has been set,we will change the ranges
     * @param ranges
     * @return
     */
    @Override
    public StateManager setPartitionRanges(List<PartitionRange> ranges) {
        if(this.ranges!=null&&this.ranges.size()!=0){
            LOGGER.info("the partition ranges has been set,we will change the partition ranges");
            for(PartitionRange range:this.ranges){
                partitionStateManagerMap.get(range.getId()).delAllDelayWatch();
            }
            partitionStateManagerMap.clear();
            this.ranges.clear();
        }
        this.ranges=ranges;
        for(PartitionRange range:ranges){
            setPartitionRange(range);
        }
        return this;
    }

    private void setPartitionRange(PartitionRange partitionRange) {
        PartitionStateManager partitionStateManager=new PartitionStateManagerSimple(this,partitionRange);
        partitionStateManagerMap.put(partitionRange.getId(),partitionStateManager);
    }

    /**
     * add the delay watch to the partition
     * @param rangeId
     * @param delay
     * @return
     */
    @Override
    public StateManager addPartitionWatch(int rangeId, double delay) {
        PartitionStateManager partitionStateManager=partitionStateManagerMap.get(rangeId);
        partitionStateManager.addDelayWatch(delay);
        return this;
    }

    /**
     *  delete the delay watch of the partition
     * @param rangeId
     * @param delay
     * @return
     */
    @Override
    public StateManager delPartitionWatch(int rangeId, double delay) {
        PartitionStateManager partitionStateManager=partitionStateManagerMap.get(rangeId);
        partitionStateManager.delDelayWatch(delay);
        return this;
    }

    /**
     * add the delay watch to all the partition
     * @param delay
     * @return this
     */
    @Override
    public StateManager addAllPartitionWatch(double delay) {
        partitionStateManagerMap.forEach((id,partitionStateManager)->{
            partitionStateManager.addDelayWatch(delay);
        });
        return this;
    }

    /**
     *  get the state of the partition at the special time
     * @param id
     * @return partitionRange
     */
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

    /**
     * get the state of the partition at the special time
     * @param id
     * @param delay
     * @return
     */
    @Override
    public List<HostResourceState> getPartitionDelayState(int id, double delay) {
        List<HostResourceState> partitionStates=partitionStateManagerMap.get(id).getPartitionDelayState(delay);
        return partitionStates;
    }

    //TODO 后期可以考虑不再创建一个allPartitionState来收集，而是直接在原数组上进行排序
    @Override
    public List<HostResourceState> getSamplingState(int simpleSize) {
        List<HostResourceState> allPartitionState=getAllPartitionState();
        Comparator<HostResourceState> hostResourceStateComparable=Comparator.comparing(HostResourceState::getCpu).thenComparing(HostResourceState::getRam).thenComparing(HostResourceState::getBw);
        allPartitionState.sort(hostResourceStateComparable);
        List<HostResourceState> samplingState=new ArrayList<>();
        for(int i=0;i< allPartitionState.size();i+=simpleSize){
            samplingState.add(allPartitionState.get(i));
        }
        return samplingState;

    }

    private List<HostResourceState> getAllPartitionState(){
        List<HostResourceState> allPartitionState=new ArrayList<>();
        partitionStateManagerMap.forEach((id,partitionStateManager)->{
            allPartitionState.addAll(partitionStateManager.getPartitionDelayState(0.0d));
        });
        return allPartitionState;
    }

    /**
     * set the id of the state manager
     * @param id
     * @return
     */
    @Override
    public void setId(long id) {
        this.id=id;
    }
    /**
     * return the datacenter of the state manager
     * @return datacenter
     */
    @Override
    public Datacenter getDatacenter() {
        return datacenter;
    }

    /**
     *  set the datacenter of the state manager
     * @param datacenter
     */
    @Override
    public void setDatacenter(Datacenter datacenter) {
        this.datacenter=datacenter;
    }
    /**
     * return the id of the state manager
     * @return id
     */
    @Override
    public long getId() {
        return id;
    }
}
