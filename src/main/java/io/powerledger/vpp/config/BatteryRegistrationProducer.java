package io.powerledger.vpp.config;

import io.powerledger.vpp.dto.BatteryRegistrationMessageDto;
import io.powerledger.vpp.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class BatteryRegistrationProducer {

    @Value("${kafka.topic.batteries}")
    private String batteryRegistrationTopic;

    private final KafkaTemplate<String, BatteryRegistrationMessageDto> kafkaTemplate;

    public void sendBulkRegistrationRequest(BatteryRegistrationMessageDto batteryRegistrationMessageDto) {
        try {
            kafkaTemplate.send(batteryRegistrationTopic, batteryRegistrationMessageDto);
            log.info("Successfully sent bulk registration request for batch {} to Kafka.",
                    batteryRegistrationMessageDto.getBatchRequestId());
        } catch (Exception ex) {
            log.error("Failed to send bulk registration request for batch {} to Kafka. Error: {}",
                    batteryRegistrationMessageDto.getBatchRequestId(), ex.getMessage());
            throw new ServiceException("Failed to send bulk registration request for batch " + batteryRegistrationMessageDto);
        }
    }
}
