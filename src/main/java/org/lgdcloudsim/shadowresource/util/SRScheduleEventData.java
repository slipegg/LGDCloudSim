package org.lgdcloudsim.shadowresource.util;

import java.util.List;
import java.util.Map;

import org.lgdcloudsim.shadowresource.requestmapper.SRRequest;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SRScheduleEventData {
    public static final int SR_REQUEST_SEND = 1;
    public static final int HOST_SR_RECEIVE = 2;

    private int type;
    private Map<Integer, List<SRRequest>> SRRequests;

    public SRScheduleEventData(int type, Map<Integer, List<SRRequest>> SRRequests) {
        this.type = type;
        this.SRRequests = SRRequests;
    }
}
