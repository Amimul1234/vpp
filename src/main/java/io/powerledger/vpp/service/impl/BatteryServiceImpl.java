package io.powerledger.vpp.service.impl;

import io.powerledger.vpp.dto.BatteryDto;
import io.powerledger.vpp.service.BatteryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BatteryServiceImpl implements BatteryService {

    private final KafkaProducerConfig kafkaProducerConfig;

    @Override
    public void saveBatteries(List<BatteryDto> batteryDtos) {
        kafkaProducerConfig.sendBatteryData(batteryDtos);
    }
}

