package org.lgdcloudsim.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.lgdcloudsim.request.Instance;
import org.lgdcloudsim.request.InstanceGroup;
import org.lgdcloudsim.request.TrainingStrategy;

// DCID,ASW,PSW,DSW,StartID,EndID
// 1,G6,P10,S14,0,99
// 1,G6,P10,S15,100,199
// 1,G6,P21,S21,200,299
// 1,G6,P21,S22,300,399
// 2,G7,P31,S32,400,499
public class ClosTopologyManager {
    Map<Integer, ClosTopology> closTopologyDCMap = new HashMap<>();
    public ClosTopologyManager(String closTopologyFileName) {
        Path path = Paths.get(closTopologyFileName);
        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty() || line.startsWith("#") || line.startsWith("DCID")) {
                    continue; // Skip empty lines, comments, and header
                }
                String[] parts = line.split(",");
                if (parts.length != 6) {
                    throw new IllegalArgumentException("Invalid line format: " + line);
                }
                int dcId = Integer.parseInt(parts[0].trim());
                String ASW = parts[1].trim();
                String PSW = parts[2].trim();
                String DSW = parts[3].trim();
                int startID = Integer.parseInt(parts[4].trim());
                int endID = Integer.parseInt(parts[5].trim());
                ClosTopology dcTopology;;
                if (!closTopologyDCMap.containsKey(dcId)) {
                    dcTopology = new ClosTopology("DC" + dcId, 3);
                    closTopologyDCMap.put(dcId, dcTopology);
                } else {
                    dcTopology = closTopologyDCMap.get(dcId);
                }
                dcTopology.AddClosTopology(ASW, PSW, DSW, startID, endID);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read Clos topology file: " + closTopologyFileName, e);
        }
    } 

    public ClosTopology getClosTopology(int dcId) {
        return closTopologyDCMap.get(dcId);
    }
}

