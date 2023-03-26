package org.cloudsimplus.hosts;

import org.cloudsimplus.core.Simulation;
import org.cloudsimplus.resources.Bandwidth;
import org.cloudsimplus.resources.Cpu;
import org.cloudsimplus.resources.Ram;
import org.cloudsimplus.resources.Storage;
import org.scalecloudsim.Instances.Instance;
import org.scalecloudsim.datacenters.Datacenter;
import org.scalecloudsim.resourcemanager.HostHistoryManager;
import org.scalecloudsim.resourcemanager.HostHistoryManagerSimple;
import org.scalecloudsim.resourcemanager.HostResourceStateHistory;

import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.requireNonNull;

public class HostSimple implements Host{
    Datacenter datacenter;
    static long hostnum=0;
    long id;
    Ram ram;
    Bandwidth bw;
    Storage storage;
    Cpu cpu;
    HostHistoryManager hostHistoryManager;
    List<Instance>instancesCreatedList;
    @Override
    public Simulation getSimulation() {
        return simulation;
    }
    @Override
    public Host setSimulation(Simulation simulation) {
        this.simulation = simulation;
        return this;
    }

    Simulation simulation;
    public boolean isLazySuitabilityEvaluation() {
        return lazySuitabilityEvaluation;
    }

    public void setLazySuitabilityEvaluation(boolean lazySuitabilityEvaluation) {
        this.lazySuitabilityEvaluation = lazySuitabilityEvaluation;
    }

    boolean lazySuitabilityEvaluation;
    public HostSimple(
            final long ram, final long bw, final long storage,final long cpu){
        this.setId(-1);
        this.ram=new Ram(ram);
        this.bw=new Bandwidth(bw);
        this.storage=new Storage(storage);
        this.cpu=new Cpu(cpu);
        this.hostHistoryManager=new HostHistoryManagerSimple(this);
        this.instancesCreatedList=new ArrayList<>();
        this.lazySuitabilityEvaluation=true;
    }
    @Override
    public void setId(long id) {
        this.id=id;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public HostHistoryManager getHostHistoryManager() {
        return hostHistoryManager;
    }

    @Override
    public HostSuitability createInstance(Instance instance) {
        final HostSuitability suitability=createInstanceInternal(instance);
        if(suitability.fully()){
            addInstanceCreatedList(instance);
            instance.setHost(this);
            hostHistoryManager.addHistory(getNowState());
            //还有一些操作需要模仿cloudsimplus
//            instance.
        }
        return suitability;
    }

    @Override
    public Host updateState() {
        hostHistoryManager.updateHistory(simulation.clock());
        return this;
    }

    public HostResourceStateHistory getNowState(){
        HostResourceStateHistory hostResourceStateHistory=new HostResourceStateHistory(
                ram.getAvailableResource(),
                bw.getAvailableResource(),
                storage.getAvailableResource(),
                cpu.getAvailableResource(),
                simulation.clock());
        return hostResourceStateHistory;
    }
    private HostSuitability createInstanceInternal(Instance instance){
        final HostSuitability suitability = isSuitableForInstance(instance);
        if(!suitability.fully()){
            return suitability;
        }
        allocateResourcesForInstance(instance);
        return suitability;
    }

    private HostSuitability isSuitableForInstance(Instance instance){
        final var suitability = new HostSuitability();
        suitability.setForRam(ram.isSuitable(instance.getRam()));
        if (!suitability.forRam()) {
            if(lazySuitabilityEvaluation)
                return suitability;
        }
        suitability.setForBw(bw.isSuitable(instance.getBw()));
        if (!suitability.forBw()) {
            if(lazySuitabilityEvaluation)
                return suitability;
        }
        suitability.setForStorage(storage.isSuitable(instance.getStorage()));
        if (!suitability.forStorage()) {
            if(lazySuitabilityEvaluation)
                return suitability;
        }
        suitability.setForCpu(cpu.isSuitable(instance.getCpu()));
        if (!suitability.forCpu()) {
            if(lazySuitabilityEvaluation)
                return suitability;
        }
        return suitability;
    }

    private void allocateResourcesForInstance(Instance instance){
        ram.allocateResource(instance.getRam());
        bw.allocateResource(instance.getBw());
        storage.allocateResource(instance.getStorage());
        cpu.allocateResource(instance.getCpu());
    }
    protected void addInstanceCreatedList(Instance instance){
        instancesCreatedList.add(requireNonNull(instance));
    }
    @Override
    public Datacenter getDatacenter() {
        return datacenter;
    }

    @Override
    public void setDatacenter(Datacenter datacenter) {
        this.datacenter=datacenter;
    }
}
