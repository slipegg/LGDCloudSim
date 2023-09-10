package org.cloudsimplus.network;

import org.cloudsimplus.core.SimEntity;

import java.util.Random;

public class RandomDelayDynamicModel implements DelayDynamicModel {
    /**
     * Get the random dynamic delay from src to dst
     * @param src       the source entity
     * @param dst       the destination entity
     * @param delay     the basic delay time
     * @param time      the random seed
     * @return          the random dynamic delay from src to dst
     */
    @Override
    public double getDynamicDelay(SimEntity src, SimEntity dst, double delay, double time) {
        Random random = new Random((int) time);
        return delay + random.nextDouble(4) + 1;
    }
}
