package cz.jbenes.ubiquity.device_api.dto;

import cz.jbenes.ubiquity.device_api.model.DeviceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeviceRequestDto {
    
    @NotNull(message = "Device type must not be null")
    private DeviceType deviceType;

    @NotBlank(message = "MAC address must not be blank")
    private String macAddress;

    private String uplinkMacAddress;
}
