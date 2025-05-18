package cz.jbenes.ubiquity.device_api.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import cz.jbenes.ubiquity.device_api.dto.DeviceRequestDto;
import cz.jbenes.ubiquity.device_api.dto.DeviceResponseDto;
import cz.jbenes.ubiquity.device_api.model.Device;
import cz.jbenes.ubiquity.device_api.model.DeviceType;
import cz.jbenes.ubiquity.device_api.repository.DeviceRepository;
import cz.jbenes.ubiquity.device_api.util.TopologyNode;
import jakarta.persistence.EntityNotFoundException;

/**
 * {@inheritDoc}
 */
@Service
public class DeviceServiceImpl implements DeviceService {

    private final DeviceRepository deviceRepository;

    public DeviceServiceImpl(DeviceRepository deviceRepository) {
        this.deviceRepository = deviceRepository;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DeviceResponseDto registerDevice(DeviceRequestDto request) {
        if (request.getMacAddress() == null || request.getMacAddress().isBlank()) {
            throw new IllegalArgumentException("MAC address must not be null or blank");
        }
        if (request.getDeviceType() == null) {
            throw new IllegalArgumentException("Device type must not be null");
        }
        if (deviceRepository.existsById(request.getMacAddress())) {
            throw new IllegalArgumentException("Device with MAC address already exists: " + request.getMacAddress());
        }

        Device uplink = null;
        if (request.getUplinkMacAddress() != null) {
            uplink = deviceRepository.findById(request.getUplinkMacAddress())
                    .orElseThrow(() -> new EntityNotFoundException("Uplink device not found: " + request.getUplinkMacAddress()));
        }

        Device device = new Device();
        device.setMacAddress(request.getMacAddress());
        device.setDeviceType(request.getDeviceType());
        device.setUplink(uplink);

        deviceRepository.save(device);
        return new DeviceResponseDto(device.getMacAddress(), device.getDeviceType());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<DeviceResponseDto> getAllDevicesSorted() {
        return deviceRepository.findAll().stream()
                .sorted(Comparator.comparingInt(d -> deviceTypeOrder(d.getDeviceType())))
                .map(d -> new DeviceResponseDto(d.getMacAddress(), d.getDeviceType()))
                .toList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DeviceResponseDto getDeviceByMac(String macAddress) {
        Device device = deviceRepository.findById(macAddress)
                .orElseThrow(() -> new EntityNotFoundException("Device not found: " + macAddress));
        return new DeviceResponseDto(device.getMacAddress(), device.getDeviceType());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<TopologyNode> getFullTopology() {
        List<Device> allDevices = deviceRepository.findAll();

        Map<String, TopologyNode> nodeMap = new HashMap<>();
        List<TopologyNode> roots = new ArrayList<>();

        // create nodes
        for (Device device : allDevices) {
            nodeMap.put(device.getMacAddress(), new TopologyNode(device.getMacAddress()));
        }

        // assign children or mark as roots
        for (Device device : allDevices) {
            TopologyNode currentNode = nodeMap.get(device.getMacAddress());
            if (device.getUplink() != null) {
                TopologyNode parentNode = nodeMap.get(device.getUplink().getMacAddress());
                parentNode.addChild(currentNode);
            } else {
                roots.add(currentNode);
            }
        }

        return roots;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TopologyNode getTopologyFrom(String macAddress) {
        Device rootDevice = deviceRepository.findById(macAddress)
                .orElseThrow(() -> new EntityNotFoundException("Device not found: " + macAddress));

        return buildSubtree(rootDevice);
    }
    
    /**
     * Recursively builds a subtree of devices starting from the given root device.
     *
     * @param root the root device
     * @return the topology node representing the subtree
     */
    private TopologyNode buildSubtree(Device root) {
        TopologyNode rootNode = new TopologyNode(root.getMacAddress());

        List<Device> children = deviceRepository.findAll().stream()
                .filter(d -> d.getUplink() != null && d.getUplink().getMacAddress().equals(root.getMacAddress()))
                .toList();

        for (Device child : children) {
            rootNode.addChild(buildSubtree(child));
        }

        return rootNode;
    }
    
    /**
     * Determines sort order by device type.
     */
    private int deviceTypeOrder(DeviceType type) {
        return switch (type) {
            case GATEWAY -> 0;
            case SWITCH -> 1;
            case ACCESS_POINT -> 2;
        };
    }
}
