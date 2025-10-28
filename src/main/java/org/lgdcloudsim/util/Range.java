package org.lgdcloudsim.util;

import java.util.List;

public class Range {
    private int min;
    private int max;

    public Range(int min, int max) {
        this.min = min;
        this.max = max;
    }

    public int getMin() {
        return min;
    }

    public int getMax() {
        return max;
    }

    public boolean contains(int value) {
        return value >= min && value <= max;
    }

    public boolean containsAll(List<Integer> values) {
        for (int value : values) {
            if (!contains(value)) {
                return false;
            }
        }
        return true;
    }

    public int getLength() {
        return max - min;
    }

    public Range merge(Range other) {
        this.min = Math.min(this.min, other.min);
        this.max = Math.max(this.max, other.max);
        return this;
    }

    @Override
    public String toString() {
        return "[" + min +
                ", " + max +
                ']';
    }
}
