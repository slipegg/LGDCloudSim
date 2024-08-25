package org.lgdcloudsim.shadowresource.partitionmanager;

import java.util.List;

import org.lgdcloudsim.shadowresource.hostsrmapper.HostSR;
import org.lgdcloudsim.shadowresource.requestmapper.SRRequest;
import org.lgdcloudsim.shadowresource.util.SRRequestScheduledRes;

public interface SRPartitionManager {
    public static final int ALREADY_IN_SCHEDULE = -1;
    public static final int NEED_START_SCHEDULE = -2;

    SRPartitionManager addToQueue(List<SRRequest> srRequests);
    
    SRPartitionManager addToQueue(HostSR hostSR);

    SRRequestScheduledRes scheduleForNewSRRequest();

    SRRequestScheduledRes scheduleForNewHostSR();

    boolean isContinueSRRequestSchedule(int partitionId);

    boolean isContinueHostSRSchedule(int partitionId);

    int collectSR(HostSR hostSR);

    void cancelHostSR(int hostId);

    int getPartitionId();

    long getTotalSRRequestedCpu();

    long getTotalHostSRCpu();

    boolean isSRRequestScheduleBusy();

    SRPartitionManager setSRRequestScheduleBusy(boolean isSRRequestScheduleBusy);

    boolean isHostSRScheduleBusy();

    SRPartitionManager setHostSRScheduleBusy(boolean isHostSRScheduleBusy);
}
