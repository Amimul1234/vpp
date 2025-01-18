package io.powerledger.vppbatterymgr.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BatteryDto {
    @NotNull(message = "Name can not be null")
    private String name;

    @NotNull(message = "Postcode can not be null")
    private String postcode;

    @Min(value = 0, message = "Capacity can not be less than zero")
    private double capacity;
}
