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
        List<UserRequest> userRequests0 = userRequestManager.generateOnceUserRequests().get(0);
        assertNotEquals(0, userRequests0.size());
    }
}
