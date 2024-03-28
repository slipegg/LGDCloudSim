package org.oldexample;

import java.util.*;

public class Simple2 {
    Map<Integer, Map<Integer, Integer>> cpuRamMap;//大于这个的在区间内的有多少

    public List<Integer> cpuRecordListInc;
    List<Integer> cpuRecordListDec;
    List<Integer> ramRecordListInc;
    List<Integer> ramRecordListDec;

    public Simple2() {
        this.cpuRecordListInc = getCpuRecordList();
        this.cpuRecordListDec = new ArrayList<>(cpuRecordListInc);
        Collections.reverse(cpuRecordListDec);
        this.ramRecordListInc = getRamRecordList();
        this.ramRecordListDec = new ArrayList<>(ramRecordListInc);
        Collections.reverse(ramRecordListDec);

        this.cpuRamMap = new HashMap<>();
        for (int cpu : cpuRecordListInc) {
            cpuRamMap.put(cpu, new HashMap<>());
            for (int ram : ramRecordListInc) {
                cpuRamMap.get(cpu).put(ram, 0);
            }
        }
    }

    public void addCpuRamRecord(int cpu, int ram) {
        int smallerCpu = getSmallerCpu(cpu);
        int smallerRam = getSmallerRam(ram);
        if (smallerCpu == -1 || smallerRam == -1) {
            return;
        }
        int tmp = cpuRamMap.get(smallerCpu).get(smallerRam);
        cpuRamMap.get(smallerCpu).put(smallerRam, tmp + 1);
    }

    public void updateCpuRamMap(int originCpu, int originRam, int nowCpu, int nowRam) {
        int smallerCpu = getSmallerCpu(originCpu);
        int smallerRam = getSmallerRam(originRam);
        if (smallerCpu == -1 || smallerRam == -1) {
            return;
        }
        int tmp = cpuRamMap.get(smallerCpu).get(smallerRam);
        cpuRamMap.get(smallerCpu).put(smallerRam, tmp - 1);
        smallerCpu = getSmallerCpu(nowCpu);
        smallerRam = getSmallerRam(nowRam);
        if (smallerCpu == -1 || smallerRam == -1) {
            return;
        }
        tmp = cpuRamMap.get(smallerCpu).get(smallerRam);
        cpuRamMap.get(smallerCpu).put(smallerRam, tmp + 1);
    }

    public int getCpuRamSum(int cpu, int ram) {
        int sum = 0;
        for (int i = 0; i < cpuRecordListDec.size(); i++) {
            if (cpuRecordListDec.get(i) < cpu) {
                break;
            }
            for (int j = 0; j < ramRecordListDec.size(); j++) {
                if (ramRecordListDec.get(j) < ram) {
                    break;
                }
                sum += cpuRamMap.get(cpuRecordListDec.get(i)).get(ramRecordListDec.get(j));
            }
        }
        return sum;
    }

    //TODO 按照最大主机的cpu数量和ram空间进行初始化
    private List<Integer> getCpuRecordList() {
        List<Integer> cpuRecordList = new ArrayList<>();
        int cpuMax = 128;
        cpuRecordList.add(1);
        for (int i = 2; i < 12; i += 2) {
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
        int ramMax = 256;
        ramRecordList.add(1);
        for (int i = 2; i < 12; i += 2) {
            ramRecordList.add(i);
        }
        for (int i = 32; i < 128; i += 4) {
            ramRecordList.add(i);
        }
        for (int i = 128; i < 256; i += 16) {
            ramRecordList.add(i);
        }
        for (int i = 256; i <= ramMax; i += 128) {
            ramRecordList.add(i);
        }
        /*
         * 实际返回：长度56，[1, 2, 4, 6, 8, 10, 12, 14, 16, 18, 20, 22, 24, 26, 28, 30, 32, 36, 40, 44, 48, 52, 56, 60, 64, 68, 72, 76, 80, 84, 88, 92, 96, 100, 104, 108, 112, 116, 120, 124, 128, 144, 160, 176, 192, 208, 224, 240, 256, 256]
         * */
        return ramRecordList;
    }

    private int getBiggerCpu(int cpu) {
        return cpuRecordListInc.stream().filter(key -> key >= cpu).findFirst().orElse(-1);
    }

    private int getBiggerRam(int ram) {
        return ramRecordListInc.stream().filter(key -> key >= ram).findFirst().orElse(-1);
    }

    private int getSmallerCpu(int cpu) {
        return cpuRecordListDec.stream().filter(key -> key <= cpu).findFirst().orElse(-1);
    }

    private int getSmallerRam(int ram) {
        return ramRecordListDec.stream().filter(key -> key <= ram).findFirst().orElse(-1);
    }
}
