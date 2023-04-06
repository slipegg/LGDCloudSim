package org.cloudsimplus.network;

import org.cloudsimplus.core.SimEntity;

public interface DelayDynamicModel {
    double getDynamicDelay(SimEntity src, SimEntity dst, double delay, double time);
}
