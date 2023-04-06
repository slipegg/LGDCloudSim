package org.scalecloudsim.statemanager;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SimpleStateSimpleTest {
    @Test
    public void aSimpleTest() {
        SimpleState simpleState = new SimpleStateSimple();
        simpleState.updateStorageSum(1024);
        simpleState.updateBwSum(512);
        simpleState.addCpuRamRecord(32, 64);
        simpleState.updateCpuRamMap(32, 64, 16, 32);
        simpleState.addCpuRamRecord(8, 17);

        int expectStorageSum = 1024;
        assertEquals(expectStorageSum, simpleState.getStorageSum());
        int expectBwSum = 512;
        assertEquals(expectBwSum, simpleState.getBwSum());
        int expectCpuRamNum = 1;
        assertEquals(expectCpuRamNum, simpleState.getCpuRamSum(16, 32));

        int expectCpuRamNum2 = 0;
        assertEquals(expectCpuRamNum2, simpleState.getCpuRamSum(32, 64));

        int expectCpuRamNum3 = 2;
        assertEquals(expectCpuRamNum3, simpleState.getCpuRamSum(4, 9));

    }
}

/*
n=33*56=1848种状态
初始化的时候时间复杂度为O(n(n+1)/2),空间复杂度为O(n(n+1)/2)
每次更新状态的时候时间复杂度为O(1),空间复杂度为O(1)
得到当前状态的时候时间复杂度为O(n),空间复杂度为O(1)
Init SimpleState Time cost:151.0ms
totalMemory: 132 Mb
freeMemory: 111 Mb
usedMemory: 20 Mb

State change 100000 times cost:146.0ms
totalMemory: 132 Mb
freeMemory: 106 Mb
usedMemory: 25 Mb
1848*1848=3403776=3.25M
3.25M/2=1.625M
1.625M*8B=13M
但是需要乘10，因为有10个数据中心，所以总空间消耗为25*10=250Mb
 */