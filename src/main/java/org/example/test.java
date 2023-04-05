package org.example;

import lombok.Getter;
import lombok.Setter;
import org.cloudsimplus.core.CloudSim;
import org.cloudsimplus.core.CloudSimTag;
import org.cloudsimplus.core.Simulation;
import org.cloudsimplus.core.events.CloudSimEvent;
import org.scalecloudsim.Instances.UserRequest;
import org.scalecloudsim.datacenters.Datacenter;
import org.scalecloudsim.datacenters.DatacenterSimple;
import org.scalecloudsim.users.User;
import org.scalecloudsim.users.UserRequestManager;
import org.scalecloudsim.users.UserRequestManagerSimple;
import org.scalecloudsim.users.UserSimple;

import java.util.List;

public class test {
    public static void main(String[] args) {
        Simulation scaleCloudSim = new CloudSim();
        UserRequestManager userRequestManager=new UserRequestManagerSimple();
        Datacenter dc1=new DatacenterSimple(scaleCloudSim,1);
        Datacenter dc2=new DatacenterSimple(scaleCloudSim,2);
        Datacenter dc3=new DatacenterSimple(scaleCloudSim,3);
        UserSimple user=new UserSimple(scaleCloudSim,1,userRequestManager);

        scaleCloudSim.start();
    }
}
