package com.frollot.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.frollot.dto.LoginRequest
import com.frollot.dto.RegisterRequest
import com.frollot.model.User
import com.frollot.model.UserType
import com.frollot.repository.RefreshTokenRepository
import com.frollot.repository.UserRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.transaction.annotation.Transactional
import java.util.*

/**
 * Tests d'intégration pour les endpoints d'authentification.
 *
 * Ces tests vérifient le comportement complet du système d'authentification,
 * incluant l'inscription, la connexion, le refresh token et la déconnexion.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthControllerIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var refreshTokenRepository: RefreshTokenRepository

    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder

    private lateinit var existingUser: User
    private val testPassword = "SecurePassword123!"

    @BeforeEach
    fun setUp() {
        // Créer un utilisateur existant pour les tests de login
        existingUser = User(
            id = UUID.randomUUID().toString(),
            email = "existing@test.com",
            passwordHash = passwordEncoder.encode(testPassword),
            userType = UserType.client,
            firstName = "Jean",
            lastName = "Existant",
            isActive = true,
            isVerified = true
        )
        userRepository.save(existingUser)
    }

    // ========================================
    // Tests: POST /api/users/register
    // ========================================

    @Test
    fun `register devrait créer un nouvel utilisateur avec succès`() {
        // Given
        val request = RegisterRequest(
            email = "nouveau@test.com",
            password = "MotDePasse123!",
            firstName = "Nouveau",
            lastName = "Utilisateur",
            userType = UserType.client
        )

        // When / Then
        mockMvc.perform(
            post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.accessToken").exists())
            .andExpect(jsonPath("$.refreshToken").exists())
            .andExpect(jsonPath("$.email").value("nouveau@test.com"))
            .andExpect(jsonPath("$.firstName").value("Nouveau"))
            .andExpect(jsonPath("$.lastName").value("Utilisateur"))
            .andExpect(jsonPath("$.userType").value("client"))
    }

    @Test
    fun `register devrait rejeter un email déjà utilisé`() {
        // Given
        val request = RegisterRequest(
            email = "existing@test.com", // Email déjà utilisé
            password = "MotDePasse123!",
            firstName = "Duplicate",
            lastName = "User",
            userType = UserType.client
        )

        // When / Then
        mockMvc.perform(
            post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isConflict)
    }

    @Test
    fun `register devrait rejeter un email invalide`() {
        // Given
        val request = RegisterRequest(
            email = "invalid-email",
            password = "MotDePasse123!",
            firstName = "Test",
            lastName = "User",
            userType = UserType.client
        )

        // When / Then
        mockMvc.perform(
            post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `register devrait rejeter un mot de passe trop court`() {
        // Given
        val request = RegisterRequest(
            email = "newuser@test.com",
            password = "short", // < 8 caractères
            firstName = "Test",
            lastName = "User",
            userType = UserType.client
        )

        // When / Then
        mockMvc.perform(
            post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `register devrait créer un salon owner`() {
        // Given
        val request = RegisterRequest(
            email = "owner@salon.com",
            password = "MotDePasse123!",
            firstName = "Salon",
            lastName = "Owner",
            userType = UserType.salon_owner
        )

        // When / Then
        mockMvc.perform(
            post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.userType").value("salon_owner"))
    }

    // ========================================
    // Tests: POST /api/users/login
    // ========================================

    @Test
    fun `login devrait retourner les tokens pour des identifiants valides`() {
        // Given
        val request = LoginRequest(
            email = "existing@test.com",
            password = testPassword
        )

        // When / Then
        mockMvc.perform(
            post("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.accessToken").exists())
            .andExpect(jsonPath("$.refreshToken").exists())
            .andExpect(jsonPath("$.email").value("existing@test.com"))
            .andExpect(jsonPath("$.firstName").value("Jean"))
            .andExpect(jsonPath("$.lastName").value("Existant"))
    }

    @Test
    fun `login devrait rejeter un mot de passe incorrect`() {
        // Given
        val request = LoginRequest(
            email = "existing@test.com",
            password = "WrongPassword123!"
        )

        // When / Then
        mockMvc.perform(
            post("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `login devrait rejeter un email inexistant`() {
        // Given
        val request = LoginRequest(
            email = "nonexistent@test.com",
            password = testPassword
        )

        // When / Then
        mockMvc.perform(
            post("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `login devrait rejeter un utilisateur inactif`() {
        // Given - désactiver l'utilisateur
        existingUser.isActive = false
        userRepository.save(existingUser)

        val request = LoginRequest(
            email = "existing@test.com",
            password = testPassword
        )

        // When / Then
        mockMvc.perform(
            post("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isForbidden)
    }

    // ========================================
    // Tests: POST /api/users/refresh
    // ========================================

    @Test
    fun `refresh devrait retourner de nouveaux tokens avec un refresh token valide`() {
        // Given - se connecter d'abord pour obtenir un refresh token
        val loginRequest = LoginRequest(
            email = "existing@test.com",
            password = testPassword
        )

        val loginResult = mockMvc.perform(
            post("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest))
        )
            .andExpect(status().isOk)
            .andReturn()

        val loginResponse = objectMapper.readTree(loginResult.response.contentAsString)
        val refreshToken = loginResponse["refreshToken"].asText()

        // When / Then
        mockMvc.perform(
            post("/api/users/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"refreshToken": "$refreshToken"}""")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.accessToken").exists())
            .andExpect(jsonPath("$.refreshToken").exists())
    }

    @Test
    fun `refresh devrait rejeter un refresh token invalide`() {
        // When / Then
        mockMvc.perform(
            post("/api/users/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"refreshToken": "invalid-token"}""")
        )
            .andExpect(status().isUnauthorized)
    }

    // ========================================
    // Tests: GET /api/users/me (authentifié)
    // ========================================

    @Test
    fun `me devrait retourner l'utilisateur courant avec un token valide`() {
        // Given - se connecter d'abord
        val loginRequest = LoginRequest(
            email = "existing@test.com",
            password = testPassword
        )

        val loginResult = mockMvc.perform(
            post("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest))
        )
            .andExpect(status().isOk)
            .andReturn()

        val loginResponse = objectMapper.readTree(loginResult.response.contentAsString)
        val accessToken = loginResponse["accessToken"].asText()

        // When / Then
        mockMvc.perform(
            get("/api/users/me")
                .header("Authorization", "Bearer $accessToken")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.email").value("existing@test.com"))
            .andExpect(jsonPath("$.firstName").value("Jean"))
    }

    @Test
    fun `me devrait rejeter une requête sans token`() {
        // When / Then
        mockMvc.perform(get("/api/users/me"))
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `me devrait rejeter un token invalide`() {
        // When / Then
        mockMvc.perform(
            get("/api/users/me")
                .header("Authorization", "Bearer invalid-token")
        )
            .andExpect(status().isUnauthorized)
    }

    // ========================================
    // Tests: POST /api/users/logout
    // ========================================

    @Test
    fun `logout devrait révoquer le refresh token`() {
        // Given - se connecter d'abord
        val loginRequest = LoginRequest(
            email = "existing@test.com",
            password = testPassword
        )

        val loginResult = mockMvc.perform(
            post("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest))
        )
            .andExpect(status().isOk)
            .andReturn()

        val loginResponse = objectMapper.readTree(loginResult.response.contentAsString)
        val accessToken = loginResponse["accessToken"].asText()
        val refreshToken = loginResponse["refreshToken"].asText()

        // When - logout
        mockMvc.perform(
            post("/api/users/logout")
                .header("Authorization", "Bearer $accessToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"refreshToken": "$refreshToken"}""")
        )
            .andExpect(status().isNoContent)

        // Then - le refresh token devrait être révoqué
        mockMvc.perform(
            post("/api/users/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"refreshToken": "$refreshToken"}""")
        )
            .andExpect(status().isUnauthorized)
    }
}

