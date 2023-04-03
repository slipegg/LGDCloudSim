package org.scalecloudsim.users;

import org.scalecloudsim.Instances.*;

import java.util.ArrayList;
import java.util.List;

public class UserRequestManagerSimple implements UserRequestManager{
    @Override
    public List<UserRequest> getUserRequests(double startTime, double endTime) {
        List<UserRequest> userRequests=new ArrayList<>();
        UserRequestGenerator userRequestGenerator=new RandomUserRequestGenerator();
        for(double i=startTime;i<endTime;i+=0.1){
            UserRequest userRequest=userRequestGenerator.generateAUserRequest();
            userRequest.setSubmitTime(i);
            userRequests.add(userRequest);
        }
        return userRequests;
    }
}
