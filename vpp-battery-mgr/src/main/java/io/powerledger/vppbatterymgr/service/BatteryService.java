package io.powerledger.vppbatterymgr.service;


import io.powerledger.vppbatterymgr.dto.BatteryDto;
import io.powerledger.vppbatterymgr.dto.ResponseDto;

import java.util.List;

public interface BatteryService {
    ResponseDto initiateBulkRegistration(List<BatteryDto> batteries);
}
