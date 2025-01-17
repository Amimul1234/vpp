package io.powerledger.vpp.repository;

import io.powerledger.vpp.entity.Battery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BatteryRepository extends JpaRepository<Battery, String> {
}
