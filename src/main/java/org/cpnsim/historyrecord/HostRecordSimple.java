package org.cpnsim.historyrecord;

import org.cpnsim.statemanager.HostStateHistory;

import java.util.*;

public class HostRecordSimple implements HistoryRecord {
    private int freePointer = -1;
    private DArray dArray;
    private Map<Integer,Integer> hostMap = new HashMap<>();
    private int recordTime = 10;//记录小于等于10s的历史状态

    public HostRecordSimple() {
        this.dArray = new DArraySimple(330);
        initFreeBlock();
    }
    public HostRecordSimple(int capacity) {
        if(capacity>=6){
            this.dArray = new DArraySimple(capacity);
        }else{
            this.dArray = new DArraySimple(6000);
        }
        initFreeBlock();
    }

    //只有在初始化才会调用
    private void initFreeBlock() {
        if (dArray.getCapacity() < 6) {
            return;
        }
        int i = 0;
        for (; i <= dArray.getCapacity() - 12; i += 6) {
            dArray.put(i, i + 6);
        }
        dArray.put(i, -1);
        freePointer = 0;
    }

    private int getFreeBlockPointer(int time) {
        if (freePointer == -1) {
            //TODO 是否有必要先把需要不必要记录的历史状态清除掉
            clearAllHistory(time);
            if(freePointer==-1)
            {
                expandFreeBlock();
            }
        }
        int freeBlock = freePointer;
        freePointer = dArray.get(freePointer);
        return freeBlock;
    }

    private void expandFreeBlock() {
        int oldCapacity = dArray.getCapacity();
        int newCapacity;
        do {
            dArray.expand(2);
            newCapacity = dArray.getCapacity();
        } while (newCapacity <= oldCapacity + 6);
        int startFreePointer = oldCapacity / 6 * 6;
        freePointer = startFreePointer;
        for (int i = startFreePointer; i <= newCapacity - 12; i += 6) {
            dArray.put(i, i + 6);
        }
        dArray.put(startFreePointer, -1);
    }

    private void clearAllHistory(int time){
        //遍历hostMap的key
        List<Integer> keys = new ArrayList<>(hostMap.keySet());
        for (int hostId : keys) {
            clearHistory(hostId, time);
        }
    }

    private void clearHistory(int hostId, int time){
        if(hostMap.containsKey(hostId)){
            int pointer = hostMap.get(hostId);
            int lastPointer = -1;
            while(true){
                if(dArray.get(pointer+1)<time-recordTime){//往后的都不需要再记录，需要设置为free
                    if(pointer==hostMap.get(hostId)){//如果是第一个,删除掉hostId
                        hostMap.remove(hostId);
                    }
                    if(lastPointer!=-1){//设置结尾
                        dArray.put(lastPointer,-1);
                    }
                    int endPointer = pointer;
                    while(dArray.get(endPointer)!=-1){//找到最后一个
                        endPointer = dArray.get(endPointer);
                    }
                    dArray.put(endPointer, freePointer);
                    freePointer = pointer;
                    break;
                }
                if(dArray.get(pointer)==-1){//如果是最后一个
                    break;
                }
                lastPointer = pointer;
                pointer = dArray.get(pointer);
            }
        }
    }

    @Override
    public void record(int hostId, int time, int cpu, int ram, int storage, int bw) {
        clearHistory(hostId, time);
        int freeBlockPointer = getFreeBlockPointer(time);
        if(hostMap.containsKey(hostId)){
            dArray.put(freeBlockPointer, hostMap.get(hostId));
        }else{
            dArray.put(freeBlockPointer, -1);
        }
        dArray.put(freeBlockPointer + 1, time);
        dArray.put(freeBlockPointer + 2, cpu);
        dArray.put(freeBlockPointer + 3, ram);
        dArray.put(freeBlockPointer + 4, storage);
        dArray.put(freeBlockPointer + 5, bw);
        hostMap.put(hostId, freeBlockPointer);
    }

    @Override
    public HostStateHistory get(int hostId, int time) {
        if(hostMap.containsKey(hostId)){
            int pointer = hostMap.get(hostId);
            while (pointer != -1) {
                if (time >= dArray.get(pointer + 1)) {
                    return new HostStateHistory(time, dArray.get(pointer + 2), dArray.get(pointer + 3), dArray.get(pointer + 4), dArray.get(pointer + 5));
                }
                pointer = dArray.get(pointer);
            }
        }
        return null;
    }

    @Override
    public List<HostStateHistory> get(int hostId) {
        List<HostStateHistory> hostStateHistories = new ArrayList<>();
        if(hostMap.containsKey(hostId)){
            int pointer = hostMap.get(hostId);
            while (pointer != -1) {
                hostStateHistories.add(new HostStateHistory(dArray.get(pointer + 1), dArray.get(pointer + 2), dArray.get(pointer + 3), dArray.get(pointer + 4), dArray.get(pointer + 5)));
                pointer = dArray.get(pointer);
            }
        }
        return hostStateHistories;
    }
}
