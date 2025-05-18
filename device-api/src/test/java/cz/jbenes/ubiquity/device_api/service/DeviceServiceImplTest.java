package cz.jbenes.ubiquity.device_api.service;

import cz.jbenes.ubiquity.device_api.dto.DeviceRequestDto;
import cz.jbenes.ubiquity.device_api.dto.DeviceResponseDto;
import cz.jbenes.ubiquity.device_api.model.Device;
import cz.jbenes.ubiquity.device_api.model.DeviceType;
import cz.jbenes.ubiquity.device_api.repository.DeviceRepository;
import cz.jbenes.ubiquity.device_api.util.TopologyNode;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import java.util.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class DeviceServiceImplTest {

    private DeviceRepository deviceRepository;
    private DeviceServiceImpl deviceService;

    @BeforeEach
    void setUp() {
        deviceRepository = mock(DeviceRepository.class);
        deviceService = new DeviceServiceImpl(deviceRepository);
    }

    @Test
    void registerDevice_shouldRegisterNewDeviceWithoutUplink() {
        DeviceRequestDto request = new DeviceRequestDto(DeviceType.GATEWAY, "mac1", null);

        when(deviceRepository.existsById("mac1")).thenReturn(false);

        ArgumentCaptor<Device> deviceCaptor = ArgumentCaptor.forClass(Device.class);

        DeviceResponseDto response = deviceService.registerDevice(request);

        verify(deviceRepository).save(deviceCaptor.capture());
        Device savedDevice = deviceCaptor.getValue();

        assertThat(savedDevice.getMacAddress()).isEqualTo("mac1");
        assertThat(savedDevice.getDeviceType()).isEqualTo(DeviceType.GATEWAY);
        assertThat(savedDevice.getUplink()).isNull();

        assertThat(response.getMacAddress()).isEqualTo("mac1");
        assertThat(response.getDeviceType()).isEqualTo(DeviceType.GATEWAY);
    }

    @Test
    void registerDevice_shouldRegisterNewDeviceWithUplink() {
        DeviceRequestDto request = new DeviceRequestDto(DeviceType.SWITCH, "mac2", "uplinkMac");
        Device uplinkDevice = new Device();
        uplinkDevice.setMacAddress("uplinkMac");

        when(deviceRepository.existsById("mac2")).thenReturn(false);
        when(deviceRepository.findById("uplinkMac")).thenReturn(Optional.of(uplinkDevice));

        ArgumentCaptor<Device> deviceCaptor = ArgumentCaptor.forClass(Device.class);

        DeviceResponseDto response = deviceService.registerDevice(request);

        verify(deviceRepository).save(deviceCaptor.capture());
        Device savedDevice = deviceCaptor.getValue();

        assertThat(savedDevice.getUplink()).isEqualTo(uplinkDevice);
        assertThat(response.getMacAddress()).isEqualTo("mac2");
        assertThat(response.getDeviceType()).isEqualTo(DeviceType.SWITCH);
    }

    @Test
    void registerDevice_shouldThrowIfDeviceExists() {
        DeviceRequestDto request = new DeviceRequestDto(DeviceType.GATEWAY, "mac1", null);
        when(deviceRepository.existsById("mac1")).thenReturn(true);

        assertThatThrownBy(() -> deviceService.registerDevice(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Device with MAC address already exists");
    }

    @Test
    void registerDevice_shouldThrowIfUplinkNotFound() {
        DeviceRequestDto request = new DeviceRequestDto(DeviceType.SWITCH, "mac2", "uplinkMac");
        when(deviceRepository.existsById("mac2")).thenReturn(false);
        when(deviceRepository.findById("uplinkMac")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> deviceService.registerDevice(request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Uplink device not found");
    }

    @Test
    void registerDevice_shouldThrowIfMacAddressIsNull() {
        DeviceRequestDto request = new DeviceRequestDto(DeviceType.GATEWAY, null, null);
        assertThatThrownBy(() -> deviceService.registerDevice(request))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void registerDevice_shouldThrowIfMacAddressIsBlank() {
        DeviceRequestDto request = new DeviceRequestDto(DeviceType.GATEWAY, "   ", null);
        assertThatThrownBy(() -> deviceService.registerDevice(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("MAC address must not be null or blank");
    }

    @Test
    void registerDevice_shouldThrowIfDeviceTypeIsNull() {
        DeviceRequestDto request = new DeviceRequestDto(null, "mac1", null);
        assertThatThrownBy(() -> deviceService.registerDevice(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Device type must not be null");
    }

    @Test
    void registerDevice_shouldRegisterMultipleDevicesWithChainedUplinks() {
        DeviceRequestDto gatewayReq = new DeviceRequestDto(DeviceType.GATEWAY, "gw", null);
        DeviceRequestDto switchReq = new DeviceRequestDto(DeviceType.SWITCH, "sw", "gw");
        DeviceRequestDto apReq = new DeviceRequestDto(DeviceType.ACCESS_POINT, "ap", "sw");

        when(deviceRepository.existsById("gw")).thenReturn(false);
        when(deviceRepository.existsById("sw")).thenReturn(false);
        when(deviceRepository.existsById("ap")).thenReturn(false);

        Device gateway = new Device();
        gateway.setMacAddress("gw");
        when(deviceRepository.findById("gw")).thenReturn(Optional.of(gateway));

        Device switchDevice = new Device();
        switchDevice.setMacAddress("sw");
        switchDevice.setUplink(gateway);
        when(deviceRepository.findById("sw")).thenReturn(Optional.of(switchDevice));

        // Register gateway
        deviceService.registerDevice(gatewayReq);
        // Register switch with uplink to gateway
        deviceService.registerDevice(switchReq);
        // Register AP with uplink to switch
        deviceService.registerDevice(apReq);

        verify(deviceRepository, times(3)).save(any(Device.class));
    }

    @Test
    void getAllDevicesSorted_shouldReturnDevicesInOrder() {
        Device d1 = new Device();
        d1.setMacAddress("mac1");
        d1.setDeviceType(DeviceType.SWITCH);

        Device d2 = new Device();
        d2.setMacAddress("mac2");
        d2.setDeviceType(DeviceType.GATEWAY);

        Device d3 = new Device();
        d3.setMacAddress("mac3");
        d3.setDeviceType(DeviceType.ACCESS_POINT);

        when(deviceRepository.findAll()).thenReturn(List.of(d1, d2, d3));

        List<DeviceResponseDto> result = deviceService.getAllDevicesSorted();

        assertThat(result).extracting(DeviceResponseDto::getDeviceType)
                .containsExactly(DeviceType.GATEWAY, DeviceType.SWITCH, DeviceType.ACCESS_POINT);
    }

    @Test
    void getAllDevicesSorted_shouldReturnEmptyListIfNoDevices() {
        when(deviceRepository.findAll()).thenReturn(Collections.emptyList());
        List<DeviceResponseDto> result = deviceService.getAllDevicesSorted();
        assertThat(result).isEmpty();
    }

    @Test
    void getAllDevicesSorted_shouldSortByDeviceTypeOrder() {
        Device d1 = new Device();
        d1.setMacAddress("mac1");
        d1.setDeviceType(DeviceType.ACCESS_POINT);

        Device d2 = new Device();
        d2.setMacAddress("mac2");
        d2.setDeviceType(DeviceType.SWITCH);

        Device d3 = new Device();
        d3.setMacAddress("mac3");
        d3.setDeviceType(DeviceType.GATEWAY);

        when(deviceRepository.findAll()).thenReturn(List.of(d1, d2, d3));

        List<DeviceResponseDto> result = deviceService.getAllDevicesSorted();

        assertThat(result).extracting(DeviceResponseDto::getDeviceType)
                .containsExactly(DeviceType.GATEWAY, DeviceType.SWITCH, DeviceType.ACCESS_POINT);
    }

    @Test
    void getAllDevicesSorted_shouldSortMultipleSameTypeDevicesByInsertionOrder() {
        Device d1 = new Device();
        d1.setMacAddress("mac1");
        d1.setDeviceType(DeviceType.SWITCH);

        Device d2 = new Device();
        d2.setMacAddress("mac2");
        d2.setDeviceType(DeviceType.SWITCH);

        Device d3 = new Device();
        d3.setMacAddress("mac3");
        d3.setDeviceType(DeviceType.GATEWAY);

        when(deviceRepository.findAll()).thenReturn(List.of(d1, d2, d3));

        List<DeviceResponseDto> result = deviceService.getAllDevicesSorted();

        assertThat(result).extracting(DeviceResponseDto::getDeviceType)
                .containsExactly(DeviceType.GATEWAY, DeviceType.SWITCH, DeviceType.SWITCH);
        assertThat(result).extracting(DeviceResponseDto::getMacAddress)
                .containsExactly("mac3", "mac1", "mac2");
    }

    @Test
    void getDeviceByMac_shouldReturnDevice() {
        Device d = new Device();
        d.setMacAddress("mac1");
        d.setDeviceType(DeviceType.SWITCH);

        when(deviceRepository.findById("mac1")).thenReturn(Optional.of(d));

        DeviceResponseDto response = deviceService.getDeviceByMac("mac1");

        assertThat(response.getMacAddress()).isEqualTo("mac1");
        assertThat(response.getDeviceType()).isEqualTo(DeviceType.SWITCH);
    }

    @Test
    void getDeviceByMac_shouldThrowIfNotFound() {
        when(deviceRepository.findById("mac1")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> deviceService.getDeviceByMac("mac1"))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Device not found");
    }

    @Test
    void getFullTopology_shouldReturnRootNodesWithChildren() {
        Device gateway = new Device();
        gateway.setMacAddress("gw");
        gateway.setDeviceType(DeviceType.GATEWAY);

        Device switch1 = new Device();
        switch1.setMacAddress("sw1");
        switch1.setDeviceType(DeviceType.SWITCH);
        switch1.setUplink(gateway);

        Device ap1 = new Device();
        ap1.setMacAddress("ap1");
        ap1.setDeviceType(DeviceType.ACCESS_POINT);
        ap1.setUplink(switch1);

        Device switch2 = new Device();
        switch2.setMacAddress("sw2");
        switch2.setDeviceType(DeviceType.SWITCH);

        when(deviceRepository.findAll()).thenReturn(List.of(gateway, switch1, ap1, switch2));

        List<TopologyNode> roots = deviceService.getFullTopology();

        assertThat(roots)
            .hasSize(2)
            .anySatisfy(root -> {
                if (root.getMacAddress().equals("gw")) {
                    assertThat(root.getChildren()).hasSize(1);
                    TopologyNode sw1Node = root.getChildren().get(0);
                    assertThat(sw1Node.getMacAddress()).isEqualTo("sw1");
                    assertThat(sw1Node.getChildren()).hasSize(1);
                    assertThat(sw1Node.getChildren().get(0).getMacAddress()).isEqualTo("ap1");
                }
                if (root.getMacAddress().equals("sw2")) {
                    assertThat(root.getChildren()).isEmpty();
                }
            });
    }

    @Test
    void getFullTopology_shouldHandleMultipleRootsAndDeepHierarchy() {
        // Root 1
        Device gateway1 = new Device();
        gateway1.setMacAddress("gw1");
        gateway1.setDeviceType(DeviceType.GATEWAY);

        Device switch1 = new Device();
        switch1.setMacAddress("sw1");
        switch1.setDeviceType(DeviceType.SWITCH);
        switch1.setUplink(gateway1);

        Device ap1 = new Device();
        ap1.setMacAddress("ap1");
        ap1.setDeviceType(DeviceType.ACCESS_POINT);
        ap1.setUplink(switch1);

        Device ap2 = new Device();
        ap2.setMacAddress("ap2");
        ap2.setDeviceType(DeviceType.ACCESS_POINT);
        ap2.setUplink(switch1);

        // Root 2
        Device gateway2 = new Device();
        gateway2.setMacAddress("gw2");
        gateway2.setDeviceType(DeviceType.GATEWAY);

        Device switch2 = new Device();
        switch2.setMacAddress("sw2");
        switch2.setDeviceType(DeviceType.SWITCH);
        switch2.setUplink(gateway2);

        Device ap3 = new Device();
        ap3.setMacAddress("ap3");
        ap3.setDeviceType(DeviceType.ACCESS_POINT);
        ap3.setUplink(switch2);

        when(deviceRepository.findAll()).thenReturn(List.of(gateway1, switch1, ap1, ap2, gateway2, switch2, ap3));

        List<TopologyNode> roots = deviceService.getFullTopology();

        assertThat(roots)
            .hasSize(2)
            .anySatisfy(root -> {
                if (root.getMacAddress().equals("gw1")) {
                    assertThat(root.getChildren()).hasSize(1);
                    TopologyNode sw1Node = root.getChildren().get(0);
                    assertThat(sw1Node.getMacAddress()).isEqualTo("sw1");
                    assertThat(sw1Node.getChildren()).hasSize(2);
                    List<String> apMacs = sw1Node.getChildren().stream().map(TopologyNode::getMacAddress).toList();
                    assertThat(apMacs).containsExactlyInAnyOrder("ap1", "ap2");
                }
                if (root.getMacAddress().equals("gw2")) {
                    assertThat(root.getChildren()).hasSize(1);
                    TopologyNode sw2Node = root.getChildren().get(0);
                    assertThat(sw2Node.getMacAddress()).isEqualTo("sw2");
                    assertThat(sw2Node.getChildren()).hasSize(1);
                    assertThat(sw2Node.getChildren().get(0).getMacAddress()).isEqualTo("ap3");
                }
            });
    }

    @Test
    void getFullTopology_shouldReturnEmptyListIfNoDevices() {
        when(deviceRepository.findAll()).thenReturn(Collections.emptyList());
        List<TopologyNode> roots = deviceService.getFullTopology();
        assertThat(roots).isEmpty();
    }

    @Test
    void getFullTopology_shouldHandleCircularReferencesGracefully() {
        Device d1 = new Device();
        d1.setMacAddress("mac1");
        d1.setDeviceType(DeviceType.GATEWAY);

        Device d2 = new Device();
        d2.setMacAddress("mac2");
        d2.setDeviceType(DeviceType.SWITCH);
        d2.setUplink(d1);

        // Introduce a circular reference
        d1.setUplink(d2);

        when(deviceRepository.findAll()).thenReturn(List.of(d1, d2));

        // Should not throw StackOverflowError, but will result in no roots
        List<TopologyNode> roots = deviceService.getFullTopology();
        assertThat(roots).isEmpty();
    }

    @Test
    void getFullTopology_shouldHandleDisconnectedGraphs() {
        Device d1 = new Device();
        d1.setMacAddress("mac1");
        d1.setDeviceType(DeviceType.GATEWAY);

        Device d2 = new Device();
        d2.setMacAddress("mac2");
        d2.setDeviceType(DeviceType.SWITCH);

        Device d3 = new Device();
        d3.setMacAddress("mac3");
        d3.setDeviceType(DeviceType.ACCESS_POINT);

        // No uplinks, all are roots
        when(deviceRepository.findAll()).thenReturn(List.of(d1, d2, d3));

        List<TopologyNode> roots = deviceService.getFullTopology();
        assertThat(roots)
            .hasSize(3)
            .allSatisfy(root -> assertThat(root.getChildren()).isEmpty());
    }

    @Test
    void getFullTopology_shouldHandleLargeHierarchy() {
        List<Device> devices = new ArrayList<>();
        Device root = new Device();
        root.setMacAddress("root");
        root.setDeviceType(DeviceType.GATEWAY);
        devices.add(root);

        Device parent = root;
        // Create a chain of 100 devices
        for (int i = 1; i <= 100; i++) {
            Device d = new Device();
            d.setMacAddress("mac" + i);
            d.setDeviceType(DeviceType.SWITCH);
            d.setUplink(parent);
            devices.add(d);
            parent = d;
        }

        when(deviceRepository.findAll()).thenReturn(devices);

        List<TopologyNode> roots = deviceService.getFullTopology();
        assertThat(roots).hasSize(1);
        TopologyNode current = roots.get(0);
        for (int i = 1; i <= 100; i++) {
            assertThat(current.getChildren()).hasSize(1);
            current = current.getChildren().get(0);
            assertThat(current.getMacAddress()).isEqualTo("mac" + i);
        }
        assertThat(current.getChildren()).isEmpty();
    }
    
    @Test
    void getTopologyFrom_shouldReturnSubtree() {
        Device gateway = new Device();
        gateway.setMacAddress("gw");
        gateway.setDeviceType(DeviceType.GATEWAY);

        Device switch1 = new Device();
        switch1.setMacAddress("sw1");
        switch1.setDeviceType(DeviceType.SWITCH);
        switch1.setUplink(gateway);

        Device ap1 = new Device();
        ap1.setMacAddress("ap1");
        ap1.setDeviceType(DeviceType.ACCESS_POINT);
        ap1.setUplink(switch1);

        when(deviceRepository.findById("sw1")).thenReturn(Optional.of(switch1));
        when(deviceRepository.findAll()).thenReturn(List.of(gateway, switch1, ap1));

        TopologyNode subtree = deviceService.getTopologyFrom("sw1");

        assertThat(subtree.getMacAddress()).isEqualTo("sw1");
        assertThat(subtree.getChildren()).hasSize(1);
        assertThat(subtree.getChildren().get(0).getMacAddress()).isEqualTo("ap1");
    }

    @Test
    void getTopologyFrom_shouldThrowIfNotFound() {
        when(deviceRepository.findById("macX")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> deviceService.getTopologyFrom("macX"))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Device not found");
    }




    @Test
    void getTopologyFrom_shouldReturnDeepSubtree() {
        Device gateway = new Device();
        gateway.setMacAddress("gw");
        gateway.setDeviceType(DeviceType.GATEWAY);

        Device switch1 = new Device();
        switch1.setMacAddress("sw1");
        switch1.setDeviceType(DeviceType.SWITCH);
        switch1.setUplink(gateway);

        Device ap1 = new Device();
        ap1.setMacAddress("ap1");
        ap1.setDeviceType(DeviceType.ACCESS_POINT);
        ap1.setUplink(switch1);

        Device ap2 = new Device();
        ap2.setMacAddress("ap2");
        ap2.setDeviceType(DeviceType.ACCESS_POINT);
        ap2.setUplink(ap1);

        when(deviceRepository.findById("sw1")).thenReturn(Optional.of(switch1));
        when(deviceRepository.findAll()).thenReturn(List.of(gateway, switch1, ap1, ap2));

        TopologyNode subtree = deviceService.getTopologyFrom("sw1");

        assertThat(subtree.getMacAddress()).isEqualTo("sw1");
        assertThat(subtree.getChildren()).hasSize(1);
        TopologyNode ap1Node = subtree.getChildren().get(0);
        assertThat(ap1Node.getMacAddress()).isEqualTo("ap1");
        assertThat(ap1Node.getChildren()).hasSize(1);
        assertThat(ap1Node.getChildren().get(0).getMacAddress()).isEqualTo("ap2");
    }

    @Test
    void getTopologyFrom_shouldReturnSingleNodeIfNoChildren() {
        Device d = new Device();
        d.setMacAddress("macSolo");
        d.setDeviceType(DeviceType.GATEWAY);

        when(deviceRepository.findById("macSolo")).thenReturn(Optional.of(d));
        when(deviceRepository.findAll()).thenReturn(List.of(d));

        TopologyNode node = deviceService.getTopologyFrom("macSolo");
        assertThat(node.getMacAddress()).isEqualTo("macSolo");
        assertThat(node.getChildren()).isEmpty();
    }

}

