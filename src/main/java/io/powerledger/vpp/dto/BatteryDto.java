package io.powerledger.vpp.dto;

import lombok.Data;

@Data
public class BatteryDto {
    private String name;
    private String postcode;
    private double capacity;
}
