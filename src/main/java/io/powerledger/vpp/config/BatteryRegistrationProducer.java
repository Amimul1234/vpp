package io.powerledger.vpp.config;

import io.powerledger.vpp.dto.BatteryDto;
import io.powerledger.vpp.dto.BatteryRegistrationMessageDto;
import io.powerledger.vpp.dto.BulkRegistrationStatusDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
@RequiredArgsConstructor
public class BatteryRegistrationProducer {

    @Value("${kafka.topic.batteries}")
    private String batteryRegistrationTopic;

    private final KafkaTemplate<String, BatteryRegistrationMessageDto> kafkaTemplate;

    public CompletableFuture<String> sendBulkRegistrationRequest(List<BatteryDto> batteries) {
        return CompletableFuture.completedFuture(bulkRequestId);
    }

    private void updateBulkRegistrationStatus(String bulkRequestId, int completedBatches) {
        // Get the current status from Redis
        BulkRegistrationStatusDto currentStatus = cacheUtil.getBulkRegistrationStatus(bulkRequestId);

        if (currentStatus != null) {
            currentStatus.setCompletedBatches(completedBatches);
            cacheUtil.updateBatteryRegistrationStatus(bulkRequestId, currentStatus);
            log.info("Updated status for bulk registration {}: {} batches completed", bulkRequestId, completedBatches);
        } else {
            log.error("No registration status found for bulk request ID {}", bulkRequestId);
        }
    }
}
