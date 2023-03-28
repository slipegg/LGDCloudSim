package org.scalecloudsim.resourcemanager;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class HostHistoryManagerSimpleTest {
    List<HostHistoryManagerSimple> hostmanagers;
    int hostNum;
    /**
     * init 100000 host
     */
    HostHistoryManagerSimpleTest(){
        hostmanagers=new ArrayList<>();
        hostNum=100000;
        for(int i=0;i<hostNum;i++){
            HostHistoryManagerSimple manager=new HostHistoryManagerSimple();
            manager.addDelayWatch(0);
            manager.addDelayWatch(2);
            manager.addDelayWatch(3);
            hostmanagers.add(manager);
        }
    }

    /**
     * 测试添加历史记录
     */
    @Test
    public void testAddSpecialTimeHistoryWatch(){
        Map expect=new TreeMap<Double,HostResourceStateHistory>();
        expect.put(0.0,new HostResourceStateHistory(-1,-1,-1,-1,-1));
        expect.put(2.0,new HostResourceStateHistory(-1,-1,-1,-1,-1));
        expect.put(3.0,new HostResourceStateHistory(-1,-1,-1,-1,-1));

        HostHistoryManagerSimple manager=new HostHistoryManagerSimple();
        manager.addDelayWatch(0);
        manager.addDelayWatch(3);
        manager.addDelayWatch(2);
        Map actual=manager.getDelayStates();

        assertEquals(expect,actual);
        assertEquals(3.0,manager.getHistoryRange());
    }
    /**
     * 测试添加历史记录
     */
    @Test
    public void testAddHistory(){
        HostHistoryManagerSimple manager=new HostHistoryManagerSimple();
        manager.addDelayWatch(0);
        manager.addDelayWatch(2);
        manager.addDelayWatch(3);
        for(int i=0;i<10;i+=2){
            HostResourceStateHistory his=new HostResourceStateHistory(i,i,i,i,i);
            manager.addHistory(his);
        }
        LinkedList actual1=manager.getHistory();
        HostResourceState actualWatch0=manager.getSpecialTimeHostState(0);
        HostResourceState actualWatch2=manager.getSpecialTimeHostState(2);
        HostResourceState actualWatch3=manager.getSpecialTimeHostState(3);

        LinkedList expect=new LinkedList<>();
        for(int i=4;i<=8;i+=2){
            HostResourceStateHistory his=new HostResourceStateHistory(i,i,i,i,i);
            expect.addFirst(his);
        }

        assertEquals(expect,actual1);
        assertEquals(expect.get(0),actualWatch0);
        assertEquals(expect.get(1),actualWatch2);
        assertEquals(expect.get(2),actualWatch3);
    }
    /**
     * test the speed of the addHistory method.
     */
    @Test
    @Timeout(10)
    public void testAddHistorySpeed(){
        //模拟60s，每秒有100k个主机的历史状态信息更新。
        for(int i=0;i<60;i++){
            for(int j=0;j<hostNum;j++){
                HostResourceStateHistory his=new HostResourceStateHistory(i,i,i,i,i);
                hostmanagers.get(j).addHistory(his);
            }
        }
    }
}
