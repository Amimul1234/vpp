package io.powerledger.vpp.util;

import io.powerledger.vpp.dto.BulkRegistrationStatusDto;
import io.powerledger.vpp.enums.BatteryRegistrationStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CacheUtil {

    private final RedisTemplate<String, BulkRegistrationStatusDto> redisTemplate;

    public void setInitialBatteryRegistrationStatus(String bulkRequestId, int totalBatchSize) {
        BulkRegistrationStatusDto initialStatus = new BulkRegistrationStatusDto();

        initialStatus.setBulkRequestId(bulkRequestId);
        initialStatus.setCompletedBatches(0);
        initialStatus.setFailedBatches(0);
        initialStatus.setTotalBatches(totalBatchSize);
        initialStatus.setStatus(BatteryRegistrationStatus.PENDING);

        redisTemplate.opsForValue().set("bulk-registration-status:" + bulkRequestId, initialStatus);
    }

}
