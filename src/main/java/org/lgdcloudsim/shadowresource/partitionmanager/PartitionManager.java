package org.lgdcloudsim.shadowresource.partitionmanager;

import java.util.List;

import org.lgdcloudsim.shadowresource.hostsrmapper.HostSR;
import org.lgdcloudsim.shadowresource.requestmapper.SRRequest;
import org.lgdcloudsim.shadowresource.util.SRRequestScheduledRes;

public interface PartitionManager {
    public static final int LAST_NO_SCHEDULE = -1;
    
    PartitionManager addToQueue(List<SRRequest> srRequests);
    
    PartitionManager addToQueue(HostSR hostSR);

    SRRequestScheduledRes scheduleForNewSRRequest();

    SRRequestScheduledRes scheduleForNewHostSR();

    boolean isContinueSRRequestSchedule(int partitionId);

    boolean isContinueHostSRSchedule(int partitionId);

    int collectSR(HostSR hostSR);

    long getTotalSRRequestedCpu();

    long getTotalHostSRCpu();

    boolean isSRRequestScheduleBusy();

    PartitionManager setSRRequestScheduleBusy(boolean isSRRequestScheduleBusy);

    boolean isHostSRScheduleBusy();

    PartitionManager setHostSRScheduleBusy(boolean isHostSRScheduleBusy);
}
