package cz.jbenes.ubiquity.device_api;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class DeviceApiApplicationTests {

    @Test
    void contextLoads() {
        // This test ensures that the Spring application context loads successfully.
    }

    @Test
    void mainMethodRunsWithoutExceptions() {
        assertDoesNotThrow(() -> DeviceApiApplication.main(new String[]{}));
    }
}
