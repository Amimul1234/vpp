package io.powerledger.vppbatterymgr.service.impl;

import io.powerledger.vppbatterymgr.config.BatteryRegistrationProducer;
import io.powerledger.vppbatterymgr.dto.BatteryDto;
import io.powerledger.vppbatterymgr.dto.BatteryRegistrationMessageDto;
import io.powerledger.vppbatterymgr.dto.BulkRegistrationStatusDto;
import io.powerledger.vppbatterymgr.dto.ResponseDto;
import io.powerledger.vppbatterymgr.enums.BatteryRegistrationStatus;
import io.powerledger.vppbatterymgr.service.BatteryService;
import io.powerledger.vppbatterymgr.util.BatchUtil;
import io.powerledger.vppbatterymgr.util.CacheUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BatteryServiceImpl implements BatteryService {

    @Value("${kafka.battery.retry-attempts}")
    private int maxRetries;

    @Value("${kafka.battery.batch-size}")
    private int batchSize;

    private final CacheUtil cacheUtil;
    private final BatteryRegistrationProducer batteryRegistrationProducer;

    @Qualifier("register")
    private final ThreadPoolTaskExecutor taskExecutor;

    @Override
    public ResponseDto initiateBulkRegistration(List<BatteryDto> batteries) {
        if (batteries == null || batteries.isEmpty()) {
            log.warn("No batteries provided for bulk registration.");
            return ResponseDto.error("Battery list cannot be null or empty.", HttpStatus.BAD_REQUEST.toString());
        }

        String bulkRequestId = UUID.randomUUID().toString();
        log.info("Initiating bulk registration asynchronously with ID: {} for {} batteries", bulkRequestId, batteries.size());

        int totalBatches = (int) Math.ceil((double) batteries.size() / batchSize);
        log.info("Processing {} batteries in {} batches", batteries.size(), totalBatches);

        setInitialStatusOnCache(bulkRequestId, totalBatches);

        taskExecutor.execute(() -> processBatches(batteries, bulkRequestId, totalBatches));

        return ResponseDto.success("Bulk registration initiated successfully.", bulkRequestId);
    }

    private void processBatches(List<BatteryDto> batteries, String bulkRequestId, int totalBatches) {
        List<List<BatteryDto>> batches = BatchUtil.splitBatteriesIntoBatches(batteries, batchSize);

        for (int index = 0; index < totalBatches; index++) {
            List<BatteryDto> batch = batches.get(index);
            int currentBatch = index + 1;
            processBatch(batch, bulkRequestId, totalBatches, currentBatch);
        }

        finalizeBulkRegistrationStatus(bulkRequestId);
    }

    private void processBatch(List<BatteryDto> batch, String bulkRequestId, int totalBatches, int currentBatch) {
        int retryCount = 0;
        boolean sent = false;
        String batchRequestId = UUID.randomUUID().toString();

        while (retryCount < maxRetries && !sent) {
            try {
                BatteryRegistrationMessageDto message = createBatchMessage(batch, batchRequestId, bulkRequestId, totalBatches, currentBatch);
                batteryRegistrationProducer.sendBulkRegistrationRequest(message);
                updateBulkRegistrationStatus(bulkRequestId, batchRequestId, null, null);
                sent = true;
            } catch (Exception ex) {
                retryCount++;
                log.error("Retry {}/{} failed for batch ID: {}. Error: {}", retryCount, maxRetries, batchRequestId, ex.getMessage());
                if (retryCount == maxRetries) {
                    updateBulkRegistrationStatus(bulkRequestId, batchRequestId, ex.getMessage(), batch);
                }
                applyExponentialBackoff(retryCount);
            }
        }
    }

    private void setInitialStatusOnCache(String bulkRequestId, int totalBatches) {
        BulkRegistrationStatusDto status = new BulkRegistrationStatusDto();
        status.setBulkRequestId(bulkRequestId);
        status.setTotalBatches(totalBatches);
        status.setStatus(BatteryRegistrationStatus.PROCESSING);
        status.setFailedBatchDetails(new HashMap<>());
        cacheUtil.initializeCacheEntry(bulkRequestId, status);
    }

    private BatteryRegistrationMessageDto createBatchMessage(List<BatteryDto> batch, String batchRequestId, String bulkRequestId, int totalBatches, int currentBatch) {
        BatteryRegistrationMessageDto message = new BatteryRegistrationMessageDto();
        message.setBulkRequestId(bulkRequestId);
        message.setBatchRequestId(batchRequestId);
        message.setBatteries(batch);
        message.setTimestamp(LocalDateTime.now());
        message.setBatchNumber(currentBatch);
        message.setTotalBatches(totalBatches);
        return message;
    }

    private void updateBulkRegistrationStatus(String bulkRequestId, String batchRequestId, String error, List<BatteryDto> failedBatch) {
        BulkRegistrationStatusDto status = cacheUtil.getCacheEntry(bulkRequestId);
        if (status == null) {
            log.error("Cache entry not found for bulk request ID: {}", bulkRequestId);
            return;
        }

        if (error == null) {
            status.setCompletedBatches(status.getCompletedBatches() + 1);
        } else {
            if (status.getFailedBatchDetails() == null) {
                status.setFailedBatchDetails(new HashMap<>());
            }
            status.setFailedBatches(status.getFailedBatches() + 1);
            status.getFailedBatchDetails().put(batchRequestId, failedBatch);
        }

        cacheUtil.updateCacheEntry(bulkRequestId, status);
        log.info("Updated status for bulk request ID: {} - Completed: {}, Failed: {}", bulkRequestId, status.getCompletedBatches(), status.getFailedBatches());
    }

    private void finalizeBulkRegistrationStatus(String bulkRequestId) {
        BulkRegistrationStatusDto status = cacheUtil.getCacheEntry(bulkRequestId);
        if (status == null) {
            log.error("Cache entry not found for bulk request ID: {}", bulkRequestId);
            return;
        }

        if (status.getFailedBatches() > 0) {
            status.setStatus(BatteryRegistrationStatus.PARTIALLY_COMPLETED);
            log.warn("Bulk request ID: {} completed with failures. Failed batches: {}", bulkRequestId, status.getFailedBatchDetails());
        } else {
            status.setStatus(BatteryRegistrationStatus.QUEUED);
            log.info("Bulk request ID: {} completed successfully.", bulkRequestId);
        }

        cacheUtil.updateCacheEntry(bulkRequestId, status);
    }

    private void applyExponentialBackoff(int retryCount) {
        try {
            Thread.sleep((long) Math.pow(2, retryCount) * 1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
