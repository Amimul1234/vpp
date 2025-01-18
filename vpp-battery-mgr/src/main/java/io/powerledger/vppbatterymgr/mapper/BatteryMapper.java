package io.powerledger.vppbatterymgr.mapper;

import io.powerledger.vppbatterymgr.dto.BatteryDto;
import io.powerledger.vppbatterymgr.entity.Battery;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface BatteryMapper {
    Battery toEntity(BatteryDto dto);

    BatteryDto toDto(Battery entity);
}
