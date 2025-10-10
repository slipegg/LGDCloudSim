package org.lgdcloudsim.request;

import lombok.Getter;

@Getter
public class TrainingStrategy {
    int DPDim;
    int PPDim;
    int TPDim;

    /**
     * Construct a TrainingStrategy with the given DPDim and PPDim.
     * DP = 2, PP = 3:
     * [
     *   [0,1,2],
     *   [3,4,5]
     * ]
     * @param DPDim
     * @param PPDim
     */
    public TrainingStrategy(int DPDim, int PPDim) {
        this.DPDim = DPDim;
        this.PPDim = PPDim;
    }

    public int getDPRank(int rank) {
        return rank / DPDim;
    }

    public int getPPRank(int rank) {
        return rank % PPDim;
    }
}
