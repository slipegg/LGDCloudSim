package org.scalecloudsim.datacenter;

import org.cloudsimplus.core.SimEntity;
import org.scalecloudsim.innerscheduler.InnerScheduler;
import org.scalecloudsim.interscheduler.InterScheduler;
import org.scalecloudsim.statemanager.StateManager;

import java.util.List;
import java.util.Set;

public interface Datacenter extends SimEntity {
    Datacenter setStateManager(StateManager stateManager);

    StateManager getStateManager();

    Datacenter addCollaborationId(int collaborationId);

    Datacenter removeCollaborationId(int collaborationId);

    Set<Integer> getCollaborationIds();

    int getHostNum();

    Datacenter setInterScheduler(InterScheduler interScheduler);

    Datacenter setInnerSchedulers(List<InnerScheduler> innerSchedulers);

    List<InnerScheduler> getInnerSchedulers();

    Datacenter setLoadBalance(LoadBalance loadBalance);

    LoadBalance getLoadBalance();

    Datacenter setResourceAllocateSelector(ResourceAllocateSelector resourceAllocateSelector);

    ResourceAllocateSelector getResourceAllocateSelector();

    Datacenter setUnitCpuPrice(double unitCpuPrice);

    double getUnitCpuPrice();

    double getCpuCost();

    Datacenter setUnitRamPrice(double unitRamPrice);

    double getUnitRamPrice();

    double getRamCost();

    Datacenter setUnitStoragePrice(double unitStoragePrice);

    double getUnitStoragePrice();

    double getStorageCost();

    Datacenter setUnitBwPrice(double unitBwPrice);

    double getUnitBwPrice();

    double getBwCost();

    Datacenter setCpuNumPerRack(int cpuNumPerRack);

    int getCpuNumPerRack();

    Datacenter setUnitRackPrice(double unitRackPrice);

    double getUnitRackPrice();

    double getRackCost();

    double getAllCost();
}
