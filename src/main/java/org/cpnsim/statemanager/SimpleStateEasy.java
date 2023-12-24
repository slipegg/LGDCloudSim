package org.cpnsim.statemanager;

import lombok.Getter;
import lombok.Setter;
import org.cpnsim.request.Instance;

@Getter
@Setter
public class SimpleStateEasy implements SimpleState {
    long cpuAvailableSum;
    long ramAvailableSum;
    long storageAvailableSum;
    long bwAvailableSum;
    StatesManager statesManager;

    public SimpleStateEasy(StatesManager statesManager) {
        this.cpuAvailableSum = 0;
        this.ramAvailableSum = 0;
        this.storageAvailableSum = 0;
        this.bwAvailableSum = 0;
        this.statesManager = statesManager;
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
    public Object generate() {
        return new SimpleStateEasyObject(statesManager.getHostNum(),
                cpuAvailableSum,ramAvailableSum,storageAvailableSum,bwAvailableSum,
                statesManager.getHostCapacityManager().getCpuCapacitySum(),
                statesManager.getHostCapacityManager().getRamCapacitySum(),
                statesManager.getHostCapacityManager().getStorageCapacitySum(),
                statesManager.getHostCapacityManager().getBwCapacitySum());
    }
}
