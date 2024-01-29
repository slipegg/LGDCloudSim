package org.cpnsim.datacenter;


import lombok.Getter;
import org.cpnsim.request.UserRequest;

import java.util.List;
import java.util.Set;

public class QueueResult<T> {
    @Getter
    List<T> waitScheduledItems;
    @Getter
    Set<UserRequest> outDatedItems;

    public QueueResult(List<T> waitScheduledItems, Set<UserRequest> outDatedItems) {
        this.waitScheduledItems = waitScheduledItems;
        this.outDatedItems = outDatedItems;
    }

    public QueueResult add(QueueResult queueResult) {
        this.waitScheduledItems.addAll(queueResult.getWaitScheduledItems());
        this.outDatedItems.addAll(queueResult.getOutDatedItems());
        return this;
    }

    public int getWaitScheduledItemsSize() {
        return waitScheduledItems.size();
    }

}
