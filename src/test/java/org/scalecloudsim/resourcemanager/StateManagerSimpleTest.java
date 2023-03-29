package org.scalecloudsim.resourcemanager;

import org.cloudsimplus.core.Cloudsim;
import org.cloudsimplus.core.Simulation;
import org.cloudsimplus.hosts.Host;
import org.cloudsimplus.hosts.HostSimple;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.scalecloudsim.Instances.Instance;
import org.scalecloudsim.Instances.InstanceSimple;
import org.scalecloudsim.datacenters.Datacenter;
import org.scalecloudsim.datacenters.DatacenterSimple;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class StateManagerSimpleTest {
    @Timeout(10)
    @Test
    public void test(){
        //测试PartitionStateManagerSimple
        // get the start time
        long start = System.currentTimeMillis();
        //定义一个host数量
        int hostNum=100000;
        //定义一个instance
        Instance aInstance=new InstanceSimple(0,1,1,1,1);

        //初始化主机
        List<Host> hosts=new ArrayList<>();
        for(int i=0;i<hostNum;i++){
            hosts.add(new HostSimple(100,100,100,100));
        }

        //获取分区范围
        List<PartitionRange> ranges=PartitionRangeManager.averageCutting(0,hostNum-1,10);
        //打印分区范围
        System.out.println(ranges);
        //初始化状态管理器
        StateManager stateManager=new StateManagerSimple(ranges);
        //初始化模拟器
        Simulation simulation=new Cloudsim();
        //初始化数据中心
        Datacenter datacenter=new DatacenterSimple(simulation,hosts,stateManager);

        //设置每个范围应该保存的观测点
        stateManager.addAllPartitionWatch(0);
        stateManager.addPartitionWatch(1,2);
        stateManager.addPartitionWatch(1,3);
        stateManager.delPartitionWatch(1,3);
        stateManager.addPartitionWatch(1,4);

        //模拟host创建实例
        while (true){
            for(int i=0;i<hostNum;i++){
                hosts.get(i).createInstance(aInstance);
            }
            if(simulation.clock()==60){
                break;
            }
            simulation.setClock(simulation.clock()+1);
        }
        //模拟host更新状态
        simulation.setClock(simulation.clock()+1);
        while (true){
            for(int i=10;i<hostNum;i++){
                hosts.get(i).updateState();
            }
            if(simulation.clock()==61){
                break;
            }
            simulation.setClock(simulation.clock()+1);
        }

        hosts.get(hostNum/10).getHostHistoryManager().getSpecialTimeHostState(simulation.clock()-2);

        //模拟获取一片分区的状态
        long start2 = System.currentTimeMillis();
        List<HostResourceState> partitionDelayState0_0=datacenter.getStateManager().getPartitionDelayState(0,0);
        List<HostResourceState> partitionDelayState1_0=datacenter.getStateManager().getPartitionDelayState(1,0);//partitionStateManager1.getPartitionDelayState(0);
        List<HostResourceState> partitionDelayState1_2=datacenter.getStateManager().getPartitionDelayState(1,2);
        List<HostResourceState> partitionDelayState1_4=datacenter.getStateManager().getPartitionDelayState(1,4);
        long end2 = System.currentTimeMillis();
        long timeElapsed2 = end2 - start2;
        System.out.println("getPartitionDelayState time: " + timeElapsed2 + " milliseconds");

        //打印获得的状态
        System.out.println(partitionDelayState0_0.get(0));
        System.out.println(partitionDelayState1_0.get(0));
        System.out.println(partitionDelayState1_2.get(0));
        System.out.println(partitionDelayState1_4.get(0));

        //生成新的分区范围
        ranges=PartitionRangeManager.averageCutting(0,hostNum-1,5);
        System.out.println(ranges);
        //调整分区范围
        stateManager.setPartitionRanges(ranges);
        //设置每个范围应该保存的观测点
        stateManager.addAllPartitionWatch(0);
        stateManager.addPartitionWatch(1,2);
        stateManager.addPartitionWatch(1,4);
        stateManager.delPartitionWatch(1,4);
        stateManager.addPartitionWatch(1,5);

        //模拟host创建实例
        simulation.setClock(simulation.clock()+1);
        while (true){
            for(int i=0;i<hostNum;i++){
                hosts.get(i).createInstance(aInstance);
            }
            if(simulation.clock()==80){
                break;
            }
            simulation.setClock(simulation.clock()+1);
        }

        //模拟host更新状态
        simulation.setClock(simulation.clock()+1);
        while (true){
            for(int i=10;i<hostNum;i++){
                hosts.get(i).updateState();
            }
            if(simulation.clock()==81){
                break;
            }
            simulation.setClock(simulation.clock()+1);
        }

        partitionDelayState0_0=datacenter.getStateManager().getPartitionDelayState(0,0);
        partitionDelayState1_0=datacenter.getStateManager().getPartitionDelayState(1,0);
        partitionDelayState1_2=datacenter.getStateManager().getPartitionDelayState(1,2);
        List<HostResourceState> partitionDelayState1_5=datacenter.getStateManager().getPartitionDelayState(1,5);

        //打印获得的状态
        System.out.println(partitionDelayState0_0.get(0));
        System.out.println(partitionDelayState1_0.get(0));
        System.out.println(partitionDelayState1_2.get(0));
        System.out.println(partitionDelayState1_5.get(0));

        //获取结束时间
        long end = System.currentTimeMillis();
        long timeElapsed = end - start;
        System.out.println("Execution time: " + timeElapsed + " milliseconds");

        HostResourceState expected0_0=new HostResourceStateHistory(20,20,20,20,80.0);
        HostResourceState expected1_0=new HostResourceStateHistory(20,20,20,20,80.0);
        HostResourceState expected1_2=new HostResourceStateHistory(21,21,21,21,79.0);
        HostResourceState expected1_5=new HostResourceStateHistory(24,24,24,24,76.0);

        assertEquals(expected0_0,partitionDelayState0_0.get(0));
        assertEquals(expected1_0,partitionDelayState1_0.get(0));
        assertEquals(expected1_2,partitionDelayState1_2.get(0));
        assertEquals(expected1_5,partitionDelayState1_5.get(0));

        //抽样状态信息
        List<HostResourceState> simpleDelayStates=datacenter.getStateManager().getSamplingState(100);
        System.out.println(simpleDelayStates.get(0));
        System.out.println(simpleDelayStates.size());
        assertEquals(expected0_0,simpleDelayStates.get(0));
        assertEquals(hostNum/100,simpleDelayStates.size());

    }
}
