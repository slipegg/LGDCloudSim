package org.cpnsim.datacenter;

/**
 * An interface to be implemented by each class that to record the price of the datacenter.
 *
 * @author Jiawen Liu
 * @since CPNSim 1.0
 */
public interface DatacenterPrice {
    /**
     * Setting the unit price of cpu resources
     *
     * @param unitCpuPrice the unit price of cpu resources
     */
    Datacenter setUnitCpuPrice(double unitCpuPrice);

    /**
     * Getting the unit price of cpu resources
     *
     * @return the unit price of cpu resources
     */
    double getUnitCpuPrice();

    /**
     * Getting the cost of cpu resources
     *
     * @return the cost of cpu resources
     */
    double getCpuCost();

    /**
     * Setting the unit price of ram resources
     *
     * @param unitRamPrice the unit price of ram resources
     */
    Datacenter setUnitRamPrice(double unitRamPrice);

    /**
     * Getting the unit price of ram resources
     *
     * @return the unit price of ram resources
     */
    double getUnitRamPrice();

    /**
     * Getting the cost of ram resources
     *
     * @return the cost of ram resources
     */
    double getRamCost();

    /**
     * Setting the unit price of storage resources
     *
     * @param unitStoragePrice the unit price of storage resources
     */
    Datacenter setUnitStoragePrice(double unitStoragePrice);

    /**
     * Getting the unit price of storage resources
     *
     * @return the unit price of storage resources
     */
    double getUnitStoragePrice();

    /**
     * Getting the cost of storage resources
     *
     * @return the cost of storage resources
     */
    double getStorageCost();

    /**
     * Setting the unit price of bandwidth resources
     *
     * @param unitBwPrice the unit price of bandwidth resources
     */
    Datacenter setUnitBwPrice(double unitBwPrice);

    /**
     * Getting the unit price of bandwidth resources
     *
     * @return the unit price of bandwidth resources
     */
    double getUnitBwPrice();

    /**
     * Getting the cost of bandwidth resources
     *
     * @return the cost of bandwidth resources
     */
    double getBwCost();

    /**
     * Setting the number of cpu resources per rack
     *
     * @param cpuNumPerRack the number of cpu resources per rack
     */
    Datacenter setCpuNumPerRack(int cpuNumPerRack);

    /**
     * Getting the number of cpu resources per rack
     *
     * @return the number of cpu resources per rack
     */
    int getCpuNumPerRack();

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

    double getTCOEnergy();

    double getTCORack();
}
