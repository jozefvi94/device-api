package cz.jbenes.ubiquity.device_api.service;

import java.util.List;

import cz.jbenes.ubiquity.device_api.dto.DeviceRequestDto;
import cz.jbenes.ubiquity.device_api.dto.DeviceResponseDto;
import cz.jbenes.ubiquity.device_api.util.TopologyNode;

/**
 * Service interface for managing devices and their network topology.
 * <p>
 * Provides methods for device registration, retrieval, and topology management.
 * </p>
 */
public interface DeviceService {

    /**
     * Registers a new device based on the provided request data.
     *
     * @param request the device registration request data
     * @return the response DTO containing registered device information
     */
    DeviceResponseDto registerDevice(DeviceRequestDto request);

    /**
     * Retrieves all registered devices sorted by a predefined order.
     *
     * @return a list of device response DTOs sorted accordingly
     */
    List<DeviceResponseDto> getAllDevicesSorted();

    /**
     * Retrieves a device by its MAC address.
     *
     * @param macAddress the MAC address of the device to retrieve
     * @return the response DTO containing device information, or null if not found
     */
    DeviceResponseDto getDeviceByMac(String macAddress);

    /**
     * Retrieves the full network topology of all devices.
     *
     * @return a list of topology nodes representing the full device topology
     */
    List<TopologyNode> getFullTopology();

    /**
     * Retrieves the network topology starting from the specified device.
     *
     * @param macAddress the MAC address of the root device for the topology
     * @return the topology node representing the device and its connections
     */
    TopologyNode getTopologyFrom(String macAddress);
    
}
