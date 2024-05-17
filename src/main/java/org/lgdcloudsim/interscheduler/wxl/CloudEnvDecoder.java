package org.lgdcloudsim.interscheduler.wxl;

import org.lgdcloudsim.datacenter.Datacenter;
import org.lgdcloudsim.network.NetworkTopology;
import org.lgdcloudsim.statemanager.SimpleStateEasyObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author 魏鑫磊
 * @date 2024/4/27 22:31
 */
public class CloudEnvDecoder {

    public static CloudEnvDTO toDTO(Map<Datacenter, Object> interScheduleSimpleStateMap, NetworkTopology networkTopology, List<Datacenter> datacenterList){
//        List<Long> cpuAvailableList=new ArrayList<>();
//        List<Long> stoAvailableList=new ArrayList<>();
        List<ArcDTO> delayRelationList=new ArrayList<>();
        List<ArcDTO> bandwidthRelationList=new ArrayList<>();
        List<DatacenterAvailableDTO> datacenterDTOList=new ArrayList<>();

        for (Datacenter datacenter :
                datacenterList) {
            SimpleStateEasyObject simpleStateEasyObject = (SimpleStateEasyObject) interScheduleSimpleStateMap.get(datacenter);

            DatacenterAvailableDTO datacenterAvailableDTO=new DatacenterAvailableDTO();
            datacenterAvailableDTO.setId(datacenter.getId());
            datacenterAvailableDTO.setAvailableCpu(simpleStateEasyObject.getCpuAvailableSum());
            datacenterAvailableDTO.setAvailableSto(simpleStateEasyObject.getStorageAvailableSum());
            datacenterDTOList.add(datacenterAvailableDTO);
        }

        for (int i = 0; i < datacenterList.size(); i++) {
            for (int j = 0; j < datacenterList.size(); j++) {
                if (i==j){
                    continue;
                }
                Datacenter datacenter1=datacenterList.get(i);
                Datacenter datacenter2=datacenterList.get(j);
                delayRelationList.add(new ArcDTO(datacenter1.getId(),datacenter2.getId(),networkTopology.getDelay(datacenter1,datacenter2)));
                bandwidthRelationList.add(new ArcDTO(datacenter1.getId(),datacenter2.getId(),networkTopology.getBw(datacenter1,datacenter2)));
            }
        }
        return new CloudEnvDTO(
//                cpuAvailableList,
//                stoAvailableList,
                datacenterDTOList,
                delayRelationList,
                bandwidthRelationList
        );
    }
}
