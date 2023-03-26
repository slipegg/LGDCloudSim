package org.scalecloudsim.datacenters;

import org.cloudsimplus.core.Simulation;
import org.cloudsimplus.hosts.Host;
import org.scalecloudsim.resourcemanager.PartitionRange;
import org.scalecloudsim.resourcemanager.StateManager;
import org.scalecloudsim.resourcemanager.StateManagerSimple;

import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.requireNonNull;

public class DatacenterSimple implements Datacenter{
    List<? extends Host>  hostList;
    StateManager stateManager;
    Simulation simulation;
    public DatacenterSimple(Simulation simulation, final List<? extends Host> hostList){
        this.simulation=simulation;
        this.hostList=requireNonNull(hostList);
        setHostList();
        PartitionRange partitionRange=new PartitionRange(0, hostList.size()-1,0);
        List<PartitionRange> partitionRanges= new ArrayList<>();
        stateManager=new StateManagerSimple(partitionRanges);
    }

    public DatacenterSimple(Simulation simulation,final List<? extends Host> hostList,StateManager stateManager){
        this.simulation=simulation;
        this.hostList=requireNonNull(hostList);
        setHostList();
        setStateManager(stateManager);
    }

    private void setHostList(){
        long lastHostId = getLastHostId();
        for (final Host host : hostList) {
            lastHostId = setupHost(host, lastHostId);
        }
    }
    protected long setupHost(final Host host, long nextId) {
        nextId = Math.max(nextId, -1);
        if(host.getId() < 0) {
            host.setId(++nextId);
        }
        host.setSimulation(simulation).setDatacenter(this);
//        host.setActive(((HostSimple)host).isActivateOnDatacenterStartup());
        return nextId;
    }
    private long getLastHostId() {
        return hostList.isEmpty() ? -1 : hostList.get(hostList.size()-1).getId();
    }
    @Override
    public List getHostList() {
        return hostList;
    }

    @Override
    public Datacenter setStateManager(StateManager stateManager) {
        this.stateManager=stateManager;
        stateManager.setDatacenter(this);
        return this;
    }

    @Override
    public StateManager getStateManager() {
        return stateManager;
    }
}
