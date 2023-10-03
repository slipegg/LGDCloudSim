package org.cpnsim.statemanager;

import lombok.Getter;
import org.apache.commons.lang3.mutable.MutableInt;

import java.util.*;

/**
 * A class to record the overall state information of hosts in an entire datacenter
 * This class implements the interface {@link SimpleState}.
 *
 * @author Jiawen Liu
 * @since CPNSim 1.0
 */
@Getter
public class SimpleStateSimple implements SimpleState {
    /** The sum of the cpu of all hosts in the datacenter */
    private long cpuAvaiableSum;

    /** The sum of the ram of all hosts in the datacenter */
    private long ramAvaiableSum;

    /** The sum of the storage of all hosts in the datacenter */
    private long storageAvaiableSum;

    /** The sum of the bw of all hosts in the datacenter */
    private long bwAvaiableSum;

    /** The number of hosts whose remaining resources are between (cpu1, ram1) combination and (cpu2, ram2) combination.
     * the first key is cpu, the second key is ram,
     * the value is the number of hosts whose remaining resources are greater than this combination of (cpu, ram),
     * but smaller than the next combination of (cpu1, ram1)
     * */
    private Map<Integer, Map<Integer, MutableInt>> cpuRamMap;

    /** The number of hosts whose remaining resources are greater than this combination of (cpu, ram).
     * the first key is cpu, the second key is ram,
     * the value is the number of hosts whose remaining resources are greater than this combination of (cpu, ram)
     * */
    private Map<Integer, Map<Integer, List<MutableInt>>> cpuRamSumMap;

    /** Incremental cpu list that needs to be recorded */
    private List<Integer> cpuRecordListInc;

    /** Decremental cpu list that needs to be recorded */
    private List<Integer> cpuRecordListDec;

    /** Incremental ram list that needs to be recorded */
    private List<Integer> ramRecordListInc;

    /** Decremental ram list that needs to be recorded */
    private List<Integer> ramRecordListDec;

    public SimpleStateSimple(int maxCpuCapacity, int maxRamCapacity) {
        this.cpuAvaiableSum = 0;
        this.ramAvaiableSum = 0;
        this.storageAvaiableSum = 0;
        this.bwAvaiableSum = 0;
        this.cpuRamMap = new TreeMap<>(Comparator.reverseOrder());
        this.cpuRamSumMap = new TreeMap<>(Comparator.reverseOrder());

        this.cpuRecordListInc = getCpuRecordList(maxCpuCapacity);
        this.cpuRecordListDec = new ArrayList<>(cpuRecordListInc);
        Collections.reverse(cpuRecordListDec);
        this.ramRecordListInc = getRamRecordList(maxRamCapacity);
        this.ramRecordListDec = new ArrayList<>(ramRecordListInc);
        Collections.reverse(ramRecordListDec);
        generateMap();
    }

    private void generateMap() {
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

    /**
     * Generate the cpu list that needs to be recorded
     *
     * @return the cpu list that needs to be recorded
     */
    private List<Integer> getCpuRecordList(int cpuMax) {
        List<Integer> cpuRecordList = new ArrayList<>();
        cpuRecordList.add(1);
        for (int i = 2; i < 32 && i < cpuMax; i += 2) {
            cpuRecordList.add(i);
        }
        for (int i = 32; i < 64 && i < cpuMax; i += 4) {
            cpuRecordList.add(i);
        }
        for (int i = 64; i <= cpuMax; i += (cpuMax - 64) / 8) {
            if (!cpuRecordList.contains(i)) {
                cpuRecordList.add(i);
            } else {
                break;
            }
        }
        if (cpuRecordList.get(cpuRecordList.size() - 1) != cpuMax) {
            cpuRecordList.add(cpuMax);
        }
        return cpuRecordList;
    }

    /**
     * Generate the ram list that needs to be recorded
     *
     * @return the ram list that needs to be recorded
     */
    private List<Integer> getRamRecordList(int ramMax) {
        List<Integer> ramRecordList = new ArrayList<>();
        ramRecordList.add(1);
        for (int i = 2; i < 32 && i < ramMax; i += 2) {
            ramRecordList.add(i);
        }
        for (int i = 32; i < 128 && i < ramMax; i += 4) {
            ramRecordList.add(i);
        }
        for (int i = 128; i < 256 && i < ramMax; i += 16) {
            ramRecordList.add(i);
        }
        for (int i = 256; i <= ramMax; i += (ramMax - 256) / 8) {
            if (!ramRecordList.contains(i)) {
                ramRecordList.add(i);
            } else {
                break;
            }
        }
        if (ramRecordList.get(ramRecordList.size() - 1) != ramMax) {
            ramRecordList.add(ramMax);
        }
        return ramRecordList;
    }

    @Override
    public SimpleState updateStorageSum(int changeStorage) {
        storageAvaiableSum += changeStorage;
        return this;
    }

    @Override
    public SimpleState updateBwSum(int changeBw) {
        bwAvaiableSum += changeBw;
        return this;
    }

    @Override
    public SimpleState addCpuRamRecord(int cpu, int ram) {
        int smallerCpu = getSmallerCpu(cpu);
        int smallerRam = getSmallerRam(ram);
        if (smallerCpu != -1 && smallerRam != -1) {
            cpuRamMap.get(smallerCpu).get(smallerRam).increment();
        }
        cpuAvaiableSum += cpu;
        ramAvaiableSum += ram;
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
        cpuAvaiableSum += nowCpu - originCpu;
        ramAvaiableSum += nowRam - originRam;
        return this;
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

    /**
     * Get the smaller cpu that is smaller than the given cpu
     *
     * @param cpu the given cpu
     * @return the smaller cpu that is smaller than the given cpu
     */
    private int getSmallerCpu(int cpu) {
        return cpuRecordListDec.stream().filter(key -> key <= cpu).findFirst().orElse(-1);
    }

    /**
     * Get the smaller ram that is smaller than the given ram
     *
     * @param ram the given ram
     * @return the smaller ram that is smaller than the given ram
     */
    private int getSmallerRam(int ram) {
        return ramRecordListDec.stream().filter(key -> key <= ram).findFirst().orElse(-1);
    }

    /**
     * Get the bigger cpu that is bigger than the given cpu
     *
     * @param cpu the given cpu
     * @return the bigger cpu that is bigger than the given cpu
     */
    private int getBiggerCpu(int cpu) {
        return cpuRecordListInc.stream().filter(key -> key >= cpu).findFirst().orElse(-1);
    }

    /**
     * Get the bigger ram that is bigger than the given ram
     *
     * @param ram the given ram
     * @return the bigger ram that is bigger than the given ram
     */
    private int getBiggerRam(int ram) {
        return ramRecordListInc.stream().filter(key -> key >= ram).findFirst().orElse(-1);
    }
}
