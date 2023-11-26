package org.cpnsim.network;

public interface DelayDynamicModel {
    double getDynamicDelay(int srcId, int dstId, double delay, double time);
}
