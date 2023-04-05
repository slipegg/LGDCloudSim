package org.scalecloudsim.Instances;

import org.cloudsimplus.hosts.HostSuitability;

public class InstanceFailInfo {
    int failedTime;
    int failedHost;
    HostSuitability faildReason;

    public InstanceFailInfo(int failedTime, int failedHost) {
        this.failedTime = failedTime;
        this.failedHost = failedHost;
    }

    public InstanceFailInfo(int failedTime, int failedHost, HostSuitability faildReason) {
        this.failedTime = failedTime;
        this.failedHost = failedHost;
        this.faildReason = faildReason;
    }

    public int getFailedTime() {
        return failedTime;
    }

    public void setFailedTime(int failedTime) {
        this.failedTime = failedTime;
    }

    public int getFailedHost() {
        return failedHost;
    }

    public void setFailedHost(int failedHost) {
        this.failedHost = failedHost;
    }

    public HostSuitability getFaildReason() {
        return faildReason;
    }

    public void setFaildReason(HostSuitability faildReason) {
        this.faildReason = faildReason;
    }
}
