package org.lgdcloudsim.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InstanceTopologyCondition {
    private int sameHostNum;
    private int sameS0Num;
    private int sameS1Num;
    private int sameS2Num;
    private int sameS3Num;

    public InstanceTopologyCondition() {

    }
    
    public InstanceTopologyCondition(int maxSameLevel, int num) {
        switch (maxSameLevel) {
            case -1:
                this.sameHostNum = num;
                break;
            case 0:
                this.sameS0Num = num;
                break;
            case 1:
                this.sameS1Num = num;
                break;
            case 2:
                this.sameS2Num = num;
                break;
            case 3:
                this.sameS3Num = num;
                break;
            default:
                break;
        }
    }
}
