package org.cpnsim.record;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserRequestRecord {
    private int id;
    private int instanceGroupNum;
    private int successInstanceGroupNum;
    private double finishTime;

    public UserRequestRecord(int id, int instanceGroupNum, int successInstanceGroupNum) {
        this.id = id;
        this.instanceGroupNum = instanceGroupNum;
        this.successInstanceGroupNum = successInstanceGroupNum;
    }
}
