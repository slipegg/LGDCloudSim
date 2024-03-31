package org.lgdcloudsim.statemanager;

import java.util.Random;

/**
 * A class to generate random host state.
 * Note that the range of randomly generated numbers here is: [min,max]
 *
 * @author Anonymous
 * @since LGDCloudSim 1.0
 */

public class RandomHostStateGenerator implements HostStateGenerator {
    /** A random number generator **/
    private Random random;

    /** the minimum amount of cpu that is available on the host */
    private int minCpu = 1;

    /** the maximum amount of cpu that is available on the host */
    private int maxCpu = 124;

    /** the minimum amount of ram that is available on the host */
    private int minRam = 1;

    /** the maximum amount of ram that is available on the host */
    private int maxRam = 1024;

    /** the minimum amount of storage that is available on the host */
    private int minStorage = 1;

    /** the maximum amount of storage that is available on the host */
    private int maxStorage = 1024;

    /** the minimum amount of bw that is available on the host */
    private int minBw = 1;

    /** the maximum amount of bw that is available on the host */
    private int maxBw = 1024;

    /**
     * Initialize a random host state generator.
     * Note that if seed=-1, it means that each generation is random,
     * and the results of each generation are different.
     * Otherwise, the same random number seed is used to generate each time,
     * and the result of each random generation is the same
     *
     * @param seed the seed of the random number generator
     * */
    public RandomHostStateGenerator(int seed) {
        if (seed == -1) {
            random = new Random();
        } else {
            random = new Random(seed);
        }
    }

    /**
     * Initialize a random host state generator.
     *
     * @param seed       the seed of the random number generator
     * @param minCpu     the minimum amount of cpu that is available on the host
     * @param maxCpu     the maximum amount of cpu that is available on the host
     * @param minRam     the minimum amount of ram that is available on the host
     * @param maxRam     the maximum amount of ram that is available on the host
     * @param minStorage the minimum amount of storage that is available on the host
     * @param maxStorage the maximum amount of storage that is available on the host
     * @param minBw      the minimum amount of bw that is available on the host
     * @param maxBw      the maximum amount of bw that is available on the host
     */
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

    /**
     * Generate the random state of a host.
     *
     * @return the state of the host.The state includes 4 integers: cpu, ram, storage and bw.
     * */
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
