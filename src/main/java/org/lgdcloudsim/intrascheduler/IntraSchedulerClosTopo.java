package org.lgdcloudsim.intrascheduler;

import org.lgdcloudsim.datacenter.DatacenterSimple;
import org.lgdcloudsim.network.ClosTopology;
import org.lgdcloudsim.request.Instance;
import org.lgdcloudsim.request.InstanceGroup;
import org.lgdcloudsim.request.InstanceTopology;
import org.lgdcloudsim.request.TrainingStrategy;
import org.lgdcloudsim.statemanager.SynState;
import org.lgdcloudsim.util.Range;
import org.lgdcloudsim.util.utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * The random intra-scheduler that extends the {@link IntraSchedulerSimple} class.
 * It will filter the suitable host from the first host id to the last host id.
 *
 * @author Anonymous
 * @since LGDCloudSim 1.0
 */
public class IntraSchedulerClosTopo extends IntraSchedulerSimple {
    /**
     * The random object.
     */
    Random random = new Random();

    public Logger LOGGER = LoggerFactory.getLogger(DatacenterSimple.class.getSimpleName());

    /**
     * Construct the intra-scheduler with the id, the first partition id and the partition number.
     *
     * @param id               the intra-scheduler id.
     * @param firstPartitionId the first synchronization partition id.
     * @param partitionNum     the number of partitions in the data center.
     */
    public IntraSchedulerClosTopo(int id, int firstPartitionId, int partitionNum) {
        super(id, firstPartitionId, partitionNum);
    }

    private ClosTopology filterForS0Topology(ClosTopology topology, SynState synState, Instance instance, int replicate, boolean isFirst) {
        ClosTopology resTopology = null;
        if (topology.isLeafTopology()) {
            if (isFirst) {
                Range hostRange = topology.getRange();
                int suitedReplicate = 0;
                for (int i = hostRange.getMin(); i <= hostRange.getMax(); i++) {
                    int suitNum = synState.suitableReplicateNum(i, instance);
                    if (suitNum > 0) {
                        topology.AddHost(i, suitNum);
                    }
                    suitedReplicate += suitNum;
                }
                if (suitedReplicate >= replicate) {
                    resTopology = topology;
                }
            } else {
                int suitedReplicate = topology.getCandidateReplicateSum();
                if (suitedReplicate >= replicate) {
                    resTopology = topology;
                }
            }
        } else {
            for (ClosTopology subTopology : topology.getSortedSubTopologies(true)) {
                resTopology = filterForS0Topology(subTopology, synState, instance, replicate, isFirst);
                if (resTopology != null) {
                    break;
                }
            }
        }
        return resTopology;
    }

    private ClosTopology filterForAcrossTopology(ClosTopology topology, SynState synState, Instance instance, int replicate, int groupLevel) {
        ClosTopology resTopology = null;
        if (topology.getLevel() == groupLevel) {
            int suitableReplicateSum = topology.getCandidateReplicateSum();
            if (suitableReplicateSum >= replicate) {
                resTopology = topology;
            }
        } else if (topology.getLevel() > groupLevel) {
            for (ClosTopology subTopology : topology.getSortedSubTopologies(true)) {
                resTopology = filterForAcrossTopology(subTopology, synState, instance, replicate, groupLevel);
                if (resTopology != null) {
                    break;
                }
            }
        }
        return resTopology;
    }

    private ClosTopology scheduleForSubInstance(Instance instance, int instanceReplicate, ClosTopology closTopology, SynState synState, boolean isFirst) {
        ClosTopology resTopology = null;
        // 先尝试同S0放置
        resTopology = filterForS0Topology(closTopology, synState, instance, instanceReplicate, isFirst);

        // 同S0放不下，需要跨机放置
        if (resTopology == null) {
            //在同S1下放置
            resTopology = filterForAcrossTopology(closTopology, synState, instance, instanceReplicate, 1);

            if (resTopology == null) {
                //在同S2下放置
                resTopology = filterForAcrossTopology(closTopology, synState, instance, instanceReplicate, 2);
            }

            if (resTopology == null) {
                // TODO: 只有不需要rdma通信的才可以跨S2放置
                // 跨S2放置
                resTopology = filterForAcrossTopology(closTopology, synState, instance, instanceReplicate, 3);
            }
        }

        return resTopology;
    }

    private ClosTopology getBestS0TopologyForInstance(ClosTopology rootTopology, Instance instance) {
        int rootLevel = rootTopology.getLevel();
        ClosTopology s0Topology = rootTopology;
        for (; rootLevel > 0; rootLevel--) {
            // 注意这里就是找最好的了
            List<ClosTopology> subTopologies = s0Topology.getSortedSubTopologies(false);
            for (ClosTopology subTopology : subTopologies) {
                if (subTopology.getCandidateReplicateSum() > 0) {
                    s0Topology = subTopology;
                    break;
                }
            }
        }
        return s0Topology;
    }

    private void recordScheduledResult(ClosTopology closTopology, Instance instance, int hostID, SynState synState, IntraSchedulerResult intraSchedulerResult) {
        int originalGPU = synState.getHostState(hostID).getGpu();
        closTopology.updateByAllocate(hostID);
        closTopology.setSpecialHostID(-1);
        closTopology.updateHostTopologyScore(originalGPU, originalGPU-instance.getGpu());
        synState.allocateTmpResource(hostID, instance);
        instance.setExpectedScheduleHostId(hostID);
        intraSchedulerResult.addScheduledInstance(instance);

        LOGGER.info("{}: IntraSchedulerClosTopo scheduled instance-{} to host-{} in {}.", getDatacenter().getSimulation().clockStr(), instance.getId(), hostID, closTopology);
    }

    private void scheduleForP2P(InstanceTopology instanceTopology, ClosTopology rootTopology, SynState synState, IntraSchedulerResult intraSchedulerResult) {
        Queue<Instance> instanceQueue = new LinkedList<>();
        Map<Instance, ClosTopology> instanceToS0Map = new HashMap<>();
        Instance maxScoreInstance = instanceTopology.getMaxScoreInstance();
        if (maxScoreInstance != null) {
            instanceQueue.offer(instanceTopology.getMaxScoreInstance());
        }

        while (instanceQueue.size() > 0) {
            Instance instanceToSchedule = instanceQueue.poll();
            List<Instance> linkedInstances = instanceTopology.getLinkedInstances(instanceToSchedule);
            Instance scheduledInstance = null;
            for (Instance linkedInstance : linkedInstances) {
                if (linkedInstance.getExpectedScheduleHostId() < 0) {
                    instanceQueue.offer(linkedInstance);
                } else {
                    scheduledInstance = linkedInstance;
                }
            }

            if (scheduledInstance == null) { // 没有调度好的关联实例，自己去找拓扑得分最高的进行调度
                ClosTopology s0Topology = getBestS0TopologyForInstance(rootTopology, instanceToSchedule);
                int hostID = s0Topology.getMostCandidateReplicateHostID();
                
                recordScheduledResult(s0Topology, instanceToSchedule, hostID, synState, intraSchedulerResult);
                instanceToS0Map.put(instanceToSchedule, s0Topology);
            } else { // 找与已调度好的实例的最近拓扑进行调度
                ClosTopology scheduledInstanceTopology = instanceToS0Map.get(scheduledInstance);
                ClosTopology closedTopology = scheduledInstanceTopology.getClosestHosts(scheduledInstance.getExpectedScheduleHostId(), rootTopology.getLevel());
                int hostID = closedTopology.getSpecialHostID();
                recordScheduledResult(closedTopology, instanceToSchedule, hostID, synState, intraSchedulerResult);
                instanceToS0Map.put(instanceToSchedule, closedTopology);
            }
        }
    }

    private int scheduleSpread(ClosTopology rootTopology, int replicateNum, int level, Map<ClosTopology, List<Integer>> scheduledHostIDs) {
        if (replicateNum<=0) {
            return 0;
        }
        if (level == 0) {
            Map<Integer, Integer> candidateReplicateMap = rootTopology.getCandidateReplicateMap();
            scheduledHostIDs.putIfAbsent(rootTopology, new ArrayList<>());
            int scheduledNum = 0;
            for (Map.Entry<Integer, Integer> entry : candidateReplicateMap.entrySet()) {
                int hostID = entry.getKey();
                int suitedReplicateNum = entry.getValue();
                for (int i=0; i < suitedReplicateNum; i++) {
                    scheduledHostIDs.get(rootTopology).add(hostID);
                    scheduledNum++;
                    if (scheduledNum >= replicateNum) {
                        return scheduledNum;
                    }
                }
            }
            return scheduledNum;
        } else {
            List<ClosTopology> subTopologies = rootTopology.getSortedSubTopologies(true);
            int scheduledSum = 0;
            for (ClosTopology subTopology : subTopologies) {
                if (replicateNum <= 0) {
                    break;
                }
                int scheduledNum = scheduleSpread(subTopology, replicateNum, level - 1, scheduledHostIDs);
                replicateNum -= scheduledNum;
                scheduledSum += scheduledNum;
            }
            return scheduledSum;
        }
    }
    
    private void scheduleForAll2All(InstanceTopology instanceTopology, ClosTopology rootTopology, SynState synState, IntraSchedulerResult intraSchedulerResult) {
        List<Instance> allInstances = instanceTopology.getAllInstances();
        Map<ClosTopology, List<Integer>> scheduledHostIDs = new HashMap<>();
        scheduleSpread(rootTopology, allInstances.size(), rootTopology.getLevel(), scheduledHostIDs);

        int scheduledInstanceNum = 0;
        for (Map.Entry<ClosTopology, List<Integer>> entry : scheduledHostIDs.entrySet()) {
            ClosTopology closTopology = entry.getKey();
            List<Integer> hostIDs = entry.getValue();
            for (int hostID : hostIDs) {
                Instance instance = allInstances.get(scheduledInstanceNum);
                recordScheduledResult(closTopology, instance, hostID, synState, intraSchedulerResult);
                scheduledInstanceNum++;
            }
        }
        if (scheduledInstanceNum < allInstances.size()) {
            throw new RuntimeException("All2All Topology scheduling error: scheduled hosts less than all instances.");
        }
    }

    private void scheduleForSubInstanceTopology(InstanceTopology instanceTopology, ClosTopology rootTopology, SynState synState, IntraSchedulerResult intraSchedulerResult, boolean isOnlyOneSubInstanceTopology) {
        List<Instance> scheduledInstances = instanceTopology.getAllInstances();
        ClosTopology preferTopology = rootTopology;
        if (!isOnlyOneSubInstanceTopology) {
            LOGGER.info("{}: IntraSchedulerClosTopo scheduling sub-instance topology {} of instanceGroup-{}, beacuse there is not only one sub-instance topology.", getDatacenter().getSimulation().clockStr(), instanceTopology, instanceTopology.getTrainingStrategy().getInstanceGroup().getId());
            preferTopology = scheduleForSubInstance(scheduledInstances.get(0), scheduledInstances.size(), rootTopology, synState, false);
            if (preferTopology == null) {
                throw new RuntimeException(String.format("IntraSchedulerClosTopo failed to schedule sub-instance topology %s, but the pre filter is ok.", instanceTopology));   
            }
        }

        LOGGER.info("{}: IntraSchedulerClosTopo scheduling sub-instance topology {} of instanceGroup-{} on clos topology {}.", getDatacenter().getSimulation().clockStr(), instanceTopology, instanceTopology.getTrainingStrategy().getInstanceGroup().getId(), preferTopology);
        switch (instanceTopology.getType()) {
            case InstanceTopology.P2P:
                // Handle P2P topology scheduling
                scheduleForP2P(instanceTopology, preferTopology, synState, intraSchedulerResult);
                break;
            case InstanceTopology.All2All:
                // Handle All2All topology scheduling
                scheduleForAll2All(instanceTopology, preferTopology, synState, intraSchedulerResult);
                break;
            default:
                break;
        }
    }

    private void scheduleForInstanceGroupWithTopology(InstanceGroup instanceGroup, SynState synState, IntraSchedulerResult intraSchedulerResult) {
        List<Instance> instances = instanceGroup.getInstances();
        Instance instance = instances.get(0);
        Integer instanceReplicate = instances.size();

        ClosTopology resTopology = scheduleForSubInstance(instance, instanceReplicate, synState.getClosTopology(), synState, true);
        if (resTopology == null) {
            // 调度失败
            for (Instance inst : instances) {
                intraSchedulerResult.addFailedScheduledInstance(inst);
            }
            LOGGER.info("{}: IntraSchedulerClosTopo failed to schedule instance group-{} due to insufficient resources.", getDatacenter().getSimulation().clockStr(), instanceGroup.getId());
            return;
        } else {
            LOGGER.info("{}: IntraSchedulerClosTopo found suitable resource in {} for instance group-{}.", getDatacenter().getSimulation().clockStr(), resTopology, instanceGroup.getId());
        }

        // 资源是足够的，进入优选阶段
        TrainingStrategy trainingStrategy = instanceGroup.getTrainingStrategy();
        Map<Integer, List<InstanceTopology>> instanceTopologyMap = trainingStrategy.getInstanceTopologyMap();
        boolean isOnlyOneSubInstanceTopology = trainingStrategy.isOnlyOneSubInstanceTopology();
        for (List<InstanceTopology> topologyList : instanceTopologyMap.values()) {
            for (InstanceTopology instanceTopology : topologyList) {
                scheduleForSubInstanceTopology(instanceTopology, resTopology, synState, intraSchedulerResult, isOnlyOneSubInstanceTopology);
            }
        }
    }

    private int scheduleForInstancesWithoutTopology(Instance instance, int replicate, int scheduledNum, SynState synState, ClosTopology closTopology, Map<ClosTopology, List<Integer>> scheduledClosTopologyHostIDsMap) {
        int newScheduledNum = 0;
        if (closTopology.isLeafTopology()) {
            Range hostRange = closTopology.getRange();
            for (int i = hostRange.getMin(); i <= hostRange.getMax(); i++) {
                int suitNum = synState.suitableReplicateNum(i, instance);
                for (int j = 0; j < suitNum; j++) {
                    scheduledClosTopologyHostIDsMap.putIfAbsent(closTopology, new ArrayList<>());
                    scheduledClosTopologyHostIDsMap.get(closTopology).add(i);
                    newScheduledNum++;
                    if (newScheduledNum + scheduledNum >= replicate) {
                        return newScheduledNum;
                    }
                }
            }
        } else {
            for (ClosTopology subTopology : closTopology.getSortedSubTopologies(true)) {
                newScheduledNum += scheduleForInstancesWithoutTopology(instance, replicate, scheduledNum+newScheduledNum, synState, subTopology, scheduledClosTopologyHostIDsMap);
                if (newScheduledNum + scheduledNum >= replicate) {
                    return newScheduledNum;
                }
            }
        }
        return newScheduledNum;
    }

    private void scheduleForInstanceGroupWithoutTopology(InstanceGroup instanceGroup, SynState synState, IntraSchedulerResult intraSchedulerResult) {
        Instance instance = instanceGroup.getInstances().get(0);
        int replicate = instanceGroup.getInstances().size();
        ClosTopology rootClosTopology = synState.getClosTopology();
        Map<ClosTopology, List<Integer>> scheduledClosTopologyHostIDsMap = new HashMap<>();
        int scheduledNum = scheduleForInstancesWithoutTopology(instance, replicate, 0, synState, rootClosTopology, scheduledClosTopologyHostIDsMap);
        if (scheduledNum < replicate) {
            // 调度失败
            for (Instance inst : instanceGroup.getInstances()) {
                intraSchedulerResult.addFailedScheduledInstance(inst);
            }
            LOGGER.info("{}: IntraSchedulerClosTopo failed to schedule instance group-{} due to insufficient resources.", getDatacenter().getSimulation().clockStr(), instanceGroup.getId());
            return;
        } else if (scheduledNum == replicate) {
            LOGGER.info("{}: IntraSchedulerClosTopo found suitable resource instance group-{} without topology.", getDatacenter().getSimulation().clockStr(), instanceGroup.getId());
            int index = 0;
            for (Map.Entry<ClosTopology, List<Integer>> entry : scheduledClosTopologyHostIDsMap.entrySet()) {
                ClosTopology closTopology = entry.getKey();
                List<Integer> hostIDs = entry.getValue();
                for (int hostID : hostIDs) {
                    Instance inst = instanceGroup.getInstances().get(index);
                    recordScheduledResult(closTopology, inst, hostID, synState, intraSchedulerResult);
                    index++;
                }
            }
        } else {
            throw new RuntimeException("Scheduling error: scheduled hosts more than required instances.");
        }
    }


    /**
     * Schedule the instances from the first host id to the last host id.
     * @param instances the instances to be scheduled.
     * @param synState the synchronization state.
     * @return the intra-scheduler result.
     */
    @Override
    protected IntraSchedulerResult scheduleInstances(List<Instance> instances, SynState synState) {
        IntraSchedulerResult intraSchedulerResult =
                new IntraSchedulerResult(this, getDatacenter().getSimulation().clock());

        synState.getClosTopology().InitTopologyGPU(getDatacenter().getStatesManager());
        List<InstanceGroup> instanceGroups = utils.groupToInstanceGroups(instances);
        LOGGER.info("{}: IntraSchedulerClosTopo scheduling {} instance groups, group ids is {}.", getDatacenter().getSimulation().clockStr(), instanceGroups.size(),
                instanceGroups.stream().map(InstanceGroup::getId).toList());
        for (InstanceGroup instanceGroup : instanceGroups) {
            LOGGER.info("{}: IntraSchedulerClosTopo scheduling instance group-{}", getDatacenter().getSimulation().clockStr(), instanceGroup.getId());
            if (instanceGroup.getTrainingStrategy() == null) {
                scheduleForInstanceGroupWithoutTopology(instanceGroup, synState, intraSchedulerResult);
            } else {
                scheduleForInstanceGroupWithTopology(instanceGroup, synState, intraSchedulerResult);
            }
            synState.getClosTopology().clearCandidate();
        }
        LOGGER.info("{}: IntraSchedulerClosTopo finish scheduling {} instance groups.", getDatacenter().getSimulation().clockStr(), instanceGroups.size());
        
        return intraSchedulerResult;
    }
}
