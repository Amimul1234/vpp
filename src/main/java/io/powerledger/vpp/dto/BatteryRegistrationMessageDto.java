package io.powerledger.vpp.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class BatteryRegistrationMessageDto {
    private List<BatteryDto> batteries;
    private LocalDateTime timestamp;
    private String batchRequestId;
    private String bulkRequestId;
    private Integer batchNumber;
    private Integer totalBatches;
}
