package io.powerledger.vppbatterymgr.repository;

import io.powerledger.vppbatterymgr.entity.Battery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BatteryRepository extends JpaRepository<Battery, String> {
}
