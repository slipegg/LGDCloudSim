package org.lgdcloudsim.request;

import lombok.Getter;

@Getter
public class TrainingStrategy {
    int DPDim;
    int PPDim;
    int TPDim;

    int rank0Index;

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
    public TrainingStrategy(int DPDim, int PPDim, int rank0Index) {
        this.DPDim = DPDim;
        this.PPDim = PPDim;
        this.TPDim = 1;
        this.rank0Index = rank0Index;
    }

    public int getDPRank(int instanceId) {
        return (instanceId - rank0Index) / DPDim;
    }

    public int getPPRank(int instanceId) {
        return (instanceId - rank0Index) % PPDim;
    }
}
