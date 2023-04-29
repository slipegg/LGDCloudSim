package org.scalecloudsim.record;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.scalecloudsim.datacenter.Datacenter;
import org.scalecloudsim.request.Instance;
import org.scalecloudsim.request.UserRequest;

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
        if (!dir.exists()) {
            dir.mkdir();
        }
        try {
            writer = new BufferedWriter(new FileWriter(this.filePath));
            // 写入表头
            printer = new CSVPrinter(writer, CSVFormat.Builder.create()
                    .setHeader("Instance", "InstanceGroup", "UserRequest", "cpu", "ram", "storage", "bandwidth", "lifetime", "datacenter", "host", "submit time", "start time", "end time", "retry num", "state", "fail_reason")
                    .build());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public CsvRecord() {
        this("ScaleCloudsimRecord-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")) + ".csv");
        //    this("scalecloudsim_record.csv");
    }

    public void writeRecord(Instance instance) {
        int dcId = -1;
        if (instance.getInstanceGroup().getReceiveDatacenter() != null) {
            dcId = instance.getInstanceGroup().getReceiveDatacenter().getId();
        }
        writeRecord(instance, dcId);
    }

    private void writeRecord(Instance instance, int dcId) {
        try {
            printer.printRecord(instance.getId(), instance.getInstanceGroup().getId(), instance.getUserRequest().getId(),
                    instance.getCpu(), instance.getRam(), instance.getStorage(), instance.getBw(), instance.getLifeTime(),
                    dcId, instance.getHost(), instance.getUserRequest().getSubmitTime(), instance.getStartTime(), instance.getFinishTime(), instance.getRetryNum(), UserRequest.stateToString(instance.getState()), instance.getUserRequest().getFailReason());

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
