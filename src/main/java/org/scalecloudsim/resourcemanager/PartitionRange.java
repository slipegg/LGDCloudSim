package org.scalecloudsim.resourcemanager;

import java.util.Objects;

public class PartitionRange {
    int id;
    int startIndex;
    int endIndex;

    public PartitionRange(int id,int startIndex, int endIndex) {
        this.id=id;
        this.startIndex = startIndex;
        this.endIndex = endIndex;
    }
    public PartitionRange(int startIndex, int endIndex) {
        this.startIndex = startIndex;
        this.endIndex = endIndex;
        this.id=-1;
    }

    public int getStartIndex() {
        return startIndex;
    }

    public void setStartIndex(int startIndex) {
        this.startIndex = startIndex;
    }

    public int getEndIndex() {
        return endIndex;
    }

    public void setEndIndex(int endIndex) {
        this.endIndex = endIndex;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PartitionRange that = (PartitionRange) o;
        return startIndex == that.startIndex && endIndex == that.endIndex;
    }

    @Override
    public int hashCode() {
        return Objects.hash(startIndex, endIndex);
    }

    @Override
    public String toString() {
        return "PartitionRange{" +
                "id=" + id +
                ", startIndex=" + startIndex +
                ", endIndex=" + endIndex +
                '}';
    }
}
