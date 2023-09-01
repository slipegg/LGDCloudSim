package org.cpnsim.request;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class InstanceSimpleTest {
    @Test
    public void testAddRetryNum() {
        // create a InstanceSimple object for testing
        InstanceSimple instanceSimple = new InstanceSimple(1, 1, 1, 1, 1);
        // set its retryMaxNum by 1
        instanceSimple.setRetryMaxNum(1);
        // expect: instanceSimple.getState() == UserRequest.WAITING
        assertEquals(UserRequest.WAITING, instanceSimple.getState());

        // add retryNum, so retryNum becomes 1 == retryMaxNum
        // expect: instanceSimple.getState() == UserRequest.FAILED
        instanceSimple.addRetryNum();
        assertEquals(UserRequest.FAILED, instanceSimple.getState());
    }
}
