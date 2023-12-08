package org.cpnsim.network;

import lombok.Getter;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.FileReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class RegionDelayManager {
    @Getter
    private Map<String, Map<String, Double>> regionDelayMap;
    @Getter
    private double averageDelay;

    public RegionDelayManager(String fileName) {
        this.regionDelayMap = new HashMap<>();

        readRegionDelayFile(fileName);
    }

    private void readRegionDelayFile(String fileName) {
        try (FileReader fileReader = new FileReader(fileName);
             CSVParser csvParser = CSVFormat.DEFAULT.withHeader().withDelimiter(',').parse(fileReader)) {

            double delaySum = 0;
            int regionCount = 0;

            // 获取表头，去除前后空格
            String[] header = csvParser.getHeaderMap().keySet().toArray(new String[0]);
            for (int i = 0; i < header.length; i++) {
                header[i] = header[i].trim();
            }

            // 逐行读取数据
            for (CSVRecord csvRecord : csvParser) {
                String region = csvRecord.get(0);
                Map<String, Double> delayMap = new HashMap<>();

                // 从第二列开始，因为第一列是区域名称
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

    public double getDelay(String sourceRegion, String destinationRegion) {
        if (!regionDelayMap.containsKey(sourceRegion)) {
            throw new IllegalArgumentException("Source region " + sourceRegion + " does not exist.");
        }

        if (!regionDelayMap.get(sourceRegion).containsKey(destinationRegion)) {
            throw new IllegalArgumentException("Destination region " + destinationRegion + " does not exist.");
        }

        return regionDelayMap.get(sourceRegion).get(destinationRegion);
    }

    public Set<String> getRegions() {
        return regionDelayMap.keySet();
    }

    public static void main(String[] args) {
        String REGION_DELAY_FILE = "./src/main/resources/regionDelay.csv";
        RegionDelayManager regionDelayManager = new RegionDelayManager(REGION_DELAY_FILE);
        System.out.println(regionDelayManager.getRegionDelayMap());
        System.out.println(regionDelayManager.getDelay("africa-south1", "asia-east1"));
    }
}
