package org.lgdcloudsim.datacenter;

import org.lgdcloudsim.conflicthandler.ConflictHandler;
import org.lgdcloudsim.core.SimEntity;
import org.lgdcloudsim.core.Simulation;
import org.lgdcloudsim.core.events.SimEvent;
import org.lgdcloudsim.intrascheduler.IntraScheduler;
import org.lgdcloudsim.interscheduler.InterScheduler;
import org.lgdcloudsim.loadbalancer.LoadBalancer;
import org.lgdcloudsim.queue.InstanceQueue;
import org.lgdcloudsim.request.InstanceGroup;
import org.lgdcloudsim.statemanager.StatesManager;

import java.awt.geom.Point2D;
import java.util.List;
import java.util.Set;

/**
 * A class to implements the {@link Datacenter} interface.
 * It is a NULL datacenter.
 * It is used to avoid NullPointerException.
 *
 * @author Anonymous
 * @since LGDCloudSim 1.0
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
    public Datacenter setIntraSchedulers(List<IntraScheduler> intraSchedulers) {
        return null;
    }

    @Override
    public List<IntraScheduler> getIntraSchedulers() {
        return null;
    }

    @Override
    public Datacenter setLoadBalancer(LoadBalancer loadBalancer) {
        return null;
    }

    @Override
    public LoadBalancer getLoadBalancer() {
        return null;
    }

    @Override
    public Datacenter setConflictHandler(ConflictHandler conflictHandler) {
        return null;
    }

    @Override
    public ConflictHandler getConflictHandler() {
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
    public Datacenter setCentralizedInterScheduleFlag(boolean centralizedInterScheduleFlag) {
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
    public String getArchitecture() {
        return null;
    }

    @Override
    public Datacenter setArchitecture(String architecture) {
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
    public Datacenter setPricePerCpuPerSec(double pricePerCpuPerSec) {
        return null;
    }

    @Override
    public double getPricePerCpuPerSec() {
        return 0;
    }

    @Override
    public Datacenter setPricePerCpu(double pricePerCpu) {
        return null;
    }

    @Override
    public double getPricePerCpu() {
        return 0;
    }

    @Override
    public double getCpuCost() {
        return 0;
    }

    @Override
    public Datacenter setPricePerRamPerSec(double pricePerRamPerSec) {
        return null;
    }

    @Override
    public double getPricePerRamPerSec() {
        return 0;
    }

    @Override
    public Datacenter setPricePerRam(double pricePerRam) {
        return null;
    }

    @Override
    public double getPricePerRam() {
        return 0;
    }

    @Override
    public double getRamCost() {
        return 0;
    }

    @Override
    public Datacenter setPricePerStoragePerSec(double pricePerStoragePerSec) {
        return null;
    }

    @Override
    public double getPricePerStoragePerSec() {
        return 0;
    }

    @Override
    public Datacenter setPricePerStorage(double pricePerStorage) {
        return null;
    }

    @Override
    public double getPricePerStorage() {
        return 0;
    }

    @Override
    public double getStorageCost() {
        return 0;
    }

    @Override
    public Datacenter setPricePerBwPerSec(double pricePerBwPerSec) {
        return null;
    }

    @Override
    public double getPricePerBwPerSec() {
        return 0;
    }

    @Override
    public Datacenter setPricePerBw(double pricePerBw) {
        return null;
    }

    @Override
    public double getPricePerBw() {
        return 0;
    }

    @Override
    public double getBwCost() {
        return 0;
    }

    @Override
    public Datacenter setHostNumPerRack(double hostNumPerRack) {
        return null;
    }

    @Override
    public double getHostNumPerRack() {
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
}
