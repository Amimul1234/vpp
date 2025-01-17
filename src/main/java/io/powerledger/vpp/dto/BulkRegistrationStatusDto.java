package io.powerledger.vpp.dto;

import io.powerledger.vpp.enums.BatteryRegistrationStatus;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
public class BulkRegistrationStatusDto {
    private BatteryRegistrationStatus status;
    private int totalBatches;
    private int failedBatches;
    private String bulkRequestId;
    private int completedBatches;
    private Map<String, List<BatteryDto>> failedBatchDetails = new HashMap<>();
}
