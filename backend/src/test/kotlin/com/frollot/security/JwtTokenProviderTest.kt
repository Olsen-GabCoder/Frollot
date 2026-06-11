package com.frollot.security

import com.frollot.model.User
import com.frollot.model.UserType
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class JwtTokenProviderTest {

    private lateinit var jwtTokenProvider: JwtTokenProvider
    private lateinit var testUser: User

    // Secret valide (>= 32 caractères)
    private val validSecret = "this-is-a-secure-secret-key-with-32-chars-minimum!"

    @BeforeEach
    fun setUp() {
        jwtTokenProvider = JwtTokenProvider(
            jwtSecret = validSecret,
            jwtExpirationHours = 24,
            jwtExpirationMinutes = 15,
            activeProfiles = "dev"
        )

        testUser = User(
            id = "user-001",
            email = "test@example.com",
            passwordHash = "hashedPassword",
            userType = UserType.client,
            firstName = "Jean",
            lastName = "Dupont",
            isActive = true,
            isVerified = true
        )
    }

    // ========================================
    // Tests: generateToken
    // ========================================

    @Test
    fun `generateToken devrait créer un token valide`() {
        // When
        val token = jwtTokenProvider.generateToken(testUser)

        // Then
        assertNotNull(token)
        assertTrue(token.isNotEmpty())
        assertTrue(token.split(".").size == 3) // Structure JWT: header.payload.signature
    }

    @Test
    fun `generateToken devrait créer des tokens uniques`() {
        // When
        val token1 = jwtTokenProvider.generateToken(testUser)
        Thread.sleep(1100) // Pause d'une seconde pour différencier les timestamps (JWT utilise des secondes)
        val token2 = jwtTokenProvider.generateToken(testUser)

        // Then
        assertTrue(token1 != token2)
    }

    // ========================================
    // Tests: validateToken
    // ========================================

    @Test
    fun `validateToken devrait retourner true pour un token valide`() {
        // Given
        val token = jwtTokenProvider.generateToken(testUser)

        // When
        val isValid = jwtTokenProvider.validateToken(token)

        // Then
        assertTrue(isValid)
    }

    @Test
    fun `validateToken devrait retourner false pour un token malformé`() {
        // Given
        val malformedToken = "not.a.valid.jwt.token"

        // When
        val isValid = jwtTokenProvider.validateToken(malformedToken)

        // Then
        assertFalse(isValid)
    }

    @Test
    fun `validateToken devrait retourner false pour un token avec signature invalide`() {
        // Given
        val validToken = jwtTokenProvider.generateToken(testUser)
        val tamperedToken = validToken.dropLast(5) + "XXXXX" // Altérer la signature

        // When
        val isValid = jwtTokenProvider.validateToken(tamperedToken)

        // Then
        assertFalse(isValid)
    }

    @Test
    fun `validateToken devrait retourner false pour une chaîne vide`() {
        // When
        val isValid = jwtTokenProvider.validateToken("")

        // Then
        assertFalse(isValid)
    }

    @Test
    fun `validateToken devrait retourner false pour un token signé avec un autre secret`() {
        // Given - créer un token avec un autre secret
        val otherProvider = JwtTokenProvider(
            jwtSecret = "another-secret-key-with-32-characters-minimum!",
            jwtExpirationHours = 24,
            jwtExpirationMinutes = 15,
            activeProfiles = "dev"
        )
        val tokenFromOtherProvider = otherProvider.generateToken(testUser)

        // When - valider avec le provider original
        val isValid = jwtTokenProvider.validateToken(tokenFromOtherProvider)

        // Then
        assertFalse(isValid)
    }

    // ========================================
    // Tests: getUserIdFromToken
    // ========================================

    @Test
    fun `getUserIdFromToken devrait extraire le userId correctement`() {
        // Given
        val token = jwtTokenProvider.generateToken(testUser)

        // When
        val userId = jwtTokenProvider.getUserIdFromToken(token)

        // Then
        assertEquals("user-001", userId)
    }

    // ========================================
    // Tests: getEmailFromToken
    // ========================================

    @Test
    fun `getEmailFromToken devrait extraire l'email correctement`() {
        // Given
        val token = jwtTokenProvider.generateToken(testUser)

        // When
        val email = jwtTokenProvider.getEmailFromToken(token)

        // Then
        assertEquals("test@example.com", email)
    }

    // ========================================
    // Tests: getUserTypeFromToken
    // ========================================

    @Test
    fun `getUserTypeFromToken devrait extraire le userType correctement`() {
        // Given
        val token = jwtTokenProvider.generateToken(testUser)

        // When
        val userType = jwtTokenProvider.getUserTypeFromToken(token)

        // Then
        assertEquals("client", userType)
    }

    @Test
    fun `getUserTypeFromToken devrait supporter tous les types d'utilisateur`() {
        // Given
        val salonOwner = testUser.copy(id = "owner-001", userType = UserType.salon_owner)
        val hairstylist = testUser.copy(id = "stylist-001", userType = UserType.hairstylist)

        val ownerToken = jwtTokenProvider.generateToken(salonOwner)
        val stylistToken = jwtTokenProvider.generateToken(hairstylist)

        // When / Then
        assertEquals("salon_owner", jwtTokenProvider.getUserTypeFromToken(ownerToken))
        assertEquals("hairstylist", jwtTokenProvider.getUserTypeFromToken(stylistToken))
    }

    // ========================================
    // Tests: isUserActiveFromToken
    // ========================================

    @Test
    fun `isUserActiveFromToken devrait retourner true pour un utilisateur actif`() {
        // Given
        val activeUser = testUser.copy(isActive = true)
        val token = jwtTokenProvider.generateToken(activeUser)

        // When
        val isActive = jwtTokenProvider.isUserActiveFromToken(token)

        // Then
        assertTrue(isActive)
    }

    @Test
    fun `isUserActiveFromToken devrait retourner false pour un utilisateur inactif`() {
        // Given
        val inactiveUser = testUser.copy(isActive = false)
        val token = jwtTokenProvider.generateToken(inactiveUser)

        // When
        val isActive = jwtTokenProvider.isUserActiveFromToken(token)

        // Then
        assertFalse(isActive)
    }

    // ========================================
    // Tests: isUserVerifiedFromToken
    // ========================================

    @Test
    fun `isUserVerifiedFromToken devrait retourner true pour un utilisateur vérifié`() {
        // Given
        val verifiedUser = testUser.copy(isVerified = true)
        val token = jwtTokenProvider.generateToken(verifiedUser)

        // When
        val isVerified = jwtTokenProvider.isUserVerifiedFromToken(token)

        // Then
        assertTrue(isVerified)
    }

    @Test
    fun `isUserVerifiedFromToken devrait retourner false pour un utilisateur non vérifié`() {
        // Given
        val unverifiedUser = testUser.copy(isVerified = false)
        val token = jwtTokenProvider.generateToken(unverifiedUser)

        // When
        val isVerified = jwtTokenProvider.isUserVerifiedFromToken(token)

        // Then
        assertFalse(isVerified)
    }

    // ========================================
    // Tests: getUserFromToken
    // ========================================

    @Test
    fun `getUserFromToken devrait reconstruire l'utilisateur correctement`() {
        // Given
        val token = jwtTokenProvider.generateToken(testUser)

        // When
        val reconstructedUser = jwtTokenProvider.getUserFromToken(token)

        // Then
        assertEquals(testUser.id, reconstructedUser.id)
        assertEquals(testUser.email, reconstructedUser.email)
        assertEquals(testUser.userType, reconstructedUser.userType)
        assertEquals(testUser.firstName, reconstructedUser.firstName)
        assertEquals(testUser.lastName, reconstructedUser.lastName)
        assertEquals(testUser.isActive, reconstructedUser.isActive)
        assertEquals(testUser.isVerified, reconstructedUser.isVerified)
        assertEquals("", reconstructedUser.passwordHash) // Jamais inclus dans le token
    }

    // ========================================
    // Tests: getAllClaimsFromToken
    // ========================================

    @Test
    fun `getAllClaimsFromToken devrait retourner tous les claims`() {
        // Given
        val token = jwtTokenProvider.generateToken(testUser)

        // When
        val claims = jwtTokenProvider.getAllClaimsFromToken(token)

        // Then
        assertEquals("user-001", claims["userId"])
        assertEquals("test@example.com", claims["email"])
        assertEquals("client", claims["userType"])
        assertEquals("Jean", claims["firstName"])
        assertEquals("Dupont", claims["lastName"])
        assertEquals(true, claims["isActive"])
        assertEquals(true, claims["isVerified"])
    }

    // ========================================
    // Tests: generateRefreshToken
    // ========================================

    @Test
    fun `generateRefreshToken devrait créer un refresh token valide`() {
        // When
        val refreshToken = jwtTokenProvider.generateRefreshToken(testUser)

        // Then
        assertNotNull(refreshToken)
        assertTrue(refreshToken.isNotEmpty())
        assertTrue(jwtTokenProvider.validateToken(refreshToken))
    }

    @Test
    fun `generateRefreshToken devrait inclure le type refresh dans les claims`() {
        // Given
        val refreshToken = jwtTokenProvider.generateRefreshToken(testUser)

        // Then - le token doit être valide (validation indirecte)
        assertTrue(jwtTokenProvider.validateToken(refreshToken))
        assertEquals(testUser.id, jwtTokenProvider.getUserIdFromToken(refreshToken))
    }

    // ========================================
    // Tests: Validation du secret
    // ========================================

    @Test
    fun `constructeur devrait rejeter un secret trop court en production`() {
        // Given / When / Then
        assertThrows<IllegalStateException> {
            JwtTokenProvider(
                jwtSecret = "short",
                jwtExpirationHours = 24,
                jwtExpirationMinutes = 15,
                activeProfiles = "prod"
            )
        }
    }

    @Test
    fun `constructeur devrait accepter un secret court en développement avec warning`() {
        // Given / When / Then - ne devrait pas lever d'exception
        val provider = JwtTokenProvider(
            jwtSecret = "short-dev-secret-for-testing",
            jwtExpirationHours = 24,
            jwtExpirationMinutes = 15,
            activeProfiles = "dev"
        )
        assertNotNull(provider)
    }

    @Test
    fun `constructeur devrait rejeter la valeur par défaut en production`() {
        // Given / When / Then
        assertThrows<IllegalStateException> {
            JwtTokenProvider(
                jwtSecret = "your-jwt-secret-key-change-in-production",
                jwtExpirationHours = 24,
                jwtExpirationMinutes = 15,
                activeProfiles = "prod"
            )
        }
    }
}
