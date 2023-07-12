package org.cpnsim.historyrecord;

import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DArraySimple implements DArray {
    private List<Integer> arrayRange;
    private List<int[]> arraysPointer;
    private int[] array;
    @Getter
    private int capacity;

    public DArraySimple() {
        this.capacity = 100;
        this.array = new int[this.capacity];
        this.arrayRange = new ArrayList<>();
        this.arrayRange.add(0);
        this.arraysPointer = new ArrayList<>();
        this.arraysPointer.add(this.array);
    }

    public DArraySimple(int capacity) {
        this.capacity = capacity;
        this.array = new int[this.capacity];
        this.arrayRange = new ArrayList<>();
        this.arrayRange.add(0);
        this.arraysPointer = new ArrayList<>();
        this.arraysPointer.add(this.array);
    }

    @Override
    public int get(int index) {
        if (index >= this.capacity) {
            throw new IllegalArgumentException("index must be less than size");
        }
        int arrayIndex = Collections.binarySearch(this.arrayRange, index);
        if (arrayIndex < 0) {
            arrayIndex = -(arrayIndex + 2);
        }
        return this.arraysPointer.get(arrayIndex)[index - arrayRange.get(arrayIndex)];
    }

    @Override
    public void put(int position, int element) {
        if (position >= this.capacity) {
            throw new IllegalArgumentException("position must be less than capacity");
        }
        int arrayIndex = Collections.binarySearch(this.arrayRange, position);
        if (arrayIndex < 0) {
            arrayIndex = -(arrayIndex + 2);
        }
        this.arraysPointer.get(arrayIndex)[position - arrayRange.get(arrayIndex)] = element;
    }

    @Override
    public void expand(double factor) {
        if (factor <= 1) {
            throw new IllegalArgumentException("factor must be greater than 1");
        }
        int[] newArray = new int[(int) (this.capacity * (factor - 1))];
        this.arraysPointer.add(newArray);
        this.arrayRange.add(this.capacity);
        this.capacity = (int) (this.capacity * factor);
    }
}
