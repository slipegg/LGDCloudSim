package org.lgdcloudsim.user;

import org.lgdcloudsim.request.UserRequest;
import org.lgdcloudsim.util.GoogleTraceRequestFile;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class UserRequestManagerGoogleTraceTest {
    @Test
    public void testGetUserRequest() {
        int MAX_CPU_CAPACITY = 100000;
        int MAX_RAM_CAPACITY = 100000;
        int STORAGE_CAPACITY = 100;
        int BW_CAPACITY = 100;
        int LIFE_TIME_MEAN = -1;
        int LIFE_TIME_STD = 0;
        double ACCESS_LATENCY_PERCENTAGE = 1;
        double ACCESS_LATENCY_MEAN = 100;
        double ACCESS_LATENCY_STD = 20;
        boolean IS_EDGE_DIRECTED = false;
        double EDGE_PERCENTAGE = 1;
        double EDGE_DELAY_MEAN = 200;
        double EDGE_DELAY_STD = 10;
        double EDGE_BW_MEAN = 10;
        double EDGE_BW_STD = 1;
        int INSTANCE_GROUP_RETRY_TIMES = 0;
        int INSTANCE_RETRY_TIMES = 0;
        Map<Integer, GoogleTraceRequestFile> GOOGLE_TRACE_REQUEST_FILE_DC_MAP = new HashMap<>() {{
            put(1, new GoogleTraceRequestFile("./src/main/resources/experiment/googleTrace/userRequest/2019_a_user_requests.csv", "United States", 5000));
            put(2, new GoogleTraceRequestFile("./src/main/resources/experiment/googleTrace/userRequest/2019_b_user_requests.csv", "United States", 5000));
            put(3, new GoogleTraceRequestFile("./src/main/resources/experiment/googleTrace/userRequest/2019_c_user_requests.csv", "United States", 5000));
            put(4, new GoogleTraceRequestFile("./src/main/resources/experiment/googleTrace/userRequest/2019_d_user_requests.csv", "United States", 5000));
            put(5, new GoogleTraceRequestFile("./src/main/resources/experiment/googleTrace/userRequest/2019_e_user_requests.csv", "United States", 5000));
            put(6, new GoogleTraceRequestFile("./src/main/resources/experiment/googleTrace/userRequest/2019_f_user_requests.csv", "United States", 5000));
            put(7, new GoogleTraceRequestFile("./src/main/resources/experiment/googleTrace/userRequest/2019_g_user_requests.csv", "United States", 5000));
            put(8, new GoogleTraceRequestFile("./src/main/resources/experiment/googleTrace/userRequest/2019_h_user_requests.csv", "United States", 5000));
        }};
        UserRequestManagerGoogleTrace userRequestManager = new UserRequestManagerGoogleTrace(GOOGLE_TRACE_REQUEST_FILE_DC_MAP, MAX_CPU_CAPACITY, MAX_RAM_CAPACITY, STORAGE_CAPACITY, BW_CAPACITY, LIFE_TIME_MEAN, LIFE_TIME_STD, INSTANCE_GROUP_RETRY_TIMES, INSTANCE_RETRY_TIMES,
                ACCESS_LATENCY_PERCENTAGE, ACCESS_LATENCY_MEAN, ACCESS_LATENCY_STD, IS_EDGE_DIRECTED, EDGE_PERCENTAGE, EDGE_DELAY_MEAN, EDGE_DELAY_STD, EDGE_BW_MEAN, EDGE_BW_STD);

        Map<Integer, List<UserRequest>> userRequests = userRequestManager.generateOnceUserRequests();
        assertNotNull(userRequests);
    }
}
