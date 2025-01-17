package io.powerledger.vpp.controller;

import io.powerledger.vpp.config.BatteryRegistrationProducer;
import io.powerledger.vpp.dto.BatteryBulkRegistrationDto;
import io.powerledger.vpp.dto.BatteryDto;
import io.powerledger.vpp.dto.ResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController("api/v1/batteries")
@RequiredArgsConstructor
public class BatteryController {

    private final BatteryRegistrationProducer registrationProducer;
    private final RegistrationStatusService registrationStatusService;

    @PostMapping(
            value = "/bulk"
    )
    @Operation(
            summary = "Register multiple batteries in bulk",
            description = "Accepts a list of batteries for registration. Large lists of batteries are processed in chunks.",
            responses = {
                    @ApiResponse(
                            responseCode = "202",
                            description = "Batteries accepted for registration",
                            content = @Content(schema = @Schema(implementation = ResponseDto.class))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid input data"
                    )
            }
    )
    public CompletableFuture<ResponseEntity<ResponseDto>> registerBatteries(
            @Valid @RequestBody BatteryBulkRegistrationDto bulkRegistrationDTO) {

        List<BatteryDto> batteries = bulkRegistrationDTO.getBatteries();
        log.info("Received bulk registration request for {} batteries", batteries.size());

        return registrationProducer.sendBulkRegistrationRequest(batteries)
                .thenApply(requestId -> {
                    String message = String.format("Bulk registration initiated. Total batteries: %d", batteries.size());
                    return ResponseEntity.accepted()
                            .body(new RegistrationResponse(requestId, message));
                });
    }

    @GetMapping(
            value = "/registration/{requestId}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(
            summary = "Check registration status",
            description = "Retrieves the current status of a bulk battery registration request",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Registration status retrieved successfully",
                            content = @Content(schema = @Schema(implementation = RegistrationStatus.class))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Registration request not found"
                    )
            }
    )
    public ResponseEntity<RegistrationStatus> getRegistrationStatus(
            @Parameter(description = "Bulk registration request ID")
            @PathVariable String requestId) {

        RegistrationStatus status = registrationStatusService.getStatus(requestId);
        if (status == null) {
            log.warn("Registration status not found for requestId: {}", requestId);
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(status);
    }
}