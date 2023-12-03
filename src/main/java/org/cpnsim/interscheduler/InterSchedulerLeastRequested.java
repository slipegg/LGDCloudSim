package org.cpnsim.interscheduler;

import lombok.Getter;
import lombok.Setter;
import org.cpnsim.core.Simulation;
import org.cpnsim.datacenter.Datacenter;
import org.cpnsim.request.Instance;
import org.cpnsim.request.InstanceGroup;
import org.cpnsim.statemanager.DetailedDcStateSimple;
import org.cpnsim.statemanager.HostState;

import java.util.*;

public class InterSchedulerLeastRequested extends InterSchedulerSimple {
    Map<Datacenter, Map<Integer, Double>> scoreHistoryMap = new HashMap<>();
    int scoredHostNumForSameInstanceGroup = 100;

    public InterSchedulerLeastRequested(int id, Simulation simulation, int collaborationId, int target, boolean isSupportForward) {
        super(id, simulation, collaborationId, target, isSupportForward);
    }

    @Override
    protected InterSchedulerResult scheduleToHost(List<InstanceGroup> instanceGroups) {
        scoreHistoryMap.clear();
        List<Datacenter> allDatacenters = simulation.getCollaborationManager().getDatacenters(collaborationId);
        InterSchedulerResult interSchedulerResult = new InterSchedulerResult(collaborationId, target, allDatacenters);
        int allDatacentersHostLength = allDatacenters.stream()
                .mapToInt(dc -> ((DetailedDcStateSimple) (interScheduleSimpleStateMap.get(dc))).getHostNum())
                .sum();

        instanceGroups.sort(new CustomComparator().reversed());

        List<InstanceGroup> sameInstanceGroups = new ArrayList<>();
        for (InstanceGroup group : instanceGroups) {
            if (sameInstanceGroups.size() != 0 && !isSameInstanceGroup(sameInstanceGroups.get(0), group)) {
                scheduleForSameInstanceGroups(sameInstanceGroups, interSchedulerResult, allDatacenters, allDatacentersHostLength);

                sameInstanceGroups.clear();
                sameInstanceGroups.add(group);
            } else {
                // 相同的InstanceGroup，加入当前数组
                sameInstanceGroups.add(group);
            }
        }

        if (sameInstanceGroups.size() != 0) {
            scheduleForSameInstanceGroups(sameInstanceGroups, interSchedulerResult, allDatacenters, allDatacentersHostLength);
        }

        return interSchedulerResult;
    }

    private boolean isSameInstanceGroup(InstanceGroup group1, InstanceGroup group2) {
        Instance instance1 = group1.getInstances().get(0);
        Instance instance2 = group2.getInstances().get(0);
        return instance1.getCpu() == instance2.getCpu() && instance1.getRam() == instance2.getRam() && instance1.getStorage() == instance2.getStorage() && instance1.getBw() == instance2.getBw();
    }

    @Getter
    @Setter
    static
    class ScoreHost{
        Datacenter datacenter;
        int hostId;
        double score;

        public ScoreHost(Datacenter datacenter, int hostId, double score){
            this.datacenter = datacenter;
            this.hostId = hostId;
            this.score = score;
        }
    }

    class ScoredHostsManager {
        PriorityQueue<ScoreHost> scoreHostPriorityQueue = new PriorityQueue<>(Comparator.comparingDouble(ScoreHost::getScore).reversed());
        Map<Datacenter, Map<Integer, Double>> scoreHistoryMap;

        public ScoredHostsManager(Map<Datacenter, Map<Integer, Double>> scoreHistoryMap){
            this.scoreHistoryMap = scoreHistoryMap;
        }

        public void filterAndScoreHost(Instance instance, int hostId, Datacenter datacenter, DetailedDcStateSimple detailedDcStateSimple){
            double score = getScoreForHost(instance, hostId, datacenter, detailedDcStateSimple);
            if(score == -1){
                return;
            }
            scoreHostPriorityQueue.add(new ScoreHost(datacenter, hostId, score));
        }

        public int getScoredHostNum(){
            return scoreHostPriorityQueue.size();
        }

        public ScoreHost pollBestScoreHost(){
            return scoreHostPriorityQueue.poll();
        }

        private double getScoreForHost(Instance instance, int hostId, Datacenter datacenter, DetailedDcStateSimple detailedDcStateSimple) {
            HostState hostState = detailedDcStateSimple.getHostState(hostId);
            if (!hostState.isSuitable(instance)) {
                return -1;
            } else {
                if(scoreHistoryMap.containsKey(datacenter) && scoreHistoryMap.get(datacenter).containsKey(hostId)){
                    return scoreHistoryMap.get(datacenter).get(hostId);
                }else{
                    int cpuCapacity = detailedDcStateSimple.getHostCapacity(hostId)[0];
                    int ramCapacity = detailedDcStateSimple.getHostCapacity(hostId)[1];
                    double score = (hostState.getCpu() * 10 / (double) cpuCapacity + hostState.getRam() * 10 / (double) ramCapacity) / 2;
                    scoreHistoryMap.computeIfAbsent(datacenter, k -> new HashMap<>()).put(hostId, score);
                    return score;
                }
            }
        }
    }

    private void scheduleForSameInstanceGroups(List<InstanceGroup> sameInstanceGroups, InterSchedulerResult interSchedulerResult, List<Datacenter> allDatacenters, int allDatacentersHostLength) {
        int randomStartIndex = random.nextInt(allDatacentersHostLength);
        int scoredHostNum = Math.min(sameInstanceGroups.size() * scoredHostNumForSameInstanceGroup, allDatacentersHostLength);
        Instance sameInstance = sameInstanceGroups.get(0).getInstances().get(0);

        ScoredHostsManager scoredHostsManager = getScoredHosts(sameInstance, randomStartIndex, allDatacenters, scoredHostNum);

        scheduleSameInstanceGroupByScoredHosts(sameInstanceGroups, interSchedulerResult, scoredHostsManager);
    }

    private ScoredHostsManager getScoredHosts(Instance sameInstance, int randomStartIndex, List<Datacenter> allDatacenters, int scoredHostNum) {
        int dcStartIndex = getDcIdByHostIdInAll(randomStartIndex, allDatacenters);
        if (dcStartIndex == -1) {
            LOGGER.warning("return dcId = -1 in getDcIdByHostIdInAll");
            throw new RuntimeException("return dcId = -1 in getDcIdByHostIdInAll");
        }

        int startHostIndexInDc = randomStartIndex;
        for(int i = 0; i < dcStartIndex; i++){
            Datacenter dc = allDatacenters.get(i);
            startHostIndexInDc -= ((DetailedDcStateSimple)(interScheduleSimpleStateMap.get(dc))).getHostNum();
        }

        ScoredHostsManager scoredHostsManager = new ScoredHostsManager(scoreHistoryMap);
        for (int i = 0; i < allDatacenters.size(); i++) {
            int dcIndex = (dcStartIndex + i) % allDatacenters.size();
            Datacenter dcSelected = allDatacenters.get(dcIndex);

            scoreHostInDatacenter(sameInstance, dcSelected, startHostIndexInDc, scoredHostNum, scoredHostsManager);
            if(scoredHostsManager.getScoredHostNum() >= scoredHostNum){
                break;
            }

            startHostIndexInDc = 0;
        }

        return scoredHostsManager;
    }

    private void scoreHostInDatacenter(Instance instance, Datacenter dc, int startHostIndexInDc, int scoredHostNum, ScoredHostsManager scoredHostsManager) {
        DetailedDcStateSimple detailedDcStateSimple = (DetailedDcStateSimple) interScheduleSimpleStateMap.get(dc);
        for (int hostId = startHostIndexInDc; hostId < detailedDcStateSimple.getHostNum(); hostId++) {
            traversalTime+=1;
            scoredHostsManager.filterAndScoreHost(instance, hostId, dc, detailedDcStateSimple);

            if(scoredHostsManager.getScoredHostNum() >= scoredHostNum){
                break;
            }
        }
    }

    private void scheduleSameInstanceGroupByScoredHosts(List<InstanceGroup> sameInstanceGroups, InterSchedulerResult interSchedulerResult, ScoredHostsManager scoredHostsManager) {
        for (InstanceGroup instanceGroup : sameInstanceGroups) {
            Instance instance = instanceGroup.getInstances().get(0);
            ScoreHost scoreHost = scoredHostsManager.pollBestScoreHost();
            if(scoreHost == null){
                interSchedulerResult.addFailedInstanceGroup(instanceGroup);
                continue;
            }

            Datacenter scheduledDatacenter = scoreHost.getDatacenter();
            int scheduledHostId = scoreHost.getHostId();

            interSchedulerResult.addDcResult(instanceGroup, scheduledDatacenter);
            instance.setExpectedScheduleHostId(scheduledHostId);
            DetailedDcStateSimple detailedDcStateSimple = (DetailedDcStateSimple) (interScheduleSimpleStateMap.get(scheduledDatacenter));
            detailedDcStateSimple.allocate(instance, scheduledHostId);
            scoreHistoryMap.get(scheduledDatacenter).remove(scheduledHostId);

            scoredHostsManager.filterAndScoreHost(instance, scheduledHostId, scheduledDatacenter, detailedDcStateSimple);
        }
    }
}

// 自定义比较器
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

        return instance1.getBw() - instance2.getBw();
    }
}
