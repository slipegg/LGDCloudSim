package org.scalecloudsim.resourcemanager;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PartitionRangeManagerTest {
    @Test
    public void testAverageCutting(){
        int hostNum=100000;
        PartitionRangeManager partitionRangeManager=new PartitionRangeManager(0,hostNum-1);
        List<PartitionRange> actual=partitionRangeManager.averageCutting(5).getRanges();

        List<PartitionRange> expect=new ArrayList<>();
        expect.add(new PartitionRange(0,0,19999));
        expect.add(new PartitionRange(1,20000,39999));
        expect.add(new PartitionRange(2,40000,59999));
        expect.add(new PartitionRange(3,60000,79999));
        expect.add(new PartitionRange(4,80000,hostNum-1));

        assertEquals(expect,actual);
    }
}
