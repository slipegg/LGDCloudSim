package org.scalecloudsim.Instances;

import java.util.Objects;

public class InstanceGroupEdge {
    InstanceGroup src;
    InstanceGroup dst;
    double delay;
    long bw;
    public InstanceGroupEdge(InstanceGroup src,InstanceGroup dst,double delay,long bw){
        this.src= Objects.requireNonNull(src);
        this.dst=Objects.requireNonNull(dst);
        this.delay=delay;
        this.bw=bw;
    }
    public double getDelay(){
        return delay;
    }
    public long getBw(){
        return bw;
    }
    public InstanceGroup getSrc(){
        return src;
    }
    public InstanceGroup getDst(){
        return dst;
    }

//    @Override
//    public boolean equals(Object obj) {
//        if(obj==this){
//            return true;
//        }
//        if (obj == null || !(obj instanceof InstanceGroupEdge)) {
//            return false;
//        }
//        InstanceGroupEdge other=(InstanceGroupEdge) obj;
//        return this.src==other.src&&this.dst==other.dst;//这里需要斟酌
//    }
}
