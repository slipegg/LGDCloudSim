package org.cpnsim.datacenter;

import org.cloudsimplus.core.SimEntity;
import org.cpnsim.innerscheduler.InnerScheduler;
import org.cpnsim.interscheduler.InterScheduler;
import org.cpnsim.statemanager.StatesManager;

import java.util.List;
import java.util.Set;

/**
 * An interface to be implemented by each class that represents a datacenter.
 * It extends {@link SimEntity} and {@link DatacenterPrice}.
 *
 * @author Jiawen Liu
 * @since CPNSim 1.0
 */
public interface Datacenter extends SimEntity, DatacenterPrice {
    /**
     * the NULL datacenter.
     **/
    public static final Datacenter NULL = new DatacenterNull();

    /** Get the collaborationIds to which the datacenter belongs. **/
    Set<Integer> getCollaborationIds();

    /** Set the {@link InterScheduler}. **/
    Datacenter setInterScheduler(InterScheduler interScheduler);

    /** Set the {@link InnerScheduler}. **/
    Datacenter setInnerSchedulers(List<InnerScheduler> innerSchedulers);

    /** Get the {@link InterScheduler}. **/
    List<InnerScheduler> getInnerSchedulers();

    /** Set the {@link LoadBalance}. **/
    Datacenter setLoadBalance(LoadBalance loadBalance);

    /** Get the {@link LoadBalance}. **/
    LoadBalance getLoadBalance();

    /** Set the {@link ResourceAllocateSelector}. **/
    Datacenter setResourceAllocateSelector(ResourceAllocateSelector resourceAllocateSelector);

    /** Get the {@link ResourceAllocateSelector}. **/
    ResourceAllocateSelector getResourceAllocateSelector();

    /** Set the {@link StatesManager}. **/
    Datacenter setStatesManager(StatesManager statesManager);

    /** Get the {@link StatesManager}. **/
    StatesManager getStatesManager();

}
