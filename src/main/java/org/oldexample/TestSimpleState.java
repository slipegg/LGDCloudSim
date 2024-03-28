package org.oldexample;

import org.lgdcloudsim.record.MemoryRecord;

import java.util.Random;

public class TestSimpleState {
    public static void main(String[] args) {
        double startTime = System.currentTimeMillis();
//        SimpleState simpleState = new SimpleStateSimple();
        Simple1 simpleState = new Simple1();
//        Simple2 simpleState = new Simple2();
        MemoryRecord.recordMemory();
        Random random = new Random(1);
        int maxCpu = 130;
        int maxRam = 260;
        int hostNums = 10000000;
        for (int i = 0; i < hostNums; i++) {
            simpleState.addCpuRamRecord(random.nextInt(maxCpu), random.nextInt(maxRam));
        }
        MemoryRecord.recordMemory();
        int changeTimes = 1000000;
        for (int i = 0; i < changeTimes; i++) {
            simpleState.updateCpuRamMap(random.nextInt(maxCpu), random.nextInt(maxRam), random.nextInt(maxCpu), random.nextInt(maxRam));
        }
        MemoryRecord.recordMemory();
        int getTimes = 1000000;
        for (int i = 0; i < getTimes; i++) {
            simpleState.getCpuRamSum(random.nextInt(maxCpu), random.nextInt(maxRam));
        }
        MemoryRecord.recordMemory();

        double endTime = System.currentTimeMillis();
//        Runtime runtime = Runtime.getRuntime();
//        long maxUsedMemory = runtime.totalMemory()-runtime.freeMemory();
        System.out.println("运行过程占用最大内存: " + MemoryRecord.getMaxUsedMemory() / 1024 / 1024 + " Mb");
        System.out.println("程序运行时间： " + (endTime - startTime) / 1000 + "s");
        System.out.println(simpleState);
    }
}
