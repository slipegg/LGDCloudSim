package org.scalecloudsim.resourcemanager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface HostHistoryManager{
    Logger LOGGER = LoggerFactory.getLogger(HostHistoryManager.class.getSimpleName());

    HostHistoryManager setHistoryRange(double range);

    double getHistoryRange();

    HostHistoryManager addSpecialTimeHistoryWatch(double time);

    HostHistoryManager addHistory(HostResourceStateHistory hostResourceStateHistory);


//    HostResourceState getHostResourceState(double time,Host host);
//
//    List<HostResourceState> getRangeHostResourceState(double time,long startIndex,long endIndex);//左闭右闭


}
