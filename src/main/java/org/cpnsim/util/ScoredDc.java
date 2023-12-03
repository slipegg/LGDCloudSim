package org.cpnsim.util;

import lombok.Getter;
import lombok.Setter;
import org.cpnsim.datacenter.Datacenter;

@Getter
@Setter
public class ScoredDc {
    Datacenter datacenter;
    double score;

    public ScoredDc(Datacenter datacenter, double score){
        this.datacenter = datacenter;
        this.score = score;
    }
}
