package org.cpnsim.util;

import lombok.Getter;
import lombok.Setter;
import org.cpnsim.datacenter.Datacenter;

@Getter
@Setter
public class ScoredHost {
    Datacenter datacenter;
    int hostId;
    double score;

    public ScoredHost(Datacenter datacenter, int hostId, double score){
        this.datacenter = datacenter;
        this.hostId = hostId;
        this.score = score;
    }
}