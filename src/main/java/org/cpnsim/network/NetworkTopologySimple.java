package org.cpnsim.network;

import lombok.Setter;
import org.cpnsim.core.CloudInformationService;
import org.cpnsim.core.SimEntity;
import org.cpnsim.datacenter.Datacenter;
import org.cpnsim.request.UserRequest;

public class NetworkTopologySimple implements NetworkTopology {
    RegionDelayManager regionDelayManager;
    @Setter
    DelayDynamicModel delayDynamicModel;
    AreaDelayManager areaDelayManager;
    DcBwManager dcBwManager;

    public NetworkTopologySimple(String regionDelayFileName, String areaDelayFileName, String dcBwFileName) {
        this.regionDelayManager = new RegionDelayManager(regionDelayFileName);
        this.areaDelayManager = new AreaDelayManager(areaDelayFileName, regionDelayManager);
        this.dcBwManager = new DcBwManager(dcBwFileName);
    }

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
            return regionDelayManager.getDelay(srcDc.getRegion(), dstDc.getRegion());
        }
        return 0;
    }

    @Override
    public double getDynamicDelay(SimEntity src, SimEntity dst, double time) {
        double standardDelay = getDelay(src, dst);
        if(standardDelay == 0){
            return 0;
        }else{
            if (delayDynamicModel == null) {
                return standardDelay;
            } else {
                return delayDynamicModel.getDynamicDelay(src.getId(), dst.getId(), standardDelay, time);
            }
        }
    }

    @Override
    public double getBw(SimEntity src, SimEntity dst) {
        return dcBwManager.getBw(src.getId(), dst.getId());
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
}
