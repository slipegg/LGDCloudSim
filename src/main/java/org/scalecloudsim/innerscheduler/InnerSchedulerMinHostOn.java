package org.scalecloudsim.innerscheduler;

import org.scalecloudsim.request.Instance;
import org.scalecloudsim.statemanager.SynState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InnerSchedulerMinHostOn extends InnerSchedulerSimple {
    public InnerSchedulerMinHostOn(int id, int firstPartitionId, int partitionNum) {
        super(id, firstPartitionId, partitionNum);
    }

    @Override
    public Map<Integer, List<Instance>> scheduleInstances(List<Instance> instances, SynState synState) {
        int hostNum = datacenter.getStatesManager().getHostNum();
        //TODO 域内调度
        Map<Integer, List<Instance>> res = new HashMap<>();
        Map<Integer, Integer> powerOnHostInstanceNum = datacenter.getStatesManager().getDatacenterPowerOnRecord().getPowerOnHostInstanceNum();


        for (Instance instance : instances) {
            int suitId = -1;

            //在已分配的主机中找到合适的主机
            for (int partitionId : synState.getSynState().keySet()) {
                Map<Integer, int[]> partitionSelfState = synState.getSelfHostState().get(partitionId);
                for (int hostId : partitionSelfState.keySet()) {
                    if (synState.isSuitable(hostId, instance)) {
                        suitId = hostId;
                        break;
                    }
                }
                if (suitId != -1) {
                    break;
                }
            }

            if (suitId == -1) {
                //在开机的主机中找到合适的主机
                for (int hostId : powerOnHostInstanceNum.keySet()) {
                    int partitionId = datacenter.getStatesManager().getPartitionRangesManager().getPartitionId(hostId);
                    if (!synState.getSelfHostState().get(partitionId).containsKey(hostId)
                            && synState.isSuitable(hostId, instance)) {
                        suitId = hostId;
                        break;
                    }
                }
            }

            //在对于延迟前的会认为开机的主机中找到合适的主机
            if (suitId == -1) {
                for (int partitionId : synState.getSynState().keySet()) {
                    Map<Integer, int[]> partitionSynState = synState.getSynState().get(partitionId);
                    for (int hostId : partitionSynState.keySet()) {
                        if (!powerOnHostInstanceNum.containsKey(hostId)) {//因为释放导致关机，但是对于延迟来说这个时候应该还没有关机
                            if (!powerOnHostInstanceNum.containsKey(hostId) &&
                                    !synState.getSelfHostState().get(partitionId).containsKey(hostId) &&
                                    synState.isSuitable(hostId, instance)) {
                                suitId = hostId;
                                break;
                            }
                        }
                    }
                    if (suitId != -1) {
                        break;
                    }
                }
            }

            //从头开始找合适的主机
            if (suitId == -1) {
                for (int partitionId = 0; partitionId < partitionNum; partitionId++) {
                    int[] range = datacenter.getStatesManager().getPartitionRangesManager().getRange(partitionId);
                    for (int hostId = range[0]; hostId <= range[1]; hostId++) {
                        if (!powerOnHostInstanceNum.containsKey(hostId) &&
                                !synState.getSelfHostState().get(partitionId).containsKey(hostId) &&
                                !synState.getSynState().get(partitionId).containsKey(hostId) &&
                                synState.isSuitable(hostId, instance)) {
                            suitId = hostId;
                            break;
                        }
                    }
                    if (suitId != -1) {
                        break;
                    }
                }
            }

            if (suitId != -1) {
                synState.allocateTmpResource(suitId, instance);
            }
            res.putIfAbsent(suitId, new ArrayList<>());
            res.get(suitId).add(instance);
        }
        return res;
    }
}
