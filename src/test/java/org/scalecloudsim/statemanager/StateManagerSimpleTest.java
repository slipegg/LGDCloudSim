package org.scalecloudsim.statemanager;

import org.cloudsimplus.core.CloudSim;
import org.cloudsimplus.core.Simulation;
import org.junit.jupiter.api.Test;
import org.scalecloudsim.innerscheduler.InnerSchedulerSimple;

import java.util.*;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class StateManagerSimpleTest {
    @Test
    public void aSimpleTest(){
        double start=System.currentTimeMillis();

//        int hostNum=50_000_000;
        int hostNum=500_000;
        int preRequestNum=1_000;

        Simulation simulation=new CloudSim();

        //初始化分区管理器，分区数为10，每个分区的平均大小为hostNum/10
        PartitionRangesManager partitionRangesManager=new PartitionRangesManager();
        partitionRangesManager.setAverageCutting(0,hostNum-1,10);

        //初始化10个调度器，每个调度器对10个分区的延迟都不一样
        List<InnerSchedulerSimple> schedulers = new ArrayList<>();
        for(int i=0;i<10;i++){//10个调度器
            Map<Integer,Double> partitionDelay=new TreeMap<>();
            for(int j=0;j<10;j++){//对10个分区的延迟
                partitionDelay.put((i+j)%10,3.0*j);
            }
            InnerSchedulerSimple scheduler = new InnerSchedulerSimple(i, partitionDelay);
            System.out.println(scheduler.getPartitionDelay());
            schedulers.add(scheduler);
        }

        //初始化状态管理器
        StateManager stateManager=new StateManagerSimple(hostNum,simulation,partitionRangesManager,schedulers);

        //模拟60s，假设每秒的请求都落在0~preRequestNum-1的主机上，从而导致他们的状态在不断变化
        int resource=1;
        simulation.setClock(1);
        for(;simulation.clock()<60;simulation.setClock(simulation.clock()+1)){
            for(int i=0;i<preRequestNum;i++){
                int[] state={resource,resource,resource,resource};
                stateManager.updateHostState(i,state);
            }
            LinkedList<HostStateHistory> history=stateManager.getHostHistory(0);
            System.out.println(simulation.clock()+"s :the history of host 0 : length:"+history.size()+" "+history);
            assertEquals(
                    min(resource+1,28),history.size());
            resource++;
        }

        //模拟10s，更新在0~preRequestNum-1的主机上的状态变化，迭代历史状态，将不需要的历史状态丢掉
        int expctedSize=27;
        for(;simulation.clock()<70;simulation.setClock(simulation.clock()+1)){
            for(int i=0;i<preRequestNum;i++){
                stateManager.updateHostState(i);
            }
            LinkedList<HostStateHistory> history=stateManager.getHostHistory(0);
            System.out.println(simulation.clock()+"s :the history of host 0 : length:"+history.size()+" "+history);
            assertEquals(expctedSize,history.size());
            expctedSize--;
        }

        //调度器0获取整个域的状态
        DelayState delayState0=stateManager.getDelayState(schedulers.get(0));
        int[] state0=delayState0.getHostState(0);
        System.out.println("delayState0: "+Arrays.toString(state0));
        int[] expectedState0={59, 59, 59, 59};
        assertEquals(Arrays.toString(expectedState0),Arrays.toString(state0));
        int[] allState0=delayState0.getAllState();
        System.out.println("allState0 length: "+allState0.length);
        int expectAllState0Length=hostNum*HostState.STATE_NUM;
        assertEquals(expectAllState0Length,allState0.length);

        //调度器1获取整个域的状态
        DelayState delayState1=stateManager.getDelayState(schedulers.get(1));
        int[] state1=delayState1.getHostState(0);
        System.out.println("delayState1: "+Arrays.toString(state1));
        int[] expectedState1={42, 42, 42, 42};
        assertEquals(Arrays.toString(expectedState1),Arrays.toString(state1));

        //模拟删除一个调度器0，然后新增另外一个调度器
        stateManager.cancelScheduler(schedulers.get(0));
        Map<Integer,Double> partitionDelay = new TreeMap<>();
        for(int i=0;i<10;i++){
            partitionDelay.put(i,2.0*i);
        }
        InnerSchedulerSimple scheduler = new InnerSchedulerSimple(10, partitionDelay);
        stateManager.registerScheduler(scheduler);

        //模拟抛弃当前分区，然后重新分区
        stateManager.calcelAllSchedulers();
        partitionRangesManager.setAverageCutting(0,hostNum-1,5);
        stateManager.setPartitionRanges(partitionRangesManager);
        List<InnerSchedulerSimple> newSchedulers = new ArrayList<>();
        for(int i=0;i<5;i++){//5个调度器
            Map<Integer,Double> newPartitionDelay=new TreeMap<>();
            for(int j=0;j<5;j++){//对5个分区的延迟
                newPartitionDelay.put((i+j)%5,2.0*j);
            }
            InnerSchedulerSimple newScheduler = new InnerSchedulerSimple(i, newPartitionDelay);
            System.out.println(newScheduler.getPartitionDelay());
            newSchedulers.add(newScheduler);
        }
        stateManager.registerSchedulers(newSchedulers);

        //然后再重新运行
        //模拟65s，假设每秒的请求都落在0~preRequestNum-1的主机上，从而导致他们的状态在不断变化
        for(;simulation.clock()<115;simulation.setClock(simulation.clock()+1)){
            for(int i=0;i<preRequestNum;i++){
                int[] state={resource,resource,resource,resource};
                stateManager.updateHostState(i,state);
            }
            LinkedList<HostStateHistory> history=stateManager.getHostHistory(0);
            System.out.println(simulation.clock()+"s :the history of host 0 : length:"+history.size()+" "+history);
            assertEquals(min(9,resource-58),history.size());
            resource++;
        }

        //模拟5s，更新在0~preRequestNum-1的主机上的状态变化，迭代历史状态，将不需要的历史状态丢掉
        expctedSize=8;
        for(;simulation.clock()<120;simulation.setClock(simulation.clock()+1)){
            for(int i=0;i<preRequestNum;i++){
                stateManager.updateHostState(i);
            }
            LinkedList<HostStateHistory> history=stateManager.getHostHistory(0);
            System.out.println(simulation.clock()+"s :the history of host 0 : length:"+history.size()+" "+history);
            assertEquals(expctedSize,history.size());
            expctedSize--;
            resource++;
        }

        //调度器0获取整个域的状态
        delayState0=stateManager.getDelayState(newSchedulers.get(0));
        state0=delayState0.getHostState(0);
        System.out.println("new delayState0: "+Arrays.toString(state0));
        expectedState0=new int[]{104, 104, 104, 104};
        assertEquals(Arrays.toString(expectedState0),Arrays.toString(state0));
        allState0=delayState0.getAllState();
        System.out.println("new allState0 length: "+allState0.length);
        expectAllState0Length=hostNum*HostState.STATE_NUM;
        assertEquals(expectAllState0Length,allState0.length);

        //调度器1获取整个域的状态
        delayState1=stateManager.getDelayState(newSchedulers.get(1));
        state1=delayState1.getHostState(0);
        System.out.println("new delayState1: "+Arrays.toString(state1));
        expectedState1=new int[]{101, 101, 101, 101};
        assertEquals(Arrays.toString(expectedState1),Arrays.toString(state1));

        System.out.println("\n运行情况：");
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

        int[] hostState=stateManager.getnowHostStateArr(hostNum-1);
        int[] expectedState={0, 0, 0, 0};
        assertEquals(Arrays.toString(expectedState),Arrays.toString(hostState));
    }
}
