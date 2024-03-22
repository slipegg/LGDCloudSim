package org.lgdcloudsim.user;

import org.lgdcloudsim.request.UserRequest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class UserRequestManagerSimpleTest {

    @Test
    void testGenerateOnceUserRequests() {
        UserRequestManagerSimple userRequestManagerSimple = new UserRequestManagerSimple();
        Map<Integer, List<UserRequest>> userRequestMap = userRequestManagerSimple.generateOnceUserRequests();

        assertNotNull(userRequestMap);
        assertEquals(1, userRequestMap.size());
        List<UserRequest> userRequests = userRequestMap.get(1);
        assertNotNull(userRequests);
        // Add more assertions as needed
    }

    @Test
    void testGetNextSendTime() {
        UserRequestManagerSimple userRequestManagerSimple = new UserRequestManagerSimple();
        double nextSendTime = userRequestManagerSimple.getNextSendTime();

        assertEquals(100.0, nextSendTime);
    }
}
