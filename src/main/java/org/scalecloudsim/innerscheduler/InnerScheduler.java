package org.scalecloudsim.innerscheduler;
import java.util.Map;
public class InnerScheduler {
     Map<Integer,Double> partitionDelay;
     int id;
     public int getId(){
         return id;
     }
    public void setPartitionDelay(Map<Integer,Double> partitionDelay) {
        this.partitionDelay = partitionDelay;
    }

    public Map<Integer,Double> getPartitionDelay() {
        return partitionDelay;
    }

    public InnerScheduler(Map<Integer,Double> partitionDelay) {
        this.partitionDelay = partitionDelay;
    }
    public InnerScheduler(int id,Map<Integer,Double> partitionDelay) {
        this.id = id;
        this.partitionDelay = partitionDelay;
    }
}
