package org.cpnsim.statemanager;

import java.util.Random;

public class RandomHostStateGenerator implements HostStateGenerator {
    int seed = 0;
    int maxCpu = 124;
    int maxRam = 1024;
    int maxStorage = 1024;
    int maxBw = 1024;


    private int generateRandomCpuState() {
        Random random;
        if (seed == -1) {
            random = new Random();
        } else {
            random = new Random(seed);
        }
        return random.nextInt(maxCpu) + 1;
    }

    private int generateRandomRamState() {
        Random random;
        if (seed == -1) {
            random = new Random();
        } else {
            random = new Random(seed);
        }
        return random.nextInt(maxRam) + 1;
    }

    private int generateRandomStorageState() {
        Random random;
        if (seed == -1) {
            random = new Random();
        } else {
            random = new Random(seed);
        }
        return random.nextInt(maxStorage) + 1;
    }

    private int generateRandomBwState() {
        Random random;
        if (seed == -1) {
            random = new Random();
        } else {
            random = new Random(seed);
        }
        return random.nextInt(maxBw) + 1;
    }

    @Override
    public int[] generateHostState() {
        int[] hostStates = new int[4];
        hostStates[0] = generateRandomCpuState();
        hostStates[1] = generateRandomRamState();
        hostStates[2] = generateRandomStorageState();
        hostStates[3] = generateRandomBwState();
        return hostStates;
    }
}
