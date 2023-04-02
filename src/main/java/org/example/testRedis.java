package org.example;
import org.cloudsimplus.hosts.HostSimple;
import org.scalecloudsim.resourcemanager.HostResourceStateHistory;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class testRedis {
    public static void main(String[] args) {
        //连接本地的 Redis 服务
//        Jedis jedis = new Jedis("localhost");
//        System.out.println("Connection to server successfully");
        double start = System.currentTimeMillis();
        int hostNum=50_000_000;
        LinkedList<HostResourceStateHistory> history=new LinkedList<>();
        for(int i=0;i<hostNum;i++){
            history.add(new HostResourceStateHistory(1,2,3,4,6));
        }
        int[] arr = new int[hostNum*4];
        arr[0]=1;
//        List<Integer> hostState = new ArrayList<>();;
//        for(int i=0; i<num; i++) {
//            hostState.add(i);
//        }
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory(); //总内存
        long freeMemory = runtime.freeMemory(); //空闲内存
        long usedMemory = totalMemory - freeMemory; //已用内存
        System.out.println("hostNum: "+hostNum);
        System.out.println("totalMemory: " + totalMemory/1000000 + " Mb");
        System.out.println("freeMemory: " + freeMemory/1000000 + " Mb");
        System.out.println("usedMemory: " + usedMemory/1000000 + " Mb");
        //存储数据到列表中
//        for(Integer i=0; i<num*4; i++) {
//            jedis.lpush("scale", i.toString());
//        }

//        List<Integer> myList = Arrays.asList(1, 2, 3, 4, 5);
//        System.out.println(myList.toString());
//        jedis.rpush("myList", myList.toString());
//        jedis.set("java","123");
        // 获取存储的数据并输出
//        List<String> list = jedis.lrange("scale", 0 ,num*4);
//        System.out.println(list.size());
//        System.out.println(list.get(num));
//        List<Integer> list = redisTemplate.opsForList().range(key, 0, -1);
//        for(int i=0; i<num; i++) {
//            System.out.println("Stored string in redis:: "+list.get(i));
//        }
        double end = System.currentTimeMillis();
        System.out.println("耗时："+(end-start)+"ms");
    }
}

/*
获取1M的数据耗时：1137.0ms

二维数组
hostNum: 50000000
totalMemory: 3338 Mb
freeMemory: 1430 Mb
usedMemory: 1908 Mb
耗时：3042.0ms

一维数组，通过乘4的方法

hostNum: 50000000
totalMemory: 1333 Mb
freeMemory: 526 Mb
usedMemory: 807 Mb
耗时：359.0ms

LinkedList<HostResourceStateHistory> history来存储，一个数据80B，所以100k*30s*80B=240MB
hostNum: 50000000
totalMemory: 7541 Mb
freeMemory: 3336 Mb
usedMemory: 4204 Mb
耗时：12860.0ms

 */