package org.scalecloudsim.datacenter;

import org.cloudsimplus.core.SimEntity;
import org.cloudsimplus.core.Simulation;
import org.cloudsimplus.core.events.SimEvent;
import org.scalecloudsim.innerscheduler.InnerScheduler;
import org.scalecloudsim.interscheduler.InterScheduler;
import org.scalecloudsim.statemanager.StatesManager;

import java.util.List;
import java.util.Set;

public class DatacenterNull implements Datacenter {
    @Override
    public SimEntity setName(String newName) throws IllegalArgumentException {
        return null;
    }

    @Override
    public Simulation getSimulation() {
        return null;
    }

    @Override
    public boolean start() {
        return false;
    }

    @Override
    public void run() {

    }

    @Override
    public boolean schedule(SimEvent evt) {
        return false;
    }

    @Override
    public boolean schedule(SimEntity dest, double delay, int tag) {
        return false;
    }

    @Override
    public boolean schedule(SimEntity dest, double delay, int tag, Object data) {
        return false;
    }

    @Override
    public void processEvent(SimEvent evt) {

    }

    @Override
    public Datacenter addCollaborationId(int collaborationId) {
        return null;
    }

    @Override
    public Datacenter removeCollaborationId(int collaborationId) {
        return null;
    }

    @Override
    public Set<Integer> getCollaborationIds() {
        return null;
    }

    @Override
    public int getHostNum() {
        return 0;
    }

    @Override
    public Datacenter setInterScheduler(InterScheduler interScheduler) {
        return null;
    }

    @Override
    public Datacenter setInnerSchedulers(List<InnerScheduler> innerSchedulers) {
        return null;
    }

    @Override
    public List<InnerScheduler> getInnerSchedulers() {
        return null;
    }

    @Override
    public Datacenter setLoadBalance(LoadBalance loadBalance) {
        return null;
    }

    @Override
    public LoadBalance getLoadBalance() {
        return null;
    }

    @Override
    public Datacenter setResourceAllocateSelector(ResourceAllocateSelector resourceAllocateSelector) {
        return null;
    }

    @Override
    public ResourceAllocateSelector getResourceAllocateSelector() {
        return null;
    }

    @Override
    public Datacenter setStatesManager(StatesManager statesManager) {
        return null;
    }

    @Override
    public StatesManager getStatesManager() {
        return null;
    }

    @Override
    public int compareTo(SimEntity o) {
        return 0;
    }

    @Override
    public int getId() {
        return -1;
    }

    @Override
    public String getName() {
        return "Datcenter Null";
    }

    @Override
    public Datacenter setUnitCpuPrice(double unitCpuPrice) {
        return null;
    }

    @Override
    public double getUnitCpuPrice() {
        return 0;
    }

    @Override
    public double getCpuCost() {
        return 0;
    }

    @Override
    public Datacenter setUnitRamPrice(double unitRamPrice) {
        return null;
    }

    @Override
    public double getUnitRamPrice() {
        return 0;
    }

    @Override
    public double getRamCost() {
        return 0;
    }

    @Override
    public Datacenter setUnitStoragePrice(double unitStoragePrice) {
        return null;
    }

    @Override
    public double getUnitStoragePrice() {
        return 0;
    }

    @Override
    public double getStorageCost() {
        return 0;
    }

    @Override
    public Datacenter setUnitBwPrice(double unitBwPrice) {
        return null;
    }

    @Override
    public double getUnitBwPrice() {
        return 0;
    }

    @Override
    public double getBwCost() {
        return 0;
    }

    @Override
    public Datacenter setCpuNumPerRack(int cpuNumPerRack) {
        return null;
    }

    @Override
    public int getCpuNumPerRack() {
        return 0;
    }

    @Override
    public Datacenter setUnitRackPrice(double unitRackPrice) {
        return null;
    }

    @Override
    public double getUnitRackPrice() {
        return 0;
    }

    @Override
    public double getRackCost() {
        return 0;
    }

    @Override
    public double getAllCost() {
        return 0;
    }
}
