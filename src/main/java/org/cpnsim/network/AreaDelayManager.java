package org.cpnsim.network;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class AreaDelayManager {
    private Map<String, Map<String, Double>> areaDelayMap;
    private RegionDelayManager regionDelayManager;

    AreaDelayManager(String fileName, RegionDelayManager regionDelayManager) {
        this.areaDelayMap = new RegionDelayManager(fileName).getRegionDelayMap();
        this.regionDelayManager = regionDelayManager;

        readAreaDelayFile(fileName);
    }

    private void readAreaDelayFile(String fileName) {
        try {
            CSVParser csvParser = new CSVParser(new FileReader(fileName), CSVFormat.DEFAULT.withHeader());

            // Get the header/column names from the CSV file
            String[] columnNames = csvParser.getHeaderMap().keySet().toArray(new String[0]);

            for (CSVRecord record : csvParser) {
                updateAreaDelayMap(record, columnNames);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateAreaDelayMap(CSVRecord record, String[] columnNames) {
        String area = record.get(columnNames[0]);

        Map<String, Double> regionDelay = new HashMap<>();

        String nearestRegion = "";
        double minDelay = Double.MAX_VALUE;
        for (int i = 1; i < columnNames.length; i++) {
            String region = columnNames[i];
            String delayString = record.get(region);

            if (delayString != null && !delayString.isEmpty()) {
                double delay = Double.parseDouble(delayString);
                regionDelay.put(region, delay);

                if (delay < minDelay) {
                    minDelay = delay;
                    nearestRegion = region;
                }
            }
        }

        if (!regionDelay.isEmpty()) {
            completeRegionDelayMap(regionDelay, nearestRegion, minDelay);
            // Add regionDelay to areaDelayMap
            areaDelayMap.put(area, regionDelay);
        }

    }

    private void completeRegionDelayMap(Map<String, Double> regionDelay, String nearestRegion, double minDelay) {
        for (String region : regionDelayManager.getRegions()) {
            if (!regionDelay.containsKey(region)) {
                double delay = regionDelayManager.getDelay(nearestRegion, region) + minDelay;
                regionDelay.put(region, delay);
            }
        }
    }

    public double getDelay(String sourceArea, String destinationRegion) {
        if (!areaDelayMap.containsKey(sourceArea)) {
            throw new IllegalArgumentException("Source area " + sourceArea + " does not exist.");
        }

        if (!areaDelayMap.get(sourceArea).containsKey(destinationRegion)) {
            throw new IllegalArgumentException("Destination region " + destinationRegion + " does not exist.");
        }

        return areaDelayMap.get(sourceArea).get(destinationRegion);
    }
}
