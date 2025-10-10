package org.lgdcloudsim.network;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
class ClosTopologyItem {
    int dcId;
    String ASW;
    String PSW;
    String DSW;

    public ClosTopologyItem(int dcId, String ASW, String PSW, String DSW) {
        this.dcId = dcId;
        this.ASW = ASW;
        this.PSW = PSW;
        this.DSW = DSW;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ClosTopologyItem that = (ClosTopologyItem) obj;
        return dcId == that.dcId && ASW.equals(that.ASW) && PSW.equals(that.PSW) && DSW.equals(that.DSW);
    }

    // 保障HashMap key的正确性
    @Override
    public int hashCode() {
        return ASW.hashCode() + PSW.hashCode() + DSW.hashCode() + Integer.hashCode(dcId);
    }

    @Override
    public String toString() {
        return "ClosTopologyItem{" +
                "dcId=" + dcId +
                ", ASW='" + ASW + '\'' +
                ", PSW='" + PSW + '\'' +
                ", DSW='" + DSW + '\'' +
                '}';
    }
}