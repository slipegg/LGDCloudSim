package org.lgdcloudsim.statemanager;

import lombok.Getter;
import org.lgdcloudsim.intrascheduler.IntraScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.TreeMap;
import java.util.Map;

/**
 * A class to manage the partition ranges in the datacenter.
 * Note that the partition here refers to the logical partition,
 * mainly for the convenience of the {@link IntraScheduler}
 * to synchronize the state of the hosts in the region on a regional basis
 *
 * @author Anonymous
 * @since LGDCloudSim 1.0
 */
public class PartitionRangesManager {
    /** The ranges of the partitions in the datacenter
     *  The key is the partition id, and the value is the range of the partition
     *  The range is represented by an array of two integers, the first integer is the start of the range,
     *  and the second integer is the end of the range
     *  For example, if the partition id is 0, and the range is [0, 2], then the partition 0 includes the hosts with id 0, 1, 2
     *  */
    @Getter
    TreeMap<Integer, int[]> ranges;

    /** An array to store the start of the ranges of the partitions in the datacenter.
     *  for example, if the ranges are [0, 2], [3, 5], [6, 8], then the array is [0, 3, 6]
     *  The aim of this array is to find the partition id of a host id quickly
     **/
    int[] rangePart;

    /**
     * An array to store the partition id of the ranges in the datacenter.
     * for example, if the ranges are [0, 2], [3, 5], [6, 8], then the array is [0, 1, 2]
     * It corresponds to ranges.
     * The aim of this array is to find the partition id of a host id quickly
     **/
    int[] rangeId;

    /**
     * The number of hosts in the datacenter
     */
    int hostNum = 0;

    /**
     * The Logger of the class
     */
    public Logger LOGGER = LoggerFactory.getLogger(PartitionRangesManager.class.getSimpleName());

    /**
     * Create a new empty PartitionRangesManager.
     */
    public PartitionRangesManager() {
        ranges = new TreeMap<>();
    }

    /**
     * Create a new PartitionRangesManager with the given ranges.
     *
     * @param ranges the ranges of the partitions in the datacenter, the key is the partition id, and the value is the range of the partition.
     */
    public PartitionRangesManager(Map<Integer, int[]> ranges) {
        this.ranges = new TreeMap<>(ranges);
        rangePart = new int[ranges.size()];
        rangeId = new int[ranges.size()];
        int i = 0;
        for (Map.Entry<Integer, int[]> entry : ranges.entrySet()) {
            int[] range = entry.getValue();
            rangePart[i] = range[0];
            rangeId[i] = entry.getKey();
            i += 1;
            hostNum += range[1] - range[0] + 1;
        }
    }

    /**
     * Get the partition id of the host with the given host id.
     *
     * @param hostId the id of the host
     * @return the partition id of the host
     */
    public Integer getPartitionId(int hostId) {
        if(hostId<rangePart[0] || hostId >= hostNum){
            LOGGER.error("Host id {} is not in the range of the datacenter,rangePart[0] = {} hostNum = {}", hostId, rangePart[0], hostNum);
            return -1;
        }
        int index = Arrays.binarySearch(rangePart, hostId);
        if(index>=0){
            return rangeId[index];
        }else{
            index = -index-2;
            return rangeId[index];
        }
    }

    /**
     * Divide the data center into num regions evenly from startId to endIndex
     *
     * @param startIndex the start id of the datacenter
     * @param endIndex the end id of the datacenter
     * @param num the number of the partition
     * @return the range of the partition
     */
    public PartitionRangesManager setAverageCutting(int startIndex, int endIndex, int num) {
        ranges.clear();
        int nextPartitionId = 0;
        int range = endIndex - startIndex + 1;
        int size = range / num;
        int remainder = range % num;
        int index = startIndex;
        rangePart = new int[num];
        rangeId = new int[num];
        hostNum = endIndex - startIndex + 1;
        for (int i = 0; i < num; i++) {
            int length = size + (remainder-- > 0 ? 1 : 0);
            int[] a = new int[2];
            a[0] = index;
            a[1] = index + length - 1;
            ranges.put(nextPartitionId, a);
            rangePart[i] = index;
            rangeId[i] = nextPartitionId;
            index += length;
            nextPartitionId++;
        }
        return this;
    }

    /**
     * Get the range of the partition with the given partition id.
     *
     * @param partitionId the id of the partition
     * @return the range of the partition
     */
    public int[] getRange(int partitionId) {
        return ranges.get(partitionId);
    }

    /**
     * Get the number of the partitions in the datacenter.
     *
     * @return the number of the partitions in the datacenter
     */
    public int getPartitionNum() {
        return rangeId.length;
    }

    /**
     * Get the partition ids of the partitions in the datacenter.
     *
     * @return the partition ids of the partitions in the datacenter
     */
    public int[] getPartitionIds() {
        return rangeId;
    }

    /**
     * Get the length of the range of the partition with the given partition id.
     *
     * @param partitionId the id of the partition
     * @return the length of the range of the partition
     */
    public int getRangeLength(int partitionId) {
        return ranges.get(partitionId)[1] - ranges.get(partitionId)[0] + 1;
    }
}
