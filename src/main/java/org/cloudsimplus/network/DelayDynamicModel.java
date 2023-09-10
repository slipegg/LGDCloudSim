package org.cloudsimplus.network;

import org.cloudsimplus.core.SimEntity;

public interface DelayDynamicModel {
    /**
     * Get the random dynamic delay from src to dst
     * @param src       the source entity
     * @param dst       the destination entity
     * @param delay     the basic delay time
     * @param time      the random seed
     * @return          the random dynamic delay from src to dst
     */
    double getDynamicDelay(SimEntity src, SimEntity dst, double delay, double time);
}
