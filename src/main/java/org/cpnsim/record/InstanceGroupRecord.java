package org.cpnsim.record;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InstanceGroupRecord {
    private int id;
    private int userRequestId;
    private double finishTime;
    private int instanceNum;
    private int successInstanceNum;

    public InstanceGroupRecord(int id, int userRequestId, int instanceNum, int successInstanceNum) {
        this.id = id;
        this.userRequestId = userRequestId;
        this.instanceNum = instanceNum;
        this.successInstanceNum = successInstanceNum;
    }
}
