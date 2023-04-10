package org.scalecloudsim.statemanager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class PartitionRangesManager {
    Map<Integer, int[]> ranges;
    public Logger LOGGER = LoggerFactory.getLogger(PartitionRangesManager.class.getSimpleName());

    public PartitionRangesManager(Map<Integer, int[]> ranges) {
        this.ranges = ranges;
    }

    public PartitionRangesManager() {
        ranges = new HashMap<>();
    }

    public Integer getPartitionId(int hostId) {
        for (Map.Entry<Integer, int[]> entry : ranges.entrySet()) {
            int[] range = entry.getValue();
            if (hostId >= range[0] && hostId <= range[1]) {
                return entry.getKey();
            }
        }
        LOGGER.error("Host id not found in ranges");
        return -1;
    }

    public PartitionRangesManager setAverageCutting(int startIndex, int endIndex, int num) {
        ranges.clear();
        int nextPartitionId = 0;
        int range = endIndex - startIndex + 1;
        int size = range / num;
        int remainder = range % num;
        int index = startIndex;
        for (int i = 0; i < num; i++) {
            int length = size + (remainder-- > 0 ? 1 : 0);
            int[] a = new int[2];
            a[0] = index;
            a[1] = index + length - 1;
            ranges.put(nextPartitionId, a);
            index += length;
            nextPartitionId++;
        }
        return this;
    }

    public Map<Integer, int[]> getRanges() {
        return ranges;
    }

    public void setRanges(Map<Integer, int[]> ranges) {
        this.ranges = ranges;
    }

    public int[] getRange(int partitionId) {
        return ranges.get(partitionId);
    }

    public int getPartitionNum() {
        return ranges.size();
    }
}
