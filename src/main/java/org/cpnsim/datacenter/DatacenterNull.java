package org.cpnsim.datacenter;

import org.cloudsimplus.core.SimEntity;
import org.cloudsimplus.core.Simulation;
import org.cloudsimplus.core.events.SimEvent;
import org.cpnsim.innerscheduler.InnerScheduler;
import org.cpnsim.interscheduler.InterScheduler;
import org.cpnsim.request.InstanceGroup;
import org.cpnsim.statemanager.StatesManager;

import java.awt.geom.Point2D;
import java.util.List;
import java.util.Set;

/**
 * A class to implements the {@link Datacenter} interface.
 * It is a NULL datacenter.
 * It is used to avoid NullPointerException.
 *
 * @author Jiawen Liu
 * @since CPNSim 1.0
 */
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
    public Set<Integer> getCollaborationIds() {
        return null;
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
    public Datacenter setCentralizedInterSchedule(boolean centralizedInterSchedule) {
        return null;
    }

    @Override
    public boolean isCentralizedInterSchedule() {
        return false;
    }

    @Override
    public double getEstimatedTCO(InstanceGroup instanceGroup) {
        return 0;
    }

    @Override
    public InstanceQueue getInstanceQueue() {
        return null;
    }

    @Override
    public String getRegion() {
        return null;
    }

    @Override
    public Datacenter setRegion(String region) {
        return null;
    }

    @Override
    public Point2D getLocation() {
        return null;
    }

    @Override
    public Datacenter setLocation(double latitude, double longitude) {
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
    public double getResourceCost() {
        return 0;
    }

    @Override
    public double getAllCost() {
        return 0;
    }

    @Override
    public DatacenterPrice setBwBillingType(String bwBillingType) {
        return null;
    }

    @Override
    public String getBwBillingType() {
        return null;
    }

    @Override
    public DatacenterPrice setBwUtilization(double bwUtilization) {
        return null;
    }

    @Override
    public double getBwUtilization() {
        return 0;
    }

    @Override
    public double getTCOEnergy() {
        return 0;
    }

    @Override
    public double getTCORack() {
        return 0;
    }
}
