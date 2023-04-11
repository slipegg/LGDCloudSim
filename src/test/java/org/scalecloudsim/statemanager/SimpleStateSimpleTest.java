package org.scalecloudsim.statemanager;

import org.cloudsimplus.core.CloudSim;
import org.cloudsimplus.core.Simulation;
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

        int expectCpuSum = 24;
        assertEquals(expectCpuSum, simpleState.getCpuAvaiableSum());
        int expectRamSum = 49;
        assertEquals(expectRamSum, simpleState.getRamAvaiableSum());
        int expectStorageSum = 1024;
        assertEquals(expectStorageSum, simpleState.getStorageAvaiableSum());
        int expectBwSum = 512;
        assertEquals(expectBwSum, simpleState.getBwAvaiableSum());
        int expectCpuRamNum = 1;
        assertEquals(expectCpuRamNum, simpleState.getCpuRamSum(16, 32));

        int expectCpuRamNum2 = 0;
        assertEquals(expectCpuRamNum2, simpleState.getCpuRamSum(32, 64));

        int expectCpuRamNum3 = 2;
        assertEquals(expectCpuRamNum3, simpleState.getCpuRamSum(4, 9));

    }

    @Test
    public void realTest() {
        Simulation sacleCloudSim = new CloudSim();
        int hostNum = 5_000_000;
        StateManager stateManager = new StateManagerSimple(hostNum, sacleCloudSim);
        HostStateGenerator isomorphicHostStateGenerator = new IsomorphicHostStateGenerator(128, 512, 10240, 1024);
        stateManager.initHostStates(isomorphicHostStateGenerator);
        long storageSum = stateManager.getSimpleState().getStorageAvaiableSum();
        long bwSum = stateManager.getSimpleState().getBwAvaiableSum();
        long expectStorageSum = 10240L * hostNum;
        assertEquals(expectStorageSum, storageSum);
        long expectBwSum = 1024L * hostNum;
        assertEquals(expectBwSum, bwSum);

        int cpuRamNum1 = stateManager.getSimpleState().getCpuRamSum(128, 512);
        int cpuRamNum2 = stateManager.getSimpleState().getCpuRamSum(1, 8);
        int cpuRamNum3 = stateManager.getSimpleState().getCpuRamSum(23, 600);
        int cpuRamNum4 = stateManager.getSimpleState().getCpuRamSum(200, 100);
        assertEquals(hostNum, cpuRamNum1);
        assertEquals(hostNum, cpuRamNum2);
        assertEquals(0, cpuRamNum3);
        assertEquals(0, cpuRamNum4);
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