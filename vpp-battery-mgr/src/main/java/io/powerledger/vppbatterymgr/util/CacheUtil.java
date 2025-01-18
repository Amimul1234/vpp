package io.powerledger.vppbatterymgr.util;

import io.powerledger.vppbatterymgr.dto.BulkRegistrationStatusDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CacheUtil {

    private final RedisTemplate<String, BulkRegistrationStatusDto> redisTemplate;

    public void initializeCacheEntry(String bulkRequestId, BulkRegistrationStatusDto initialStatus) {
        redisTemplate.opsForValue().set(getCacheKey(bulkRequestId), initialStatus);
        log.info("Cache initialized for bulk request ID: {}", bulkRequestId);
    }

    public BulkRegistrationStatusDto getCacheEntry(String bulkRequestId) {
        return redisTemplate.opsForValue().get(getCacheKey(bulkRequestId));
    }

    public void updateCacheEntry(String bulkRequestId, BulkRegistrationStatusDto status) {
        redisTemplate.opsForValue().set(getCacheKey(bulkRequestId), status);
        log.debug("Cache updated for bulk request ID: {}", bulkRequestId);
    }

    private String getCacheKey(String bulkRequestId) {
        return "bulk-registration-status:" + bulkRequestId;
    }
}
