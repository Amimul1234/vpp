package io.powerledger.vpp.service;

import io.powerledger.vpp.dto.BatteryDto;

import java.util.List;

public interface BatteryService {
    void saveBatteries(List<BatteryDto> batteryDtos);
}
