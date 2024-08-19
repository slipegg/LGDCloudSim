package org.lgdcloudsim.shadowresource.requestmapper;

import lombok.Getter;
import lombok.Setter;
import org.lgdcloudsim.request.Instance;

@Getter
@Setter
public class SRRequest {
    private double predictLife;
    private Instance instance;
    private boolean isSRRequest;

    public SRRequest(Instance instance, double  predictLife) {
        this.predictLife = predictLife;
        this.instance = instance;
        this.isSRRequest = true;
    }
}
