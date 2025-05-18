package cz.jbenes.ubiquity.device_api.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cz.jbenes.ubiquity.device_api.dto.DeviceRequestDto;
import cz.jbenes.ubiquity.device_api.dto.DeviceResponseDto;
import cz.jbenes.ubiquity.device_api.service.DeviceService;
import cz.jbenes.ubiquity.device_api.util.TopologyNode;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/devices")
public class DeviceController {
    private final DeviceService deviceService;

    public DeviceController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    /**
     * Register a new network device.
     */
    @PostMapping
    public ResponseEntity<DeviceResponseDto> registerDevice(@Valid @RequestBody DeviceRequestDto request) {
        return ResponseEntity.ok(deviceService.registerDevice(request));
    }

    /**
     * Get all registered devices, sorted by type: Gateway > Switch > Access Point.
     */
    @GetMapping
    public ResponseEntity<List<DeviceResponseDto>> getAllDevices() {
        return ResponseEntity.ok(deviceService.getAllDevicesSorted());
    }

    /**
     * Get a single device by its MAC address.
     */
    @GetMapping("/{macAddress}")
    public ResponseEntity<DeviceResponseDto> getDeviceByMac(@PathVariable String macAddress) {
        return ResponseEntity.ok(deviceService.getDeviceByMac(macAddress));
    }

    /**
     * Get the full network topology as a tree.
     */
    @GetMapping("/topology")
    public ResponseEntity<List<TopologyNode>> getFullTopology() {
        return ResponseEntity.ok(deviceService.getFullTopology());
    }

    /**
     * Get the network topology starting from a specific MAC address.
     */
    @GetMapping("/topology/{macAddress}")
    public ResponseEntity<TopologyNode> getTopologyFrom(@PathVariable String macAddress) {
        return ResponseEntity.ok(deviceService.getTopologyFrom(macAddress));
    }
}

