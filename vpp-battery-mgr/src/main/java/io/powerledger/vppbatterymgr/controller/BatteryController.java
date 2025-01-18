package io.powerledger.vppbatterymgr.controller;

import io.powerledger.vppbatterymgr.dto.BatteryDto;
import io.powerledger.vppbatterymgr.dto.ResponseDto;
import io.powerledger.vppbatterymgr.service.BatteryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/batteries")
@RequiredArgsConstructor
public class BatteryController {

    private final BatteryService batteryService;

    @PostMapping
    @Operation(
            summary = "Bulk register batteries",
            description = "Accepts a list of batteries and initiates a bulk registration process.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Bulk registration process initiated successfully",
                            content = @Content(schema = @Schema(implementation = ResponseDto.class))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid input format"
                    )
            }
    )
    public ResponseEntity<ResponseDto> bulkRegisterBatteries(
            @RequestBody @Parameter(description = "List of batteries to register", required = true) List<BatteryDto> batteries) {
        log.info("Received bulk registration request for {} batteries", batteries.size());
        return new ResponseEntity<>(batteryService.initiateBulkRegistration(batteries), HttpStatus.OK);
    }
}
