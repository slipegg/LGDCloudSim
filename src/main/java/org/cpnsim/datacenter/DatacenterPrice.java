package org.cpnsim.datacenter;

public interface DatacenterPrice {

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
