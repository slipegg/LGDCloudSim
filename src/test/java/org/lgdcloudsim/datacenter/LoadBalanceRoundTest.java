//package org.lgdcloudsim.datacenter;
//
//import org.lgdcloudsim.core.CloudSim;
//import org.lgdcloudsim.core.Simulation;
//import org.lgdcloudsim.intrascheduler.IntraScheduler;
//import org.lgdcloudsim.intrascheduler.IntraSchedulerSimple;
//import org.lgdcloudsim.request.Instance;
//import org.lgdcloudsim.request.InstanceSimple;
//import org.junit.jupiter.api.Test;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//
//import static org.junit.Assert.assertEquals;
//
//public class LoadBalanceRoundTest {
//    @Test
//    void testLoadBalanceRound() {
//        Simulation simulation = new CloudSim();
//        Datacenter datacenter = new DatacenterSimple(simulation);
//
//        List<IntraScheduler> intraSchedulers = new ArrayList<>();
//        for (int i = 0; i < 3; i++) {
//            Map<Integer, Double> partitionDelay = Map.of(1, 0.0);
//            IntraScheduler intraScheduler = new IntraSchedulerSimple(partitionDelay);
//            intraSchedulers.add(intraScheduler);
//        }
//        datacenter.setIntraSchedulers(intraSchedulers);
//
//        LoadBalance loadBalance = new LoadBalanceRound();
//        datacenter.setLoadBalance(loadBalance);
//
//        List<Instance> instances = new ArrayList<>();
//        for (int i = 0; i < 20; i++) {
//            Instance instance = new InstanceSimple(i, i, i, i, i);
//            instances.add(instance);
//        }
//
//        loadBalance.setLoadBalanceCostTime(0.1);
//        assertEquals(0.1, loadBalance.getLoadBalanceCostTime(), 0.01);
//
////        Set<InnerScheduler> innerSchedulerList1 = loadBalance.sendInstances(instances);
////        List<InnerScheduler> exceptedInnerSchedulerList1 = List.of(innerSchedulers.get(0), innerSchedulers.get(1), innerSchedulers.get(2));
////        assertEquals(exceptedInnerSchedulerList1, innerSchedulerList1);
////
////        Set<InnerScheduler> innerSchedulerList2 = loadBalance.sendInstances(instances);
////        List<InnerScheduler> exceptedInnerSchedulerList2 = List.of(innerSchedulers.get(1), innerSchedulers.get(2), innerSchedulers.get(0));
////        assertEquals(exceptedInnerSchedulerList2, innerSchedulerList2);
//    }
//}
