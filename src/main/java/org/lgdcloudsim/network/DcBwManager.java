package org.lgdcloudsim.network;

import lombok.Getter;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * DcBwManager manages the bandwidth between the data centers.
 * It is initialized through a csv file such as "./resources/dcBw.csv".
 * If needed, the instance group in the affinity group request can lease bandwidth between data centers.
 * When the instance group is received by the data center, the bandwidth between data centers is occupied.
 * When the instance group ends running, the bandwidth between data centers is released.
 * It also calculates the total cost of rented bandwidth for all user requests.
 *
 * @author Anonymous
 * @since LGDCloudSim 1.0
 */
public class DcBwManager {
    /**
     * The map of the bandwidth between the data centers which is initialized through a csv file.
     */
    private Map<Integer, Map<Integer, Double>> bwMap;

    /**
     * The map of the unit price of the bandwidth between the data centers which is initialized through a csv file.
     */
    private Map<Integer, Map<Integer, Double>> unitPriceMap;

    /**
     * The total cost of rented bandwidth requested by all users
     */
    @Getter
    private double bwTCO;

    /**
     * Construct a data center bandwidth manager with the csv file name and the directed flag.
     *
     * @param fileName   the csv file name.
     * @param isDirected the directed flag.
     */
    public DcBwManager(String fileName, boolean isDirected) {
        this.bwMap = new HashMap<>();
        this.unitPriceMap = new HashMap<>();
        readBwFile(fileName, isDirected);
        bwTCO = 0;
    }

    /**
     * Construct a data center bandwidth manager with the csv file name.
     * The directed flag is set to false by default.
     *
     * @param fileName the csv file name.
     */
    public DcBwManager(String fileName) {
        this(fileName, false);
    }

    /**
     * Read the bandwidth file and update the bandwidth map.
     *
     * @param fileName   the csv file name.
     * @param isDirected the directed flag.
     */
    private void readBwFile(String fileName, boolean isDirected) {
        try {
            CSVParser csvParser = new CSVParser(new FileReader(fileName), CSVFormat.DEFAULT.withHeader());

            for (CSVRecord record : csvParser) {
                int srcDcId = Integer.parseInt(record.get("srcDcId"));
                int dstDcId = Integer.parseInt(record.get("dstDcId"));
                double bandwidth = Double.parseDouble(record.get("bandwidth"));

                // Check if "unitPrice" column exists and its value is not empty
                String unitPriceString = record.get("unitPrice");
                double unitPrice = (unitPriceString != null && !unitPriceString.isEmpty()) ? Double.parseDouble(unitPriceString) : 0.0;

                // Update bwMap
                bwMap.computeIfAbsent(srcDcId, k -> new HashMap<>()).put(dstDcId, bandwidth);
                if (!isDirected) bwMap.computeIfAbsent(dstDcId, k -> new HashMap<>()).put(srcDcId, bandwidth);

                // Update unitPriceMap
                unitPriceMap.computeIfAbsent(srcDcId, k -> new HashMap<>()).put(dstDcId, unitPrice);
                if (!isDirected) unitPriceMap.computeIfAbsent(dstDcId, k -> new HashMap<>()).put(srcDcId, unitPrice);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Get the bandwidth between the source data center and the destination data center.
     *
     * @param srcDcId the source data center id.
     * @param dstDcId the destination data center id.
     * @return the bandwidth between the source data center and the destination data center.
     */
    public double getBw(int srcDcId, int dstDcId) {
        if (!bwMap.containsKey(srcDcId)) {
            throw new IllegalArgumentException("Source DC " + srcDcId + " does not exist.");
        }

        if (!bwMap.get(srcDcId).containsKey(dstDcId)) {
            throw new IllegalArgumentException("Destination DC " + dstDcId + " does not exist.");
        }

        return bwMap.get(srcDcId).get(dstDcId);
    }

    /**
     * Get the unit price of the bandwidth between the source data center and the destination data center.
     *
     * @param srcDcId the source data center id.
     * @param dstDcId the destination data center id.
     * @return the unit price of the bandwidth between the source data center and the destination data center.
     */
    public double getUnitPrice(int srcDcId, int dstDcId) {
        if (!unitPriceMap.containsKey(srcDcId)) {
            throw new IllegalArgumentException("Source DC " + srcDcId + " does not exist.");
        }

        if (!unitPriceMap.get(srcDcId).containsKey(dstDcId)) {
            throw new IllegalArgumentException("Destination DC " + dstDcId + " does not exist.");
        }

        return unitPriceMap.get(srcDcId).get(dstDcId);
    }

    /**
     * Allocate the bandwidth between the source data center and the destination data center.
     * The cost is only related to the amount of bandwidth occupied and the unit price of the broadband,
     * not to the rental time.
     * TODO: Consider the rental time when calculating broadband rental prices.
     *
     * @param srcDcId    the source data center id.
     * @param dstDcId    the destination data center id.
     * @param allocateBw the bandwidth to allocate.
     * @return whether the bandwidth is allocated successfully.
     */
    public boolean allocateBw(int srcDcId, int dstDcId, double allocateBw) {
        if (!bwMap.containsKey(srcDcId)) {
            throw new IllegalArgumentException("Source DC " + srcDcId + " does not exist.");
        }

        if (!bwMap.get(srcDcId).containsKey(dstDcId)) {
            throw new IllegalArgumentException("Destination DC " + dstDcId + " does not exist.");
        }

        double bw = bwMap.get(srcDcId).get(dstDcId);
        if (bw < allocateBw) {
            return false;
        }

        bwMap.get(srcDcId).put(dstDcId, bw - allocateBw);
        bwTCO += allocateBw * unitPriceMap.get(srcDcId).get(dstDcId);
        return true;
    }

    /**
     * Release the bandwidth between the source data center and the destination data center.
     *
     * @param srcDcId   the source data center id.
     * @param dstDcId   the destination data center id.
     * @param releaseBw the bandwidth to release.
     */
    public void releaseBw(int srcDcId, int dstDcId, double releaseBw) {
        if (!bwMap.containsKey(srcDcId)) {
            throw new IllegalArgumentException("Source DC " + srcDcId + " does not exist.");
        }

        if (!bwMap.get(srcDcId).containsKey(dstDcId)) {
            throw new IllegalArgumentException("Destination DC " + dstDcId + " does not exist.");
        }

        double bw = bwMap.get(srcDcId).get(dstDcId);
        bwMap.get(srcDcId).put(dstDcId, bw + releaseBw);
    }
}
