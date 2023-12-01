package org.cpnsim.statemanager;

import org.junit.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

public class TestHostCapacityManager {
    @Test
    public void testHostCapacityManager(){
        HostCapacityManager hostCapacityManager = new HostCapacityManager();
        hostCapacityManager.orderlyAddSameCapacityHost(100, new int[]{100,100,100,100});
        hostCapacityManager.orderlyAddSameCapacityHost(100, new int[]{200,200,200,200});
        hostCapacityManager.orderlyAddSameCapacityHost(100, new int[]{300,300,300,300});
        hostCapacityManager.orderlyAddSameCapacityHost(50, new int[]{400,400,400,400});

        int[] exceptedCapacity = new int[]{100,100,100,100};
        int[] actualCapacity = hostCapacityManager.getHostCapacity(0);
        assertArrayEquals(exceptedCapacity, actualCapacity);
        actualCapacity = hostCapacityManager.getHostCapacity(99);
        assertArrayEquals(exceptedCapacity, actualCapacity);

        exceptedCapacity = new int[]{200,200,200,200};
        actualCapacity = hostCapacityManager.getHostCapacity(100);
        assertArrayEquals(exceptedCapacity, actualCapacity);
        actualCapacity = hostCapacityManager.getHostCapacity(199);
        assertArrayEquals(exceptedCapacity, actualCapacity);

        exceptedCapacity = new int[]{300,300,300,300};
        actualCapacity = hostCapacityManager.getHostCapacity(200);
        assertArrayEquals(exceptedCapacity, actualCapacity);
        actualCapacity = hostCapacityManager.getHostCapacity(299);
        assertArrayEquals(exceptedCapacity, actualCapacity);

        exceptedCapacity = new int[]{400,400,400,400};
        actualCapacity = hostCapacityManager.getHostCapacity(300);
        assertArrayEquals(exceptedCapacity, actualCapacity);
        actualCapacity = hostCapacityManager.getHostCapacity(349);
        assertArrayEquals(exceptedCapacity, actualCapacity);


    }
}
