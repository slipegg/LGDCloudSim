package org.cpnsim.statemanager;

import java.util.Random;

public class RandomHostStateGenerator implements HostStateGenerator {
    private Random random;
    //全部是左闭右闭区间
    private int minCpu = 1;
    private int maxCpu = 124;
    private int minRam = 1;
    private int maxRam = 1024;
    private int minStorage = 1;
    private int maxStorage = 1024;
    private int minBw = 1;
    private int maxBw = 1024;

    public RandomHostStateGenerator(int seed) {//-1代表不设置种子，每次都不一样
        if (seed == -1) {
            random = new Random();
        } else {
            random = new Random(seed);
        }
    }

    public RandomHostStateGenerator(int seed, int minCpu, int maxCpu, int minRam, int maxRam, int minStorage, int maxStorage, int minBw, int maxBw) {
        this(seed);
        this.minCpu = minCpu;
        this.maxCpu = maxCpu;
        this.minRam = minRam;
        this.maxRam = maxRam;
        this.minStorage = minStorage;
        this.maxStorage = maxStorage;
        this.minBw = minBw;
        this.maxBw = maxBw;
    }

    @Override
    public int[] generateHostState() {
        int[] hostStates = new int[4];
        hostStates[0] = random.nextInt(maxCpu - minCpu + 1) + minCpu;
        hostStates[1] = random.nextInt(maxRam - minRam + 1) + minRam;
        hostStates[2] = random.nextInt(maxStorage - minStorage + 1) + minStorage;
        hostStates[3] = random.nextInt(maxBw - minBw + 1) + minBw;
        return hostStates;
    }
}
