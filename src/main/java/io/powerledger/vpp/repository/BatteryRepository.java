package io.powerledger.vpp.repository;

import io.powerledger.vpp.entity.Battery;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BatteryRepository extends JpaRepository<String, Battery> {
}
