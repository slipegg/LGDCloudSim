package org.cpnsim.statemanager;

import lombok.Getter;
import lombok.Setter;
import org.cpnsim.datacenter.Datacenter;
import org.cpnsim.datacenter.InstanceQueue;
import org.cpnsim.innerscheduler.InnerScheduler;
import org.cpnsim.request.Instance;

import java.util.*;

@Getter
@Setter
public class SimpleStateEasy implements SimpleState {
    long cpuAvailableSum;
    long ramAvailableSum;
    long storageAvailableSum;
    long bwAvailableSum;

    public SimpleStateEasy() {
        this.cpuAvailableSum = 0;
        this.ramAvailableSum = 0;
        this.storageAvailableSum = 0;
        this.bwAvailableSum = 0;
    }

    @Override
    public SimpleState initHostSimpleState(int hostId, int[] hostState) {
        cpuAvailableSum += hostState[0];
        ramAvailableSum += hostState[1];
        storageAvailableSum += hostState[2];
        bwAvailableSum += hostState[3];
        return this;
    }

    @Override
    public SimpleState updateSimpleStateAllocated(int hostId, int[] hostState, Instance instance) {
        cpuAvailableSum -= instance.getCpu();
        ramAvailableSum -= instance.getRam();
        storageAvailableSum -= instance.getStorage();
        bwAvailableSum -= instance.getBw();
        return this;
    }

    @Override
    public SimpleState updateSimpleStateReleased(int hostId, int[] hostState, Instance instance) {
        cpuAvailableSum += instance.getCpu();
        ramAvailableSum += instance.getRam();
        storageAvailableSum += instance.getStorage();
        bwAvailableSum += instance.getBw();
        return this;
    }

    @Override
    public Object generate(Datacenter datacenter) {
        long cpuAvailableSum = this.cpuAvailableSum;
        long ramAvailableSum = this.ramAvailableSum;
        long storageAvailableSum = this.storageAvailableSum;
        long bwAvailableSum = this.bwAvailableSum;
        InstanceQueue instanceQueue = datacenter.getInstanceQueue();
        for (Instance instance : instanceQueue.getAllItem(false)) {
            cpuAvailableSum -= instance.getCpu();
            ramAvailableSum -= instance.getRam();
            storageAvailableSum -= instance.getStorage();
            bwAvailableSum -= instance.getBw();
        }
        List<InnerScheduler> innerSchedulers = datacenter.getInnerSchedulers();
        for (InnerScheduler innerScheduler : innerSchedulers) {
            InstanceQueue innerInstanceQueue = innerScheduler.getInstanceQueue();
            for (Instance instance : innerInstanceQueue.getAllItem(false)) {
                cpuAvailableSum -= instance.getCpu();
                ramAvailableSum -= instance.getRam();
                storageAvailableSum -= instance.getStorage();
                bwAvailableSum -= instance.getBw();
            }
        }
        List<HostState> simpleHostStates = new ArrayList<>();
        Set<Integer> simpleHostIds = new HashSet<>();
        Random random = new Random();
        int hostNum = datacenter.getStatesManager().getHostNum();
        int hostId = -1;
        for (int i = 0; i < 100 && i < hostNum; i++) {
            hostId = random.nextInt(hostNum);
            while (simpleHostIds.contains(hostId)) {
                hostId += 1;
            }
            simpleHostIds.add(hostId);
            simpleHostStates.add(datacenter.getStatesManager().getNowHostState(hostId));
        }
        return new SimpleStateEasyObject(cpuAvailableSum, ramAvailableSum, storageAvailableSum, bwAvailableSum, simpleHostStates);
    }
}
