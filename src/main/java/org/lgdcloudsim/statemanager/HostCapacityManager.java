package org.lgdcloudsim.statemanager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A class to manage the capacity of the hosts.
 * Considering that the status of hosts in a data center usually has certain similarities,
 * HostCapacityManager will record the CPU, memory, storage and bandwidth capacity of a certain type of host,
 * and then record the ID range of this type of host.
 *
 * @author Anonymous
 * @since LGDCloudSim 1.0
 */
public class HostCapacityManager {
    /**
     * The start id of the hosts with the same capacity.
     */
    List<Integer> sameCapacityHostStartIds;

    /**
     * A list to store the capacity of the different types of hosts.
     * It corresponds to sameCapacityHostStartIds.
     */
    List<int[]> hostCapacity;

    /**
     * The number of hosts.
     */
    int hostNum;

    /**
     * The sum of the capacity of all hosts.
     * The first element is the sum of the CPU capacity,
     * the second element is the sum of the ram capacity,
     * the third element is the sum of the storage capacity,
     * the fourth element is the sum of the bandwidth capacity.
     */
    long[] hostCapacitySum;

    /**
     * Create a new HostCapacityManager.
     */
    public  HostCapacityManager(){
        sameCapacityHostStartIds = new ArrayList<>();
        hostCapacity = new ArrayList<>();
        hostNum = 0;
        hostCapacitySum = new long[HostState.STATE_NUM];
    }

    /**
     * Add some host with the same capacity.
     * Now it is only supported to add the same capacity host orderly.
     *
     * @param length           the number of hosts to be added
     * @param resourceCapacity the capacity of the hosts to be added
     */
    public void orderlyAddSameCapacityHost(int length, int[] resourceCapacity) {
        sameCapacityHostStartIds.add(hostNum);
        hostNum += length;
        hostCapacity.add(resourceCapacity);
        for(int i=0;i<HostState.STATE_NUM;i++) {
            hostCapacitySum[i] += (long) resourceCapacity[i] * length;
        }
    }

    /**
     * Get the capacity of the host with the given host id through binary search.
     * @param hostId the id of the host
     * @return the capacity of the host, including the CPU, memory, storage and bandwidth capacity
     */
    public int[] getHostCapacity(int hostId) {
        if(hostId<0|| hostId>=hostNum) {
            throw new IllegalArgumentException("hostId "+hostId+"is out of range [0,"+hostNum+"] in getHostCapacity");
        }
        int index = Arrays.binarySearch(sameCapacityHostStartIds.toArray(), hostId);
        if(index>=0) {
            return hostCapacity.get(index);
        } else {
            return hostCapacity.get(-index-2);
        }
    }

    /**
     * Get the CPU capacity of all hosts.
     * @return the sum of the CPU capacity of all hosts
     */
    public long getCpuCapacitySum() {
        return hostCapacitySum[0];
    }

    /**
     * Get the RAM capacity of all hosts.
     * @return the sum of the RAM capacity of all hosts
     */
    public long getRamCapacitySum() {
        return hostCapacitySum[1];
    }

    /**
     * Get the storage capacity of all hosts.
     * @return the sum of the storage capacity of all hosts
     */
    public long getStorageCapacitySum() {
        return hostCapacitySum[2];
    }

    /**
     * Get the bandwidth capacity of all hosts.
     * @return the sum of the bandwidth capacity of all hosts
     */
    public long getBwCapacitySum() {
        return hostCapacitySum[3];
    }
}
