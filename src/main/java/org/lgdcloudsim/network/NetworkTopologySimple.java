package org.lgdcloudsim.network;

import lombok.Setter;

import java.util.Set;

import org.lgdcloudsim.core.CloudInformationService;
import org.lgdcloudsim.core.SimEntity;
import org.lgdcloudsim.datacenter.Datacenter;
import org.lgdcloudsim.request.UserRequest;

/**
 * NetworkTopologySimple is a simple implementation of the network topology.
 *
 * @author Anonymous
 * @since LGDCloudSim 1.0
 */
public class NetworkTopologySimple implements NetworkTopology {
    /**
     * The region delay manager.
     */
    RegionDelayManager regionDelayManager;

    /**
     * The delay dynamic model.
     */
    @Setter
    DelayDynamicModel delayDynamicModel;

    /**
     * The area delay manager.
     */
    AreaDelayManager areaDelayManager;

    /**
     * The data center bandwidth manager.
     */
    DcBwManager dcBwManager;

    /**
     * Construct a network topology with the region delay file name, the area delay file name and the data center bandwidth file name.
     *
     * @param regionDelayFileName the region delay file name.
     * @param areaDelayFileName   the area delay file name.
     * @param dcBwFileName        the data center bandwidth file name.
     */
    public NetworkTopologySimple(String regionDelayFileName, String areaDelayFileName, String dcBwFileName) {
        this.regionDelayManager = new RegionDelayManager(regionDelayFileName);
        this.areaDelayManager = new AreaDelayManager(areaDelayFileName, regionDelayManager);
        this.dcBwManager = new DcBwManager(dcBwFileName);
    }

    /**
     * Construct a network topology with the region delay file name, the area delay file name and the data center bandwidth file name.
     *
     * @param regionDelayFileName the region delay file name.
     * @param areaDelayFileName   the area delay file name.
     * @param dcBwFileName        the data center bandwidth file name.
     * @param isDirected          true if the data center bandwidth is directed, false otherwise.
     */
    public NetworkTopologySimple(String regionDelayFileName, String areaDelayFileName, String dcBwFileName, boolean isDirected) {
        this.regionDelayManager = new RegionDelayManager(regionDelayFileName);
        this.areaDelayManager = new AreaDelayManager(areaDelayFileName, regionDelayManager);
        this.dcBwManager = new DcBwManager(dcBwFileName, isDirected);
    }

    @Override
    public double getDelay(SimEntity src, SimEntity dst) {
        if (src instanceof CloudInformationService || dst instanceof CloudInformationService) {
            return regionDelayManager.getAverageDelay();
        }
        if (src instanceof Datacenter srcDc && dst instanceof Datacenter dstDc) {
            double delay = regionDelayManager.getDelay(srcDc.getRegion(), dstDc.getRegion());
            if (src == dst) {
                delay -= 20;
            }
            return delay;
        }
        return 0;
    }

    @Override
    public double getDynamicDelay(SimEntity src, SimEntity dst, double time) {
        double standardDelay = getDelay(src, dst);
        if (standardDelay == 0) {
            return 0;
        } else {
            if (delayDynamicModel == null) {
                return standardDelay;
            } else {
                return delayDynamicModel.getDynamicDelay(src.getId(), dst.getId(), standardDelay, time);
            }
        }
    }

    @Override
    public double getBw(Integer src, Integer dst) {
        return dcBwManager.getBw(src, dst);
    }

    @Override
    public double getBw(SimEntity src, SimEntity dst) {
        return dcBwManager.getBw(src.getId(), dst.getId());
    }

    @Override
    public double getUnitPrice(Integer src, Integer dst) {
        return dcBwManager.getUnitPrice(src, dst);
    }

    @Override
    public double getUnitPrice(SimEntity src, SimEntity dst) {
        return dcBwManager.getUnitPrice(src.getId(), dst.getId());
    }

    @Override
    public boolean allocateBw(SimEntity src, SimEntity dst, double allocateBw) {
        return dcBwManager.allocateBw(src.getId(), dst.getId(), allocateBw);
    }

    @Override
    public NetworkTopology releaseBw(SimEntity src, SimEntity dst, double releaseBw) {
        dcBwManager.releaseBw(src.getId(), dst.getId(), releaseBw);
        return this;
    }

    @Override
    public double getNetworkTCO() {
        return dcBwManager.getBwTCO();
    }

    @Override
    public double getAccessLatency(UserRequest userRequest, Datacenter datacenter) {
        return areaDelayManager.getDelay(userRequest.getArea(), datacenter.getRegion());
    }

    @Override
    public Set<Integer> getDcIdList() {
        return dcBwManager.getDcIdList();
    }
}
