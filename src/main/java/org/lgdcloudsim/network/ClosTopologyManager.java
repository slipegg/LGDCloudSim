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
import java.util.List;
import java.util.Map;

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
    private final List<RangeEntry> entries = new ArrayList<>();

    // Precomputed Clos -> [minStart, maxEnd]
    private final Map<ClosTopologyItem, int[]> ClosRangeMap = new HashMap<>();

    /**
     * Construct the topology by parsing a CSV file.
     * csvFilePath can be an absolute/relative file system path or a classpath resource path.
     */
    public ClosTopologyManager (String csvFilePath) {
        if (csvFilePath == null || csvFilePath.trim().isEmpty()) {
            return;
        }

        List<String> lines = new ArrayList<>();

        Path fsPath = Paths.get(csvFilePath);
        if (Files.exists(fsPath)) {
            try (BufferedReader r = Files.newBufferedReader(fsPath, StandardCharsets.UTF_8)) {
                String line;
                while ((line = r.readLine()) != null) {
                    lines.add(line);
                }
            } catch (IOException ignored) {
                // fall through to try classpath
            }
        }

        if (lines.isEmpty()) {
            // try classpath resource
            InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(csvFilePath);
            if (is != null) {
                try (BufferedReader r = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = r.readLine()) != null) {
                        lines.add(line);
                    }
                } catch (IOException ignored) {
                }
            }
        }

        // detect header and build column indices mapping
        int firstIdx = 0;
        while (firstIdx < lines.size()) {
            String t = lines.get(firstIdx);
            if (t == null) { firstIdx++; continue; }
            t = t.trim();
            if (t.isEmpty() || t.startsWith("#") || t.startsWith("//")) { firstIdx++; continue; }
            break;
        }

        Map<String, Integer> colIdx = new HashMap<>();
        boolean hasHeader = false;
        if (firstIdx < lines.size()) {
            String candidate = lines.get(firstIdx).trim();
            String[] parts = candidate.split(",");
            // check if candidate looks like a header (contains known tokens)
            int found = 0;
            for (int i = 0; i < parts.length; i++) {
                String p = parts[i].trim().toLowerCase();
                if (p.equals("dcid") || p.equals("asw") || p.equals("psw") || p.equals("dsw") || p.equals("startid") || p.equals("endid") || p.equals("startid") || p.equals("endid")) {
                    found++;
                }
            }
            if (found >= 3) { // likely a header if it contains at least 3 known column names
                hasHeader = true;
                for (int i = 0; i < parts.length; i++) {
                    String p = parts[i].trim().toLowerCase();
                    colIdx.put(p, i);
                }
            } else {
                // no header; determine mapping by column count of this first data line
                if (parts.length >= 6) {
                    // assume: DCID,ASW,PSW,DSW,StartID,EndID
                    colIdx.put("dcid", 0);
                    colIdx.put("asw", 1);
                    colIdx.put("psw", 2);
                    colIdx.put("dsw", 3);
                    colIdx.put("startid", 4);
                    colIdx.put("endid", 5);
                } else if (parts.length >= 5) {
                    // assume: ASW,PSW,DSW,StartID,EndID
                    colIdx.put("asw", 0);
                    colIdx.put("psw", 1);
                    colIdx.put("dsw", 2);
                    colIdx.put("startid", 3);
                    colIdx.put("endid", 4);
                } else {
                    // cannot determine mapping; nothing to parse
                    return;
                }
            }
        }

        int startLine = firstIdx + (hasHeader ? 1 : 0);
        for (int li = startLine; li < lines.size(); li++) {
            String raw = lines.get(li);
            if (raw == null) continue;
            String line = raw.trim();
            if (line.isEmpty()) continue;
            if (line.startsWith("#") || line.startsWith("//")) continue;

            String[] parts = line.split(",");
            // ensure we have enough columns for the configured indices
            int maxIdx = -1;
            for (Integer idx : colIdx.values()) if (idx != null && idx > maxIdx) maxIdx = idx;
            if (parts.length <= maxIdx) continue; // malformed

            // parse fields using column map
            int dcId = 0;
            if (colIdx.containsKey("dcid")) {
                String s = parts[colIdx.get("dcid")].trim();
                try {
                    dcId = Integer.parseInt(s);
                } catch (NumberFormatException nfe) {
                    // malformed dcId; skip this line
                    continue;
                }
            }

            String asw = parts[colIdx.get("asw")].trim();
            String psw = parts[colIdx.get("psw")].trim();
            String dsw = parts[colIdx.get("dsw")].trim();

            int start, end;
            try {
                start = Integer.parseInt(parts[colIdx.get("startid")].trim());
                end = Integer.parseInt(parts[colIdx.get("endid")].trim());
            } catch (Exception ex) {
                // malformed numbers
                continue;
            }

            ClosTopologyItem item = new ClosTopologyItem(dcId, asw, psw, dsw);
            entries.add(new RangeEntry(start, end, item));

            // update Clos range
            int[] cur = ClosRangeMap.get(item);
            if (cur == null) {
                ClosRangeMap.put(item, new int[]{start, end});
            } else {
                throw new IllegalArgumentException("Duplicate ClosTopologyItem entry in CSV: " + line);
            }
        }

        // sort by start for binary search
        Collections.sort(entries, (a, b) -> Integer.compare(a.start, b.start));
    }

    /**
     * Return the ClosTopologyItem for a given hostId, or null if not found.
     * Uses binary search on the sorted ranges for O(log n) lookup.
     */
    ClosTopologyItem getClosTopologyItemByHostId(int hostId) {
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

