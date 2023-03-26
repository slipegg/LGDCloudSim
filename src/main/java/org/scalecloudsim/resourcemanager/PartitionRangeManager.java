package org.scalecloudsim.resourcemanager;

import java.util.ArrayList;
import java.util.List;

public class PartitionRangeManager {
    int startIndex;
    int endIndex;
    List<PartitionRange> ranges;

    public PartitionRangeManager(int startIndex, int endIndex) {
        this.startIndex = startIndex;
        this.endIndex = endIndex;
        ranges=new ArrayList<>();
    }


    public PartitionRangeManager averageCutting(int num){
        int id=0;
        int range = endIndex - startIndex + 1;
        int size = range / num;
        int remainder = range % num;
        int index = startIndex;
        for (int i = 0; i < num; i++) {
            int length = size + (remainder-- > 0 ? 1 : 0);
            PartitionRange partitionRange=new PartitionRange(id,index,index+length-1);
            id++;
            index += length;
            ranges.add(partitionRange);
        }
        return this;
    }

    public List<PartitionRange> getRanges() {
        return ranges;
    }

    public void setRanges(List<PartitionRange> ranges) {
        this.ranges = ranges;
    }
}
