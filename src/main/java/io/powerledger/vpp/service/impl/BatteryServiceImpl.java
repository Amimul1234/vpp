package io.powerledger.vpp.service.impl;

import io.powerledger.vpp.config.BatteryRegistrationProducer;
import io.powerledger.vpp.dto.BatteryDto;
import io.powerledger.vpp.dto.BatteryRegistrationMessageDto;
import io.powerledger.vpp.dto.ResponseDto;
import io.powerledger.vpp.service.BatteryService;
import io.powerledger.vpp.util.BatchUtil;
import io.powerledger.vpp.util.CacheUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BatteryServiceImpl implements BatteryService {

    @Value("${kafka.battery.batch-size:100}")
    private int batchSize;

    private final CacheUtil cacheUtil;
    private final BatteryRegistrationProducer batteryRegistrationProducer;

    @Override
    public ResponseDto initiateBulkRegistration(List<BatteryDto> batteries) {
        String bulkRequestId = UUID.randomUUID().toString();
        log.info("Initiating bulk registration with ID: {} for {} batteries", bulkRequestId, batteries.size());

        int totalBatches = (int) Math.ceil((double) batteries.size() / batchSize);
        log.info("Processing bulk registration with {} batteries in {} batches", batteries.size(), totalBatches);

        cacheUtil.setInitialBatteryRegistrationStatus(bulkRequestId, totalBatches);

        List<List<BatteryDto>> batches = BatchUtil.splitBatteriesIntoBatches(batteries, batchSize);

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
            log.debug("Sent batch {}/{} for bulk request {} to topic {}", i + 1, totalBatches, bulkRequestId, batteryRegistrationTopic);

            // Update the registration status after sending each batch
            updateBulkRegistrationStatus(bulkRequestId, i + 1);
        }
    }
}
