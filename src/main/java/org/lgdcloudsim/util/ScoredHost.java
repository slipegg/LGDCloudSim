package org.lgdcloudsim.util;

import lombok.Getter;
import lombok.Setter;
import org.lgdcloudsim.datacenter.Datacenter;

/**
 * A class that represents the scored host.
 * It contains the data center, the host id, and the score of the host.
 * The score is used to filter and sort the hosts.
 *
 * @author Anonymous
 * @since LGDCloudSim 1.0
 */
@Getter
@Setter
public class ScoredHost {
    /**
     * The data center.
     */
    Datacenter datacenter;

    /**
     * The host id.
     */
    int hostId;

    /**
     * The score of the host.
     */
    double score;

    /**
     * Construct the scored host with the data center, the host id, and the score.
     *
     * @param datacenter the data center.
     * @param hostId     the host id.
     * @param score      the score of the host.
     */
    public ScoredHost(Datacenter datacenter, int hostId, double score){
        this.datacenter = datacenter;
        this.hostId = hostId;
        this.score = score;
    }
}