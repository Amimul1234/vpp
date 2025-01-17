package io.powerledger.vpp.service.impl;

import io.powerledger.vpp.config.BatteryRegistrationProducer;
import io.powerledger.vpp.dto.BatteryDto;
import io.powerledger.vpp.dto.BatteryRegistrationMessageDto;
import io.powerledger.vpp.dto.BulkRegistrationStatusDto;
import io.powerledger.vpp.dto.ResponseDto;
import io.powerledger.vpp.enums.BatteryRegistrationStatus;
import io.powerledger.vpp.service.BatteryService;
import io.powerledger.vpp.util.BatchUtil;
import io.powerledger.vpp.util.CacheUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BatteryServiceImpl implements BatteryService {

    private static final String ERROR_RETRY_MSG = "Retry %d/%d failed for batch ID: %s. Error: %s";
    private static final String ERROR_CACHE_MISSING_MSG = "Cache entry not found for bulk request ID: %s";
    private static final String ERROR_BULK_REG_FAILED_MSG = "Bulk registration failed due to missing status.";
    private static final String SUCCESS_BULK_REG_MSG = "Bulk registration completed successfully.";
    private static final String PARTIAL_BULK_REG_MSG = "Bulk registration partially completed with failed batches.";

    @Value("${kafka.battery.retry-attempts}")
    private int maxRetries;

    @Value("${kafka.battery.batch-size}")
    private int batchSize;

    private final CacheUtil cacheUtil;
    private final BatteryRegistrationProducer batteryRegistrationProducer;

    @Override
    public ResponseDto initiateBulkRegistration(List<BatteryDto> batteries) {
        if (batteries == null || batteries.isEmpty()) {
            log.warn("No batteries provided for bulk registration.");
            return ResponseDto.error("Battery list cannot be null or empty.", HttpStatus.BAD_REQUEST.toString());
        }

        String bulkRequestId = UUID.randomUUID().toString();
        log.info("Initiating bulk registration with ID: {} for {} batteries", bulkRequestId, batteries.size());

        int totalBatches = (int) Math.ceil((double) batteries.size() / batchSize);
        log.info("Processing {} batteries in {} batches", batteries.size(), totalBatches);

        setInitialStatusOnCache(bulkRequestId, totalBatches);

        List<List<BatteryDto>> batches = BatchUtil.splitBatteriesIntoBatches(batteries, batchSize);

        batches.parallelStream().forEach(batch -> processBatch(batch, bulkRequestId, totalBatches));

        finalizeBulkRegistrationStatus(bulkRequestId);

        return createBulkRegistrationResponse(bulkRequestId);
    }

    private void processBatch(List<BatteryDto> batch, String bulkRequestId, int totalBatches) {
        int retryCount = 0;
        boolean sent = false;
        String batchRequestId = UUID.randomUUID().toString();

        while (retryCount < maxRetries && !sent) {
            try {
                BatteryRegistrationMessageDto message = createBatchMessage(batch, batchRequestId, bulkRequestId, totalBatches);
                batteryRegistrationProducer.sendBulkRegistrationRequest(message);
                updateBulkRegistrationStatus(bulkRequestId, batchRequestId, null, null);
                sent = true;
            } catch (Exception ex) {
                retryCount++;
                log.error(String.format(ERROR_RETRY_MSG, retryCount, maxRetries, batchRequestId, ex.getMessage()));
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

    private BatteryRegistrationMessageDto createBatchMessage(List<BatteryDto> batch, String batchRequestId,
                                                             String bulkRequestId, int totalBatches) {
        BatteryRegistrationMessageDto message = new BatteryRegistrationMessageDto();
        message.setBulkRequestId(bulkRequestId);
        message.setBatchRequestId(batchRequestId);
        message.setBatteries(batch);
        message.setTimestamp(LocalDateTime.now());
        message.setBatchNumber(batch.size());
        message.setTotalBatches(totalBatches);
        return message;
    }

    private void updateBulkRegistrationStatus(String bulkRequestId, String batchRequestId, String error,
                                              List<BatteryDto> failedBatch) {
        BulkRegistrationStatusDto status = cacheUtil.getCacheEntry(bulkRequestId);
        if (status == null) {
            log.error(String.format(ERROR_CACHE_MISSING_MSG, bulkRequestId));
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
        log.info("Updated status for bulk request ID: {} - Completed: {}, Failed: {}",
                bulkRequestId, status.getCompletedBatches(), status.getFailedBatches());
    }

    private void finalizeBulkRegistrationStatus(String bulkRequestId) {
        BulkRegistrationStatusDto status = cacheUtil.getCacheEntry(bulkRequestId);
        if (status == null) {
            log.error(String.format(ERROR_CACHE_MISSING_MSG, bulkRequestId));
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

    private ResponseDto createBulkRegistrationResponse(String bulkRequestId) {
        BulkRegistrationStatusDto status = cacheUtil.getCacheEntry(bulkRequestId);
        if (status == null) {
            log.error(ERROR_BULK_REG_FAILED_MSG);
            return ResponseDto.error(ERROR_BULK_REG_FAILED_MSG, HttpStatus.INTERNAL_SERVER_ERROR.toString());
        }

        if (status.getFailedBatches() > 0) {
            return ResponseDto.error(PARTIAL_BULK_REG_MSG, HttpStatus.PARTIAL_CONTENT.toString());
        }

        return ResponseDto.success(SUCCESS_BULK_REG_MSG, bulkRequestId);
    }

    private void applyExponentialBackoff(int retryCount) {
        try {
            Thread.sleep((long) Math.pow(2, retryCount) * 1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
