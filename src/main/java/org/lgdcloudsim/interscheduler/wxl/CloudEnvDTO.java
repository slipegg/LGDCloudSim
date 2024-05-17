package org.lgdcloudsim.interscheduler.wxl;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.lgdcloudsim.datacenter.Datacenter;

import java.util.List;

/**
 * @author 魏鑫磊
 * @date 2024/4/27 22:20
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CloudEnvDTO {

//    private List<Long> cpuAvailableList;
//
//    private List<Long> stoAvailableList;

    private List<DatacenterAvailableDTO> datacenterList;

    private List<ArcDTO> delayRelationList;

    private List<ArcDTO> bandwidthRelationList;

}
