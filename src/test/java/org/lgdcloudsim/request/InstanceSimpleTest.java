package org.lgdcloudsim.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.*;

public class InstanceSimpleTest {

    private InstanceSimple instance;

    @BeforeEach
    void setUp() {
        instance = new InstanceSimple(1, 2, 4, 8, 16);
    }

    @Test
    void testConstructorWithDefaultLifecycle() {
        assertEquals(1, instance.getId());
        assertEquals(2, instance.getCpu());
        assertEquals(4, instance.getRam());
        assertEquals(8, instance.getStorage());
        assertEquals(16, instance.getBw());
        assertEquals(-1, instance.getLifecycle());
        assertNull(instance.getUserRequest());
        assertNull(instance.getInstanceGroup());
        assertEquals(-1, instance.getDestHostId());
        assertEquals(0, instance.getRetryMaxNum());
        assertEquals(-1, instance.getHost());
        assertEquals(-1, instance.getExpectedScheduleHostId());
        assertEquals(-1, instance.getStartTime());
        assertEquals(-1, instance.getFinishTime());
        assertEquals(UserRequest.WAITING, instance.getState());
        assertNull(instance.getRetryHostIds());
    }

    @Test
    void testConstructorWithCustomLifecycle() {
        InstanceSimple instanceWithCustomLifecycle = new InstanceSimple(2, 4, 8, 16, 32, 100);
        assertEquals(100, instanceWithCustomLifecycle.getLifecycle());
    }

    @Test
    void testSetId() {
        instance.setId(3);
        assertEquals(3, instance.getId());
    }

    @Test
    void testIsSetDestHost() {
        assertFalse(instance.isSetDestHost());
        instance.setDestHostId(1);
        assertTrue(instance.isSetDestHost());
    }

    @Test
    void testAddRetryNum() {
        assertFalse(instance.isFailed());
        instance.setRetryMaxNum(2);
        instance.addRetryNum();
        instance.addRetryNum();
        assertEquals(UserRequest.WAITING, instance.getState());
        assertFalse(instance.isFailed());
        instance.addRetryNum();
        assertTrue(instance.isFailed());
    }

    @Test
    void testAddRetryHostId() {
        assertNull(instance.getRetryHostIds());
        instance.addRetryHostId(1);
        List<Integer> retryHostIds = instance.getRetryHostIds();
        assertNotNull(retryHostIds);
        assertEquals(1, retryHostIds.size());
        assertEquals(1, retryHostIds.get(0));
    }

    @Test
    void testSetUserRequest() {
        UserRequest userRequest = new UserRequestSimple(1);
        instance.setUserRequest(userRequest);
        assertEquals(userRequest, instance.getUserRequest());
    }

    @Test
    void testToString() {
        String expectedToString = "InstanceSimple{" +
                "id=1, cpu=2, ram=4, storage=8, bw=16, lifecycle=-1}";
        assertEquals(expectedToString, instance.toString());
    }
}
