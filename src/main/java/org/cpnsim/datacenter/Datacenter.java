package org.cpnsim.datacenter;

import org.cloudsimplus.core.SimEntity;
import org.cpnsim.innerscheduler.InnerScheduler;
import org.cpnsim.interscheduler.InterScheduler;
import org.cpnsim.statemanager.StatesManager;

import java.util.List;
import java.util.Set;

public interface Datacenter extends SimEntity, DatacenterPrice {
    public static final Datacenter NULL = new DatacenterNull();

    Datacenter addCollaborationId(int collaborationId);

    Datacenter removeCollaborationId(int collaborationId);

    Set<Integer> getCollaborationIds();

    Datacenter setInterScheduler(InterScheduler interScheduler);

    Datacenter setInnerSchedulers(List<InnerScheduler> innerSchedulers);

    List<InnerScheduler> getInnerSchedulers();

    Datacenter setLoadBalance(LoadBalance loadBalance);

    LoadBalance getLoadBalance();

    Datacenter setResourceAllocateSelector(ResourceAllocateSelector resourceAllocateSelector);

    ResourceAllocateSelector getResourceAllocateSelector();

    Datacenter setStatesManager(StatesManager statesManager);

    StatesManager getStatesManager();

}
