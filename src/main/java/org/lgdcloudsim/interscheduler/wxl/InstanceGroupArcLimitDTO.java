package org.lgdcloudsim.interscheduler.wxl;

import lombok.Data;

/**
 * @author 魏鑫磊
 * @date 2024/4/27 21:41
 */
@Data
public class InstanceGroupArcLimitDTO {

    private Integer fromId;

    private Integer toId;

    private double value;

    public InstanceGroupArcLimitDTO(){}
    public InstanceGroupArcLimitDTO(Integer fromId, Integer toId, double value){
        this.fromId=fromId;
        this.toId=toId;
        this.value=value;
    }
}
