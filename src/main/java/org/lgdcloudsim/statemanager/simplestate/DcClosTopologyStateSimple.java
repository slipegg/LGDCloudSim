package org.lgdcloudsim.statemanager.simplestate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lgdcloudsim.datacenter.Datacenter;
import org.lgdcloudsim.network.ClosTopology;
import org.lgdcloudsim.statemanager.StatesManager;

import lombok.Getter;

@Getter
public class DcClosTopologyStateSimple {
    Datacenter originalDatacenter;
    Map<Integer, List<ClosTopologyStateSimple>> closTopologyStateSimpleMap;

    /**
     * Construct a new DcClosTopologyStateSimple.
     *
     * @param detailedDcStateSimple The detailed data center state simple.
     */
    public DcClosTopologyStateSimple(Datacenter datacenter, ClosTopology rootClosTopology) {
        this.originalDatacenter = datacenter;
        this.closTopologyStateSimpleMap = new HashMap<>();
        constructClosTopologyStateSimpleMap(rootClosTopology, datacenter.getStatesManager());
    }

    public void constructClosTopologyStateSimpleMap(ClosTopology closTopology, StatesManager statesManager) {
        if (closTopology.isLeafTopology()) {
            this.closTopologyStateSimpleMap.putIfAbsent(closTopology.getLevel(), new ArrayList<>());
            this.closTopologyStateSimpleMap.get(closTopology.getLevel()).add(new ClosTopologyStateSimple(this.originalDatacenter, closTopology));
        } else {
            for (ClosTopology childClosTopology : closTopology.getSubTopologies()) {
                constructClosTopologyStateSimpleMap(childClosTopology, statesManager);
            }

            this.closTopologyStateSimpleMap.putIfAbsent(closTopology.getLevel(), new ArrayList<>());
            this.closTopologyStateSimpleMap.get(closTopology.getLevel()).add(new ClosTopologyStateSimple(this.originalDatacenter, closTopology));
        }
    }
}
