package org.cpnsim.util;

import lombok.Getter;
import lombok.Setter;

public class GoogleTraceRequestFile {
    @Setter
    @Getter
    private String filePath;
    @Setter
    @Getter
    private String area;
    @Setter
    @Getter
    private Integer rowNum;

    public GoogleTraceRequestFile(String filePath, String area, Integer rowNum) {
        this.filePath = filePath;
        this.area = area;
        this.rowNum = rowNum;
    }
}
