package org.example;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
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

//        try {
//            Thread.sleep(10000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }

//        //获取分区范围
//        List<PartitionRange> ranges=PartitionRangeManager.averageCutting(0,hostNum-1,10);
//        //打印分区范围
////        System.out.println(ranges);
//        //初始化状态管理器
//        StateManager stateManager=new StateManagerSimple(ranges);
//        //初始化模拟器
//        Simulation simulation=new Cloudsim();
//        //初始化数据中心
//        Datacenter datacenter=new DatacenterSimple(simulation,hosts,stateManager);
//
//        //设置每个范围应该保存的观测点
//        stateManager.addAllPartitionWatch(0);
////        stateManager.addPartitionWatch(1,2);
////        stateManager.addPartitionWatch(1,3);
////        stateManager.delPartitionWatch(1,3);
////        stateManager.addPartitionWatch(1,4);
////        for(int i=0;i<10;i++){
////            stateManager.addAllPartitionWatch(i);
////        }
//
//        //模拟host创建实例
//        while (true){
//            for(int i=0;i<hostNum;i++){
//                hosts.get(i).createInstance(aInstance);
//            }
//            if(simulation.clock()==60){
//                break;
//            }
//            simulation.setClock(simulation.clock()+1);
//        }
//        //模拟host更新状态
//        simulation.setClock(simulation.clock()+1);
//        while (true){
//            for(int i=10;i<hostNum;i++){
//                hosts.get(i).updateState();
//            }
//            if(simulation.clock()==62){
//                break;
//            }
//            simulation.setClock(simulation.clock()+1);
//        }
//
//
//        //模拟获取一片分区的状态
//        long start2 = System.currentTimeMillis();
//        List<HostResourceState> partitionDelayState0_0=datacenter.getStateManager().getPartitionDelayState(0,0);
//        List<HostResourceState> partitionDelayState1_0=datacenter.getStateManager().getPartitionDelayState(1,0);//partitionStateManager1.getPartitionDelayState(0);
//        List<HostResourceState> partitionDelayState1_2=datacenter.getStateManager().getPartitionDelayState(1,2);
//        List<HostResourceState> partitionDelayState1_4=datacenter.getStateManager().getPartitionDelayState(1,4);
//        long end2 = System.currentTimeMillis();
//        long timeElapsed2 = end2 - start2;
//        System.out.println("getPartitionDelayState time: " + timeElapsed2 + " milliseconds");

        //打印获得的状态
//        System.out.println(partitionDelayState0_0.get(0));
//        System.out.println(partitionDelayState1_0.get(0));
//        System.out.println(partitionDelayState1_2.get(0));
//        System.out.println(partitionDelayState1_4.get(0));

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
        System.out.println("totalMemory: " + totalMemory/1000000 + " Mb");
        System.out.println("freeMemory: " + freeMemory/1000000 + " Mb");
        System.out.println("usedMemory: " + usedMemory/1000000 + " Mb");
    }
    public test(){
//        for(int i=500_000;i<=5_000_000;i+=500_000){
//            System.out.println("hostNum: "+i);
//            fun(i);
//        }
        fun(5_000_000);
    }
    public static void main(String[] args) {
        new test();
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
 */