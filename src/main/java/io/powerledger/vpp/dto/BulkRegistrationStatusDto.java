package io.powerledger.vpp.dto;

import io.powerledger.vpp.enums.BatteryRegistrationStatus;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class BulkRegistrationStatusDto {
    private BatteryRegistrationStatus status;
    private int totalBatches;
    private int failedBatches;
    private String bulkRequestId;
    private int completedBatches;
    private Map<Integer, List<String>> failedBatchDetails;
}
