package org.lgdcloudsim.shadowresource.util;

import java.util.ArrayList;
import java.util.List;

import org.lgdcloudsim.shadowresource.requestmapper.SRRequest;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SRConflictHandlerResult {
    private List<SRRequest> successSRRequests;
    private List<SRRequest> faileSRRequests;
    
    public SRConflictHandlerResult(){
        successSRRequests = new ArrayList<>();
        faileSRRequests = new ArrayList<>();
    }
}
