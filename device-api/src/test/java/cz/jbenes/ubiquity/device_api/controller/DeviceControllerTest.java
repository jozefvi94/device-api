package cz.jbenes.ubiquity.device_api.controller;

import cz.jbenes.ubiquity.device_api.dto.DeviceRequestDto;
import cz.jbenes.ubiquity.device_api.dto.DeviceResponseDto;
import cz.jbenes.ubiquity.device_api.service.DeviceService;
import cz.jbenes.ubiquity.device_api.util.TopologyNode;
import jakarta.persistence.EntityNotFoundException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DeviceControllerTest {

    private DeviceService deviceService;
    private DeviceController deviceController;

    @BeforeEach
    void setUp() {
        deviceService = mock(DeviceService.class);
        deviceController = new DeviceController(deviceService);
    }

    @Test
    void registerDevice_shouldRegisterDevice() {
        DeviceRequestDto requestDto = new DeviceRequestDto();
        DeviceResponseDto responseDto = new DeviceResponseDto();
        when(deviceService.registerDevice(ArgumentMatchers.any(DeviceRequestDto.class))).thenReturn(responseDto);

        ResponseEntity<DeviceResponseDto> response = deviceController.registerDevice(requestDto);

        assertEquals(responseDto, response.getBody());
        assertEquals(org.springframework.http.HttpStatus.OK, response.getStatusCode());
        verify(deviceService).registerDevice(requestDto);
    }

    @Test
    void getAllDevices_shouldReturnListOfDevices() {
        DeviceResponseDto device1 = new DeviceResponseDto();
        DeviceResponseDto device2 = new DeviceResponseDto();
        List<DeviceResponseDto> devices = Arrays.asList(device1, device2);
        when(deviceService.getAllDevicesSorted()).thenReturn(devices);

        ResponseEntity<List<DeviceResponseDto>> response = deviceController.getAllDevices();

        assertEquals(devices, response.getBody());
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(deviceService).getAllDevicesSorted();
    }

    @Test
    void getAllDevices_shouldReturnEmptyListWhenNoDevices() {
        when(deviceService.getAllDevicesSorted()).thenReturn(List.of());

        ResponseEntity<List<DeviceResponseDto>> response = deviceController.getAllDevices();

        List<DeviceResponseDto> body = Objects.requireNonNull(response.getBody());
        assertTrue(body.isEmpty());
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(deviceService).getAllDevicesSorted();
    }

    @Test
    void getDeviceByMac_shouldReturnDevice() {
        String mac = "AA:BB:CC:DD:EE:FF";
        DeviceResponseDto device = new DeviceResponseDto();
        when(deviceService.getDeviceByMac(mac)).thenReturn(device);

        ResponseEntity<DeviceResponseDto> response = deviceController.getDeviceByMac(mac);

        assertEquals(device, response.getBody());
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(deviceService).getDeviceByMac(mac);
    }

    @Test
    void getDeviceByMac_shouldThrowWhenDeviceNotFound() {
        String mac = "00:00:00:00:00:00";
        when(deviceService.getDeviceByMac(mac)).thenThrow(new EntityNotFoundException("Device not found"));

        assertThrows(EntityNotFoundException.class, () -> deviceController.getDeviceByMac(mac));
        verify(deviceService).getDeviceByMac(mac);
    }

    @Test
    void getFullTopology_shouldReturnTopologyList() {
        TopologyNode node1 = new TopologyNode();
        TopologyNode node2 = new TopologyNode();
        List<TopologyNode> topology = Arrays.asList(node1, node2);
        when(deviceService.getFullTopology()).thenReturn(topology);

        ResponseEntity<List<TopologyNode>> response = deviceController.getFullTopology();

        assertEquals(topology, response.getBody());
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(deviceService).getFullTopology();
    }

    @Test
    void getFullTopology_shouldReturnEmptyListWhenNoTopology() {
        when(deviceService.getFullTopology()).thenReturn(List.of());

        ResponseEntity<List<TopologyNode>> response = deviceController.getFullTopology();

        List<TopologyNode> body = Objects.requireNonNull(response.getBody());
        assertTrue(body.isEmpty());
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(deviceService).getFullTopology();
    }

    @Test
    void getTopologyFrom_shouldThrowWhenNodeNotFound() {
        String mac = "FF:FF:FF:FF:FF:FF";
        when(deviceService.getTopologyFrom(mac)).thenThrow(new EntityNotFoundException("Node not found"));

        assertThrows(EntityNotFoundException.class, () -> deviceController.getTopologyFrom(mac));
        verify(deviceService).getTopologyFrom(mac);
    }

    @Test
    void getTopologyFrom_shouldReturnNodeWhenFound() {
        String mac = "11:22:33:44:55:66";
        TopologyNode node = new TopologyNode();
        when(deviceService.getTopologyFrom(mac)).thenReturn(node);

        ResponseEntity<TopologyNode> response = deviceController.getTopologyFrom(mac);

        assertEquals(node, response.getBody());
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(deviceService).getTopologyFrom(mac);
    }

}