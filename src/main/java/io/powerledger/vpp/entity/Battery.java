package io.powerledger.vpp.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Data
@Entity
@Table(name = "battery")
public class Battery {

    @Id
    @GeneratedValue
    @Column(name = "uuid", updatable = false, nullable = false)
    private String uuid;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "post_code", nullable = false)
    private String postcode;

    @Column(name = "watt_capacity", nullable = false)
    private double capacity;

    @PrePersist
    public void prePersist() {
        if (uuid == null) {
            uuid = UUID.randomUUID().toString();
        }
    }
}
