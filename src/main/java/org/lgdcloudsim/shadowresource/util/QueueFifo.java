package org.lgdcloudsim.shadowresource.util;

import java.util.LinkedList;
import java.util.List;

public class QueueFifo<T> implements Queue<T> {
    private LinkedList<T> items;

    private int defaultBatchNum;

    public QueueFifo(int defaultBatchNum) {
        items = new LinkedList<>();
        this.defaultBatchNum = defaultBatchNum;
    }

    public QueueFifo() {
        this(100);
    }

    @Override
    public int size() {
        return items.size();
    }

    @Override
    public List<T> getBatchItem() {
        return getBatchItem(defaultBatchNum);
    }

    @Override
    public List<T> getBatchItem(int num) {
        List<T> result = new LinkedList<>();

        for (int i = 0; i < num; i++) {
            if (items.isEmpty()) {
                break;
            }
            result.add(items.remove(0));
        }

        return result;
    }

    @Override
    public List<T> getAllItem() {
        int size = items.size();
        return getBatchItem(size);
    }

    @Override
    public Queue<T> add(T item) {
        items.add(item);
        return this;
    }

    @Override
    public Queue<T> add(List<T> hostSRs) {
        items.addAll(hostSRs);
        return this;
    }

    @Override
    public boolean isEmpty() {
        return items.isEmpty();
    }   
}
