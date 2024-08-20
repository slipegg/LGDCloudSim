package org.lgdcloudsim.shadowresource.partitionmanager;

import java.util.List;

import org.lgdcloudsim.shadowresource.hostsrmapper.HostSR;
import org.lgdcloudsim.shadowresource.requestmapper.SRRequest;
import org.lgdcloudsim.shadowresource.util.SRRequestScheduledRes;

public interface SRPartitionManager {
    public static final int LAST_NO_SCHEDULE = -1;

    SRPartitionManager addToQueue(List<SRRequest> srRequests);
    
    SRPartitionManager addToQueue(HostSR hostSR);

    SRRequestScheduledRes scheduleForNewSRRequest();

    SRRequestScheduledRes scheduleForNewHostSR();

    boolean isContinueSRRequestSchedule(int partitionId);

    boolean isContinueHostSRSchedule(int partitionId);

    int collectSR(HostSR hostSR);

    long getTotalSRRequestedCpu();

    long getTotalHostSRCpu();

    boolean isSRRequestScheduleBusy();

    SRPartitionManager setSRRequestScheduleBusy(boolean isSRRequestScheduleBusy);

    boolean isHostSRScheduleBusy();

    SRPartitionManager setHostSRScheduleBusy(boolean isHostSRScheduleBusy);
}
