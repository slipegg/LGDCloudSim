package org.example;

import org.cloudsimplus.core.Cloudsim;
import org.cloudsimplus.core.Simulation;
import org.cloudsimplus.core.SimulationNull;
import org.cloudsimplus.hosts.*;
import org.scalecloudsim.Instances.Instance;
import org.scalecloudsim.Instances.InstanceSimple;
import org.cloudsimplus.hosts.Host;
import org.cloudsimplus.hosts.HostSimple;
import org.cloudsimplus.resources.Ram;
import org.openjdk.jol.info.ClassLayout;
import org.scalecloudsim.resourcemanager.HostResourceState;
import org.scalecloudsim.resourcemanager.HostResourceStateHistory;
import org.scalecloudsim.datacenters.Datacenter;
import org.scalecloudsim.datacenters.DatacenterSimple;
import org.scalecloudsim.resourcemanager.*;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

public class test {
    public void fun(int hostNum){
        //测试PartitionStateManagerSimple
        // get the start time
        long start = System.currentTimeMillis();
        //定义一个instance
        Instance aInstance=new InstanceSimple(0,1,1,1,1);
        //模拟每秒100k个实例请求
        //初始化主机
        List<Host> hosts=new ArrayList<>();
        for(int i=0;i<hostNum;i++){
            hosts.add(new HostSimple(100,100,100,100));
        }
//        HostResourceState hostResourceState=new HostResourceState(100L,100L,100L,100L);
//        Host host=new HostSimple(100L,100L,100L,100L);
//        Ram ram = new Ram(100L);
//        ClassLayout classLayout = ClassLayout.parseInstance(host);
//        System.out.println(classLayout.toPrintable());
//        List<HostResourceStateHistory> hosts=new ArrayList<>();
//        for(int i=0;i<hostNum;i++){
//            hosts.add(new HostResourceStateHistory(100L,100L,100L,100L,1.2));
//        }

        //获取分区范围
        List<PartitionRange> ranges=PartitionRangeManager.averageCutting(0,hostNum-1,10);
        //打印分区范围
//        System.out.println(ranges);
        //初始化状态管理器
        StateManager stateManager=new StateManagerSimple(ranges);
        //初始化模拟器
        Simulation simulation=new Cloudsim();
        //初始化数据中心
        Datacenter datacenter=new DatacenterSimple(simulation,hosts,stateManager);

        //设置每个范围应该保存的观测点
//        stateManager.addAllPartitionWatch(0);
//        stateManager.addPartitionWatch(1,2);
//        stateManager.addPartitionWatch(1,3);
//        stateManager.delPartitionWatch(1,3);
//        stateManager.addPartitionWatch(1,4);
        for(int i=0;i<10;i++){
            stateManager.addAllPartitionWatch(i);
        }

        //模拟host创建实例
        while (true){
            for(int i=0;i<10_000;i++){
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
            if(simulation.clock()==62){
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

        // 打印获得的状态
        System.out.println(partitionDelayState0_0.get(0));
        System.out.println(partitionDelayState1_0.get(0));
        System.out.println(partitionDelayState1_2.get(0));
        System.out.println(partitionDelayState1_4.get(0));

//        //生成新的分区范围
//        ranges=PartitionRangeManager.averageCutting(0,hostNum-1,5);
//        System.out.println(ranges);
//        //调整分区范围
//        stateManager.setPartitionRanges(ranges);
//        //设置每个范围应该保存的观测点
//        stateManager.addAllPartitionWatch(0);
//        stateManager.addPartitionWatch(1,2);
//        stateManager.addPartitionWatch(1,4);
//        stateManager.delPartitionWatch(1,4);
//        stateManager.addPartitionWatch(1,5);
//
//        //模拟host创建实例
//        simulation.setClock(simulation.clock()+1);
//        while (true){
//            for(int i=0;i<hostNum;i++){
//                hosts.get(i).createInstance(aInstance);
//            }
//            if(simulation.clock()==80){
//                break;
//            }
//            simulation.setClock(simulation.clock()+1);
//        }
//
//        //模拟host更新状态
//        for(;simulation.clock()<82;simulation.setClock(simulation.clock()+1)){
//            for(int i=10;i<hostNum;i++){
//                hosts.get(i).updateState();
//            }
//        }
//
//        partitionDelayState0_0=datacenter.getStateManager().getPartitionDelayState(0,0);
//        partitionDelayState1_0=datacenter.getStateManager().getPartitionDelayState(1,0);
//        partitionDelayState1_2=datacenter.getStateManager().getPartitionDelayState(1,2);
//        List<HostResourceState> partitionDelayState1_5=datacenter.getStateManager().getPartitionDelayState(1,5);
//
//        //打印获得的状态
//        System.out.println(partitionDelayState0_0.get(0));
//        System.out.println(partitionDelayState1_0.get(0));
//        System.out.println(partitionDelayState1_2.get(0));
//        System.out.println(partitionDelayState1_5.get(0));
//
//        //抽样状态信息
//        List<HostResourceState> simpleDelayStates=datacenter.getStateManager().getSamplingState(100);
//        System.out.println(simpleDelayStates.get(0));
//        System.out.println(simpleDelayStates.size());

        //获取结束时间
        long end = System.currentTimeMillis();
        long timeElapsed = end - start;
        System.out.println("Execution time: " + timeElapsed + " milliseconds");

        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory(); //总内存
        long freeMemory = runtime.freeMemory(); //空闲内存
        long usedMemory = totalMemory - freeMemory; //已用内存
        System.out.println("hostNum: "+hostNum);
        System.out.println("totalMemory: " + totalMemory/1000000 + " Mb");
        System.out.println("freeMemory: " + freeMemory/1000000 + " Mb");
        System.out.println("usedMemory: " + usedMemory/1000000 + " Mb");
    }
    public test(){
        fun(5_000_000);
    }
    public static void main(String[] args) {

//        new test();
        int hostNum=5_000_000;
        short cpu=100;
        ClassLayout classLayout = ClassLayout.parseInstance(new HostSimple(1,2,3,4));
        System.out.println(classLayout.toPrintable());
        List<HostSimple> hosts=new ArrayList<>();
        for(int i=0;i<hostNum;i++){
            hosts.add(new HostSimple(1,2,3,4));
        }
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory(); //总内存
        long freeMemory = runtime.freeMemory(); //空闲内存
        long usedMemory = totalMemory - freeMemory; //已用内存
        System.out.println("hostNum: "+hostNum);
        System.out.println("totalMemory: " + totalMemory/1000000 + " Mb");
        System.out.println("freeMemory: " + freeMemory/1000000 + " Mb");
        System.out.println("usedMemory: " + usedMemory/1000000 + " Mb");
    }
//        int hostNum=1_000_00;
////        new test(hostNum);
//        // 创建一个工作簿对象
//        Workbook workbook = new XSSFWorkbook();
//// 创建一个工作表对象
//        Sheet sheet = workbook.createSheet("写入示例");
//// 创建第一行并写入表头数据
//        Row row0 = sheet.createRow(0);
//        row0.createCell(0).setCellValue("姓名");
//        row0.createCell(1).setCellValue("年龄");
//        row0.createCell(2).setCellValue("性别");
//        row0.createCell(3).setCellValue("分数");
//// 创建第二行并写入数据
//        Row row1 = sheet.createRow(1);
//        row1.createCell(0).setCellValue("张三");
//        row1.createCell(1).setCellValue(18);
//        row1.createCell(2).setCellValue("男");
//        row1.createCell(3).setCellValue(90.5);
//// 创建第三行并写入数据
//        Row row2 = sheet.createRow(2);
//        row2.createCell(0).setCellValue("李四");
//        row2.createCell(1).setCellValue(19);
//        row2.createCell(2).setCellValue("女");
//        row2.createCell(3).setCellValue(95.5);
//        try {
//            // 创建一个输出流对象
//            FileOutputStream fos = new FileOutputStream("./test.xlsx");
//            // 将工作簿对象写入输出流对象
//            workbook.write(fos);
//            // 关闭工作簿对象
//            workbook.close();
//            // 关闭输出流对象
//            fos.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
}
/*
hostNum:1_000_000
Execution time: 60491 milliseconds
totalMemory: 2107 Mb
freeMemory: 196 Mb
usedMemory: 1910 Mb

hostNum:500_000
Execution time: 22597 milliseconds
totalMemory: 2107 Mb
freeMemory: 631 Mb
usedMemory: 1475 Mb

hostNum:100_000
Execution time: 4016 milliseconds
totalMemory: 1528 Mb
freeMemory: 1054 Mb
usedMemory: 474 Mb

//仅初始化 一个host为420b  一个host为88b  一个ram为40b 所以88+40*4=248b
Execution time: 2194 milliseconds
hostNum: 10000000
totalMemory: 6954 Mb
freeMemory: 2940 Mb
usedMemory: 4013 Mb

Execution time: 3525 milliseconds
hostNum: 20000000
totalMemory: 8237 Mb
freeMemory: 123 Mb
usedMemory: 8113 Mb

Execution time: 11798 milliseconds
hostNum: 50000000
totalMemory: 34359 Mb
freeMemory: 5880 Mb
usedMemory: 28479 Mb

Execution time: 8547 milliseconds
hostNum: 40000000
totalMemory: 31792 Mb
freeMemory: 8875 Mb
usedMemory: 22917 Mb

Execution time: 6300 milliseconds
hostNum: 30000000
totalMemory: 30081 Mb
freeMemory: 12926 Mb
usedMemory: 17154 Mb

Execution time: 4235 milliseconds
hostNum: 20000000
totalMemory: 27212 Mb
freeMemory: 15753 Mb
usedMemory: 11458 Mb

Execution time: 2382 milliseconds
hostNum: 10 000 000
totalMemory: 22447 Mb
freeMemory: 16571 Mb
usedMemory: 5876 Mb

Execution time: 1192 milliseconds
hostNum: 5 000 000
totalMemory: 14495 Mb
freeMemory: 11589 Mb
usedMemory: 2906 Mb

Execution time: 314 milliseconds
hostNum: 1 000 000
totalMemory: 4831 Mb
freeMemory: 4263 Mb
usedMemory: 568 Mb

//hostState初始化 一个hoststate为78B =16+8*4=48B +8(对象的引用),为什么有78B
Execution time: 0 milliseconds
hostNum: 1
totalMemory: 536 Mb
freeMemory: 532 Mb
usedMemory: 4 Mb

long:
Execution time: 59 milliseconds
hostNum: 1 000 000
totalMemory: 536 Mb
freeMemory: 460 Mb
usedMemory: 75 Mb

Execution time: 108 milliseconds
hostNum: 2000000
totalMemory: 1610 Mb
freeMemory: 1440 Mb
usedMemory: 170 Mb

Execution time: 152 milliseconds
hostNum: 3000000
totalMemory: 1610 Mb
freeMemory: 1359 Mb
usedMemory: 251 Mb

Execution time: 189 milliseconds
hostNum: 4000000
totalMemory: 1610 Mb
freeMemory: 1305 Mb
usedMemory: 305 Mb

hostNum: 5000000
totalMemory: 1610 Mb
freeMemory: 1203 Mb
usedMemory: 407 Mb

int:
Execution time: 46 milliseconds
hostNum: 1 000 000
totalMemory: 536 Mb
freeMemory: 484 Mb
usedMemory: 52 Mb

//模拟运行
Execution time: 8923 milliseconds
hostNum: 100000
totalMemory: 6677 Mb
freeMemory: 5690 Mb
usedMemory: 987 Mb

Execution time: 60077 milliseconds
hostNum: 1 000 000
totalMemory: 18471 Mb
freeMemory: 10404 Mb
usedMemory: 8067 Mb

Execution time: 124361 milliseconds
hostNum: 2000000
totalMemory: 21961 Mb
freeMemory: 9076 Mb
usedMemory: 12884 Mb

 */