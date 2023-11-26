package org.cpnsim.network;

import lombok.Getter;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class DcBwManager {
    private Map<Integer, Map<Integer, Double>> bwMap;
    private Map<Integer, Map<Integer, Double>> unitPriceMap;
    @Getter
    private double bwTCO;

    public DcBwManager(String fileName, boolean isDirected) {
        this.bwMap = new HashMap<>();
        this.unitPriceMap = new HashMap<>();
        readBwFile(fileName, isDirected);
        bwTCO = 0;
    }

    public DcBwManager(String fileName) {
        this(fileName, false);
    }

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

    public double getBw(int srcDcId, int dstDcId) {
        if (!bwMap.containsKey(srcDcId)) {
            throw new IllegalArgumentException("Source DC " + srcDcId + " does not exist.");
        }

        if (!bwMap.get(srcDcId).containsKey(dstDcId)) {
            throw new IllegalArgumentException("Destination DC " + dstDcId + " does not exist.");
        }

        return bwMap.get(srcDcId).get(dstDcId);
    }

    public double getUnitPrice(int srcDcId, int dstDcId) {
        if (!unitPriceMap.containsKey(srcDcId)) {
            throw new IllegalArgumentException("Source DC " + srcDcId + " does not exist.");
        }

        if (!unitPriceMap.get(srcDcId).containsKey(dstDcId)) {
            throw new IllegalArgumentException("Destination DC " + dstDcId + " does not exist.");
        }

        return unitPriceMap.get(srcDcId).get(dstDcId);
    }

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
