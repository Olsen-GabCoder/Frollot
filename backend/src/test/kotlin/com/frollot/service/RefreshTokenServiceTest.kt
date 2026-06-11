package com.frollot.service

import com.frollot.model.RefreshToken
import com.frollot.model.User
import com.frollot.model.UserType
import com.frollot.repository.RefreshTokenRepository
import com.frollot.repository.UserRepository
import com.frollot.security.JwtTokenProvider
import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import java.time.LocalDateTime
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@ExtendWith(MockKExtension::class)
class RefreshTokenServiceTest {

    @MockK
    private lateinit var refreshTokenRepository: RefreshTokenRepository

    @MockK
    private lateinit var userRepository: UserRepository

    @MockK
    private lateinit var jwtTokenProvider: JwtTokenProvider

    private lateinit var refreshTokenService: RefreshTokenService

    private lateinit var testUser: User
    private val testUserId = "user-001"

    @BeforeEach
    fun setUp() {
        refreshTokenService = RefreshTokenService(
            refreshTokenRepository,
            userRepository,
            jwtTokenProvider
        )

        testUser = User(
            id = testUserId,
            email = "test@example.com",
            passwordHash = "hashedPassword",
            userType = UserType.client,
            firstName = "Jean",
            lastName = "Dupont",
            isActive = true
        )
    }

    // ========================================
    // Tests: createRefreshToken
    // ========================================

    @Test
    fun `createRefreshToken devrait créer un token valide`() {
        // Given
        every { refreshTokenRepository.save(any()) } answers { firstArg() }

        // When
        val token = refreshTokenService.createRefreshToken(testUserId)

        // Then
        assertNotNull(token)
        assertTrue(token.isNotEmpty())
        
        verify { refreshTokenRepository.save(match { 
            it.userId == testUserId && 
            it.token.isNotEmpty() && 
            !it.revoked &&
            it.expiresAt.isAfter(LocalDateTime.now())
        }) }
    }

    @Test
    fun `createRefreshToken devrait générer des tokens uniques`() {
        // Given
        val tokens = mutableListOf<String>()
        every { refreshTokenRepository.save(any()) } answers { 
            tokens.add((firstArg() as RefreshToken).token)
            firstArg() 
        }

        // When
        val token1 = refreshTokenService.createRefreshToken(testUserId)
        val token2 = refreshTokenService.createRefreshToken(testUserId)

        // Then
        assertTrue(token1 != token2)
    }

    @Test
    fun `createRefreshToken devrait définir une expiration de 7 jours`() {
        // Given
        var savedToken: RefreshToken? = null
        every { refreshTokenRepository.save(any()) } answers { 
            savedToken = firstArg()
            firstArg() 
        }

        // When
        refreshTokenService.createRefreshToken(testUserId)

        // Then
        assertNotNull(savedToken)
        val expectedExpiration = LocalDateTime.now().plusDays(7)
        // Tolérance de 1 minute pour la comparaison
        assertTrue(savedToken!!.expiresAt.isAfter(expectedExpiration.minusMinutes(1)))
        assertTrue(savedToken!!.expiresAt.isBefore(expectedExpiration.plusMinutes(1)))
    }

    // ========================================
    // Tests: validateRefreshToken
    // ========================================

    @Test
    fun `validateRefreshToken devrait retourner le token si valide`() {
        // Given
        val validToken = RefreshToken(
            id = 1L,
            token = "valid-token",
            userId = testUserId,
            expiresAt = LocalDateTime.now().plusDays(5),
            revoked = false
        )
        every { refreshTokenRepository.findByToken("valid-token") } returns validToken

        // When
        val result = refreshTokenService.validateRefreshToken("valid-token")

        // Then
        assertNotNull(result)
        assertEquals("valid-token", result.token)
        assertEquals(testUserId, result.userId)
    }

    @Test
    fun `validateRefreshToken devrait retourner null pour un token expiré`() {
        // Given
        val expiredToken = RefreshToken(
            id = 1L,
            token = "expired-token",
            userId = testUserId,
            expiresAt = LocalDateTime.now().minusDays(1), // Expiré hier
            revoked = false
        )
        every { refreshTokenRepository.findByToken("expired-token") } returns expiredToken

        // When
        val result = refreshTokenService.validateRefreshToken("expired-token")

        // Then
        assertNull(result)
    }

    @Test
    fun `validateRefreshToken devrait retourner null pour un token révoqué`() {
        // Given
        val revokedToken = RefreshToken(
            id = 1L,
            token = "revoked-token",
            userId = testUserId,
            expiresAt = LocalDateTime.now().plusDays(5),
            revoked = true // Révoqué
        )
        every { refreshTokenRepository.findByToken("revoked-token") } returns revokedToken

        // When
        val result = refreshTokenService.validateRefreshToken("revoked-token")

        // Then
        assertNull(result)
    }

    @Test
    fun `validateRefreshToken devrait retourner null pour un token inexistant`() {
        // Given
        every { refreshTokenRepository.findByToken("non-existent") } returns null

        // When
        val result = refreshTokenService.validateRefreshToken("non-existent")

        // Then
        assertNull(result)
    }

    // ========================================
    // Tests: rotateRefreshToken
    // ========================================

    @Test
    fun `rotateRefreshToken devrait effectuer la rotation correctement`() {
        // Given
        val oldToken = RefreshToken(
            id = 1L,
            token = "old-token",
            userId = testUserId,
            expiresAt = LocalDateTime.now().plusDays(5),
            revoked = false
        )
        
        every { refreshTokenRepository.findByToken("old-token") } returns oldToken
        every { refreshTokenRepository.save(any()) } answers { firstArg() }
        every { userRepository.findById(testUserId) } returns Optional.of(testUser)
        every { jwtTokenProvider.generateToken(testUser) } returns "new-access-token"

        // When
        val (accessToken, refreshToken) = refreshTokenService.rotateRefreshToken("old-token")

        // Then
        assertEquals("new-access-token", accessToken)
        assertNotNull(refreshToken)
        assertTrue(refreshToken != "old-token")

        // Vérifier que l'ancien token a été révoqué
        verify { refreshTokenRepository.save(match { it.token == "old-token" && it.revoked }) }
        
        // Vérifier qu'un nouveau token a été créé
        verify { refreshTokenRepository.save(match { it.token != "old-token" && !it.revoked }) }
    }

    @Test
    fun `rotateRefreshToken devrait rejeter un token invalide`() {
        // Given
        every { refreshTokenRepository.findByToken("invalid-token") } returns null

        // When / Then
        assertThrows<IllegalArgumentException> {
            refreshTokenService.rotateRefreshToken("invalid-token")
        }
    }

    @Test
    fun `rotateRefreshToken devrait rejeter un token expiré`() {
        // Given
        val expiredToken = RefreshToken(
            id = 1L,
            token = "expired-token",
            userId = testUserId,
            expiresAt = LocalDateTime.now().minusDays(1),
            revoked = false
        )
        every { refreshTokenRepository.findByToken("expired-token") } returns expiredToken

        // When / Then
        assertThrows<IllegalArgumentException> {
            refreshTokenService.rotateRefreshToken("expired-token")
        }
    }

    @Test
    fun `rotateRefreshToken devrait rejeter si utilisateur non trouvé`() {
        // Given
        val validToken = RefreshToken(
            id = 1L,
            token = "valid-token",
            userId = "non-existent-user",
            expiresAt = LocalDateTime.now().plusDays(5),
            revoked = false
        )
        
        every { refreshTokenRepository.findByToken("valid-token") } returns validToken
        every { refreshTokenRepository.save(any()) } answers { firstArg() }
        every { userRepository.findById("non-existent-user") } returns Optional.empty()

        // When / Then
        assertThrows<IllegalArgumentException> {
            refreshTokenService.rotateRefreshToken("valid-token")
        }
    }

    // ========================================
    // Tests: revokeRefreshToken
    // ========================================

    @Test
    fun `revokeRefreshToken devrait révoquer un token existant`() {
        // Given
        val token = RefreshToken(
            id = 1L,
            token = "token-to-revoke",
            userId = testUserId,
            expiresAt = LocalDateTime.now().plusDays(5),
            revoked = false
        )
        
        every { refreshTokenRepository.findByToken("token-to-revoke") } returns token
        every { refreshTokenRepository.save(any()) } answers { firstArg() }

        // When
        refreshTokenService.revokeRefreshToken("token-to-revoke")

        // Then
        verify { refreshTokenRepository.save(match { it.revoked }) }
    }

    @Test
    fun `revokeRefreshToken devrait ignorer un token inexistant`() {
        // Given
        every { refreshTokenRepository.findByToken("non-existent") } returns null

        // When
        refreshTokenService.revokeRefreshToken("non-existent")

        // Then - pas d'exception, pas de sauvegarde
        verify(exactly = 0) { refreshTokenRepository.save(any()) }
    }

    // ========================================
    // Tests: revokeAllUserTokens
    // ========================================

    @Test
    fun `revokeAllUserTokens devrait révoquer tous les tokens de l'utilisateur`() {
        // Given
        val tokens = listOf(
            RefreshToken(id = 1L, token = "token-1", userId = testUserId, 
                        expiresAt = LocalDateTime.now().plusDays(5), revoked = false),
            RefreshToken(id = 2L, token = "token-2", userId = testUserId, 
                        expiresAt = LocalDateTime.now().plusDays(3), revoked = false),
            RefreshToken(id = 3L, token = "token-3", userId = testUserId, 
                        expiresAt = LocalDateTime.now().plusDays(1), revoked = true) // Déjà révoqué
        )
        
        every { refreshTokenRepository.findAllByUserId(testUserId) } returns tokens
        every { refreshTokenRepository.save(any()) } answers { firstArg() }

        // When
        refreshTokenService.revokeAllUserTokens(testUserId)

        // Then - seulement 2 tokens non révoqués doivent être sauvegardés
        verify(exactly = 2) { refreshTokenRepository.save(any()) }
    }

    @Test
    fun `revokeAllUserTokens devrait ne rien faire si aucun token`() {
        // Given
        every { refreshTokenRepository.findAllByUserId(testUserId) } returns emptyList()

        // When
        refreshTokenService.revokeAllUserTokens(testUserId)

        // Then
        verify(exactly = 0) { refreshTokenRepository.save(any()) }
    }

    // ========================================
    // Tests: cleanupExpiredTokens
    // ========================================

    @Test
    fun `cleanupExpiredTokens devrait supprimer les tokens expirés`() {
        // Given
        every { refreshTokenRepository.deleteByExpiresAtBefore(any()) } just Runs

        // When
        refreshTokenService.cleanupExpiredTokens()

        // Then
        verify { refreshTokenRepository.deleteByExpiresAtBefore(match { 
            it.isBefore(LocalDateTime.now()) 
        }) }
    }
}

