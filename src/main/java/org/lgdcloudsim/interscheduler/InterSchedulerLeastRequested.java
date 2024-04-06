package org.lgdcloudsim.interscheduler;

import org.lgdcloudsim.core.Simulation;
import org.lgdcloudsim.datacenter.Datacenter;
import org.lgdcloudsim.network.NetworkTopology;
import org.lgdcloudsim.request.Instance;
import org.lgdcloudsim.request.InstanceGroup;
import org.lgdcloudsim.statemanager.DetailedDcStateSimple;
import org.lgdcloudsim.statemanager.HostState;
import org.lgdcloudsim.statemanager.SimpleStateEasyObject;
import org.lgdcloudsim.util.ScoredDatacentersManager;
import org.lgdcloudsim.util.ScoredDc;
import org.lgdcloudsim.util.ScoredHost;
import org.lgdcloudsim.util.ScoredHostsManager;

import java.util.*;

/**
 * The least requested inter-scheduler.
 * It is extended from the {@link InterSchedulerSimple}.
 * It has changed the scheduleToDatacenter, scheduleToHost and scheduleMixed functions to implement the least requested scheduling strategy.
 * The schedule algorithm is as follows:
 * <ol>
 * <li> Sort the batch of requests that need to be scheduled and select the requests with the same resource requirement to schedule together in each scheduling round.
 * For scheduleToHost, the type of request is instance, and for scheduleToDatacenter, the type of request is instance group.</li>
 * <li> Screen out a certain number of suitable scheduling targets for the request starting from random positions and score them. Note that these scores are cached.
 *    For different requests, 100 suitable hosts are needed for scheduleToHost, while 3 data centers are needed for scheduleToDatacenter.
 *    The score is contingent on the remaining resource proportion of the target, with higher proportions yielding higher scores.</li>
 * <li>Select the one with the highest score among all scheduling targets for scheduling.</li>
 * </ol>
 * For scheduleMixed function, it will first try to schedule the instance to the host in the data center where the inter-scheduler is located.
 * If the scheduling fails, it will try to forward the instance to other data centers.
 *
 * @author Anonymous
 * @since LGDCloudSim 1.0
 */
public class InterSchedulerLeastRequested extends InterSchedulerSimple {
    /**
     * The history map of the host scores.
     */
    Map<Datacenter, Map<Integer, Double>> scoreHostHistoryMap = new HashMap<>();
    /**
     * The history map of the data center scores.
     */
    Map<Datacenter, Double> scoreDcHistoryMap = new HashMap<>();
    /**
     * The number of scored hosts needs for the same instance group.
     */
    int scoredHostNumForSameInstanceGroup = 100;
    /**
     * The number of scored data centers needs for the same instance group.
     */
    int scoredDcNumForSameInstanceGroup = 3;

    /**
     * The constructor of the least requested inter-scheduler.
     *
     * @param id               the id of the inter-scheduler.
     * @param simulation       the simulation object.
     * @param collaborationId  the collaboration id of the inter-scheduler.
     * @param target           the target id of the inter-scheduler.
     * @param isSupportForward whether the inter-scheduler supports forward.
     */
    public InterSchedulerLeastRequested(int id, Simulation simulation, int collaborationId, int target, boolean isSupportForward) {
        super(id, simulation, collaborationId, target, isSupportForward);
    }

    /**
     * Schedule the instance groups to the data centers.
     * @param instanceGroups the instance groups to be scheduled
     * @return the result of the scheduling.
     */
    @Override
    protected InterSchedulerResult scheduleToDatacenter(List<InstanceGroup> instanceGroups) {
        final List<Datacenter> allDatacenters = simulation.getCollaborationManager().getDatacenters(collaborationId);
        InterSchedulerResult interSchedulerResult = new InterSchedulerResult(this, allDatacenters);
        instanceGroups.sort(new CustomComparator().reversed());

        List<InstanceGroup> sameInstanceGroups = new ArrayList<>();
        for (InstanceGroup group : instanceGroups) {
            if (!sameInstanceGroups.isEmpty() && !isSameRequestInstanceGroup(sameInstanceGroups.get(0), group)) {
                scheduleForSameInstanceGroupsToDc(sameInstanceGroups, interSchedulerResult, allDatacenters);

                sameInstanceGroups.clear();
                sameInstanceGroups.add(group);
            } else {
                sameInstanceGroups.add(group);
            }
        }

        if (!sameInstanceGroups.isEmpty()) {
            scheduleForSameInstanceGroupsToDc(sameInstanceGroups, interSchedulerResult, allDatacenters);
        }

        return interSchedulerResult;
    }

    /**
     * Schedule a batch of same instance groups to the data centers.
     * @param sameInstanceGroups the same instance groups to be scheduled.
     * @param interSchedulerResult the result of the scheduling.
     * @param availableDatacenters the available data centers.
     */
    private void scheduleForSameInstanceGroupsToDc(List<InstanceGroup> sameInstanceGroups, InterSchedulerResult interSchedulerResult, List<Datacenter> availableDatacenters) {
        if (sameInstanceGroups.get(0).isNetworkLimited()) {
            availableDatacenters = getAvailableDatacenterByNetworkLimit(sameInstanceGroups.get(0), availableDatacenters, interSchedulerResult);
        }
        if (availableDatacenters.isEmpty()) {
            sameInstanceGroups.forEach(interSchedulerResult::addFailedInstanceGroup);
            return;
        }

        int randomStartIndex = random.nextInt(availableDatacenters.size());
        int scoredDcNum = Math.min(sameInstanceGroups.size() * scoredDcNumForSameInstanceGroup, availableDatacenters.size());
        InstanceGroup sameInstanceGroup = sameInstanceGroups.get(0);

        ScoredDatacentersManager scoredDatacentersManager = getScoredDatacenters(sameInstanceGroup, randomStartIndex, availableDatacenters, scoredDcNum);

        scheduleSameInstanceGroupByScoredDcs(sameInstanceGroups, interSchedulerResult, scoredDatacentersManager);
    }

    /**
     * Get the available data centers by the network limit.
     * @param instanceGroup the instance group.
     * @param allDatacenters all the data centers.
     * @param interSchedulerResult the result of the scheduling.
     * @return the available data centers.
     */
    private List<Datacenter> getAvailableDatacenterByNetworkLimit(InstanceGroup instanceGroup, List<Datacenter> allDatacenters, InterSchedulerResult interSchedulerResult) {
        List<Datacenter> availableDatacenters = new ArrayList<>(allDatacenters);
        filterAvailableDatacenterByAccessLatency(instanceGroup, availableDatacenters);
        filterAvailableDatacenterByEdgeDelayLimit(instanceGroup, availableDatacenters, interSchedulerResult);
        filterAvailableDatacenterByEdgeBwLimit(instanceGroup, availableDatacenters, interSchedulerResult);
        return availableDatacenters;
    }

    /**
     * Filter the available data centers by the access latency.
     * @param instanceGroup the instance group.
     * @param availableDatacenters the available data centers.
     */
    private void filterAvailableDatacenterByAccessLatency(InstanceGroup instanceGroup, List<Datacenter> availableDatacenters) {
        NetworkTopology networkTopology = simulation.getNetworkTopology();
        availableDatacenters.removeIf(dc -> networkTopology.getAccessLatency(instanceGroup.getUserRequest(), dc) > instanceGroup.getAccessLatency());
    }

    /**
     * Filter the available data centers by the edge delay limit.
     * @param instanceGroup the instance group.
     * @param availableDatacenters the available data centers.
     * @param interSchedulerResult the result of the scheduling.
     */
    private void filterAvailableDatacenterByEdgeDelayLimit(InstanceGroup instanceGroup, List<Datacenter> availableDatacenters, InterSchedulerResult interSchedulerResult) {
        NetworkTopology networkTopology = simulation.getNetworkTopology();
        for (InstanceGroup dstInstanceGroup : instanceGroup.getUserRequest().getInstanceGroupGraph().getDstList(instanceGroup)) {
            Datacenter scheduledDatacenter = getPossibleScheduledDatacenter(dstInstanceGroup, interSchedulerResult);
            if (scheduledDatacenter != Datacenter.NULL) {
                availableDatacenters.removeIf(dc -> networkTopology.getDelay(dc, scheduledDatacenter) > instanceGroup.getUserRequest().getInstanceGroupGraph().getDelay(instanceGroup, dstInstanceGroup));
            }
        }
        for (InstanceGroup srcInstanceGroup : instanceGroup.getUserRequest().getInstanceGroupGraph().getSrcList(instanceGroup)) {
            Datacenter scheduledDatacenter = getPossibleScheduledDatacenter(srcInstanceGroup, interSchedulerResult);
            if (scheduledDatacenter != Datacenter.NULL) {
                availableDatacenters.removeIf(dc -> networkTopology.getDelay(dc, scheduledDatacenter) > instanceGroup.getUserRequest().getInstanceGroupGraph().getDelay(srcInstanceGroup, instanceGroup));
            }
        }
    }

    /**
     * Filter the available data centers by the edge bandwidth limit.
     * @param instanceGroup the instance group.
     * @param availableDatacenters the available data centers.
     * @param interSchedulerResult the result of the scheduling.
     */
    private void filterAvailableDatacenterByEdgeBwLimit(InstanceGroup instanceGroup, List<Datacenter> availableDatacenters, InterSchedulerResult interSchedulerResult) {
        NetworkTopology networkTopology = simulation.getNetworkTopology();
        for (InstanceGroup dstInstanceGroup : instanceGroup.getUserRequest().getInstanceGroupGraph().getDstList(instanceGroup)) {
            Datacenter scheduledDatacenter = getPossibleScheduledDatacenter(dstInstanceGroup, interSchedulerResult);
            if (scheduledDatacenter != Datacenter.NULL) {
                availableDatacenters.removeIf(dc -> networkTopology.getBw(dc, scheduledDatacenter) < instanceGroup.getUserRequest().getInstanceGroupGraph().getBw(instanceGroup, dstInstanceGroup));
            }
        }
        for (InstanceGroup srcInstanceGroup : instanceGroup.getUserRequest().getInstanceGroupGraph().getSrcList(instanceGroup)) {
            Datacenter scheduledDatacenter = getPossibleScheduledDatacenter(srcInstanceGroup, interSchedulerResult);
            if (scheduledDatacenter != Datacenter.NULL) {
                availableDatacenters.removeIf(dc -> networkTopology.getBw(dc, scheduledDatacenter) < instanceGroup.getUserRequest().getInstanceGroupGraph().getBw(srcInstanceGroup, instanceGroup));
            }
        }
    }

    /**
     * Get the possible scheduled data center from the previous scheduling result.
     * @param instanceGroup the instance group.
     * @param interSchedulerResult the result of the scheduling.
     * @return the possible scheduled data center.
     */
    private Datacenter getPossibleScheduledDatacenter(InstanceGroup instanceGroup, InterSchedulerResult interSchedulerResult) {
        if (instanceGroup.getReceiveDatacenter() != Datacenter.NULL) {
            return instanceGroup.getReceiveDatacenter();
        } else {
            return interSchedulerResult.getScheduledDatacenter(instanceGroup);
        }
    }

    /**
     * Get the scored of the available data centers for the same instance group.
     * @param sameInstanceGroup the same instance group.
     * @param randomStartIndex the random start index.
     * @param allDatacenters all the data centers.
     * @param scoredDcNum the number of scored data centers.
     * @return the scored data centers.
     */
    private ScoredDatacentersManager getScoredDatacenters(InstanceGroup sameInstanceGroup, int randomStartIndex, List<Datacenter> allDatacenters, int scoredDcNum){
        ScoredDatacentersManager scoredDatacentersManager = new ScoredDatacentersManager(scoreDcHistoryMap);

        for(int i = 0; i < allDatacenters.size(); i++){
            int dcIndex = (randomStartIndex + i) % allDatacenters.size();
            Datacenter datacenter = allDatacenters.get(dcIndex);

            double score = getScoreForDc(sameInstanceGroup, datacenter, (SimpleStateEasyObject)interScheduleSimpleStateMap.get(datacenter));
            if(score == -1){
                continue;
            }

            scoredDatacentersManager.filterAndScoreDc(datacenter, score);

            if (scoredDatacentersManager.getScoredDcNum() >= scoredDcNum) {
                break;
            }
        }

        return scoredDatacentersManager;
    }

    /**
     * Get the score of the data center for the same instance group by the simple state{@link SimpleStateEasyObject}.
     * Score = (cpuAvailableSum * 10 / cpuCapacitySum + ramAvailableSum * 10 / ramCapacitySum) / 2
     * It refers to the calculation method of <a href="https://github.com/kubernetes/kubernetes/commit/a176001aa1d7d909bafbaf42794d5dd9584ad9ea">kubernetes</a>.
     * @param instanceGroup the instance group.
     * @param datacenter the data center.
     * @param simpleStateEasyObject the simple state{@link SimpleStateEasyObject} of the data center.
     * @return the score of the data center.
     */
    private double getScoreForDc(InstanceGroup instanceGroup, Datacenter datacenter, SimpleStateEasyObject simpleStateEasyObject) {
        long cpuSum = instanceGroup.getCpuSum();
        long ramSum = instanceGroup.getRamSum();
        long storageSum = instanceGroup.getStorageSum();
        long bwSum = instanceGroup.getBwSum();
        if(simpleStateEasyObject.getCpuAvailableSum()<cpuSum || simpleStateEasyObject.getRamAvailableSum()<ramSum || simpleStateEasyObject.getStorageAvailableSum()<storageSum || simpleStateEasyObject.getBwAvailableSum()<bwSum){
            return -1;
        }else{
            if(scoreDcHistoryMap.containsKey(datacenter)){
                return scoreDcHistoryMap.get(datacenter);
            }else{
                double score = (simpleStateEasyObject.getCpuAvailableSum() * 10 / (double) simpleStateEasyObject.getCpuCapacitySum() + simpleStateEasyObject.getRamAvailableSum() * 10 / (double) simpleStateEasyObject.getRamCapacitySum()) / 2;
                scoreDcHistoryMap.put(datacenter, score);
                return score;
            }
        }
    }

    /**
     * Schedule a batch of same instance groups to the data centers by the scored data centers.
     * It will allocate the resource to the data center with the highest score and update the score of the data center.
     * @param sameInstanceGroups the same instance groups to be scheduled.
     * @param interSchedulerResult the result of the scheduling.
     * @param scoredDatacentersManager the scored data centers.
     */
    private void scheduleSameInstanceGroupByScoredDcs(List<InstanceGroup> sameInstanceGroups, InterSchedulerResult interSchedulerResult, ScoredDatacentersManager scoredDatacentersManager){
        for (InstanceGroup instanceGroup : sameInstanceGroups) {
            ScoredDc scoredDc = scoredDatacentersManager.pollBestScoreDc();
            if(scoredDc == null){
                interSchedulerResult.addFailedInstanceGroup(instanceGroup);
                continue;
            }

            Datacenter scheduledDatacenter = scoredDc.getDatacenter();

            interSchedulerResult.addDcResult(instanceGroup, scheduledDatacenter);
            SimpleStateEasyObject simpleStateEasyObject = (SimpleStateEasyObject) interScheduleSimpleStateMap.get(scheduledDatacenter);
            simpleStateEasyObject.allocateResource(instanceGroup.getCpuSum(),instanceGroup.getRamSum(),instanceGroup.getStorageSum(),instanceGroup.getBwSum());
            scoreDcHistoryMap.remove(scheduledDatacenter);

            double score = getScoreForDc(instanceGroup, scheduledDatacenter, simpleStateEasyObject);
            if(score!=-1){
                scoredDatacentersManager.filterAndScoreDc(scheduledDatacenter, score);
            }
        }
    }

    /**
     * Schedule the instance in the instance groups to the hosts of all available data centers.
     * @param instanceGroups the instance groups to be scheduled
     * @return the result of the scheduling.
     */
    @Override
    protected InterSchedulerResult scheduleToHost(List<InstanceGroup> instanceGroups) {
        scoreHostHistoryMap.clear();
        List<Datacenter> allDatacenters = simulation.getCollaborationManager().getDatacenters(collaborationId);
        InterSchedulerResult interSchedulerResult = new InterSchedulerResult(this, allDatacenters);
        int allDatacentersHostLength = allDatacenters.stream()
                .mapToInt(dc -> ((DetailedDcStateSimple) (interScheduleSimpleStateMap.get(dc))).getHostNum())
                .sum();

        instanceGroups.sort(new CustomComparator().reversed());

        List<InstanceGroup> sameInstanceGroups = new ArrayList<>();
        for (InstanceGroup group : instanceGroups) {
            if (!sameInstanceGroups.isEmpty() && !isSameRequestInstanceGroup(sameInstanceGroups.get(0), group)) {
                scheduleForSameInstanceGroupsToHost(sameInstanceGroups, interSchedulerResult, allDatacenters, allDatacentersHostLength);

                sameInstanceGroups.clear();
                sameInstanceGroups.add(group);
            } else {
                sameInstanceGroups.add(group);
            }
        }

        if (!sameInstanceGroups.isEmpty()) {
            scheduleForSameInstanceGroupsToHost(sameInstanceGroups, interSchedulerResult, allDatacenters, allDatacentersHostLength);
        }

        return interSchedulerResult;
    }

    /**
     * Judge whether the two instance groups are same.
     * Note that we have assumed that there is only one instance in the instance group.
     * TODO: We need to consider the case where there are multiple instances in the instance group.
     * @param group1 the first instance group.
     * @param group2 the second instance group.
     * @return whether the two instance groups are same.
     */
    private boolean isSameRequestInstanceGroup(InstanceGroup group1, InstanceGroup group2) {
        if (group1.isNetworkLimited() || group2.isNetworkLimited()) {
            return false;
        } else {
            Instance instance1 = group1.getInstances().get(0);
            Instance instance2 = group2.getInstances().get(0);
            return instance1.getCpu() == instance2.getCpu() && instance1.getRam() == instance2.getRam() && instance1.getStorage() == instance2.getStorage() && instance1.getBw() == instance2.getBw();
        }
    }

    /**
     * Schedule a batch of same instance groups to the hosts of all available data centers.
     * @param sameInstanceGroups the same instance groups to be scheduled.
     * @param interSchedulerResult the result of the scheduling.
     * @param allDatacenters all the data centers.
     * @param allDatacentersHostLength the total number of hosts in all the data centers.
     */
    private void scheduleForSameInstanceGroupsToHost(List<InstanceGroup> sameInstanceGroups, InterSchedulerResult interSchedulerResult, List<Datacenter> allDatacenters, int allDatacentersHostLength) {
        int randomStartIndex = random.nextInt(allDatacentersHostLength);
        int scoredHostNum = Math.min(sameInstanceGroups.size() * scoredHostNumForSameInstanceGroup, allDatacentersHostLength);
        Instance sameInstance = sameInstanceGroups.get(0).getInstances().get(0);

        ScoredHostsManager scoredHostsManager = getScoredHostsManager(sameInstance, randomStartIndex, allDatacenters, scoredHostNum);

        scheduleSameInstanceGroupsByScoredHosts(sameInstanceGroups, interSchedulerResult, scoredHostsManager);
    }

    /**
     * Get the {@link ScoredHostsManager} for the same instance.
     * The {@link ScoredHostsManager} will score the hosts in the data centers and return the scored hosts.
     * @param sameInstance the same instance.
     * @param randomStartIndex the random start index.
     * @param allDatacenters all the data centers.
     * @param scoredHostNum the number of scored hosts.
     * @return the {@link ScoredHostsManager}.
     */
    private ScoredHostsManager getScoredHostsManager(Instance sameInstance, int randomStartIndex, List<Datacenter> allDatacenters, int scoredHostNum) {
        int dcStartIndex = getDcIdByHostIdInAll(randomStartIndex, allDatacenters);
        if (dcStartIndex == -1) {
            LOGGER.warn("return dcId = -1 in getDcIdByHostIdInAll");
            throw new RuntimeException("return dcId = -1 in getDcIdByHostIdInAll");
        }

        int startHostIndexInDc = randomStartIndex;
        for(int i = 0; i < dcStartIndex; i++){
            Datacenter dc = allDatacenters.get(i);
            startHostIndexInDc -= ((DetailedDcStateSimple)(interScheduleSimpleStateMap.get(dc))).getHostNum();
        }

        ScoredHostsManager scoredHostsManager = new ScoredHostsManager(scoreHostHistoryMap);
        for (int i = 0; i < allDatacenters.size(); i++) {
            int dcIndex = (dcStartIndex + i) % allDatacenters.size();
            Datacenter dcSelected = allDatacenters.get(dcIndex);
            int hostNum = ((DetailedDcStateSimple) interScheduleSimpleStateMap.get(dcSelected)).getHostNum();

            scoreHostInDatacenter(sameInstance, dcSelected, startHostIndexInDc, hostNum - startHostIndexInDc, scoredHostNum, scoredHostsManager);
            if(scoredHostsManager.getScoredHostNum() >= scoredHostNum){
                break;
            }

            startHostIndexInDc = 0;
        }

        return scoredHostsManager;
    }

    /**
     * Score the hosts in the data center.
     * @param instance the instance.
     * @param dc the data center.
     * @param startHostIndexInDc the start host index in the data center.
     * @param length the length of the hosts in the data center.
     * @param scoredHostNum the number of scored hosts.
     * @param scoredHostsManager the {@link ScoredHostsManager}.
     */
    private void scoreHostInDatacenter(Instance instance, Datacenter dc, int startHostIndexInDc, int length, int scoredHostNum, ScoredHostsManager scoredHostsManager) {
        DetailedDcStateSimple detailedDcStateSimple = (DetailedDcStateSimple) interScheduleSimpleStateMap.get(dc);
        for(int i = 0, hostId; i< length; i++) {
            hostId = (startHostIndexInDc + i)%detailedDcStateSimple.getHostNum();
            traversalTime+=1;
            double score = getScoreForHost(instance, hostId, dc, detailedDcStateSimple);
            if(score == -1){
                continue;
            }
            scoredHostsManager.addScoredHost(hostId, dc, score);

            if(scoredHostsManager.getScoredHostNum() >= scoredHostNum){
                break;
            }
        }
    }

    /**
     * Get the score of the host for the instance.
     * Score = (cpuAvailable * 10 / cpuCapacity + ramAvailable * 10 / ramCapacity) / 2
     * @param instance the instance.
     * @param hostId the host id.
     * @param datacenter the data center.
     * @param detailedDcStateSimple the detailed state of the data center.
     * @return the score of the host.
     */
    private double getScoreForHost(Instance instance, int hostId, Datacenter datacenter, DetailedDcStateSimple detailedDcStateSimple) {
        HostState hostState = detailedDcStateSimple.getHostState(hostId);
        if (!hostState.isSuitable(instance)) {
            return -1;
        } else {
            if(scoreHostHistoryMap.containsKey(datacenter) && scoreHostHistoryMap.get(datacenter).containsKey(hostId)){
                return scoreHostHistoryMap.get(datacenter).get(hostId);
            }else{
                int cpuCapacity = detailedDcStateSimple.getHostCapacity(hostId)[0];
                int ramCapacity = detailedDcStateSimple.getHostCapacity(hostId)[1];
                double score = (hostState.getCpu() * 10 / (double) cpuCapacity + hostState.getRam() * 10 / (double) ramCapacity) / 2;
                scoreHostHistoryMap.computeIfAbsent(datacenter, k -> new HashMap<>()).put(hostId, score);
                return score;
            }
        }
    }

    /**
     * Schedule a batch of same instance groups to the hosts by the scored hosts.
     * It will schedule the instance to the host with the highest score and update the score of the host.
     * @param sameInstanceGroups the same instance groups to be scheduled.
     * @param interSchedulerResult the result of the scheduling.
     * @param scoredHostsManager the scored hosts.
     */
    private void scheduleSameInstanceGroupsByScoredHosts(List<InstanceGroup> sameInstanceGroups, InterSchedulerResult interSchedulerResult, ScoredHostsManager scoredHostsManager) {
        for (InstanceGroup instanceGroup : sameInstanceGroups) {
            ScoredHost scoredHost = scoredHostsManager.pollBestScoreHost();
            if(scoredHost == null){
                interSchedulerResult.addFailedInstanceGroup(instanceGroup);
                continue;
            }

            markScheduledInstance(interSchedulerResult, scoredHostsManager, instanceGroup, scoredHost);
        }
    }

    /**
     * First try to schedule the instance of instance groups to the data center.
     * If the scheduling fails, it will try to forward the instance to other data centers.
     * @param instanceGroups the instance groups to be scheduled
     * @return the result of the scheduling.
     */
    @Override
    protected InterSchedulerResult scheduleMixed(List<InstanceGroup> instanceGroups) {
        List<Datacenter> allDatacenters = simulation.getCollaborationManager().getDatacenters(collaborationId);
        InterSchedulerResult interSchedulerResult = new InterSchedulerResult(this, allDatacenters);

        instanceGroups.sort(new CustomComparator().reversed());

        List<InstanceGroup> sameInstanceGroups = new ArrayList<>();
        for (InstanceGroup group : instanceGroups) {
            if (!sameInstanceGroups.isEmpty() && !isSameRequestInstanceGroup(sameInstanceGroups.get(0), group)) {
                scheduleForSameInstanceGroupsMixed(sameInstanceGroups, interSchedulerResult, allDatacenters);

                sameInstanceGroups.clear();
                sameInstanceGroups.add(group);
            } else {
                sameInstanceGroups.add(group);
            }
        }

        if (!sameInstanceGroups.isEmpty()) {
            scheduleForSameInstanceGroupsMixed(sameInstanceGroups, interSchedulerResult, allDatacenters);
        }

        return interSchedulerResult;
    }

    /**
     * Schedule a batch of same instance groups to the hosts or the data centers by the scored hosts or the scored data centers.
     * @param sameInstanceGroups the same instance groups to be scheduled.
     * @param interSchedulerResult the result of the scheduling.
     * @param allDatacenters all the data centers.
     */
    private void scheduleForSameInstanceGroupsMixed(List<InstanceGroup> sameInstanceGroups, InterSchedulerResult interSchedulerResult, List<Datacenter> allDatacenters) {
        List<Datacenter> availableDatacenters = new ArrayList<>(allDatacenters);
        int hostNum = ((DetailedDcStateSimple) interScheduleSimpleStateMap.get(this.datacenter)).getHostNum();
        int randomStartIndex = random.nextInt(hostNum);
        int scoredHostNum = Math.min(sameInstanceGroups.size() * scoredHostNumForSameInstanceGroup, hostNum);
        Instance sameInstance = sameInstanceGroups.get(0).getInstances().get(0);
        ScoredHostsManager scoredHostsManager = new ScoredHostsManager(scoreHostHistoryMap);

        scoreHostInDatacenter(sameInstance, this.datacenter, randomStartIndex, hostNum, scoredHostNum, scoredHostsManager);

        List<InstanceGroup> forwardInstanceGroups = scheduleSameInstanceGroupsByScoredHostsMix(sameInstanceGroups, interSchedulerResult, scoredHostsManager);

        if (!forwardInstanceGroups.isEmpty()) {
            availableDatacenters.remove(this.datacenter);
            removeHistoryForwardDatacenter(availableDatacenters, forwardInstanceGroups.get(0));
            scheduleForSameInstanceGroupsToDc(forwardInstanceGroups, interSchedulerResult, availableDatacenters);
        }
    }

    /**
     * Remove the history forward data center from the available data centers.
     * @param allDatacenters all the data centers.
     * @param instanceGroup the instance group.
     */
    private void removeHistoryForwardDatacenter(List<Datacenter> allDatacenters, InstanceGroup instanceGroup){
        for(Integer dcId : instanceGroup.getForwardDatacenterIdsHistory()){
            Datacenter dc = simulation.getCollaborationManager().getDatacenterById(dcId);
            allDatacenters.remove(dc);
        }
    }

    /**
     * Schedule a batch of same instance groups to the hosts by the scored hosts and return the instance groups that need to be forwarded.
     * @param sameInstanceGroups the same instance groups to be scheduled.
     * @param interSchedulerResult the result of the scheduling.
     * @param scoredHostsManager the scored hosts.
     * @return the instance groups that need to be forwarded.
     */
    private List<InstanceGroup> scheduleSameInstanceGroupsByScoredHostsMix(List<InstanceGroup> sameInstanceGroups, InterSchedulerResult interSchedulerResult, ScoredHostsManager scoredHostsManager) {
        List<InstanceGroup> forwardInstanceGroups = new ArrayList<>();
        for (InstanceGroup instanceGroup : sameInstanceGroups) {
            ScoredHost scoredHost = scoredHostsManager.pollBestScoreHost();
            if(scoredHost == null){
                if(instanceGroup.getForwardDatacenterIdsHistory().size()<3){
                    forwardInstanceGroups.add(instanceGroup);
                }else{
                    interSchedulerResult.addFailedInstanceGroup(instanceGroup);
                }
            }else{
                markScheduledInstance(interSchedulerResult, scoredHostsManager, instanceGroup, scoredHost);
            }
        }
        return forwardInstanceGroups;
    }

    /**
     * Mark the scheduled data center to the instance group and update the state of the data center.
     * Mark the scheduled host to the instance group and update the state of the host.
     * @param interSchedulerResult the result of the scheduling.
     * @param scoredHostsManager the scored hosts.
     * @param instanceGroup the instance group.
     * @param scoredHost the scored host.
     */
    private void markScheduledInstance(InterSchedulerResult interSchedulerResult, ScoredHostsManager scoredHostsManager, InstanceGroup instanceGroup, ScoredHost scoredHost) {
        Datacenter scheduledDatacenter = scoredHost.getDatacenter();
        int scheduledHostId = scoredHost.getHostId();
        Instance instance = instanceGroup.getInstances().get(0);

        interSchedulerResult.addDcResult(instanceGroup, scheduledDatacenter);
        instance.setExpectedScheduleHostId(scheduledHostId);
        DetailedDcStateSimple detailedDcStateSimple = (DetailedDcStateSimple) (interScheduleSimpleStateMap.get(scheduledDatacenter));
        detailedDcStateSimple.allocate(instance, scheduledHostId);
        scoreHostHistoryMap.get(scheduledDatacenter).remove(scheduledHostId);

        double score = getScoreForHost(instance, scheduledHostId, scheduledDatacenter, detailedDcStateSimple);
        if(score!=-1){
            scoredHostsManager.addScoredHost(scheduledHostId, scheduledDatacenter, score);
        }
    }

    /**
     * A comparator used to compare whether groups of instances are the same.
     * It assumes that there is only one instance per instance group.
     * TODO：We need to consider the case where there are multiple instances in the instance group.
     */
    class CustomComparator implements Comparator<InstanceGroup> {
        @Override
        public int compare(InstanceGroup group1, InstanceGroup group2) {
            Instance instance1 = group1.getInstances().get(0);
            Instance instance2 = group2.getInstances().get(0);

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

            int result4 = instance1.getBw() - instance2.getBw();
            if (result4 != 0) {
                return result4;
            }

            List<Integer> forwardDatacenterIds1 = group1.getForwardDatacenterIdsHistory();
            List<Integer> forwardDatacenterIds2 = group2.getForwardDatacenterIdsHistory();

            boolean isForwardSame = forwardDatacenterIds1.contains(forwardDatacenterIds2)&&forwardDatacenterIds2.contains(forwardDatacenterIds1);
            if(!isForwardSame){
                return compareListsElementByElement(forwardDatacenterIds1, forwardDatacenterIds2);
            }

            return 0;
        }

        /**
         * Compare two lists element by element.
         * @param list1 the first list.
         * @param list2 the second list.
         * @return the comparison result.
         */
        private int compareListsElementByElement(List<Integer> list1, List<Integer> list2) {
            if(list1.size()!=list2.size()){
                return list1.size()-list2.size();
            }
            List<Integer> sortedList1 = new ArrayList<>(list1);
            List<Integer> sortedList2 = new ArrayList<>(list2);
            Collections.sort(sortedList1);
            Collections.sort(sortedList2);
            for (int i = 0; i < sortedList1.size(); i++) {
                int elementComparison = Integer.compare(sortedList1.get(i), sortedList2.get(i));
                if (elementComparison != 0) {
                    return elementComparison;
                }
            }
            return 0;
        }
    }
}
