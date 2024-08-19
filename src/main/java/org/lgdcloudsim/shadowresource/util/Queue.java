package org.lgdcloudsim.shadowresource.util;

import java.util.List;

public interface Queue<T> {
    int size();

    List<T> getBatchItem();

    List<T> getBatchItem(int num);

    List<T> getAllItem();

    Queue<T> add(T hostSR);

    Queue<T> add(List<T> hostSRs);

    boolean isEmpty();
}
