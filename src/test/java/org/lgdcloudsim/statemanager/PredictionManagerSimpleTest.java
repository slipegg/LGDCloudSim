package org.lgdcloudsim.statemanager;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;

public class PredictionManagerSimpleTest {
    @Test
    void testPredictionManagerSimple() {
        List<HostStateHistory> hostStateHistories = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            HostStateHistory hostStateHistory = new HostStateHistory(i, i, i, i, i);
            hostStateHistories.add(hostStateHistory);
        }
        PredictionManager predictionManager = new PredictionManagerSimple();
        int[] predictedState = predictionManager.predictHostState(hostStateHistories);
        int[] expectedState = {4, 4, 4, 4};
        assertArrayEquals(expectedState, predictedState);
    }
}
