package io.powerledger.vpp.config;

import io.powerledger.vpp.dto.BatteryBulkRegistrationDto;
import io.powerledger.vpp.dto.BatteryDto;
import io.powerledger.vpp.dto.BatteryRegistrationMessageDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
@RequiredArgsConstructor
public class BatteryRegistrationProducer {

    @Value("${kafka.battery.batch-size:100}")
    private int batchSize;

    @Value("${kafka.topic.batteries}")
    private String batteryRegistrationTopic;

    private final KafkaTemplate<String, BatteryRegistrationMessageDto> kafkaTemplate;

    public CompletableFuture<String> sendBulkRegistrationRequest(BatteryBulkRegistrationDto batteryBulkRegistrationDto) {
        String bulkRequestId = UUID.randomUUID().toString();
        List<BatteryDto> batteries = batteryBulkRegistrationDto.getBatteries();

        int totalBatches = (int) Math.ceil((double) batteries.size() / batchSize);

        log.info("Processing bulk registration with {} batteries in {} batches", batteries.size(), totalBatches);

        List<List<BatteryDto>> batches = splitBatteriesIntoBatches(batteries, batchSize);

        for (int i = 0; i < batches.size(); i++) {
            List<BatteryDto> batch = batches.get(i);

            BatteryRegistrationMessageDto message = new BatteryRegistrationMessageDto();
            message.setBatteries(batch);
            message.setTimestamp(LocalDateTime.now());
            message.setBatchRequestId(UUID.randomUUID().toString());
            message.setBulkRequestId(bulkRequestId);
            message.setBatchNumber(i + 1);
            message.setTotalBatches(totalBatches);

            kafkaTemplate.send(batteryRegistrationTopic, message.getBatchRequestId(), message);

            log.debug("Sent batch {}/{} for bulk request {} to topic {}",
                    i + 1, totalBatches, bulkRequestId, batteryRegistrationTopic);
        }

        return CompletableFuture.completedFuture(bulkRequestId);
    }

    private List<List<BatteryDto>> splitBatteriesIntoBatches(List<BatteryDto> batteries, int batchSize) {
        List<List<BatteryDto>> batches = new ArrayList<>();
        for (int i = 0; i < batteries.size(); i += batchSize) {
            int end = Math.min(i + batchSize, batteries.size());
            batches.add(batteries.subList(i, end));
        }
        return batches;
    }
}
