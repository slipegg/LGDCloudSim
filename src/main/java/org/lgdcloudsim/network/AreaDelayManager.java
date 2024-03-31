package org.lgdcloudsim.network;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * AreaDelayManager manages the delay between the area where the user is located and the region where the data center is located.
 * Each delay is initialized through a csv file.
 * We have compiled the default network delay data through
 * <a href="https://cloud.google.com/network-intelligence-center/docs/performance-dashboard/how-to/view-google-cloud-latency">the Google Data Center Network Dataset</a>
 * in ./resources/areaDelay.csv.
 * areaDelay.csv does not have data from each area to all regions.
 * The delay from area to region without data =
 * the minimum value of (area to region with data + region with data to region without data)
 * The delay between the regions is managed by the {@link RegionDelayManager}.
 *
 * @author Anonymous
 * @since LGDCloudSim 1.0
 */
public class AreaDelayManager {
    /**
     * The map of the delay between the area where the user is located and the region where the data center is located
     * which is initialized through a csv file.
     * It may not contain the delay from each area to all regions.
     */
    private Map<String, Map<String, Double>> areaDelayMap;

    /**
     * The region delay manager which manages the delay between the regions where the data centers are located.
     */
    private RegionDelayManager regionDelayManager;

    /**
     * Construct an area delay manager with the csv file name and the region delay manager.
     *
     * @param fileName           the csv file name.
     * @param regionDelayManager the region delay manager.
     */
    AreaDelayManager(String fileName, RegionDelayManager regionDelayManager) {
        this.areaDelayMap = new RegionDelayManager(fileName).getRegionDelayMap();
        this.regionDelayManager = regionDelayManager;

        readAreaDelayFile(fileName);
    }

    /**
     * Read the area delay file and update the area delay map.
     *
     * @param fileName the csv file name.
     */
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

    /**
     * Update the area delay map with the record and the column names.
     *
     * @param record      the record of the csv file.
     * @param columnNames the column names of the csv file.
     */
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

    /**
     * Complete the region delay map with the nearest region, the minimum delay and the region delay.
     *
     * @param regionDelay   the region delay map.
     * @param nearestRegion the nearest region.
     * @param minDelay      the minimum delay.
     */
    private void completeRegionDelayMap(Map<String, Double> regionDelay, String nearestRegion, double minDelay) {
        for (String region : regionDelayManager.getRegions()) {
            if (!regionDelay.containsKey(region)) {
                double delay = regionDelayManager.getDelay(nearestRegion, region) + minDelay;
                regionDelay.put(region, delay);
            }
        }
    }

    /**
     * Get the delay between the area where the user is located and the region where the data center is located.
     *
     * @param sourceArea        the area where the user is located.
     * @param destinationRegion the region where the data center is located.
     * @return the delay.
     */
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
