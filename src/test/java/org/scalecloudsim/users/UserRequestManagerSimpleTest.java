package org.scalecloudsim.users;

import org.junit.jupiter.api.Test;
import org.scalecloudsim.Instances.UserRequest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class UserRequestManagerSimpleTest {
    @Test
    public void testGetUserRequests() {
        UserRequestManager userRequestManager = new UserRequestManagerSimple();
        List<UserRequest> userRequests0 = userRequestManager.getUserRequests(0.0,5.0);
        List<UserRequest> userRequests1 = userRequestManager.getUserRequests(5.0,10.0);
        assertNotEquals(0,userRequests0.size());
        assertNotEquals(0,userRequests1.size());
        System.out.println(userRequests0);
        System.out.println(userRequests1);
    }
}
