package org.scalecloudsim.resourcemanager;

import java.util.ArrayList;
import java.util.List;

public class PartitionRangeManager {
    public static List<PartitionRange>  averageCutting(int startIndex,int endIndex,int num){
        List<PartitionRange> ranges=new ArrayList<>();
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
        return ranges;
    }
}
