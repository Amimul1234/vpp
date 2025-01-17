package io.powerledger.vpp.config;

import io.powerledger.vpp.dto.BatteryRegistrationMessageDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
@RequiredArgsConstructor
public class BatteryRegistrationProducer {

    @Value("${kafka.topic.batteries}")
    private String batteryRegistrationTopic;

    private final KafkaTemplate<String, BatteryRegistrationMessageDto> kafkaTemplate;

    public void sendBulkRegistrationRequest(BatteryRegistrationMessageDto batteryRegistrationMessageDto) {
        CompletableFuture.completedFuture(bulkRequestId);
    }
}
