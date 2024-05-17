package org.lgdcloudsim.interscheduler.wxl;

import lombok.Data;
import org.lgdcloudsim.datacenter.Datacenter;
import org.lgdcloudsim.statemanager.SimpleStateEasyObject;

/**
 * @author 魏鑫磊
 * @date 2024/5/1 20:27
 */
@Data
public class DatacenterAvailableDTO {

    private Integer id;

    private Long availableCpu;

    private Long availableSto;

}
