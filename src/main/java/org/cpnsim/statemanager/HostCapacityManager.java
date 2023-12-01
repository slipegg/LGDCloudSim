package org.cpnsim.statemanager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HostCapacityManager {
    List<Integer> sameCapacityHostStartIds;
    List<int[]> hostCapacity;
    int hostNum;
    public  HostCapacityManager(){
        sameCapacityHostStartIds = new ArrayList<>();
        hostCapacity = new ArrayList<>();
        hostNum = 0;
    }

    public void orderlyAddSameCapacityHost(int length, int[] resourceCapacity) {
        sameCapacityHostStartIds.add(hostNum);
        hostNum += length;
        hostCapacity.add(resourceCapacity);
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
}
