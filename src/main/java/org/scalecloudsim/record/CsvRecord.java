package org.scalecloudsim.record;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.scalecloudsim.datacenter.Datacenter;
import org.scalecloudsim.request.Instance;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class CsvRecord {
    @Getter
    @Setter
    private String filePath;
    BufferedWriter writer;
    @Getter
    CSVPrinter printer;

    public CsvRecord(String filePath) {
        this.filePath = "Record/" + filePath;
        File dir = new File("Record");
        dir.mkdirs();
        try {
            writer = new BufferedWriter(new FileWriter(this.filePath));
            // 写入表头
            printer = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader("Instance", "InstanceGroup", "UserRequest", "cpu", "ram", "storage", "bandwidth", "lifetime", "datacenter", "host", "start time", "end time", "retry num", "state"
            ));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public CsvRecord() {
        this("ScaleCloudsimRecord-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")) + ".csv");
        //    this("scalecloudsim_record.csv");
    }

    public void writeRecord(int userRequest, int instanceGroup, int instance, int cpu, int ram, int storage, int bandwidth, double lifetime, int datacenter, int host, double startTime, double endTime, int retryNum, int state) {
        try {
            printer.printRecord(userRequest, instanceGroup, instance, cpu, ram, storage, bandwidth, lifetime, datacenter, host, startTime, endTime, retryNum, state);
//            printer.printRecord(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13,14);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeRecord(Instance instance, Datacenter datacenter) {
        try {
            printer.printRecord(instance.getId(), instance.getInstanceGroup().getId(), instance.getUserRequest().getId(),
                    instance.getCpu(), instance.getRam(), instance.getStorage(), instance.getBw(), instance.getLifeTime(),
                    datacenter.getId(), instance.getHost(), instance.getStartTime(), instance.getFinishTime(), instance.getRetryNum(), instance.getState());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        try {
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
