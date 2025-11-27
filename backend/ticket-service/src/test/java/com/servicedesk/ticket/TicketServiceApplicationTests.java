package com.servicedesk.ticket;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Application Context Tests")
class TicketServiceApplicationTests extends BaseIntegrationTest {

    @Test
    @DisplayName("Should load application context successfully")
    void contextLoads() {
        // This test verifies that the Spring context loads successfully
        // with all beans properly configured
    }
}
