package org.lgdcloudsim.intrascheduler;

import org.lgdcloudsim.request.Instance;
import org.lgdcloudsim.statemanager.HostState;
import org.lgdcloudsim.statemanager.SynState;
import org.lgdcloudsim.util.ScoredHost;
import org.lgdcloudsim.util.ScoredHostsManager;

import java.util.*;

/**
 * The intra-scheduler that implements the least requested scheduling strategy.
 * It is extended from the {@link IntraSchedulerSimple}.
 * It changes the scheduleInstances function to implement the least requested scheduling strategy.
 * The least requested scheduling strategy is as follows:
 * <ul>
 *      <li>Sort the instances by the resource requirements in descending order.</li>
 *      <li>Get the same instances to schedule.</li>
 *      <li>Filter {@link #scoredHostNumForSameInstance} hosts and score them from a random start index.
 *      Note that the score will be cached in the {@link #scoreHostHistoryMap}.</li>
 *      <li>Schedule the same instances to the hosts with the highest scores one by one.</li>
 * </ul>
 * The score of a host that has met the requirements of the instance is calculated as follows:
 * Score = (cpu remaining resources * 10 / total cpu resources + ram remaining resources * 10 / total ram resources) / 2
 *
 * @author Anonymous
 * @since LGDCloudSim 1.0
 */
public class IntraSchedulerLeastRequested extends IntraSchedulerSimple {
    /**
     * The cache of the host score history.
     */
    Map<Integer, Double> scoreHostHistoryMap = new HashMap<>();

    /**
     * The number of scored hosts for the same instance.
     */
    int scoredHostNumForSameInstance = 100;

    /**
     * The time spent on the getting host state.
     * It needs to be excluded from the scheduling time.
     */
    long excludeTimeNanos = 0;

    /**
     * The random object.
     */
    Random random = new Random();

    /**
     * The constructor of the least requested intra-scheduler.
     *
     * @param id               the id of the intra-scheduler
     * @param firstPartitionId the first synchronization partition id
     * @param partitionNum     the number of partitions
     */
    public IntraSchedulerLeastRequested(int id, int firstPartitionId, int partitionNum) {
        super(id, firstPartitionId, partitionNum);
    }

    /**
     * Schedule the instances to the host by the least requested scheduling strategy.
     * @param instances the instances to be scheduled
     * @param synState the synchronization state
     * @return the result of the scheduling
     */
    @Override
    protected IntraSchedulerResult scheduleInstances(List<Instance> instances, SynState synState) {
        processBeforeSchedule();
        IntraSchedulerResult intraSchedulerResult = new IntraSchedulerResult(this, getDatacenter().getSimulation().clock());

        instances.sort(new CustomComparator().reversed());

        List<Instance> sameInstance = new ArrayList<>();
        for(Instance instance : instances){
            if(!sameInstance.isEmpty() && !isSameRequestInstance(sameInstance.get(0), instance)){
                scheduleForSameInstancesToHost(sameInstance, intraSchedulerResult, synState);

                sameInstance.clear();
                sameInstance.add(instance);
            }else{
                sameInstance.add(instance);
            }
        }

        if(!sameInstance.isEmpty()){
            scheduleForSameInstancesToHost(sameInstance, intraSchedulerResult, synState);
        }

        excludeTime = excludeTimeNanos/1_000_000;
        return intraSchedulerResult;
    }

    /**
     * Process before the scheduling.
     */
    protected void processBeforeSchedule(){
        excludeTimeNanos = 0;
        scoreHostHistoryMap.clear();
    }

    /**
     * Judge whether the two instances are the same instance with the same resource requirements.
     * @param instance1 the first instance
     * @param instance2 the second instance
     * @return true if the two instances are the same instance with the same resource requirements, otherwise false.
     */
    private boolean isSameRequestInstance(Instance instance1, Instance instance2){
        return instance1.getCpu() == instance2.getCpu() && instance1.getRam() == instance2.getRam() && instance1.getStorage() == instance2.getStorage() && instance1.getBw() == instance2.getBw();
    }

    /**
     * Schedule the same instances to the host by the least requested scheduling strategy.
     * @param sameInstances the same instances to be scheduled
     * @param intraSchedulerResult the result of the scheduling
     * @param synState the synchronization state
     */
    private void scheduleForSameInstancesToHost(List<Instance> sameInstances, IntraSchedulerResult intraSchedulerResult, SynState synState) {
        int hostNum = datacenter.getStatesManager().getHostNum();
        int randomStartIndex = random.nextInt(hostNum);
        int scoredHostNum = Math.min(sameInstances.size() * scoredHostNumForSameInstance, hostNum);
        Instance sameInstance = sameInstances.get(0);

        ScoredHostsManager scoredHostsManager = getScoredHostsManager(sameInstance, randomStartIndex, scoredHostNum, synState);

        scheduleSameInstancesByScoredHosts(sameInstances, scoredHostsManager, intraSchedulerResult, synState);
    }

    /**
     * Score host for the instance from the random start index.
     * And get the {@link ScoredHostsManager} for the same instances.
     * @param instance the instance to be scheduled
     * @param randomStartIndex the random start index
     * @param scoredHostNum the number of scored hosts
     * @param synState the synchronization state
     * @return the {@link ScoredHostsManager}
     */
    protected ScoredHostsManager getScoredHostsManager(Instance instance, int randomStartIndex, int scoredHostNum, SynState synState){
        ScoredHostsManager scoredHostsManager = new ScoredHostsManager(new HashMap<>(Map.of(datacenter, scoreHostHistoryMap)));
        List<Integer> innerSchedulerView = getDatacenter().getStatesManager().getIntraSchedulerView(this);
        int viewSize = innerSchedulerView.get(1)-innerSchedulerView.get(0)+1;
        for(int i=0; i<viewSize; i++){
            int hostId = (randomStartIndex + i) % viewSize + innerSchedulerView.get(0);
            double score = getScoreForHost(instance, hostId, synState);
            if(score==-1){
                continue;
            }

            scoredHostsManager.addScoredHost(hostId, datacenter, score);

            if(scoredHostsManager.getScoredHostNum() >= scoredHostNum){
                break;
            }
        }
        return scoredHostsManager;
    }

    /**
     * Get the score for the host.
     * @param instance the instance to be scheduled
     * @param hostId the id of the host
     * @param synState the synchronization state
     * @return the score for the host
     */
    protected double getScoreForHost(Instance instance, int hostId, SynState synState){
        long startTime = System.nanoTime();
        HostState hostState = synState.getHostState(hostId);
        long endTime  = System.nanoTime();
        excludeTimeNanos += endTime - startTime;
        if (!hostState.isSuitable(instance)) {
            return -1;
        } else {
            if(scoreHostHistoryMap.containsKey(hostId)){
                return scoreHostHistoryMap.get(hostId);
            }else{
                int cpuCapacity = datacenter.getStatesManager().getHostCapacityManager().getHostCapacity(hostId)[0];
                int ramCapacity = datacenter.getStatesManager().getHostCapacityManager().getHostCapacity(hostId)[1];
                double score = (hostState.getCpu() * 10 / (double) cpuCapacity + hostState.getRam() * 10 / (double) ramCapacity) / 2;
                scoreHostHistoryMap.put(hostId, score);
                return score;
            }
        }
    }

    /**
     * Schedule the same instances to the hosts with the highest scores one by one.
     * @param sameInstances the same instances to be scheduled
     * @param scoredHostsManager the scored hosts manager
     * @param intraSchedulerResult the result of the scheduling
     * @param synState the synchronization state
     */
    private void scheduleSameInstancesByScoredHosts(List<Instance> sameInstances, ScoredHostsManager scoredHostsManager, IntraSchedulerResult intraSchedulerResult, SynState synState) {
        for(Instance instance : sameInstances){
            ScoredHost scoredHost= scoredHostsManager.pollBestScoreHost();
            while (scoredHost !=null && (instance.getRetryHostIds()!=null&&instance.getRetryHostIds().contains(scoredHost.getHostId()))){
                scoredHost= scoredHostsManager.pollBestScoreHost();
            }

            if(scoredHost == null){
                intraSchedulerResult.addFailedScheduledInstance(instance);
            }else{
                int scheduledHostId = scoredHost.getHostId();
                instance.setExpectedScheduleHostId(scheduledHostId);
                intraSchedulerResult.addScheduledInstance(instance);
                synState.allocateTmpResource(scheduledHostId, instance);
                scoreHostHistoryMap.remove(scheduledHostId);

                double score = getScoreForHost(instance, scheduledHostId, synState);
                if(score!=-1){
                    scoredHostsManager.addScoredHost(scheduledHostId, datacenter, score);
                }
            }
        }
    }

    /**
     * The custom comparator for the instance.
     * It compares the instance by the resource requirements.
     */
    class CustomComparator implements Comparator<Instance> {
        @Override
        public int compare(Instance instance1, Instance instance2) {

            int result1 = instance1.getCpu() - instance2.getCpu();
            if (result1 != 0) {
                return result1;
            }

            int result2 = instance1.getRam() - instance2.getRam();
            if (result2 != 0) {
                return result2;
            }

            int result3 = instance1.getStorage() - instance2.getStorage();
            if (result3 != 0) {
                return result3;
            }

            return instance1.getBw() - instance2.getBw();
        }
    }
}
