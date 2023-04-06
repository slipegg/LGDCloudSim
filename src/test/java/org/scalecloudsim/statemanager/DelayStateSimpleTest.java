package org.scalecloudsim.statemanager;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DelayStateSimpleTest {
    //测试得到一个大表+小表的结果
    //初始化了50M个host，每个host有4个状态，每个dc有10个host，每个dc有10个part，并假设100K/s*30s的请求都落在了第0区的第0part中
    //初始化50M个host花费800Mb，初始化100k/s*30s的请求导致的对应的状态变化记录花费了330Mb，获得一张一片区域的delay状态新表花费110Mb(理论应该为80Mb，可能和中间变量有关）
    //总共花费1234Mb，耗时1594ms
    @Test
    public void testGetHostState(){
        double start = System.currentTimeMillis();
        int hostNum = 50_000_000;
        int dcNum =10;
        int partNum=10;
        int requestNum=100_000*30;
        int[][] allHostState = new int[dcNum][hostNum*HostState.STATE_NUM/dcNum];
        for(int i=0;i<dcNum;i++){
            for(int j=0;j<hostNum/dcNum*HostState.STATE_NUM;j++){
                allHostState[i][j]=j;
            }
        }
        int[] nowState=allHostState[0];
        PartitionRangesManager partitionRangesManager = new PartitionRangesManager();
        partitionRangesManager.setAverageCutting(0,hostNum/dcNum,partNum);

        //假设全部请求落在了第0区的第0part中
        Map<Integer, Map<Integer, HostStateHistory>> oldState=new HashMap<>();
        Map<Integer,HostStateHistory> hostStateMap=new HashMap<>();
        for(int i=0;i<requestNum;i++){
            HostStateHistory hostState = new HostStateHistory(i+1,i,i,i,0);
            hostStateMap.put(i,hostState);
        }
        oldState.put(0,hostStateMap);
        DelayStateSimple delayStateSimple = new DelayStateSimple(nowState,oldState,partitionRangesManager);
        int[] actualState = delayStateSimple.getHostState(0);
        for(int i=0;i<actualState.length;i++){
            System.out.println(actualState[i]);
        }
        assertEquals(1,actualState[0]);
        assertEquals(0,actualState[1]);
        assertEquals(0,actualState[2]);
        assertEquals(0,actualState[3]);
        int[] allDelayState=delayStateSimple.getAllState();
        assertEquals(2,allDelayState[4]);
        assertEquals(1,allDelayState[5]);
        assertEquals(1,allDelayState[6]);
        assertEquals(1,allDelayState[7]);
        System.out.println("allDelayState.length:"+allDelayState.length);

        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory(); //总内存
        long freeMemory = runtime.freeMemory(); //空闲内存
        long usedMemory = totalMemory - freeMemory; //已用内存
        System.out.println("hostNum: "+hostNum);
        System.out.println("totalMemory: " + totalMemory/1000000 + " Mb");
        System.out.println("freeMemory: " + freeMemory/1000000 + " Mb");
        System.out.println("usedMemory: " + usedMemory/1000000 + " Mb");
        double end = System.currentTimeMillis();
        System.out.println("耗时："+(end-start)+"ms");
        allHostState[dcNum-1][0]=11;//得在最后使用一下，不然编译器会优化掉，提前释放内存
        assertEquals(19999999,allHostState[dcNum-1][hostNum*HostState.STATE_NUM/dcNum-1]);
    }
}
