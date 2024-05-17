package org.lgdcloudsim.interscheduler.wxl;

import lombok.Data;

/**
 * @author 魏鑫磊
 * @date 2024/4/27 22:26
 */
@Data
public class ArcDTO {

    private Integer fromId;

    private Integer toId;

    private double value;

    public ArcDTO(){}
    public ArcDTO(Integer fromId, Integer toId, double value){
        this.fromId=fromId;
        this.toId=toId;
        this.value=value;
    }

}
