package org.cpnsim.statemanager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class PartitionRangesManager {
    Map<Integer, int[]> ranges;
    int[] rangePart;
    int[] rangeId;
    public Logger LOGGER = LoggerFactory.getLogger(PartitionRangesManager.class.getSimpleName());

    public PartitionRangesManager(Map<Integer, int[]> ranges) {
        this.ranges = ranges;
        rangePart = new int[ranges.size() * 2];
        rangeId = new int[ranges.size()];
        int i = 0;
        for (Map.Entry<Integer, int[]> entry : ranges.entrySet()) {
            int[] range = entry.getValue();
            rangePart[i] = range[0];
            rangePart[i + 1] = range[1];
            rangeId[i / 2] = entry.getKey();
            i += 2;
        }
    }

    public Integer getPartitionId(int hostId) {
        int index = 0;
        while (index < rangePart.length) {
            if (hostId >= rangePart[index] && hostId <= rangePart[index + 1]) {
                return rangeId[index / 2];
            }
            index += 2;
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
        rangePart = new int[num * 2];
        rangeId = new int[num];
        for (int i = 0; i < num; i++) {
            int length = size + (remainder-- > 0 ? 1 : 0);
            int[] a = new int[2];
            a[0] = index;
            a[1] = index + length - 1;
            ranges.put(nextPartitionId, a);
            rangePart[i * 2] = index;
            rangePart[i * 2 + 1] = index + length - 1;
            rangeId[i] = nextPartitionId;
            index += length;
            nextPartitionId++;
        }
        return this;
    }

    public Map<Integer, int[]> getRanges() {
        return ranges;
    }

    public int[] getRange(int partitionId) {
        return ranges.get(partitionId);
    }

    public int getPartitionNum() {
        return rangePart.length / 2;
    }

    public int[] getPartitionIds() {
        return rangeId;
    }

    public int getRangeLength(int partitionId) {
        return ranges.get(partitionId)[1] - ranges.get(partitionId)[0] + 1;
    }
}
