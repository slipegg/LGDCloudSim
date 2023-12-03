package org.cpnsim.statemanager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HostCapacityManager {
    List<Integer> sameCapacityHostStartIds;

    List<int[]> hostCapacity;

    int hostNum;

    long[] hostCapacitySum;

    public  HostCapacityManager(){
        sameCapacityHostStartIds = new ArrayList<>();
        hostCapacity = new ArrayList<>();
        hostNum = 0;
        hostCapacitySum = new long[HostState.STATE_NUM];
    }

    public void orderlyAddSameCapacityHost(int length, int[] resourceCapacity) {
        sameCapacityHostStartIds.add(hostNum);
        hostNum += length;
        hostCapacity.add(resourceCapacity);
        for(int i=0;i<HostState.STATE_NUM;i++) {
            hostCapacitySum[i] += (long) resourceCapacity[i] * length;
        }
    }

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

    public long getCpuCapacitySum() {
        return hostCapacitySum[0];
    }

    public long getRamCapacitySum() {
        return hostCapacitySum[1];
    }

    public long getStorageCapacitySum() {
        return hostCapacitySum[2];
    }

    public long getBwCapacitySum() {
        return hostCapacitySum[3];
    }
}
