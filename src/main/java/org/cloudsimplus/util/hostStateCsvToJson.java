package org.cloudsimplus.util;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class hostStateCsvToJson {
    public static void main(String[] args) {
        String csvFile = "./src/main/resources/experiment/googleTrace/hostResource/2019_h_host_resource.csv"; // CSV文件路径

        int maxCpuCapacity = 10000;
        int maxRamCapacity = 10000;
        int storageCapacity = 100000;
        int bwCapacity = 100000;

        int hostNum = 0;

        List<Map<String, Integer>> hostList = new ArrayList<>();
        JSONArray jsonArray = new JSONArray();

        try (Reader reader = new FileReader(csvFile);
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {

            // 遍历CSV记录
            for (CSVRecord csvRecord : csvParser) {
                // 创建JSON对象并设置键值对
                Map<String, Integer> host = new HashMap<>();
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("cpu", (int) (Double.parseDouble(csvRecord.get("cpus")) * maxCpuCapacity));
                jsonObject.put("ram", (int) (Double.parseDouble(csvRecord.get("ram")) * maxRamCapacity));
                jsonObject.put("storage", storageCapacity);
                jsonObject.put("bw", bwCapacity);
                int machineCount = Integer.parseInt(csvRecord.get("machine_count"));
                hostNum += machineCount;
                jsonObject.put("length", machineCount);
//                host.put("cpu", (int)(Double.parseDouble(csvRecord.get("cpus"))*maxCpuCapacity));
//                host.put("ram", (int)(Double.parseDouble(csvRecord.get("ram"))*maxRamCapacity));
//                host.put("storage", storageCapacity);
//                host.put("bw", bwCapacity);
//                host.put("length", Integer.parseInt(csvRecord.get("machine_count")));
                // 将JSON对象添加到JSON数组中
//                hostList.add(host);
                jsonArray.put(jsonObject);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 输出转换后的JSON数据
        System.out.println(jsonArray.toString(4));
        System.out.println("hostNum: " + hostNum);
    }
}