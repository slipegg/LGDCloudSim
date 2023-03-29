package org.example;

import org.cloudsimplus.core.Cloudsim;
import org.cloudsimplus.core.Simulation;
import org.cloudsimplus.core.SimulationNull;
import org.cloudsimplus.hosts.Host;
import org.cloudsimplus.hosts.HostSimple;
import org.scalecloudsim.Instances.Instance;
import org.scalecloudsim.Instances.InstanceSimple;
import org.scalecloudsim.datacenters.Datacenter;
import org.scalecloudsim.datacenters.DatacenterSimple;
import org.scalecloudsim.resourcemanager.*;

import java.util.*;

public class test {
    public static void main(String[] args) {
//        int hostNum=1000000;
//        List history=new ArrayList<HostResourceState>();
//        Random random = new Random();
//        long ram = random.nextLong(100);
//        long bw = random.nextLong(100);
//        long storage = random.nextLong(100);
//        long cpu = random.nextLong(100);
//        long start = System.currentTimeMillis();
//        for(int i=0;i<hostNum;i++){
//            HostResourceState hs=new HostResourceState(ram,bw,storage,cpu);
//            history.add(hs);
//        }
//        //获取结束时间
//        long end = System.currentTimeMillis();
//        //计算执行时间
//        long timeElapsed = end - start;
//        System.out.println("Execution time: " + timeElapsed + " milliseconds");

//        Map<Double, HostResourceStateHistory> hs1=new HashMap<Double,HostResourceStateHistory>();
//        Map<Double,HostResourceStateHistory> hs2=new HashMap<Double,HostResourceStateHistory>();
//        HostResourceStateHistory h1=new HostResourceStateHistory(1,1,1,1,1);
//        HostResourceStateHistory h2=new HostResourceStateHistory(2,2,2,2,1);
//        hs1.put(0.0,h1);
//        hs2.put(0.0,h2);
//        List partion=new ArrayList<HostResourceState>();
//        partion.add(hs1.get(0.0));
//        partion.add(hs2.get(0.0));
//        System.out.println(partion.get(0));
//        h1.setBw(22);
//        System.out.println(partion.get(0));
//
//
//        Map<Double,HostResourceStateHistory> specialTimeStatus=new TreeMap<>();
//        HostResourceStateHistory s1=new HostResourceStateHistory(1,1,1,1,1);
//        HostResourceStateHistory s3=new HostResourceStateHistory(1,1,1,1,3);
//        HostResourceStateHistory s2=new HostResourceStateHistory(1,1,1,1,2);
//        specialTimeStatus.put(1.0,s1);
//        specialTimeStatus.put(3.0,s3);
//        specialTimeStatus.put(2.0,s2);
//
//        specialTimeStatus.forEach((key, value) -> System.out.println("Key: " + key + ", Value: " + value));

        //测试HostHistoryManagerSimple

//        long start = System.currentTimeMillis();
//        List<HostHistoryManagerSimple> hostmanagers=new ArrayList<>();
//        int hostNum=100000;
//        for(int i=0;i<hostNum;i++){
//        HostHistoryManagerSimple manager=new HostHistoryManagerSimple();
//            manager.addDelayWatch(0);
//            manager.addDelayWatch(2);
//            manager.addDelayWatch(3);
//            hostmanagers.add(manager);
//        }
//            for(int i=0;i<60;i++){
//                for(int j=0;j<hostNum;j++){
//                HostResourceStateHistory his=new HostResourceStateHistory(i,i,i,i,i);
//                hostmanagers.get(j).addHistory(his);
//            }
//        }
//
////        //获取结束时间
//        long end = System.currentTimeMillis();
//        long timeElapsed = end - start;
//        System.out.println("Execution time: " + timeElapsed + " milliseconds");
//        System.out.println(hostmanagers.get(hostNum-1).getHistory());
//        System.out.println(hostmanagers.get(hostNum-1).getDelayStates());

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
        for(;simulation.clock()<82;simulation.setClock(simulation.clock()+1)){
            for(int i=10;i<hostNum;i++){
                hosts.get(i).updateState();
            }
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

        //抽样状态信息
        List<HostResourceState> simpleDelayStates=datacenter.getStateManager().getSamplingState(100);
        System.out.println(simpleDelayStates.get(0));
        System.out.println(simpleDelayStates.size());

        //获取结束时间
        long end = System.currentTimeMillis();
        long timeElapsed = end - start;
        System.out.println("Execution time: " + timeElapsed + " milliseconds");

    }
}
