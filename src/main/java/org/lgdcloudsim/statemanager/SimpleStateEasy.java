package org.lgdcloudsim.statemanager;

import lombok.Getter;
import lombok.Setter;
import org.lgdcloudsim.request.Instance;

/**
 * A class implementing the {@link SimpleState} interface.
 * When synchronizing the state to the inter-scheduler,
 * it will generate a simple copy of the state, see {@link SimpleStateEasyObject}.
 *
 * @author Anonymous
 * @since LGDCloudSim 1.0
 */
@Getter
@Setter
public class SimpleStateEasy implements SimpleState {
    /**
     * The sum of the available cpu of all hosts in the datacenter.
     */
    long cpuAvailableSum;

    /**
     * The sum of the available ram of all hosts in the datacenter.
     */
    long ramAvailableSum;

    /**
     * The sum of the available storage of all hosts in the datacenter.
     */
    long storageAvailableSum;

    /**
     * The sum of the available bw of all hosts in the datacenter.
     */
    long bwAvailableSum;

    /**
     * The {@link StatesManager} it belongs to.
     */
    StatesManager statesManager;

    /**
     * Construct a new SimpleStateEasy.
     *
     * @param statesManager the {@link StatesManager} it belongs to.
     */
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
