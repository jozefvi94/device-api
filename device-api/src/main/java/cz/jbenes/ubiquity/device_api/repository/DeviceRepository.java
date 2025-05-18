package cz.jbenes.ubiquity.device_api.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import cz.jbenes.ubiquity.device_api.model.Device;

public interface DeviceRepository extends JpaRepository<Device, String> {
    
}
