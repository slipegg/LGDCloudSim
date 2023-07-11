package org.cpnsim.historyrecord;

public interface DArray {
    int get(int index);

    void put(int position, int element);

    void expand(double factor);
}
