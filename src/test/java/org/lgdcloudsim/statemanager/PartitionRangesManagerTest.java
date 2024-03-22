package org.lgdcloudsim.statemanager;

import org.junit.jupiter.api.Test;

import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class PartitionRangesManagerTest {
    @Test
    void testPartitionRangesManager() {
        TreeMap<Integer, int[]> ranges = new TreeMap<>();
        ranges.put(0, new int[]{0, 19});
        ranges.put(1, new int[]{20, 39});
        ranges.put(2, new int[]{40, 59});
        PartitionRangesManager partitionRangesManager = new PartitionRangesManager(ranges);
        int exceptedPartitionId0 = 0;
        int actualPartitionId0 = partitionRangesManager.getPartitionId(5);
        assertEquals(exceptedPartitionId0, actualPartitionId0);
        int exceptedPartitionId1 = 1;
        int actualPartitionId1 = partitionRangesManager.getPartitionId(20);
        assertEquals(exceptedPartitionId1, actualPartitionId1);
        int exceptedPartitionId2 = 2;
        int actualPartitionId2 = partitionRangesManager.getPartitionId(59);
        assertEquals(exceptedPartitionId2, actualPartitionId2);

        int[] exceptedRange = {20, 39};
        int[] actualRange = partitionRangesManager.getRange(1);
        assertArrayEquals(exceptedRange, actualRange);

        int exceptedPartitionNum = 3;
        int actualPartitionNum = partitionRangesManager.getPartitionNum();
        assertEquals(exceptedPartitionNum, actualPartitionNum);

        int exceptedRangeLength = 20;
        int actualRangeLength = partitionRangesManager.getRangeLength(2);
        assertEquals(exceptedRangeLength, actualRangeLength);
    }

    @Test
    void testSetAverageCutting() {
        PartitionRangesManager partitionRangesManager = new PartitionRangesManager();
        partitionRangesManager.setAverageCutting(0, 100, 3);
        int[] exceptedRange0 = {0, 33};
        int[] actualRange0 = partitionRangesManager.getRange(0);
        assertArrayEquals(exceptedRange0, actualRange0);
        int[] exceptedRange1 = {34, 67};
        int[] actualRange1 = partitionRangesManager.getRange(1);
        assertArrayEquals(exceptedRange1, actualRange1);
        int[] exceptedRange2 = {68, 100};
        int[] actualRange2 = partitionRangesManager.getRange(2);
        assertArrayEquals(exceptedRange2, actualRange2);
    }
}
