package io.powerledger.vpp.service;

import io.powerledger.vpp.dto.BatteryDto;
import io.powerledger.vpp.dto.ResponseDto;

import java.util.List;

public interface BatteryService {
    ResponseDto initiateBulkRegistration(List<BatteryDto> batteries);
}
