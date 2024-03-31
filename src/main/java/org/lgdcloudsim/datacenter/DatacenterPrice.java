package org.lgdcloudsim.datacenter;

import org.lgdcloudsim.request.Instance;

/**
 * An interface to be implemented by each class that to record the price of the datacenter.
 *
 * @author Anonymous
 * @since LGDCloudSim 1.0
 */
public interface DatacenterPrice {
    /**
     * Setting the unit price of cpu resources per second
     *
     * @param pricePerCpuPerSec the unit price of cpu resources per second
     *
     */
    Datacenter setPricePerCpuPerSec(double pricePerCpuPerSec);

    /**
     * Getting the unit price of cpu resources per second
     *
     * @return the unit price of cpu resources per second
     */
    double getPricePerCpuPerSec();

    /**
     * Setting the price of renting a cpu
     *
     * @param pricePerCpu the price of renting a cpu
     */
    Datacenter setPricePerCpu(double pricePerCpu);

    /**
     * Getting the price of renting a cpu
     *
     * @return the price of renting a cpu
     */
    double getPricePerCpu();

    /**
     * Getting the cost of cpu resources
     *
     * @return the cost of cpu resources
     */
    double getCpuCost();

    /**
     * Setting the unit price of ram resources per second
     *
     * @param pricePerRamPerSec the unit price of ram resources per second
     */
    Datacenter setPricePerRamPerSec(double pricePerRamPerSec);

    /**
     * Getting the unit price of ram resources per second
     *
     * @return the unit price of ram resources per second
     */
    double getPricePerRamPerSec();

    /**
     * Setting the price of renting a ram
     *
     * @param pricePerRam the price of renting a ram
     */
    Datacenter setPricePerRam(double pricePerRam);

    /**
     * Getting the price of renting a ram
     *
     * @return the price of renting a ram
     */
    double getPricePerRam();

    /**
     * Getting the cost of ram resources
     *
     * @return the cost of ram resources
     */
    double getRamCost();

    /**
     * Setting the unit price of storage resources per second
     *
     * @param pricePerStoragePerSec the unit price of storage resources per second
     */
    Datacenter setPricePerStoragePerSec(double pricePerStoragePerSec);

    /**
     * Getting the unit price of storage resources per second
     *
     * @return the unit price of storage resources per second
     */
    double getPricePerStoragePerSec();

    /**
     * Setting the price of renting a storage
     *
     * @param pricePerStorage the price of renting a storage
     */
    Datacenter setPricePerStorage(double pricePerStorage);

    /**
     * Getting the price of renting a storage
     *
     * @return the price of renting a storage
     */
    double getPricePerStorage();

    /**
     * Getting the cost of storage resources
     *
     * @return the cost of storage resources
     */
    double getStorageCost();

    /**
     * Setting the unit price of bandwidth resources per second
     *
     * @param pricePerBwPerSec the unit price of bandwidth resources per second
     */
    Datacenter setPricePerBwPerSec(double pricePerBwPerSec);

    /**
     * Getting the unit price of bandwidth resources per second
     *
     * @return the unit price of bandwidth resources per second
     */
    double getPricePerBwPerSec();

    /**
     * Setting the price of renting bandwidth
     *
     * @param pricePerBw the price of renting bandwidth
     */
    Datacenter setPricePerBw(double pricePerBw);

    /**
     * Getting the price of renting bandwidth
     *
     * @return the price of renting bandwidth
     */
    double getPricePerBw();

    /**
     * Getting the cost of bandwidth resources
     *
     * @return the cost of bandwidth resources
     */
    double getBwCost();

    /**
     * Setting the number of cpu resources per rack
     *
     * @param hostNumPerRack the number of cpu resources per rack
     */
    Datacenter setHostNumPerRack(double hostNumPerRack);

    /**
     * Getting the number of cpu resources per rack
     *
     * @return the number of cpu resources per rack
     */
    double getHostNumPerRack();

    /**
     * Set the price of renting a rack
     *
     * @param unitRackPrice the price of renting a rack
     */
    Datacenter setUnitRackPrice(double unitRackPrice);

    /**
     * Get the price of renting a rack
     *
     * @return the price of renting a rack
     */
    double getUnitRackPrice();

    /**
     * A host needs to pay the basic rental cost to power on,
     * and here we calculate the cost caused by the maximum number of hosts that need to be powered on
     * throughout the entire process
     * rackCost = maxPowerOnHostNum / hostNumPerRack * unitRackPrice
     *
     * @return the cost of renting racks
     */
    double getRackCost();

    /**
     * Calculate the price of cpu,ram,storage,bandwidth resources spent on running all instances
     *
     * @return the cost of renting racks
     */
    double getResourceCost();

    /**
     * Calculate the RackCost+ResourceCost
     *
     * @return the total cost of the datacenter
     */
    double getAllCost();

    /**
     * Set the billing type of bandwidth
     * It can be "used" or "fixed"
     * <ul>
     *     <li>used: the cost of bandwidth will be calculated according the amount of network data used:(instance.getBw() * lifeTimeSec) / 8 / 1024 * bwUtilization * unitBwPrice </li>
     *     <li>fixed: the cost of bandwidth will be calculated according the network bandwidth speed and time used: instance.getBw() * unitBwPrice * lifeTimeSec</li>
     * </ul>
     *
     * @param bwBillingType the billing type of bandwidth
     * @return the datacenter
     * @see org.lgdcloudsim.datacenter.DatacenterSimple#calculateInstanceBwCost(Instance)
     */
    DatacenterPrice setBwBillingType(String bwBillingType);

    /**
     * Get the billing type of bandwidth
     *
     * @return the billing type of bandwidth
     */
    String getBwBillingType();

    /**
     * Set the utilization of bandwidth.
     * It is used to calculate the cost of bandwidth when the billing type is "used"
     * @param bwUtilization the utilization of bandwidth
     * @return the datacenter
     */
    DatacenterPrice setBwUtilization(double bwUtilization);

    /**
     * Get the utilization of bandwidth
     * @return the utilization of bandwidth
     */
    double getBwUtilization();
}
