package cz.jbenes.ubiquity.device_api.dto;

import cz.jbenes.ubiquity.device_api.model.DeviceType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeviceResponseDto {
    private String macAddress;
    private DeviceType deviceType;
}
