package org.lgdcloudsim.interscheduler.wxl;

import lombok.Data;
import org.lgdcloudsim.request.InstanceGroup;
import org.lgdcloudsim.request.InstanceGroupGraph;
import org.lgdcloudsim.request.UserRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author 魏鑫磊
 * @date 2024/4/27 21:42
 */
@Data
public class UserRequestDTO {

    private Integer id;

    private List<InstanceGroupDTO> instanceGroupList;

    private List<InstanceGroupArcLimitDTO> delayLimitList;

    private List<InstanceGroupArcLimitDTO> bandwidthLimitList;

    UserRequestDTO(){}
    UserRequestDTO(UserRequest userRequest, List<InstanceGroup> instanceGroups){
        this.setId(userRequest.getId());
        InstanceGroupGraph instanceGroupGraph= userRequest.getInstanceGroupGraph();
        this.setInstanceGroupList(instanceGroups.stream().map(InstanceGroupDTO::new).collect(Collectors.toList()));
        delayLimitList=new ArrayList<>();
        bandwidthLimitList=new ArrayList<>();
        for (int i = 0; i < instanceGroups.size(); i++) {
            for (int j = 0; j < instanceGroups.size(); j++) {
                if (i==j){
                    continue;
                }
                InstanceGroup instanceGroup1=instanceGroups.get(i);
                InstanceGroup instanceGroup2=instanceGroups.get(j);
                delayLimitList.add(new InstanceGroupArcLimitDTO(
                        instanceGroup1.getId(),
                        instanceGroup2.getId(),
                        instanceGroupGraph.getDelay(instanceGroup1,instanceGroup2))
                );
                bandwidthLimitList.add(new InstanceGroupArcLimitDTO(
                        instanceGroup1.getId(),
                        instanceGroup2.getId(),
                        instanceGroupGraph.getBw(instanceGroup1,instanceGroup2))
                );
            }
        }
//        System.out.println("delay");
//        for (InstanceGroupArcLimitDTO arc :
//                delayLimitList) {
//            System.out.println(arc);
//        }
//        System.out.println("bandwidth");
//        for (InstanceGroupArcLimitDTO arc :
//                bandwidthLimitList) {
//            System.out.println(arc);
//        }
    }


}
