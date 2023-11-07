package org.cpnsim.statemanager;

import lombok.Getter;

public class SynGapManager {
    @Getter
    private double synGap;
    private double smallSynGap;
    private int partitionNum;
    @Getter
    private int smallSynGapCount;

    public SynGapManager(double synGap, int partitionNum) {
        this.synGap = synGap;
        this.partitionNum = partitionNum;
        this.smallSynGap = synGap / partitionNum;
        this.smallSynGapCount = 0;
    }

    public boolean isSynCostTime() {
        return synGap > 0;
    }

    public double getNextSynDelay(double nowTime) {
        long nextSynGapCount = smallSynGapCount + 1;
        if (nextSynGapCount % partitionNum == 0) {
            return synGap * (nextSynGapCount / partitionNum) - nowTime;
        } else {
            return synGap * (nextSynGapCount / partitionNum) + smallSynGap * (nextSynGapCount % partitionNum) - nowTime;
        }
    }

    public void synGapCountAddOne() {
        if (smallSynGapCount == Integer.MAX_VALUE) {
            System.out.println("smallSynGapCount overflow");
            System.exit(-1);
        }
        smallSynGapCount++;
    }

    public double getSynTime(int smallSynGapCount) {
        if (smallSynGapCount < 0) {
            return 0;
        } else if (smallSynGapCount % partitionNum == 0) {
            return synGap * (smallSynGapCount / partitionNum);
        } else {
            return synGap * (smallSynGapCount / partitionNum) + smallSynGap * (smallSynGapCount % partitionNum);
        }
    }
}
