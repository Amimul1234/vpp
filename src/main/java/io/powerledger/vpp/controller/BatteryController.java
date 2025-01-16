package io.powerledger.vpp.controller;

import io.powerledger.vpp.dto.BatteryDto;
import io.powerledger.vpp.service.BatteryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Battery API", description = "Endpoints for managing and querying batteries")
@RestController
@RequestMapping("/api/v1/batteries")
@RequiredArgsConstructor
public class BatteryController {

    private final BatteryService batteryService;

    @Operation(summary = "Add a list of batteries", description = "Persists a list of batteries in the database")
    @PostMapping
    public ResponseEntity<Void> addBatteries(@RequestBody @Validated List<BatteryDto> batteryDtos) {
        batteryService.saveBatteries(batteryDtos);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
