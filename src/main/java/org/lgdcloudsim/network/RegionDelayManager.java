package org.lgdcloudsim.network;

import lombok.Getter;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * RegionDelayManager manages the delay between the regions where the data centers are located.
 * Each delay is initialized through a csv file.
 * We have compiled the default network delay data through
 * <a href="https://cloud.google.com/network-intelligence-center/docs/performance-dashboard/how-to/view-google-cloud-latency">the Google Data Center Network Dataset</a>
 * in ./resources/regionDelay.csv.
 * Note: Since there is no network delay between different data centers in the same region in the data set,
 * there is only the network delay between the same data center in the same region.
 * Therefore, we added a 20ms delay to the delay within the same data center
 * to represent the network delay between different data centers in the same region.
 * Since the cloud manager does not have a region attribute,
 * the delay between the data center and cloud manager({@link org.lgdcloudsim.core.CloudInformationService})
 * is the average delay between all regions.
 *
 * @author Anonymous
 * @since LGDCloudSim 1.0
 */
public class RegionDelayManager {
    /**
     * The map of the delay between the regions where the data centers are located
     * which is initialized through a csv file.
     */
    @Getter
    private Map<String, Map<String, Double>> regionDelayMap;

    /**
     * The average delay between regions.
     */
    @Getter
    private double averageDelay;

    /**
     * Construct a region delay manager with the csv file name.
     *
     * @param fileName the csv file name.
     */
    public RegionDelayManager(String fileName) {
        this.regionDelayMap = new HashMap<>();

        readRegionDelayFile(fileName);
    }

    /**
     * Read the region delay file and update the regionDelayMap.
     *
     * @param fileName the csv file name.
     */
    private void readRegionDelayFile(String fileName) {
        try (FileReader fileReader = new FileReader(fileName);
             CSVParser csvParser = CSVFormat.DEFAULT.withHeader().withDelimiter(',').parse(fileReader)) {

            double delaySum = 0;
            int regionCount = 0;

            // Get the header/column names from the CSV file
            String[] header = csvParser.getHeaderMap().keySet().toArray(new String[0]);
            for (int i = 0; i < header.length; i++) {
                header[i] = header[i].trim();
            }

            // Read the data line by line
            for (CSVRecord csvRecord : csvParser) {
                String region = csvRecord.get(0);
                Map<String, Double> delayMap = new HashMap<>();

                // Start from the second column, because the first column is the region name
                for (int i = 1; i < csvRecord.size(); i++) {
                    String destinationRegion = header[i];
                    if (!csvRecord.get(i).equals("")) {
                        double delay = Double.parseDouble(csvRecord.get(i));
                        delayMap.put(destinationRegion, delay);
                        delaySum += delay;
                        regionCount++;
                    }
                }

                regionDelayMap.put(region, delayMap);
                averageDelay = delaySum / regionCount;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Get the delay between the source region and the destination region.
     *
     * @param sourceRegion      the source region.
     * @param destinationRegion the destination region.
     * @return the delay between the source region and the destination region.
     */
    public double getDelay(String sourceRegion, String destinationRegion) {
        if (!regionDelayMap.containsKey(sourceRegion)) {
            throw new IllegalArgumentException("Source region " + sourceRegion + " does not exist.");
        }

        if (!regionDelayMap.get(sourceRegion).containsKey(destinationRegion)) {
            throw new IllegalArgumentException("Destination region " + destinationRegion + " does not exist.");
        }

        return regionDelayMap.get(sourceRegion).get(destinationRegion);
    }

    /**
     * Get the regions in the region delay map.
     *
     * @return the regions in the region delay map.
     */
    public Set<String> getRegions() {
        return regionDelayMap.keySet();
    }
}
