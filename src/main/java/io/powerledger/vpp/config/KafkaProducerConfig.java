package io.powerledger.vpp.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.powerledger.vpp.dto.BatteryDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaProducerConfig {

    @Value("${kafka.topic.batteries}")
    private String batteryTopic;

    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public void sendBatteryData(List<BatteryDto> batteryDtos) {
        batteryDtos.forEach(dto -> {
            try {
                String message = objectMapper.writeValueAsString(dto);
                kafkaTemplate.send(batteryTopic, message);
                log.info("Sent battery data to Kafka: {}", dto);
            } catch (JsonProcessingException e) {
                log.error("Error serializing battery data: {}", dto, e);
            }
        });
    }
}
