package org.lgdcloudsim.util;

import lombok.Getter;
import lombok.Setter;
import org.lgdcloudsim.datacenter.Datacenter;

/**
 * A class that represents the scored data center.
 * It contains the data center and the score of the data center.
 * The score is used to filter and sort the data centers.
 *
 * @author Anonymous
 * @since LGDCloudSim 1.0
 */
@Getter
@Setter
public class ScoredDc {
    /**
     * The data center.
     */
    Datacenter datacenter;

    /**
     * The score of the data center.
     */
    double score;

    /**
     * Construct the scored data center with the data center and the score.
     *
     * @param datacenter the data center.
     * @param score      the score of the data center.
     */
    public ScoredDc(Datacenter datacenter, double score){
        this.datacenter = datacenter;
        this.score = score;
    }
}
