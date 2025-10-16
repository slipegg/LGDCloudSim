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
    private static class RangeEntry {
        final int start;
        final int end;
        final ClosTopologyItem item;

        RangeEntry(int start, int end, ClosTopologyItem item) {
            this.start = start;
            this.end = end;
            this.item = item;
        }
    }

    // Sorted by start
    private List<RangeEntry> entries = new ArrayList<>();

    // Precomputed Clos -> [minStart, maxEnd]
    private Map<ClosTopologyItem, int[]> ClosRangeMap = new HashMap<>();

    /**
     * Construct the topology by parsing a CSV file.
     * csvFilePath can be an absolute/relative file system path or a classpath resource path.
     */
    public ClosTopologyManager(String csvFilePath) {
        if (csvFilePath == null || csvFilePath.trim().isEmpty()) {
            return;
        }

        List<String> lines = new ArrayList<>();

        Path fsPath = Paths.get(csvFilePath);
        if (Files.exists(fsPath)) {
            try (BufferedReader r = Files.newBufferedReader(fsPath, StandardCharsets.UTF_8)) {
                String line;
                while ((line = r.readLine()) != null) {
                    if (!line.isEmpty() && line.charAt(0) == '\uFEFF') {
                        line = line.substring(1);
                    }
                    lines.add(line);
                }
            } catch (IOException ignored) {}
        } else {
            // try classpath
            try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(csvFilePath)) {
                if (is != null) {
                    try (BufferedReader r = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                        String line;
                        while ((line = r.readLine()) != null) {
                            if (!line.isEmpty() && line.charAt(0) == '\uFEFF') {
                                line = line.substring(1);
                            }
                            lines.add(line);
                        }
                    }
                }
            } catch (IOException ignored) {}
        }

        if (lines.isEmpty()) return;

        // skip comments and blank lines before header
        int firstIdx = 0;
        while (firstIdx < lines.size()) {
            String t = lines.get(firstIdx);
            if (t == null) { firstIdx++; continue; }
            t = t.trim();
            if (t.isEmpty() || t.startsWith("#") || t.startsWith("//")) { firstIdx++; continue; }
            break;
        }
        if (firstIdx >= lines.size()) return;

        // detect header
        Map<String, Integer> colIdx = new HashMap<>();
        String header = lines.get(firstIdx).trim();
        String[] headerParts = header.split(",", -1);
        for (int i = 0; i < headerParts.length; i++) {
            String h = headerParts[i].trim().toLowerCase();
            if (h.equals("dcid") || h.equals("asw") || h.equals("psw") || h.equals("dsw")
                    || h.equals("startid") || h.equals("endid")) {
                colIdx.put(h, i);
            }
        }

        boolean hasHeader = colIdx.size() >= 4;
        int startLine = firstIdx + (hasHeader ? 1 : 0);

        // fallback: assume fixed order
        if (!hasHeader) {
            colIdx.put("dcid", 0);
            colIdx.put("asw", 1);
            colIdx.put("psw", 2);
            colIdx.put("dsw", 3);
            colIdx.put("startid", 4);
            colIdx.put("endid", 5);
        }

        for (int li = startLine; li < lines.size(); li++) {
            String raw = lines.get(li);
            if (raw == null) continue;
            String line = raw.trim();
            if (line.isEmpty() || line.startsWith("#") || line.startsWith("//")) continue;

            String[] parts = line.split(",", -1);
            for (int i = 0; i < parts.length; i++) parts[i] = parts[i].trim();

            try {
                int dcid = colIdx.containsKey("dcid") ? Integer.parseInt(parts[colIdx.get("dcid")]) : 0;
                String asw = parts[colIdx.get("asw")];
                String psw = parts[colIdx.get("psw")];
                String dsw = parts[colIdx.get("dsw")];
                int start = Integer.parseInt(parts[colIdx.get("startid")]);
                int end = Integer.parseInt(parts[colIdx.get("endid")]);

                ClosTopologyItem item = new ClosTopologyItem(dcid, asw, psw, dsw);
                entries.add(new RangeEntry(start, end, item));

                if (ClosRangeMap.containsKey(item)) {
                    throw new IllegalArgumentException("Duplicate ClosTopologyItem entry: " + line);
                }
                ClosRangeMap.put(item, new int[]{start, end});

            } catch (Exception ex) {
                System.err.println("[WARN] Skip malformed CSV line: " + line);
            }
        }

        Collections.sort(entries, (a, b) -> Integer.compare(a.start, b.start));
    }

    /**
     * Return the ClosTopologyItem for a given hostId, or null if not found.
     * Uses binary search on the sorted ranges for O(log n) lookup.
     */
    public ClosTopologyItem getClosTopologyItemByHostId(int hostId) {
        int lo = 0, hi = entries.size() - 1;
        while (lo <= hi) {
            int mid = (lo + hi) >>> 1;
            RangeEntry e = entries.get(mid);
            if (hostId < e.start) {
                hi = mid - 1;
            } else if (hostId > e.end) {
                lo = mid + 1;
            } else {
                return e.item;
            }
        }
        return null;
    }

    public int calculateDPSpread(InstanceGroup instanceGroup) {
        int spread = 0;
        TrainingStrategy trainingStrategy = instanceGroup.getTrainingStrategy();
        if (trainingStrategy == null) {
            return spread;
        }
        
        int dpDim = trainingStrategy.getDPDim();
        int ppDim = trainingStrategy.getPPDim();
        for(int i = 0; i < dpDim; i++) {
            HashSet<String> pswSet = new HashSet<>();
            List<Instance> instanceList = instanceGroup.getInstances();
            for(int j = 0; j < ppDim; j++) {
                Instance instance = instanceList.get(i * ppDim + j);
                int hostId = instance.getHost();
                ClosTopologyItem closItem = getClosTopologyItemByHostId(hostId);
                pswSet.add(closItem.getPSW());
            }
            if (pswSet.size() > spread) {
                spread = pswSet.size();
            }
        }
        
        return spread;
    }

    public int calculatePPSpread(InstanceGroup instanceGroup) {
        int spread = 0;
        TrainingStrategy trainingStrategy = instanceGroup.getTrainingStrategy();
        if (trainingStrategy == null) {
            return spread;
        }
        
        int dpDim = trainingStrategy.getDPDim();
        int ppDim = trainingStrategy.getPPDim();
        for(int i = 0; i < ppDim; i++) {
            HashSet<String> pswSet = new HashSet<>();
            List<Instance> instanceList = instanceGroup.getInstances();
            for(int j = 0; j < dpDim; j++) {
                Instance instance = instanceList.get(j * ppDim + i);
                int hostId = instance.getHost();
                ClosTopologyItem closItem = getClosTopologyItemByHostId(hostId);
                pswSet.add(closItem.getPSW());
            }
            if (pswSet.size() > spread) {
                spread = pswSet.size();
            }
        }
        return spread;    
    }

    public double calculateSpreadScore(InstanceGroup instanceGroup, double alpha, double beta) {
        int dpSpread = calculateDPSpread(instanceGroup);
        int ppSpread = calculatePPSpread(instanceGroup);
        return alpha * dpSpread + beta * ppSpread;  
    }

    /**
     * Return merged host id range [minStart, maxEnd] for the given ClosTopologyItem.
     * Returns null if ClosTopologyItem not found.
     */
    int[] getHostIdRangeByClos(ClosTopologyItem item) {
        if (item == null) return null;
        int[] r = ClosRangeMap.get(item);
        if (r == null) return null;
        return new int[]{r[0], r[1]};
    }   
}

