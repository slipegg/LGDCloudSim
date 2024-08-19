package org.lgdcloudsim.shadowresource.util;

import java.util.ArrayList;
import java.util.List;

import org.lgdcloudsim.shadowresource.requestmapper.SRRequest;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SRScheduleRes {
    List<SRRequest> successSRRequests;
    List<SRRequest> noScheduledSRRequests;
    double costTime;

    public SRScheduleRes(){
        successSRRequests = new ArrayList<>();
        noScheduledSRRequests = new ArrayList<>();
    }

    public SRScheduleRes(List<SRRequest> successSrRequests, List<SRRequest> noScheduleSRRequests) {
        this.successSRRequests = successSrRequests;
        this.noScheduledSRRequests = noScheduleSRRequests;
    }

    public SRScheduleRes addSuccess(SRRequest srRequest) {
        successSRRequests.add(srRequest);
        return this;
    }

    public SRScheduleRes addNoScheduled(SRRequest srRequest) {
        noScheduledSRRequests.add(srRequest);
        return this;
    }

    public SRScheduleRes add(SRScheduleRes srScheduleRes) {
        successSRRequests.addAll(srScheduleRes.getSuccessSRRequests());
        noScheduledSRRequests.addAll(srScheduleRes.getNoScheduledSRRequests());
        return this;
    }
}
