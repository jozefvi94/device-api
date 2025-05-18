package cz.jbenes.ubiquity.device_api.controller;

import cz.jbenes.ubiquity.device_api.dto.DeviceRequestDto;
import cz.jbenes.ubiquity.device_api.dto.DeviceResponseDto;
import cz.jbenes.ubiquity.device_api.service.DeviceService;
import cz.jbenes.ubiquity.device_api.util.TopologyNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.util.Arrays;
import java.util.List;
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
    void registerDevice_shouldReturnRegisteredDevice() {
        DeviceRequestDto requestDto = new DeviceRequestDto();
        DeviceResponseDto responseDto = new DeviceResponseDto();
        when(deviceService.registerDevice(ArgumentMatchers.any(DeviceRequestDto.class))).thenReturn(responseDto);

        ResponseEntity<DeviceResponseDto> response = deviceController.registerDevice(requestDto);

        assertEquals(responseDto, response.getBody());
        assertEquals(org.springframework.http.HttpStatus.OK, response.getStatusCode());
        verify(deviceService).registerDevice(requestDto);
    }

    @Test
    void registerDevice_shouldCallServiceWithValidRequest() {
        DeviceRequestDto requestDto = mock(DeviceRequestDto.class);
        DeviceResponseDto responseDto = new DeviceResponseDto();
        when(deviceService.registerDevice(requestDto)).thenReturn(responseDto);

        ResponseEntity<DeviceResponseDto> response = deviceController.registerDevice(requestDto);

        assertNotNull(response.getBody());
        verify(deviceService, times(1)).registerDevice(requestDto);
    }

    @Test
    void registerDevice_shouldReturnNullWhenServiceReturnsNull() {
        DeviceRequestDto requestDto = new DeviceRequestDto();
        when(deviceService.registerDevice(requestDto)).thenReturn(null);

        ResponseEntity<DeviceResponseDto> response = deviceController.registerDevice(requestDto);

        assertNull(response.getBody());
        assertEquals(HttpStatus.OK, response.getStatusCode());
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

        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(deviceService).getAllDevicesSorted();
    }
    
    @Test
    void getAllDevices_shouldHandleServiceReturningNull() {
        when(deviceService.getAllDevicesSorted()).thenReturn(null);

        ResponseEntity<List<DeviceResponseDto>> response = deviceController.getAllDevices();

        assertNull(response.getBody());
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
    void getDeviceByMac_shouldReturnNullWhenDeviceNotFound() {
        String mac = "00:00:00:00:00:00";
        when(deviceService.getDeviceByMac(mac)).thenReturn(null);

        ResponseEntity<DeviceResponseDto> response = deviceController.getDeviceByMac(mac);

        assertNull(response.getBody());
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(deviceService).getDeviceByMac(mac);
    }

    @Test
    void getDeviceByMac_shouldHandleNullMacAddress() {
        when(deviceService.getDeviceByMac(null)).thenReturn(null);

        ResponseEntity<DeviceResponseDto> response = deviceController.getDeviceByMac(null);

        assertNull(response.getBody());
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(deviceService).getDeviceByMac(null);
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

        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(deviceService).getFullTopology();
    }

    @Test
    void getFullTopology_shouldHandleServiceReturningNull() {
        when(deviceService.getFullTopology()).thenReturn(null);

        ResponseEntity<List<TopologyNode>> response = deviceController.getFullTopology();

        assertNull(response.getBody());
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(deviceService).getFullTopology();
    }

    @Test
    void getTopologyFrom_shouldReturnNullWhenNodeNotFound() {
        String mac = "FF:FF:FF:FF:FF:FF";
        when(deviceService.getTopologyFrom(mac)).thenReturn(null);

        ResponseEntity<TopologyNode> response = deviceController.getTopologyFrom(mac);

        assertNull(response.getBody());
        assertEquals(HttpStatus.OK, response.getStatusCode());
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

    @Test
    void getTopologyFrom_shouldHandleNullMacAddress() {
        when(deviceService.getTopologyFrom(null)).thenReturn(null);

        ResponseEntity<TopologyNode> response = deviceController.getTopologyFrom(null);

        assertNull(response.getBody());
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(deviceService).getTopologyFrom(null);
    }
}