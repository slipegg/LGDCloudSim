package org.lgdcloudsim.interscheduler.wxl;

import org.lgdcloudsim.request.InstanceGroup;
import org.lgdcloudsim.request.UserRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 魏鑫磊
 * @date 2024/4/27 15:45
 */
public class UserRequestDecoder {

    public static List<UserRequestDTO> toDTO(List<InstanceGroup> instanceGroups){
        Map<UserRequest, List<InstanceGroup>> map=new HashMap<>();
        for (InstanceGroup instanceGroup :
                instanceGroups) {
            UserRequest userRequest=instanceGroup.getUserRequest();
            if (!map.containsKey(userRequest)){
                map.put(userRequest, new ArrayList<>());
            }
            map.get(userRequest).add(instanceGroup);
        }
        List<UserRequestDTO> userRequestDTOList=new ArrayList<>();
        for (UserRequest userRequest :
                map.keySet()) {
            List<InstanceGroup> instanceGroups1=map.get(userRequest);
            userRequestDTOList.add(new UserRequestDTO(userRequest, instanceGroups1));
        }
//        System.out.println(userRequestDTOList);
        return userRequestDTOList;
    }

}
