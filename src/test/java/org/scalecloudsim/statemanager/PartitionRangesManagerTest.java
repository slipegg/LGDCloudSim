package org.scalecloudsim.statemanager;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PartitionRangesManagerTest {
    /**
     * 测试平均分割
     */
    @Test
    public void testAverageCutting(){
        PartitionRangesManager partitionRangesManager = new PartitionRangesManager();
        partitionRangesManager.setAverageCutting(0,5_000_000-1,10);
        Map<Integer, int[]> ranges=partitionRangesManager.getRanges();

        int startAnswer=0;
        int endAnswer=499_999;
        for(Map.Entry<Integer,int[]> entry:ranges.entrySet()){
            assertEquals(startAnswer,entry.getValue()[0]);
            assertEquals(endAnswer,entry.getValue()[1]);
            startAnswer+=500_000;
            endAnswer+=500_000;
        }
    }
    @Test
    public void testGetPartitionId(){
        PartitionRangesManager partitionRangesManager = new PartitionRangesManager();
        partitionRangesManager.setAverageCutting(0,5_000_000-1,10);
        assertEquals(0,partitionRangesManager.getPartitionId(0));
        assertEquals(0,partitionRangesManager.getPartitionId(499_999));
        assertEquals(1,partitionRangesManager.getPartitionId(500_000));
        assertEquals(1,partitionRangesManager.getPartitionId(999_999));
        assertEquals(2,partitionRangesManager.getPartitionId(1_000_000));
        assertEquals(2,partitionRangesManager.getPartitionId(1_499_999));
        assertEquals(3,partitionRangesManager.getPartitionId(1_500_000));
        assertEquals(3,partitionRangesManager.getPartitionId(1_999_999));
        assertEquals(4,partitionRangesManager.getPartitionId(2_000_000));
        assertEquals(4,partitionRangesManager.getPartitionId(2_499_999));
        assertEquals(5,partitionRangesManager.getPartitionId(2_500_000));
        assertEquals(5,partitionRangesManager.getPartitionId(2_999_999));
        assertEquals(6,partitionRangesManager.getPartitionId(3_000_000));
        assertEquals(6,partitionRangesManager.getPartitionId(3_499_999));
        assertEquals(7,partitionRangesManager.getPartitionId(3_500_000));
        assertEquals(7,partitionRangesManager.getPartitionId(3_999_999));
        assertEquals(8,partitionRangesManager.getPartitionId(4_000_000));
        assertEquals(8,partitionRangesManager.getPartitionId(4_499_999));
        assertEquals(9,partitionRangesManager.getPartitionId(4_500_000));
        assertEquals(9,partitionRangesManager.getPartitionId(4_999_999));
    }
}
