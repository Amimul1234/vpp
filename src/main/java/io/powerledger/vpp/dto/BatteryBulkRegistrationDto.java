package io.powerledger.vpp.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "Data transfer object for bulk battery registration")
public class BatteryBulkRegistrationDto {

    @NotEmpty(message = "Battery list cannot be empty")
    @Size(min = 1, max = 10000, message = "Number of batteries must be between 1 and 10000")
    @Schema(description = "List of batteries to register")
    private List<@Valid BatteryDto> batteries;

}
