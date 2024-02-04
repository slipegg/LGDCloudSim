package org.cpnsim.datacenter;

import lombok.Getter;
import org.cpnsim.request.Instance;
import org.cpnsim.request.UserRequest;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FailedOutdatedResult<T> {
    @Getter
    List<T> failRes;
    @Getter
    Set<UserRequest> outdatedRequests;

    public FailedOutdatedResult() {
        this.failRes = new ArrayList<>();
        this.outdatedRequests = new HashSet<>();
    }

    public FailedOutdatedResult(List<T> failRes, Set<UserRequest> outdatedRequests) {
        this.failRes = failRes;
        this.outdatedRequests = outdatedRequests;
    }

    public void addFailRes(T request) {
        this.failRes.add(request);
    }

    public void addOutdatedRequests(UserRequest userRequest) {
        this.outdatedRequests.add(userRequest);
    }
}
