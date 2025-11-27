package com.servicedesk.ticket.e2e;

import com.servicedesk.ticket.BaseIntegrationTest;
import com.servicedesk.ticket.dto.CreateTicketRequest;
import com.servicedesk.ticket.dto.auth.LoginRequest;
import com.servicedesk.ticket.dto.auth.RegisterRequest;
import com.servicedesk.ticket.entity.Ticket;
import org.junit.jupiter.api.*;
import org.springframework.http.MediaType;

import java.util.Set;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Ticket Controller E2E Tests")
class TicketControllerE2ETest extends BaseIntegrationTest {

    private static String accessToken;
    private static UUID testProjectId;
    private static UUID createdTicketId;
    private static String createdTicketNumber;

    @BeforeAll
    static void setUpTestData(@Autowired org.springframework.test.web.servlet.MockMvc mockMvc,
                               @Autowired com.fasterxml.jackson.databind.ObjectMapper objectMapper) throws Exception {
        // Register and login test user
        RegisterRequest registerRequest = RegisterRequest.builder()
                .email("ticket-test@example.com")
                .password("Password123!")
                .firstName("Ticket")
                .lastName("Tester")
                .build();

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk());

        LoginRequest loginRequest = LoginRequest.builder()
                .email("ticket-test@example.com")
                .password("Password123!")
                .build();

        var result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        accessToken = objectMapper.readTree(response).path("data").path("accessToken").asText();

        // Create test project
        String projectRequest = """
            {
                "name": "Test Project",
                "key": "TEST",
                "description": "Test project for E2E tests"
            }
            """;

        var projectResult = mockMvc.perform(post("/api/v1/projects")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(projectRequest))
                .andExpect(status().isCreated())
                .andReturn();

        String projectResponse = projectResult.getResponse().getContentAsString();
        testProjectId = UUID.fromString(objectMapper.readTree(projectResponse).path("data").path("id").asText());
    }

    @Test
    @Order(1)
    @DisplayName("Should create ticket successfully")
    void shouldCreateTicket() throws Exception {
        CreateTicketRequest request = CreateTicketRequest.builder()
                .subject("Test Ticket - Login Issue")
                .description("User cannot login to the system")
                .projectId(testProjectId)
                .priority(Ticket.TicketPriority.HIGH)
                .type(Ticket.TicketType.INCIDENT)
                .channel(Ticket.TicketChannel.WEB)
                .tags(Set.of("login", "authentication"))
                .build();

        var result = mockMvc.perform(post("/api/v1/tickets")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.subject").value("Test Ticket - Login Issue"))
                .andExpect(jsonPath("$.data.priority").value("HIGH"))
                .andExpect(jsonPath("$.data.type").value("INCIDENT"))
                .andExpect(jsonPath("$.data.status").value("OPEN"))
                .andExpect(jsonPath("$.data.ticketNumber").isNotEmpty())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        createdTicketId = UUID.fromString(objectMapper.readTree(response).path("data").path("id").asText());
        createdTicketNumber = objectMapper.readTree(response).path("data").path("ticketNumber").asText();
    }

    @Test
    @Order(2)
    @DisplayName("Should get ticket by ID")
    void shouldGetTicketById() throws Exception {
        Assumptions.assumeTrue(createdTicketId != null, "Ticket should be created first");

        mockMvc.perform(get("/api/v1/tickets/" + createdTicketId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(createdTicketId.toString()))
                .andExpect(jsonPath("$.data.subject").value("Test Ticket - Login Issue"));
    }

    @Test
    @Order(3)
    @DisplayName("Should get ticket by number")
    void shouldGetTicketByNumber() throws Exception {
        Assumptions.assumeTrue(createdTicketNumber != null, "Ticket should be created first");

        mockMvc.perform(get("/api/v1/tickets/number/" + createdTicketNumber)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.ticketNumber").value(createdTicketNumber));
    }

    @Test
    @Order(4)
    @DisplayName("Should list tickets with pagination")
    void shouldListTicketsWithPagination() throws Exception {
        mockMvc.perform(get("/api/v1/tickets")
                        .header("Authorization", "Bearer " + accessToken)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.totalElements").isNumber())
                .andExpect(jsonPath("$.data.page").value(0))
                .andExpect(jsonPath("$.data.size").value(10));
    }

    @Test
    @Order(5)
    @DisplayName("Should filter tickets by status")
    void shouldFilterTicketsByStatus() throws Exception {
        mockMvc.perform(get("/api/v1/tickets")
                        .header("Authorization", "Bearer " + accessToken)
                        .param("statuses", "OPEN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[*].status", everyItem(is("OPEN"))));
    }

    @Test
    @Order(6)
    @DisplayName("Should filter tickets by priority")
    void shouldFilterTicketsByPriority() throws Exception {
        mockMvc.perform(get("/api/v1/tickets")
                        .header("Authorization", "Bearer " + accessToken)
                        .param("priorities", "HIGH"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[*].priority", everyItem(is("HIGH"))));
    }

    @Test
    @Order(7)
    @DisplayName("Should search tickets by keyword")
    void shouldSearchTickets() throws Exception {
        mockMvc.perform(get("/api/v1/tickets")
                        .header("Authorization", "Bearer " + accessToken)
                        .param("search", "Login Issue"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    @Order(8)
    @DisplayName("Should update ticket")
    void shouldUpdateTicket() throws Exception {
        Assumptions.assumeTrue(createdTicketId != null, "Ticket should be created first");

        String updateRequest = """
            {
                "subject": "Updated - Login Issue Resolved",
                "priority": "MEDIUM",
                "description": "Issue has been investigated"
            }
            """;

        mockMvc.perform(put("/api/v1/tickets/" + createdTicketId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.subject").value("Updated - Login Issue Resolved"))
                .andExpect(jsonPath("$.data.priority").value("MEDIUM"));
    }

    @Test
    @Order(9)
    @DisplayName("Should add comment to ticket")
    void shouldAddCommentToTicket() throws Exception {
        Assumptions.assumeTrue(createdTicketId != null, "Ticket should be created first");

        String commentRequest = """
            {
                "content": "This is a test comment",
                "internal": false
            }
            """;

        mockMvc.perform(post("/api/v1/tickets/" + createdTicketId + "/comments")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(commentRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").value("This is a test comment"));
    }

    @Test
    @Order(10)
    @DisplayName("Should change ticket status")
    void shouldChangeTicketStatus() throws Exception {
        Assumptions.assumeTrue(createdTicketId != null, "Ticket should be created first");

        mockMvc.perform(patch("/api/v1/tickets/" + createdTicketId + "/status")
                        .header("Authorization", "Bearer " + accessToken)
                        .param("status", "IN_PROGRESS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("IN_PROGRESS"));
    }

    @Test
    @Order(11)
    @DisplayName("Should reject ticket creation without authentication")
    void shouldRejectUnauthenticatedTicketCreation() throws Exception {
        CreateTicketRequest request = CreateTicketRequest.builder()
                .subject("Unauthorized Ticket")
                .projectId(testProjectId)
                .build();

        mockMvc.perform(post("/api/v1/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(12)
    @DisplayName("Should reject ticket creation without subject")
    void shouldRejectTicketWithoutSubject() throws Exception {
        CreateTicketRequest request = CreateTicketRequest.builder()
                .projectId(testProjectId)
                .build();

        mockMvc.perform(post("/api/v1/tickets")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(13)
    @DisplayName("Should return 404 for non-existent ticket")
    void shouldReturn404ForNonExistentTicket() throws Exception {
        UUID nonExistentId = UUID.randomUUID();

        mockMvc.perform(get("/api/v1/tickets/" + nonExistentId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNotFound());
    }
}
