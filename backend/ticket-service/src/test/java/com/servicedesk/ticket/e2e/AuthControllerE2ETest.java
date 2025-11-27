package com.servicedesk.ticket.e2e;

import com.servicedesk.ticket.BaseIntegrationTest;
import com.servicedesk.ticket.dto.auth.LoginRequest;
import com.servicedesk.ticket.dto.auth.RegisterRequest;
import org.junit.jupiter.api.*;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Auth Controller E2E Tests")
class AuthControllerE2ETest extends BaseIntegrationTest {

    private static String accessToken;
    private static String refreshToken;

    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_PASSWORD = "Password123!";

    @Test
    @Order(1)
    @DisplayName("Should register new user successfully")
    void shouldRegisterNewUser() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .email(TEST_EMAIL)
                .password(TEST_PASSWORD)
                .firstName("Test")
                .lastName("User")
                .phone("+1234567890")
                .locale("en")
                .timezone("UTC")
                .build();

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.data.user.email").value(TEST_EMAIL))
                .andExpect(jsonPath("$.data.user.firstName").value("Test"))
                .andExpect(jsonPath("$.data.user.lastName").value("User"));
    }

    @Test
    @Order(2)
    @DisplayName("Should reject duplicate email registration")
    void shouldRejectDuplicateEmail() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .email(TEST_EMAIL)
                .password(TEST_PASSWORD)
                .firstName("Test")
                .lastName("User")
                .build();

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @Order(3)
    @DisplayName("Should login with valid credentials")
    void shouldLoginWithValidCredentials() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .email(TEST_EMAIL)
                .password(TEST_PASSWORD)
                .rememberMe(false)
                .build();

        var result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.refreshToken").isNotEmpty())
                .andReturn();

        // Extract tokens for subsequent tests
        String response = result.getResponse().getContentAsString();
        accessToken = objectMapper.readTree(response).path("data").path("accessToken").asText();
        refreshToken = objectMapper.readTree(response).path("data").path("refreshToken").asText();
    }

    @Test
    @Order(4)
    @DisplayName("Should reject login with invalid password")
    void shouldRejectInvalidPassword() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .email(TEST_EMAIL)
                .password("wrongpassword")
                .build();

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @Order(5)
    @DisplayName("Should reject login with non-existent email")
    void shouldRejectNonExistentEmail() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .email("nonexistent@example.com")
                .password(TEST_PASSWORD)
                .build();

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @Order(6)
    @DisplayName("Should get current user with valid token")
    void shouldGetCurrentUserWithValidToken() throws Exception {
        Assumptions.assumeTrue(accessToken != null, "Access token should be available from login test");

        mockMvc.perform(get("/api/v1/auth/me")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value(TEST_EMAIL));
    }

    @Test
    @Order(7)
    @DisplayName("Should reject unauthorized access to /me endpoint")
    void shouldRejectUnauthorizedAccess() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(8)
    @DisplayName("Should logout successfully")
    void shouldLogoutSuccessfully() throws Exception {
        Assumptions.assumeTrue(accessToken != null, "Access token should be available from login test");

        mockMvc.perform(post("/api/v1/auth/logout")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("Should reject registration with invalid email format")
    void shouldRejectInvalidEmailFormat() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .email("invalid-email")
                .password(TEST_PASSWORD)
                .firstName("Test")
                .lastName("User")
                .build();

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should reject registration with short password")
    void shouldRejectShortPassword() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .email("newuser@example.com")
                .password("short")
                .firstName("Test")
                .lastName("User")
                .build();

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should reject registration with missing required fields")
    void shouldRejectMissingRequiredFields() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .email("newuser@example.com")
                .build();

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(request)))
                .andExpect(status().isBadRequest());
    }
}
