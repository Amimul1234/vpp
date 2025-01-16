package io.powerledger.vpp.mapper;

import io.powerledger.vpp.dto.BatteryDto;
import io.powerledger.vpp.entity.Battery;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface BatteryMapper {
    Battery toEntity(BatteryDto dto);

    BatteryDto toDto(Battery entity);
}
