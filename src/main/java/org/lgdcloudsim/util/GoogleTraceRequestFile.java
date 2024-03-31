package org.lgdcloudsim.util;

import lombok.Getter;
import lombok.Setter;

/**
 * A class that represents the Google trace request file.
 * It contains the google trace request CSV file path, the area where the request is from, and the row number of the file.
 *
 * @author Anonymous
 * @since LGDCloudSim 1.0
 */
@Getter
@Setter
public class GoogleTraceRequestFile {
    /**
     * The google trace request CSV file path.
     */
    private String filePath;
    /**
     * The area where the request is from.
     */
    private String area;
    /**
     * The row number of the file.
     */
    private Integer rowNum;

    /**
     * Construct the google trace request file with the file path, the area, and the row number.
     *
     * @param filePath the google trace request CSV file path.
     * @param area     the area where the request is from.
     * @param rowNum   the row number of the file.
     */
    public GoogleTraceRequestFile(String filePath, String area, Integer rowNum) {
        this.filePath = filePath;
        this.area = area;
        this.rowNum = rowNum;
    }
}
