package org.lgdcloudsim.statemanager;

import lombok.Getter;

/**
 * A class to manage the synchronization gap.
 * The reason why we need this class is that sometimes the number of partitions does not divide the synchronization interval.
 * In this case, additional judgments are needed to determine the synchronization time of each partition.
 *
 * @author Anonymous
 * @since LGDCloudSim 1.0
 */
public class SynGapManager {
    /**
     * The synchronization gap.
     */
    @Getter
    private double synGap;
    /**
     * The partition synchronization gap.
     * It is calculated by dividing the synchronization gap by the number of partitions.
     */
    private double partitionSynGap;
    /**
     * The number of partitions.
     */
    private int partitionNum;
    /**
     * The number of partition synchronization gap.
     */
    @Getter
    private int partitionSynCount;

    /**
     * Initialize the synchronization gap manager.
     *
     * @param synGap       the synchronization gap.
     * @param partitionNum the number of partitions.
     */
    public SynGapManager(double synGap, int partitionNum) {
        this.synGap = synGap;
        this.partitionNum = partitionNum;
        this.partitionSynGap = synGap / partitionNum;
        this.partitionSynCount = 0;
    }

    /**
     * Whether the synchronization cost time.
     * If the synchronization gap is greater than 0, it means that the synchronization cost time.
     * @return whether the synchronization cost time.
     */
    public boolean isSynCostTime() {
        return synGap > 0;
    }

    /**
     * Get the next synchronization delay.
     * The delay is calculated by the following formula:
     * <pre>
     *     nextSynGapCount % partitionNum == 0: synGap * (nextSynGapCount / partitionNum) - nowTime
     *     nextSynGapCount % partitionNum != 0: synGap * (nextSynGapCount / partitionNum) + partitionSynGap * (nextSynGapCount % partitionNum) - nowTime
     * </pre>
     * It ensures that when the number of partition synchronizations is an integer multiple of the partition,
     * the next synchronization time will also be an integer multiple of synGap.
     * Otherwise, only more partitionSynGap calculations are needed.
     *
     * @param nowTime the current time.
     * @return the next synchronization delay.
     */
    public double getNextSynDelay(double nowTime) {
        long nextSynGapCount = partitionSynCount + 1;
        if (nextSynGapCount % partitionNum == 0) {
            return synGap * (nextSynGapCount / partitionNum) - nowTime;
        } else {
            return synGap * (nextSynGapCount / partitionNum) + partitionSynGap * (nextSynGapCount % partitionNum) - nowTime;
        }
    }

    /**
     * Add one to the partition synchronization count.
     * If the partition synchronization gap count is Integer.MAX_VALUE, it means that the partition synchronization gap count overflows.
     * In this case, the program will exit.
     */
    public void partitionSynGapCountAddOne() {
        if (partitionSynCount == Integer.MAX_VALUE) {
            System.out.println("smallSynGapCount overflow");
            System.exit(-1);
        }
        partitionSynCount++;
    }

    /**
     * Calculate the parition synchronization time according to the partition synchronization count.
     *
     * @param partitionSynCount the partition synchronization count.
     * @return the partition synchronization time.
     */
    public double getSynTime(int partitionSynCount) {
        if (partitionSynCount < 0) {
            return 0;
        } else if (partitionSynCount % partitionNum == 0) {
            return synGap * (partitionSynCount / partitionNum);
        } else {
            return synGap * (partitionSynCount / partitionNum) + partitionSynGap * (partitionSynCount % partitionNum);
        }
    }
}
