package org.cpnsim.user;

import org.junit.jupiter.api.Test;
import org.cpnsim.request.UserRequest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class UserRequestManagerSimpleTest {
    @Test
    public void testGetUserRequests() {
        UserRequestManager userRequestManager = new UserRequestManagerSimple();
        List<UserRequest> userRequests0 = userRequestManager.getUserRequestMap(0.0, 5.0, 1);
        List<UserRequest> userRequests1 = userRequestManager.getUserRequestMap(5.0, 10.0, 1);
        assertNotEquals(0, userRequests0.size());
        assertNotEquals(0, userRequests1.size());
        System.out.println(userRequests0);
        System.out.println(userRequests1);
    }
}
