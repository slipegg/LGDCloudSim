package org.lgdcloudsim.shadowresource.requestmapper;

import lombok.Getter;
import lombok.Setter;
import org.lgdcloudsim.request.Instance;

@Getter
@Setter
public class SRRequest {
    public static int PREEMPTED = -1;
    public static int NORMAL = 0;
    private double predictLife;
    private Instance instance;
    private int state;

    public SRRequest(Instance instance, double  predictLife) {
        this.predictLife = predictLife;
        this.instance = instance;
        this.state = NORMAL;
    }
}
