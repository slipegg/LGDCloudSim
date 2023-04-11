package org.scalecloudsim.statemanager;

import lombok.Getter;
import lombok.Setter;
import org.cloudsimplus.core.Simulation;
import org.scalecloudsim.request.Instance;
import org.scalecloudsim.datacenter.Datacenter;
import org.scalecloudsim.innerscheduler.InnerScheduler;

import java.util.*;

//TODO 是否可以做到host的资源类型数量可变
public class StateManagerSimple implements StateManager {
    Simulation simulation;//驱动
    PartitionRangesManager partitionRangesManager;//分区范围管理器
    Map<Integer, PartitionManager> partitionManagerMap;//区域管理器Map
    Set<InnerScheduler> validSchedulerSet;//注册的有效的scheduler
    Map<Integer, Double> lastChangeTime;//host状态上一次变化的时间，不存在在这里就是0.
    int hostNum;
    TreeMap<Integer, LinkedList<HostStateHistory>> hostHistoryMaps;//host历史状态，放置在这里可以使得范围调整，区域管理器更新后主机历史状态不丢失
    @Getter
    int[] hostStates;
    @Getter
    @Setter
    SimpleState simpleState;
    @Getter
    @Setter
    Datacenter datacenter;

    public StateManagerSimple(int hostNum, Simulation simulation) {
        this.hostNum = hostNum;
        this.simulation = simulation;
        hostStates = new int[hostNum * HostState.STATE_NUM];
        partitionManagerMap = new HashMap<>();
        validSchedulerSet = new HashSet<>();
        lastChangeTime = new TreeMap<>();
        hostHistoryMaps = new TreeMap<>();
        simpleState = new SimpleStateSimple();
    }

    public StateManagerSimple(int hostNum, Simulation simulation,
                              PartitionRangesManager partitionRangesManager,
                              List<InnerScheduler> schedulers) {
        this.hostNum = hostNum;
        this.simulation = simulation;
        hostStates = new int[hostNum * HostState.STATE_NUM];
        partitionManagerMap = new HashMap<>();
        validSchedulerSet = new HashSet<>();
        lastChangeTime = new TreeMap<>();
        hostHistoryMaps = new TreeMap<>();
        setPartitionRanges(partitionRangesManager);
        for (InnerScheduler scheduler : schedulers) {
            registerScheduler(scheduler);
        }
        simpleState = new SimpleStateSimple();
    }

    @Override
    public StateManager setPartitionRanges(PartitionRangesManager partitionRangesManager) {
        if (validSchedulerSet.size() != 0) {
            LOGGER.error("There are still registered schedulers here. Please cancel them before resetting the partition ranges");
            return this;
        } else {
            this.partitionRangesManager = partitionRangesManager;
            partitionManagerMap.clear();
            for (Integer partitionId : partitionRangesManager.getRanges().keySet()) {
                partitionManagerMap.put(partitionId, new PartitionManagerSimple(this, partitionId));
            }
        }
//        if(this.partitionRangesManager==null){
//            this.partitionRangesManager=partitionRangesManager;
//            for(Integer paritionId:partitionRangesManager.getRanges().keySet()){
//                partitionManagerMap.put(paritionId,new PartitionManagerSimple(this,paritionId));
//            }
//        }
//        else {
//            if(validSchedulerSet.size()!=0){
//                LOGGER.error("There are still registered schedulers here. Please cancel them before resetting the partition ranges");
//            }
//            else{
//                this.partitionRangesManager=partitionRangesManager;
//                partitionManagerMap.clear();
//                for(Integer partitionId:partitionRangesManager.getRanges().keySet()){
//                    partitionManagerMap.put(partitionId,new PartitionManagerSimple(this,partitionId));
//                }
//            }
//        }
        return this;
    }

    @Override
    public StateManager registerScheduler(InnerScheduler scheduler) {
        if (!isValidScheduler(scheduler)) {
            LOGGER.error("scheduler" + scheduler + " is not registered");
        } else {
            for (Map.Entry<Integer, Double> entry : scheduler.getPartitionDelay().entrySet()) {
                int partitionId = entry.getKey();
                double delay = entry.getValue();
                partitionManagerMap.get(partitionId).addDelayWatch(delay);
            }
        }
        return this;
    }

    @Override
    public StateManager registerSchedulers(List<InnerScheduler> scheduler) {
        for (InnerScheduler InnerScheduler : scheduler) {
            registerScheduler(InnerScheduler);
        }
        return this;
    }

    @Override
    public StateManager cancelScheduler(InnerScheduler scheduler) {
        if (!isValidScheduler(scheduler)) {
            LOGGER.error("scheduler" + scheduler + " is not registered");
        } else {
            validSchedulerSet.remove(scheduler);
            for (Map.Entry<Integer, Double> entry : scheduler.getPartitionDelay().entrySet()) {
                int id = entry.getKey();
                double delay = entry.getValue();
                partitionManagerMap.get(id).delDelayWatch(delay);
            }
        }

        return this;
    }

    @Override
    public StateManager calcelAllSchedulers() {
        List<InnerScheduler> tmpSchedulerList = new ArrayList<>(validSchedulerSet);
        for (InnerScheduler scheduler : tmpSchedulerList) {
            cancelScheduler(scheduler);
        }
        return this;
    }

    private boolean isValidScheduler(InnerScheduler scheduler) {
        if (validSchedulerSet.contains(scheduler)) {
            return true;
        }
        Map<Integer, Double> partitionDelay = scheduler.getPartitionDelay();
        Map<Integer, int[]> ranges = partitionRangesManager.getRanges();
        boolean isSameKey = true;
        if (partitionDelay.size() != ranges.size()) {
            isSameKey = false;
            LOGGER.error("scheduler(" + scheduler + ") partitionDelay.size()!=ranges.size()");
        } else {
            for (Integer key : partitionDelay.keySet()) {
                if (!ranges.containsKey(key)) {
                    isSameKey = false;
                    LOGGER.error("scheduler partitionDelay.keySet()!=ranges.keySet()");
                    break;
                }
            }
        }
        if (isSameKey) {
            validSchedulerSet.add(scheduler);
        }
        return isSameKey;
    }

    @Override
    public DelayState getDelayState(InnerScheduler scheduler) {
        if (!isValidScheduler(scheduler)) {
            LOGGER.error("scheduler" + scheduler + " is not registered");
            return null;
        }
        Map<Integer, Double> partitionDelay = scheduler.getPartitionDelay();
        Map<Integer, Map<Integer, HostStateHistory>> oldState = new HashMap<>();
        for (Map.Entry<Integer, Double> entry : partitionDelay.entrySet()) {
            int partitionId = entry.getKey();
            double delay = entry.getValue();
            Map<Integer, HostStateHistory> partitionHistory = partitionManagerMap.get(partitionId).getDelayPartitionState(delay);
            oldState.put(partitionId, partitionHistory);
        }
        DelayState delayState = new DelayStateSimple(hostStates, oldState, partitionRangesManager);
        return delayState;
    }

    @Override
    public Simulation getSimulation() {
        return simulation;
    }

    @Override
    public TreeMap<Integer, LinkedList<HostStateHistory>> getHostHistoryMaps() {
        return hostHistoryMaps;
    }

    @Override
    public int[] getnowHostStateArr(int hostId) {
        int[] nowHostState = new int[HostState.STATE_NUM];
        System.arraycopy(hostStates, hostId * HostState.STATE_NUM + 0, nowHostState, 0, HostState.STATE_NUM);
        return nowHostState;
    }

    @Override
    public HostStateHistory getnowHostStateHistory(int hostId) {
        HostStateHistory hostStateHistory = new HostStateHistory(
                hostStates[hostId * HostState.STATE_NUM],
                hostStates[hostId * HostState.STATE_NUM + 1],
                hostStates[hostId * HostState.STATE_NUM + 2],
                hostStates[hostId * HostState.STATE_NUM + 3],
                getLastChangeTime(hostId));
        return hostStateHistory;
    }

    @Override
    public HostState getnowHostState(int hostId) {
        HostState hostState = new HostState(
                hostStates[hostId * HostState.STATE_NUM],
                hostStates[hostId * HostState.STATE_NUM + 1],
                hostStates[hostId * HostState.STATE_NUM + 2],
                hostStates[hostId * HostState.STATE_NUM + 3]);
        return hostState;
    }

    @Override
    public LinkedList<HostStateHistory> getHostHistory(int hostId) {
        LinkedList<HostStateHistory> history = new LinkedList<>();
        HostStateHistory nowState = new HostStateHistory(
                hostStates[hostId * HostState.STATE_NUM],
                hostStates[hostId * HostState.STATE_NUM + 1],
                hostStates[hostId * HostState.STATE_NUM + 2],
                hostStates[hostId * HostState.STATE_NUM + 3],
                getLastChangeTime(hostId));
        history.addFirst(nowState);

        if (hostHistoryMaps.containsKey(hostId)) {
            LinkedList<HostStateHistory> oldState = hostHistoryMaps.get(hostId);
            history.addAll(oldState);
        }
        return history;
    }

    @Override
    public HostStateHistory getHostStateHistory(int hostId, double time) {
        if (time > simulation.clock()) {
            LOGGER.error("time is larger than now");
            return null;
        } else {
            if (time >= lastChangeTime.get(hostId)) {
                return getnowHostStateHistory(hostId);
            } else {
                LinkedList<HostStateHistory> history = getHostHistory(hostId);
                for (HostStateHistory hostStateHistory : history) {
                    if (hostStateHistory.getTime() <= time) {
                        return hostStateHistory;
                    }
                }
            }
            LOGGER.error("The history before time(" + time + ") has been lost");
            return null;
        }
    }

    @Override
    public PartitionRangesManager getPartitionRangesManager() {
        return partitionRangesManager;
    }


    private double getLastChangeTime(int hostId) {
        double lastTime = 0;
        if (lastChangeTime.containsKey(hostId)) {
            lastTime = lastChangeTime.get(hostId);
        }
        return lastTime;
    }

    @Override
    public StateManager updateHostState(int hostId, int[] state) {
        int partitionId = partitionRangesManager.getPartitionId(hostId);
        PartitionManager partitionManager = partitionManagerMap.get(partitionId);
        HostStateHistory hostStateHistory = new HostStateHistory(
                hostStates[hostId * HostState.STATE_NUM],
                hostStates[hostId * HostState.STATE_NUM + 1],
                hostStates[hostId * HostState.STATE_NUM + 2],
                hostStates[hostId * HostState.STATE_NUM + 3],
                getLastChangeTime(hostId));
        partitionManager.addHostHistory(hostId, hostStateHistory);
        System.arraycopy(state, 0, hostStates, hostId * HostState.STATE_NUM + 0, HostState.STATE_NUM);
        lastChangeTime.put(hostId, simulation.clock());
        return this;
    }

    @Override
    public StateManager updateHostState(int hostId) {
        int partitionId = partitionRangesManager.getPartitionId(hostId);
        PartitionManager partitionManager = partitionManagerMap.get(partitionId);
        partitionManager.updateHostHistory(getLastChangeTime(hostId), hostId);
        return this;
    }

    @Override
    public StateManager initHostStates(HostStateGenerator hostStateGenerator) {
        for (int i = 0; i < hostNum; i++) {
            int[] state = hostStateGenerator.generateHostState();
            System.arraycopy(state, 0, hostStates, i * HostState.STATE_NUM, HostState.STATE_NUM);
            simpleState.addCpuRamRecord(state[0], state[1]);
            simpleState.updateStorageSum(state[2]);
            simpleState.updateBwSum(state[3]);
        }
        return this;
    }

    @Override
    public boolean isSuitable(int hostId, Instance instance) {
        return hostStates[hostId * HostState.STATE_NUM] >= instance.getCpu() &&
                hostStates[hostId * HostState.STATE_NUM + 1] >= instance.getRam() &&
                hostStates[hostId * HostState.STATE_NUM + 2] >= instance.getStorage() &&
                hostStates[hostId * HostState.STATE_NUM + 3] >= instance.getBw();
    }

    @Override
    public StateManager allocateResource(int hostId, Instance instance) {
        int partitionId = partitionRangesManager.getPartitionId(hostId);
        PartitionManager partitionManager = partitionManagerMap.get(partitionId);
        HostStateHistory hostStateHistory = new HostStateHistory(
                hostStates[hostId * HostState.STATE_NUM],
                hostStates[hostId * HostState.STATE_NUM + 1],
                hostStates[hostId * HostState.STATE_NUM + 2],
                hostStates[hostId * HostState.STATE_NUM + 3],
                getLastChangeTime(hostId));
        partitionManager.addHostHistory(hostId, hostStateHistory);
        hostStates[hostId * HostState.STATE_NUM] -= instance.getCpu();
        hostStates[hostId * HostState.STATE_NUM + 1] -= instance.getRam();
        hostStates[hostId * HostState.STATE_NUM + 2] -= instance.getStorage();
        hostStates[hostId * HostState.STATE_NUM + 3] -= instance.getBw();
        lastChangeTime.put(hostId, simulation.clock());

        simpleState.updateCpuRamMap(hostStateHistory.getCpu(), hostStateHistory.getRam(),
                hostStates[hostId * HostState.STATE_NUM], hostStates[hostId * HostState.STATE_NUM + 1]);
        simpleState.updateStorageSum(-1 * instance.getStorage());
        simpleState.updateBwSum(-1 * instance.getBw());
        return this;
    }

    public StateManager releaseResource(int hostId, Instance instance) {
        //TODO 有重复代码，待优化
        int partitionId = partitionRangesManager.getPartitionId(hostId);
        PartitionManager partitionManager = partitionManagerMap.get(partitionId);
        HostStateHistory hostStateHistory = new HostStateHistory(
                hostStates[hostId * HostState.STATE_NUM],
                hostStates[hostId * HostState.STATE_NUM + 1],
                hostStates[hostId * HostState.STATE_NUM + 2],
                hostStates[hostId * HostState.STATE_NUM + 3],
                getLastChangeTime(hostId));
        partitionManager.addHostHistory(hostId, hostStateHistory);
        hostStates[hostId * HostState.STATE_NUM] += instance.getCpu();
        hostStates[hostId * HostState.STATE_NUM + 1] += instance.getRam();
        hostStates[hostId * HostState.STATE_NUM + 2] += instance.getStorage();
        hostStates[hostId * HostState.STATE_NUM + 3] += instance.getBw();
        lastChangeTime.put(hostId, simulation.clock());

        simpleState.updateCpuRamMap(hostStateHistory.getCpu(), hostStateHistory.getRam(),
                hostStates[hostId * HostState.STATE_NUM], hostStates[hostId * HostState.STATE_NUM + 1]);
        simpleState.updateStorageSum(instance.getStorage());
        simpleState.updateBwSum(instance.getBw());
        return this;
    }

    @Override
    public List<Double> getPartitionWatchDelay(int hostId) {
        int partitionId = partitionRangesManager.getPartitionId(hostId);
        PartitionManager partitionManager = partitionManagerMap.get(partitionId);
        return partitionManager.getDelayWatchList();
    }

}
