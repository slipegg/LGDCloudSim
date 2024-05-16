package org.lgdcloudsim.intrascheduler;

import lombok.Getter;
import lombok.Setter;
import org.lgdcloudsim.queue.QueueResult;
import org.lgdcloudsim.request.Instance;
import org.lgdcloudsim.datacenter.Datacenter;
import org.lgdcloudsim.queue.InstanceQueue;
import org.lgdcloudsim.queue.InstanceQueueFifo;
import org.lgdcloudsim.statemanager.HostState;
import org.lgdcloudsim.statemanager.SynState;

import java.util.List;
import java.util.Random;

/**
 * The simple intra-scheduler that implements the {@link IntraScheduler} interface.
 * It is used to schedule the instances in the data center.
 * The scheduling strategy is as follows:
 * <ul>
 *     <li>Get the latest synchronization partition id from the {@link org.lgdcloudsim.statemanager.StatesManager}.</li>
 *     <li>Find the suitable host from a random start host id in the latest synchronization partition.</li>
 *     <li>If there is no suitable host the latest synchronization partition,
 *     try to find the suitable host from the last two synchronization partitions and so on.</li>
 * </ul>
 *
 * @author Anonymous
 * @since LGDCloudSim 1.0
 */
public class IntraSchedulerSimple implements IntraScheduler {
    /**
     * The data center it belongs to.
     */
    @Getter
    Datacenter datacenter;

    /**
     * The intra-scheduler id.
     */
    @Getter
    int id;

    /**
     * The name of the intra-scheduler.
     */
    @Getter
    @Setter
    String name;

    /**
     * The new instance queue.
     */
    @Getter
    InstanceQueue instanceQueue;

    /**
     * The retry instance queue.
     */
    @Getter
    @Setter
    InstanceQueue retryInstanceQueue;

    /**
     * The schedule cost time.
     */
    @Getter
    @Setter
    double scheduleCostTime = 0;

    /**
     * The first synchronization partition id.
     */
    @Getter
    @Setter
    int firstPartitionId = -1;

    /**
     * The partition num of the data center.
     */
    int partitionNum = 0;

    /**
     * The time needed to exclude.
     * It will record the time for each scheduler to obtain status,
     * and this part of the time will be subtracted from the final calculation of the total scheduling time.
     * Because it may take a long time for the scheduler to obtain the state through the state manager,
     * if no exclusions are performed,
     * the scheduling time may not be significantly related to the number of traversals.
     */
    double excludeTime = 0;


    /**
     * The random number generator.
     */
    Random random = new Random();

    /**
     * The constructor of the simple intra-scheduler.
     *
     * @param id               the id of the intra-scheduler
     * @param firstPartitionId the first synchronization partition id
     * @param partitionNum     the number of partitions in the data center
     */
    public IntraSchedulerSimple(int id, int firstPartitionId, int partitionNum) {
        instanceQueue = new InstanceQueueFifo();
        retryInstanceQueue = new InstanceQueueFifo();
        this.firstPartitionId = firstPartitionId;
        this.partitionNum = partitionNum;
        setId(id);
    }

    /**
     * Set the id of the intra-scheduler.
     * @param id the id of the intra-scheduler
     */
    public void setId(int id) {
        this.id = id;
        this.name = "InScheduler" + id;
    }

    /**
     * Add the instance to the instance queue.
     * If the flag isRetry is true, the instance will be added to the retry instance queue.
     * Otherwise, the instance will be added to the new instance queue.
     * @param instances the instances to be added to the instance queue
     * @param isRetry whether the instance is added to the retry instance queue
     * @return the intra-scheduler itself
     */
    @Override
    public IntraScheduler addInstance(List<Instance> instances, boolean isRetry) {
        if (isRetry) {
            retryInstanceQueue.add(instances);
        } else {
            instanceQueue.add(instances);
        }
        return this;
    }

    /**
     * Get whether the new instance queue and the retry instance queue are empty.
     * @return the new instance queue size
     */
    @Override
    public boolean isQueuesEmpty() {
        return instanceQueue.isEmpty() && retryInstanceQueue.isEmpty();
    }

    /**
     * Get the new instance queue size.
     * @return the new instance queue size
     */
    @Override
    public int getNewInstanceQueueSize() {
        return instanceQueue.size();
    }

    /**
     * Get the retry instance queue size.
     * @return the retry instance queue size
     */
    @Override
    public int getRetryInstanceQueueSize() {
        return retryInstanceQueue.size();
    }

    /**
     * Schedule a batch of instances from the instance queue to the host.
     * @return the result of the scheduling
     */
    @Override
    public IntraSchedulerResult schedule() {
        SynState synState = datacenter.getStatesManager().getSynState(this);

        QueueResult<Instance> queueResult = getWaitSchedulingInstances();
        List<Instance> waitScheduledItems = queueResult.getWaitScheduledItems();

        double startTime = System.currentTimeMillis();
        IntraSchedulerResult intraSchedulerResult = scheduleInstances(waitScheduledItems, synState);
        double endTime = System.currentTimeMillis();

        this.scheduleCostTime = Math.max(0, (endTime - startTime) - excludeTime);//= BigDecimal.valueOf((instances.size() * 0.25)).setScale(datacenter.getSimulation().getSimulationAccuracy(), RoundingMode.HALF_UP).doubleValue();//* instances.size();//(endTime-startTime)/10;

        setInstanceIntraScheduleEndTime(waitScheduledItems, getDatacenter().getSimulation().clock()+this.scheduleCostTime);

        intraSchedulerResult.setOutDatedUserRequests(queueResult.getOutDatedItems());
        return intraSchedulerResult;
    }

    void setInstanceIntraScheduleEndTime(List<Instance> instances, double scheduleEndTime) {
        for (Instance instance : instances) {
            instance.setIntraScheduleEndTime(scheduleEndTime);
        }
    }

    /**
     * Get a batch of instances to be scheduled from the instance queue.
     * @return a batch of instances to be scheduled
     */
    private QueueResult<Instance> getWaitSchedulingInstances() {
        double nowTime = getDatacenter().getSimulation().clock();
        if (retryInstanceQueue.isEmpty()) {
            return instanceQueue.getBatchItem(nowTime);
        } else {
            QueueResult<Instance> queueResult = retryInstanceQueue.getBatchItem(nowTime);

            if (queueResult.getWaitScheduledItemsSize() < instanceQueue.getBatchNum()) {
                int itemNum = instanceQueue.getBatchNum() - queueResult.getWaitScheduledItemsSize();
                QueueResult<Instance> queueResultTmp = instanceQueue.getItems(itemNum, nowTime);
                queueResult.add(queueResultTmp);
            }
            return queueResult;
        }
    }

    /**
     * Schedule the instances to the host.
     * The scheduling strategy is as follows:
     * <ul>
     *     <li>Get the latest synchronization partition id from the {@link org.lgdcloudsim.statemanager.StatesManager}.</li>
     *     <li>Find the suitable host from start to end in the partition according to the synchronization partition id.</li>
     *     <li>If there is no suitable host the latest synchronization partition,
     *     try to find the suitable host from the last two synchronization partitions and so on.</li>
     * </ul>
     * @param instances the instances to be scheduled
     * @param synState the synchronization state
     * @return the result of the scheduling
     */
    protected IntraSchedulerResult scheduleInstances(List<Instance> instances, SynState synState) {
        double excludeTimeNanos = 0;
        IntraSchedulerResult intraSchedulerResult = new IntraSchedulerResult(this, getDatacenter().getSimulation().clock());

        int synPartitionId = firstPartitionId;
        if (datacenter.getStatesManager().isSynCostTime()) {
            synPartitionId = (firstPartitionId + datacenter.getStatesManager().getPartitionSynCount()) % partitionNum;
        }
        for (Instance instance : instances) {
            int suitId = -1;

            for (int p = 0; p < partitionNum; p++) {
                int[] range = datacenter.getStatesManager().getPartitionRangesManager().getRange((synPartitionId + partitionNum - p) % partitionNum);
                int startHostId = random.nextInt(range[1] - range[0] + 1);
                int rangeLength = range[1] - range[0] + 1;
                for (int i = 0; i < rangeLength; i++) {
                    int hostId = range[0] + (startHostId + i) % rangeLength;
                    long startTime = System.nanoTime();
                    HostState hostState = synState.getHostState(hostId);
                    long endTime = System.nanoTime();
                    excludeTimeNanos += endTime - startTime;
                    if (hostState.isSuitable(instance)) {
                        suitId = hostId;
                        break;
                    }
                }
                if (suitId != -1) {
                    break;
                }
            }

            if (suitId != -1) {
                synState.allocateTmpResource(suitId, instance);
                instance.setExpectedScheduleHostId(suitId);
                intraSchedulerResult.addScheduledInstance(instance);
            } else {
                intraSchedulerResult.addFailedScheduledInstance(instance);
            }
        }

        excludeTime = excludeTimeNanos/1_000_000;
        return intraSchedulerResult;
    }

    /**
     * Set the data center it belongs to.
     */
    @Override
    public void setDatacenter(Datacenter datacenter) {
        this.datacenter = datacenter;
    }
}
