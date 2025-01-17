package io.powerledger.vppbatterymgr.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class BatteryRegistrationMessageDto implements Serializable {
    private List<BatteryDto> batteries;
    private LocalDateTime timestamp;
    private String batchRequestId;
    private String bulkRequestId;
    private Integer batchNumber;
    private Integer totalBatches;
}
