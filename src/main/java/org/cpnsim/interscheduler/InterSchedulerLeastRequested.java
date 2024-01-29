package org.cpnsim.interscheduler;

import org.cpnsim.core.Simulation;
import org.cpnsim.datacenter.Datacenter;
import org.cpnsim.network.NetworkTopology;
import org.cpnsim.request.Instance;
import org.cpnsim.request.InstanceGroup;
import org.cpnsim.request.InstanceGroupGraph;
import org.cpnsim.statemanager.DetailedDcStateSimple;
import org.cpnsim.statemanager.HostState;
import org.cpnsim.statemanager.SimpleStateEasyObject;
import org.cpnsim.util.ScoredDatacentersManager;
import org.cpnsim.util.ScoredDc;
import org.cpnsim.util.ScoredHost;
import org.cpnsim.util.ScoredHostsManager;

import java.util.*;

public class InterSchedulerLeastRequested extends InterSchedulerSimple {
    Map<Datacenter, Map<Integer, Double>> scoreHostHistoryMap = new HashMap<>();
    Map<Datacenter, Double> scoreDcHistoryMap = new HashMap<>();
    int scoredHostNumForSameInstanceGroup = 100;
    int scoredDcNumForSameInstanceGroup = 3;

    public InterSchedulerLeastRequested(int id, Simulation simulation, int collaborationId, int target, boolean isSupportForward) {
        super(id, simulation, collaborationId, target, isSupportForward);
    }

    @Override
    protected InterSchedulerResult scheduleToDatacenter(List<InstanceGroup> instanceGroups) {
        final List<Datacenter> allDatacenters = simulation.getCollaborationManager().getDatacenters(collaborationId);
        InterSchedulerResult interSchedulerResult = new InterSchedulerResult(collaborationId, target, isSupportForward, allDatacenters);

//        long allDatacentersAvailableCpuSum = allDatacenters.stream()
//                .mapToLong(dc -> ((SimpleStateEasyObject)interScheduleSimpleStateMap.get(dc)).getCpuAvailableSum()).sum();
//
//        for(InstanceGroup instanceGroup : instanceGroups){
//            long randomIndex = random.nextLong(allDatacentersAvailableCpuSum);
//
//            Datacenter dc = getDatacenterByIndex(randomIndex,allDatacenters);
//
//            if(dc == Datacenter.NULL){
//                interSchedulerResult.addFailedInstanceGroup(instanceGroup);
//            }else{
//                interSchedulerResult.addDcResult(instanceGroup, dc);
//                SimpleStateEasyObject simpleStateEasyObject = (SimpleStateEasyObject)interScheduleSimpleStateMap.get(dc);
//                simpleStateEasyObject.allocateResource(instanceGroup.getCpuSum(),instanceGroup.getRamSum(),instanceGroup.getStorageSum(),instanceGroup.getBwSum());
//            }
//        }

        instanceGroups.sort(new CustomComparator().reversed());

        List<InstanceGroup> sameInstanceGroups = new ArrayList<>();
        for (InstanceGroup group : instanceGroups) {
            if (sameInstanceGroups.size() != 0 && !isSameRequestInstanceGroup(sameInstanceGroups.get(0), group)) {
                scheduleForSameInstanceGroupsToDc(sameInstanceGroups, interSchedulerResult, allDatacenters);

                sameInstanceGroups.clear();
                sameInstanceGroups.add(group);
            } else {
                // 相同的InstanceGroup，加入当前数组
                sameInstanceGroups.add(group);
            }
        }

        if (sameInstanceGroups.size() != 0) {
            scheduleForSameInstanceGroupsToDc(sameInstanceGroups, interSchedulerResult, allDatacenters);
        }

        return interSchedulerResult;
    }

    private Datacenter getDatacenterByIndex(long randomIndex, List<Datacenter> allDatacenters){
        for(Datacenter dc : allDatacenters){
            randomIndex-=((SimpleStateEasyObject) (interScheduleSimpleStateMap.get(dc))).getCpuAvailableSum();
            if(randomIndex<0){
                return dc;
            }
        }
        return Datacenter.NULL;
    }

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

    private List<Datacenter> getAvailableDatacenterByNetworkLimit(InstanceGroup instanceGroup, List<Datacenter> allDatacenters, InterSchedulerResult interSchedulerResult) {
        List<Datacenter> availableDatacenters = new ArrayList<>(allDatacenters);
        filterAvailableDatacenterByAccessLatency(instanceGroup, availableDatacenters);
        filterAvailableDatacenterByEdgeDelayLimit(instanceGroup, availableDatacenters, interSchedulerResult);
        filterAvailableDatacenterByEdgeBwLimit(instanceGroup, availableDatacenters, interSchedulerResult);
        return availableDatacenters;
    }

    private void filterAvailableDatacenterByAccessLatency(InstanceGroup instanceGroup, List<Datacenter> availableDatacenters) {
        NetworkTopology networkTopology = simulation.getNetworkTopology();
        availableDatacenters.removeIf(dc -> networkTopology.getAccessLatency(instanceGroup.getUserRequest(), dc) > instanceGroup.getAccessLatency());
    }

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

    private Datacenter getPossibleScheduledDatacenter(InstanceGroup instanceGroup, InterSchedulerResult interSchedulerResult) {
        if (instanceGroup.getReceiveDatacenter() != Datacenter.NULL) {
            return instanceGroup.getReceiveDatacenter();
        } else {
            return interSchedulerResult.getScheduledDatacenter(instanceGroup);
        }
    }

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

    @Override
    protected InterSchedulerResult scheduleToHost(List<InstanceGroup> instanceGroups) {
        scoreHostHistoryMap.clear();
        List<Datacenter> allDatacenters = simulation.getCollaborationManager().getDatacenters(collaborationId);
        InterSchedulerResult interSchedulerResult = new InterSchedulerResult(collaborationId, target, allDatacenters);
        int allDatacentersHostLength = allDatacenters.stream()
                .mapToInt(dc -> ((DetailedDcStateSimple) (interScheduleSimpleStateMap.get(dc))).getHostNum())
                .sum();

        instanceGroups.sort(new CustomComparator().reversed());

        List<InstanceGroup> sameInstanceGroups = new ArrayList<>();
        for (InstanceGroup group : instanceGroups) {
            if (sameInstanceGroups.size() != 0 && !isSameRequestInstanceGroup(sameInstanceGroups.get(0), group)) {
                scheduleForSameInstanceGroupsToHost(sameInstanceGroups, interSchedulerResult, allDatacenters, allDatacentersHostLength);

                sameInstanceGroups.clear();
                sameInstanceGroups.add(group);
            } else {
                // 相同的InstanceGroup，加入当前数组
                sameInstanceGroups.add(group);
            }
        }

        if (sameInstanceGroups.size() != 0) {
            scheduleForSameInstanceGroupsToHost(sameInstanceGroups, interSchedulerResult, allDatacenters, allDatacentersHostLength);
        }

        return interSchedulerResult;
    }

    private boolean isSameRequestInstanceGroup(InstanceGroup group1, InstanceGroup group2) {
        if (group1.isNetworkLimited() || group2.isNetworkLimited()) {
            return false;
        } else {
            Instance instance1 = group1.getInstances().get(0);
            Instance instance2 = group2.getInstances().get(0);
            return instance1.getCpu() == instance2.getCpu() && instance1.getRam() == instance2.getRam() && instance1.getStorage() == instance2.getStorage() && instance1.getBw() == instance2.getBw();
        }
    }

    private void scheduleForSameInstanceGroupsToHost(List<InstanceGroup> sameInstanceGroups, InterSchedulerResult interSchedulerResult, List<Datacenter> allDatacenters, int allDatacentersHostLength) {
        int randomStartIndex = random.nextInt(allDatacentersHostLength);
        int scoredHostNum = Math.min(sameInstanceGroups.size() * scoredHostNumForSameInstanceGroup, allDatacentersHostLength);
        Instance sameInstance = sameInstanceGroups.get(0).getInstances().get(0);

        ScoredHostsManager scoredHostsManager = getScoredHostsManager(sameInstance, randomStartIndex, allDatacenters, scoredHostNum);

        scheduleSameInstanceGroupsByScoredHosts(sameInstanceGroups, interSchedulerResult, scoredHostsManager);
    }

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

    @Override
    protected InterSchedulerResult scheduleMixed(List<InstanceGroup> instanceGroups) {
        List<Datacenter> allDatacenters = simulation.getCollaborationManager().getDatacenters(collaborationId);
        InterSchedulerResult interSchedulerResult = new InterSchedulerResult(collaborationId, target, isSupportForward, allDatacenters);

        instanceGroups.sort(new CustomComparator().reversed());

        List<InstanceGroup> sameInstanceGroups = new ArrayList<>();
        for (InstanceGroup group : instanceGroups) {
            if (sameInstanceGroups.size() != 0 && !isSameRequestInstanceGroup(sameInstanceGroups.get(0), group)) {
                scheduleForSameInstanceGroupsMixed(sameInstanceGroups, interSchedulerResult, allDatacenters);

                sameInstanceGroups.clear();
                sameInstanceGroups.add(group);
            } else {
                // 相同的InstanceGroup，加入当前数组
                sameInstanceGroups.add(group);
            }
        }

        if (sameInstanceGroups.size() != 0) {
            scheduleForSameInstanceGroupsMixed(sameInstanceGroups, interSchedulerResult, allDatacenters);
        }

        return interSchedulerResult;
    }

    private void scheduleForSameInstanceGroupsMixed(List<InstanceGroup> sameInstanceGroups, InterSchedulerResult interSchedulerResult, List<Datacenter> allDatacenters) {
        List<Datacenter> availableDatacenters = new ArrayList<>(allDatacenters);
        int hostNum = ((DetailedDcStateSimple) interScheduleSimpleStateMap.get(this.datacenter)).getHostNum();
        int randomStartIndex = random.nextInt(hostNum);
        int scoredHostNum = Math.min(sameInstanceGroups.size() * scoredHostNumForSameInstanceGroup, hostNum);
        Instance sameInstance = sameInstanceGroups.get(0).getInstances().get(0);
        ScoredHostsManager scoredHostsManager = new ScoredHostsManager(scoreHostHistoryMap);

        scoreHostInDatacenter(sameInstance, this.datacenter, randomStartIndex, hostNum, scoredHostNum, scoredHostsManager);

        List<InstanceGroup> forwardInstanceGroups = scheduleSameInstanceGroupsByScoredHostsMix(sameInstanceGroups, interSchedulerResult, scoredHostsManager);

        if(forwardInstanceGroups.size() != 0){
            availableDatacenters.remove(this.datacenter);
            removeHistoryForwardDatacenter(availableDatacenters, forwardInstanceGroups.get(0));
            scheduleForSameInstanceGroupsToDc(forwardInstanceGroups, interSchedulerResult, availableDatacenters);
        }
    }

    private void removeHistoryForwardDatacenter(List<Datacenter> allDatacenters, InstanceGroup instanceGroup){
        for(Integer dcId : instanceGroup.getForwardDatacenterIdsHistory()){
            Datacenter dc = simulation.getCollaborationManager().getDatacenterById(dcId);
            allDatacenters.remove(dc);
        }
    }

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

        private int compareListsElementByElement(List<Integer> list1, List<Integer> list2) {
            if(list1.size()!=list2.size()){
                return list1.size()-list2.size();
            }
            List<Integer> sortedList1 = new ArrayList<>(list1);
            List<Integer> sortedList2 = new ArrayList<>(list2);
            Collections.sort(sortedList1);
            Collections.sort(sortedList2);
            // 逐元素比较
            for (int i = 0; i < sortedList1.size(); i++) {
                int elementComparison = Integer.compare(sortedList1.get(i), sortedList2.get(i));
                if (elementComparison != 0) {
                    return elementComparison;
                }
            }
            return 0; // 所有元素相同
        }
    }
}
