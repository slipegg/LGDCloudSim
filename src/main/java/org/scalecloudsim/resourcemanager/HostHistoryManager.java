package org.scalecloudsim.resourcemanager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface HostHistoryManager{
    Logger LOGGER = LoggerFactory.getLogger(HostHistoryManager.class.getSimpleName());
    HostHistoryManager setHistoryRange(double range);
    double getHistoryRange();
    HostResourceState addDelayWatch(double delayTime);
    HostHistoryManager delDelayWatch(double delayTime);
    HostHistoryManager updateHistory(double clock);
    HostHistoryManager addHistory(HostResourceStateHistory hostResourceStateHistory);
    HostResourceState getSpecialTimeHostState(double time);
}
