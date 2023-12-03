package org.cpnsim.innerscheduler;

import org.cpnsim.request.Instance;
import org.cpnsim.statemanager.HostState;
import org.cpnsim.statemanager.SynState;
import org.cpnsim.util.ScoredHost;
import org.cpnsim.util.ScoredHostsManager;

import java.util.*;

public class InnerSchedulerLeastRequested extends InnerSchedulerSimple{
    Map<Integer, Double> scoreHostHistoryMap = new HashMap<>();
    int scoredHostNumForSameInstance = 100;

    Random random = new Random();

    public InnerSchedulerLeastRequested(int id, int firstPartitionId, int partitionNum) {
        super(id, firstPartitionId, partitionNum);
    }


    //        for (Instance instance : instances) {
//            int suitId = -1;
//
//            int synPartitionId = firstPartitionId;
//            if (datacenter.getStatesManager().isSynCostTime()) {
//                synPartitionId = (firstPartitionId + datacenter.getStatesManager().getSmallSynGapCount()) % partitionNum;
//            }
//            for (int p = 0; p < partitionNum; p++) {
//                int[] range = datacenter.getStatesManager().getPartitionRangesManager().getRange((synPartitionId + p) % partitionNum);
//                for (int i = range[0]; i <= range[1]; i++) {
//                    if (synState.isSuitable(i, instance)) {
//                        suitId = i;
//                        break;
//                    }
//                }
//                if (suitId != -1) {
//                    break;
//                }
//            }
//            if (suitId != -1) {
//                synState.allocateTmpResource(suitId, instance);
//                instance.setExpectedScheduleHostId(suitId);
//                innerSchedulerResult.addScheduledInstance(instance);
//            } else {
//                innerSchedulerResult.addFailedScheduledInstance(instance);
//            }
//        }
    @Override
    protected InnerSchedulerResult scheduleInstances(List<Instance> instances, SynState synState) {
        scoreHostHistoryMap.clear();
        InnerSchedulerResult innerSchedulerResult = new InnerSchedulerResult(this, getDatacenter().getSimulation().clock());

        instances.sort(new CustomComparator().reversed());

        List<Instance> sameInstance = new ArrayList<>();
        for(Instance instance : instances){
            if(sameInstance.size()!=0 && !isSameRequestInstance(sameInstance.get(0), instance)){
                scheduleForSameInstancesToHost(sameInstance, innerSchedulerResult, synState);

                sameInstance.clear();
                sameInstance.add(instance);
            }else{
                sameInstance.add(instance);
            }
        }

        if(sameInstance.size()>0){
            scheduleForSameInstancesToHost(sameInstance, innerSchedulerResult, synState);
        }

        return innerSchedulerResult;
    }

    private boolean isSameRequestInstance(Instance instance1, Instance instance2){
        return instance1.getCpu() == instance2.getCpu() && instance1.getRam() == instance2.getRam() && instance1.getStorage() == instance2.getStorage() && instance1.getBw() == instance2.getBw();
    }

    private void scheduleForSameInstancesToHost(List<Instance> sameInstances, InnerSchedulerResult innerSchedulerResult, SynState synState){
        int hostNum = datacenter.getStatesManager().getHostNum();
        int randomStartIndex = random.nextInt(hostNum);
        int scoredHostNum = Math.min(sameInstances.size() * scoredHostNumForSameInstance, hostNum);
        Instance sameInstance = sameInstances.get(0);

        ScoredHostsManager scoredHostsManager = getScoredHostsManager(sameInstance, randomStartIndex, scoredHostNum, synState);

        scheduleSameInstancesByScoredHosts(sameInstances, scoredHostsManager, innerSchedulerResult, synState);
    }

    private ScoredHostsManager getScoredHostsManager(Instance instance, int randomStartIndex, int scoredHostNum, SynState synState){
        ScoredHostsManager scoredHostsManager = new ScoredHostsManager(new HashMap<>(Map.of(datacenter, scoreHostHistoryMap)));
        int hostNum = datacenter.getStatesManager().getHostNum();
        for(int i=0; i<hostNum; i++){
            int hostId = (randomStartIndex + i) % hostNum;
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

    private double getScoreForHost(Instance instance, int hostId, SynState synState){
        HostState hostState = synState.getHostState(hostId);
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

    private void scheduleSameInstancesByScoredHosts(List<Instance> sameInstances, ScoredHostsManager scoredHostsManager, InnerSchedulerResult innerSchedulerResult, SynState synState){
        for(Instance instance : sameInstances){
            ScoredHost scoredHost = scoredHostsManager.pollBestScoreHost();
            if(scoredHost == null){
                innerSchedulerResult.addFailedScheduledInstance(instance);
            }else{
                int scheduledHostId = scoredHost.getHostId();
                instance.setExpectedScheduleHostId(scheduledHostId);
                innerSchedulerResult.addScheduledInstance(instance);
                synState.allocateTmpResource(scheduledHostId, instance);
                scoreHostHistoryMap.remove(scheduledHostId);

                double score = getScoreForHost(instance, scheduledHostId, synState);
                if(score!=-1){
                    scoredHostsManager.addScoredHost(scheduledHostId, datacenter, score);
                }
            }
        }
    }

    // 自定义比较器
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
