package org.scalecloudsim.statemanager;

import ch.qos.logback.core.joran.sanity.Pair;
import lombok.Getter;
import org.apache.commons.lang3.mutable.MutableInt;

import java.util.*;

@Getter
public class SimpleStateSimple implements SimpleState {
    long storageSum;
    long bwSum;
    Map<Integer, Map<Integer, MutableInt>> cpuRamMap;
    Map<Integer, Map<Integer, List<MutableInt>>> cpuRamSumMap;

    List<Integer> cpuRecordListInc;
    List<Integer> cpuRecordListDec;
    List<Integer> ramRecordListInc;
    List<Integer> ramRecordListDec;

    public SimpleStateSimple() {
        this.storageSum = 0;
        this.bwSum = 0;
        this.cpuRamMap = new TreeMap<>(Comparator.reverseOrder());
        this.cpuRamSumMap = new TreeMap<>(Comparator.reverseOrder());

        this.cpuRecordListInc = getCpuRecordList();
        this.cpuRecordListDec = new ArrayList<>(cpuRecordListInc);
        Collections.reverse(cpuRecordListDec);
        this.ramRecordListInc = getRamRecordList();
        this.ramRecordListDec = new ArrayList<>(ramRecordListInc);
        Collections.reverse(ramRecordListDec);
        for (int cpu : cpuRecordListInc) {
            Map<Integer, MutableInt> ramMap = new TreeMap<>(Comparator.reverseOrder());
            Map<Integer, List<MutableInt>> ramSumMap = new TreeMap<>(Comparator.reverseOrder());
            cpuRamMap.put(cpu, ramMap);
            cpuRamSumMap.put(cpu, ramSumMap);
            for (int ram : ramRecordListInc) {
                MutableInt mutableInt = new MutableInt(0);
                ramMap.put(ram, mutableInt);
                List<MutableInt> mutableIntList = new ArrayList<>();
                ramSumMap.put(ram, mutableIntList);
                for (int smallerCpu : cpuRecordListInc) {
                    if (smallerCpu > cpu) {
                        break;
                    }
                    for (int smallerRam : ramRecordListInc) {
                        if (smallerRam > ram) {
                            break;
                        }
                        cpuRamSumMap.get(smallerCpu).get(smallerRam).add(mutableInt);
                    }
                }
            }
        }
    }

    private List<Integer> getCpuRecordList() {
        List<Integer> cpuRecordList = new ArrayList<>();
        int cpuMax = 128;
        cpuRecordList.add(1);
        for (int i = 2; i < 32; i += 2) {
            cpuRecordList.add(i);
        }
        for (int i = 32; i < 64; i += 4) {
            cpuRecordList.add(i);
        }
        for (int i = 64; i <= cpuMax; i += 8) {
            cpuRecordList.add(i);
        }
        /*
         * 实际返回：长度33，[1, 2, 4, 6, 8, 10, 12, 14, 16, 18, 20, 22, 24, 26, 28, 30, 32, 36, 40, 44, 48, 52, 56, 60, 64, 72, 80, 88, 96, 104, 112, 120, 128]
         * */
        return cpuRecordList;
    }

    private List<Integer> getRamRecordList() {
        List<Integer> ramRecordList = new ArrayList<>();
        int ramMax = 1024;
        ramRecordList.add(1);
        for (int i = 2; i < 32; i += 2) {
            ramRecordList.add(i);
        }
        for (int i = 32; i < 128; i += 4) {
            ramRecordList.add(i);
        }
        for (int i = 128; i <= 256; i += 16) {
            ramRecordList.add(i);
        }
        for (int i = 256; i <= ramMax; i += 128) {
            ramRecordList.add(i);
        }
        /*
         * 实际返回：长度56，[1, 2, 4, 6, 8, 10, 12, 14, 16, 18, 20, 22, 24, 26, 28, 30, 32, 36, 40, 44, 48, 52, 56, 60, 64, 68, 72, 76, 80, 84, 88, 92, 96, 100, 104, 108, 112, 116, 120, 124, 128, 144, 160, 176, 192, 208, 224, 240, 256, 256, 384, 512, 640, 768, 896, 1024]
         * */
        return ramRecordList;
    }

    @Override
    public SimpleState updateStorageSum(int changeStorage) {
        storageSum += changeStorage;
        return this;
    }

    @Override
    public SimpleState updateBwSum(int changeBw) {
        bwSum += changeBw;
        return this;
    }

    @Override
    public SimpleState addCpuRamRecord(int cpu, int ram) {
        int smallerCpu = getSmallerCpu(cpu);
        int smallerRam = getSmallerRam(ram);
        if (smallerCpu != -1 && smallerRam != -1) {
            cpuRamMap.get(smallerCpu).get(smallerRam).increment();
        }
        return this;
    }

    @Override
    public SimpleState updateCpuRamMap(int originCpu, int originRam, int nowCpu, int nowRam) {
        int smallerOriginCpu = getSmallerCpu(originCpu);
        int smallerOriginRam = getSmallerRam(originRam);
        int smallerNowCpu = getSmallerCpu(nowCpu);
        int smallerNowRam = getSmallerRam(nowRam);
        if (smallerOriginCpu != -1 && smallerOriginRam != -1) {
            cpuRamMap.get(smallerOriginCpu).get(smallerOriginRam).decrement();
        }
        if (smallerNowCpu != -1 && smallerNowRam != -1) {
            cpuRamMap.get(smallerNowCpu).get(smallerNowRam).increment();
        }
        return this;
    }

    private int getSmallerCpu(int cpu) {
        return cpuRecordListDec.stream().filter(key -> key <= cpu).findFirst().orElse(-1);
    }

    private int getSmallerRam(int ram) {
        return ramRecordListDec.stream().filter(key -> key <= ram).findFirst().orElse(-1);
    }

    @Override
    public int getCpuRamSum(int cpu, int ram) {
        int biggerCpu = getBiggerCpu(cpu);
        int biggerRam = getBiggerRam(ram);
        if (biggerCpu == -1 || biggerRam == -1) {
            return 0;
        }
        List<MutableInt> mutableIntList = cpuRamSumMap.get(biggerCpu).get(biggerRam);
        return mutableIntList.stream().mapToInt(MutableInt::intValue).sum();
    }

    private int getBiggerCpu(int cpu) {
        return cpuRecordListInc.stream().filter(key -> key >= cpu).findFirst().orElse(-1);
    }

    private int getBiggerRam(int ram) {
        return ramRecordListInc.stream().filter(key -> key >= ram).findFirst().orElse(-1);
    }

    @Override
    public List<List<Integer>> getCpuRamItem() {
        List<List<Integer>> cpuRamItemList = new ArrayList<>();
        for (Map.Entry<Integer, Map<Integer, MutableInt>> cpuItem : cpuRamMap.entrySet()) {
            for (Integer ram : cpuItem.getValue().keySet()) {
                List<Integer> cpuRamItem = new ArrayList<>();
                cpuRamItem.add(cpuItem.getKey());
                cpuRamItem.add(ram);
                cpuRamItemList.add(cpuRamItem);
            }
        }
        return cpuRamItemList;
    }
}
