package org.lgdcloudsim.util;

import lombok.Getter;
import org.lgdcloudsim.request.Instance;
import org.lgdcloudsim.request.UserRequest;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A class to record the failed requests and the outdated requests.
 * The failed request may need to retry if it has not exceeded the maximum number of retries.
 * The outdated request needs to be marked as failed.
 *
 * @param <T> the type of the failed request. It can be {@link Instance} or {@link org.lgdcloudsim.request.InstanceGroup}
 * @author Anonymous
 * @since LGDCloudSim 1.0
 */
public class FailedOutdatedResult<T> {
    /**
     * The list of failed requests
     */
    @Getter
    List<T> failRes;

    /**
     * The set of outdated requests
     */
    @Getter
    Set<UserRequest> outdatedRequests;

    /**
     * Create a new instance of FailedOutdatedResult with an empty list of failed requests and an empty set of outdated requests.
     */
    public FailedOutdatedResult() {
        this.failRes = new ArrayList<>();
        this.outdatedRequests = new HashSet<>();
    }

    /**
     * Create a new instance of FailedOutdatedResult with the given list of failed requests and the given set of outdated requests.
     *
     * @param failRes          the list of failed requests
     * @param outdatedRequests the set of outdated requests
     */
    public FailedOutdatedResult(List<T> failRes, Set<UserRequest> outdatedRequests) {
        this.failRes = failRes;
        this.outdatedRequests = outdatedRequests;
    }

    /**
     * Add a failed request to the list of failed requests
     * @param request the failed request
     */
    public void addFailRes(T request) {
        this.failRes.add(request);
    }

    /**
     * Add an outdated request to the set of outdated requests
     * @param userRequest the outdated request
     */
    public void addOutdatedRequests(UserRequest userRequest) {
        this.outdatedRequests.add(userRequest);
    }
}
