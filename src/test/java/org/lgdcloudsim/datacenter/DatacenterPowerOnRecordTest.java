package org.lgdcloudsim.datacenter;

import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertEquals;

public class DatacenterPowerOnRecordTest {
    @Test
    public void testDatacenterPowerOnRecord() {
        DatacenterPowerOnRecord datacenterPowerOnRecord = new DatacenterPowerOnRecord();
        datacenterPowerOnRecord.hostAllocateInstance(1, 0);
        datacenterPowerOnRecord.hostAllocateInstance(2, 0);
        datacenterPowerOnRecord.hostAllocateInstance(3, 0);
        datacenterPowerOnRecord.hostAllocateInstance(1, 5);

        assertEquals(2, datacenterPowerOnRecord.getPowerOnHostInstanceNum().get(1).intValue());

        datacenterPowerOnRecord.hostReleaseInstance(1, 20);
        assertEquals(1, datacenterPowerOnRecord.getPowerOnHostInstanceNum().get(1).intValue());
        datacenterPowerOnRecord.hostReleaseInstance(2, 20);
        datacenterPowerOnRecord.hostReleaseInstance(3, 20);
        datacenterPowerOnRecord.hostReleaseInstance(1, 25);
        assertEquals(null, datacenterPowerOnRecord.getPowerOnHostInstanceNum().get(1));

        assertEquals(65, datacenterPowerOnRecord.getAllPowerOnTime(), 0.1);
        assertEquals(3, datacenterPowerOnRecord.getMaxHostNum());
    }
}