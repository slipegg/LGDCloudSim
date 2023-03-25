package org.scalecloudsim.resourcemanager;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.*;

public class HostHistoryManagerSimpleTest {
    @Test
    public void testAddSpecialTimeHistoryWatch(){
        Map expect=new TreeMap<Double,HostResourceStateHistory>();
        expect.put(0.0,null);
        expect.put(2.0,null);
        expect.put(3.0,null);

        HostHistoryManagerSimple manager=new HostHistoryManagerSimple();
        manager.addSpecialTimeHistoryWatch(0);
        manager.addSpecialTimeHistoryWatch(3);
        manager.addSpecialTimeHistoryWatch(2);
        Map actual=manager.getSpecialTimeStates();

        assertEquals(expect,actual);
    }

    @Test
    public void testAddHistory(){
        HostHistoryManagerSimple manager=new HostHistoryManagerSimple();
        manager.addSpecialTimeHistoryWatch(0);
        manager.addSpecialTimeHistoryWatch(2);
        manager.addSpecialTimeHistoryWatch(3);
        for(int i=0;i<5;i++){
            HostResourceStateHistory his=new HostResourceStateHistory(i,i,i,i,i);
            manager.addHistory(his);
        }
        LinkedList actual=manager.getHistory();

        LinkedList expect=new LinkedList<>();
        for(int i=1;i<5;i++){
            HostResourceStateHistory his=new HostResourceStateHistory(i,i,i,i,i);
            expect.addFirst(his);
        }

        assertEquals(expect,actual);
    }
}
