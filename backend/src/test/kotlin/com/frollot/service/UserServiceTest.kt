package com.frollot.service

import com.frollot.dto.RegisterRequest
import com.frollot.exception.EmailAlreadyExistsException
import com.frollot.model.User
import com.frollot.model.UserType
import com.frollot.repository.UserRepository
import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.security.crypto.password.PasswordEncoder
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@ExtendWith(MockKExtension::class)
class UserServiceTest {

    @MockK
    private lateinit var userRepository: UserRepository

    @MockK
    private lateinit var passwordEncoder: PasswordEncoder

    private lateinit var userService: UserService

    @BeforeEach
    fun setUp() {
        userService = UserService(userRepository, passwordEncoder)
    }

    // ========================================
    // Tests: registerUser
    // ========================================

    @Test
    fun `registerUser devrait créer un utilisateur avec un email valide`() {
        // Given
        val request = RegisterRequest(
            email = "test@example.com",
            password = "securePassword123",
            firstName = "Jean",
            lastName = "Dupont",
            userType = UserType.client
        )

        every { userRepository.existsByEmail("test@example.com") } returns false
        every { passwordEncoder.encode("securePassword123") } returns "hashedPassword"
        every { userRepository.save(any()) } answers { firstArg() }

        // When
        val result = userService.registerUser(request)

        // Then
        assertNotNull(result.id)
        assertEquals("test@example.com", result.email)
        assertEquals("Jean", result.firstName)
        assertEquals("Dupont", result.lastName)
        assertEquals(UserType.client, result.userType)
        assertEquals("hashedPassword", result.passwordHash)
        assertTrue(result.isActive)
        assertFalse(result.isVerified)

        verify { userRepository.existsByEmail("test@example.com") }
        verify { passwordEncoder.encode("securePassword123") }
        verify { userRepository.save(any()) }
    }

    @Test
    fun `registerUser devrait normaliser l'email en minuscules`() {
        // Given
        val request = RegisterRequest(
            email = "TEST@EXAMPLE.COM",
            password = "securePassword123",
            firstName = "Jean",
            lastName = "Dupont",
            userType = UserType.client
        )

        // L'email est vérifié tel quel (avant normalisation)
        every { userRepository.existsByEmail("TEST@EXAMPLE.COM") } returns false
        every { passwordEncoder.encode("securePassword123") } returns "hashedPassword"
        every { userRepository.save(any()) } answers { firstArg() }

        // When
        val result = userService.registerUser(request)

        // Then - L'email doit être normalisé en minuscules
        assertEquals("test@example.com", result.email)
    }

    @Test
    fun `registerUser devrait trim les espaces du nom et prénom`() {
        // Given
        val request = RegisterRequest(
            email = "test@example.com",
            password = "securePassword123",
            firstName = "  Jean  ",
            lastName = "  Dupont  ",
            userType = UserType.client
        )

        every { userRepository.existsByEmail("test@example.com") } returns false
        every { passwordEncoder.encode(any()) } returns "hashedPassword"
        every { userRepository.save(any()) } answers { firstArg() }

        // When
        val result = userService.registerUser(request)

        // Then
        assertEquals("Jean", result.firstName)
        assertEquals("Dupont", result.lastName)
    }

    @Test
    fun `registerUser devrait rejeter un email déjà utilisé`() {
        // Given
        val request = RegisterRequest(
            email = "existing@example.com",
            password = "securePassword123",
            firstName = "Jean",
            lastName = "Dupont",
            userType = UserType.client
        )

        every { userRepository.existsByEmail("existing@example.com") } returns true

        // When / Then
        val exception = assertThrows<EmailAlreadyExistsException> {
            userService.registerUser(request)
        }

        assertTrue(exception.message!!.contains("existing@example.com"))
        verify { userRepository.existsByEmail("existing@example.com") }
        verify(exactly = 0) { userRepository.save(any()) }
    }

    @Test
    fun `registerUser devrait rejeter un email invalide`() {
        // Given
        val request = RegisterRequest(
            email = "invalid-email",
            password = "securePassword123",
            firstName = "Jean",
            lastName = "Dupont",
            userType = UserType.client
        )

        every { userRepository.existsByEmail("invalid-email") } returns false

        // When / Then
        val exception = assertThrows<IllegalArgumentException> {
            userService.registerUser(request)
        }

        assertEquals("Format d'email invalide", exception.message)
    }

    @Test
    fun `registerUser devrait rejeter un email sans domaine`() {
        // Given
        val request = RegisterRequest(
            email = "test@",
            password = "securePassword123",
            firstName = "Jean",
            lastName = "Dupont",
            userType = UserType.client
        )

        every { userRepository.existsByEmail("test@") } returns false

        // When / Then
        assertThrows<IllegalArgumentException> {
            userService.registerUser(request)
        }
    }

    @Test
    fun `registerUser devrait rejeter un mot de passe trop court`() {
        // Given
        val request = RegisterRequest(
            email = "test@example.com",
            password = "short",  // < 8 caractères
            firstName = "Jean",
            lastName = "Dupont",
            userType = UserType.client
        )

        every { userRepository.existsByEmail("test@example.com") } returns false

        // When / Then
        val exception = assertThrows<IllegalArgumentException> {
            userService.registerUser(request)
        }

        assertEquals("Le mot de passe doit contenir au moins 8 caractères", exception.message)
    }

    @Test
    fun `registerUser devrait accepter un mot de passe de 8 caractères exactement`() {
        // Given
        val request = RegisterRequest(
            email = "test@example.com",
            password = "12345678",  // Exactement 8 caractères
            firstName = "Jean",
            lastName = "Dupont",
            userType = UserType.client
        )

        every { userRepository.existsByEmail("test@example.com") } returns false
        every { passwordEncoder.encode("12345678") } returns "hashedPassword"
        every { userRepository.save(any()) } answers { firstArg() }

        // When
        val result = userService.registerUser(request)

        // Then
        assertNotNull(result)
    }

    @Test
    fun `registerUser devrait créer un propriétaire de salon`() {
        // Given
        val request = RegisterRequest(
            email = "owner@salon.com",
            password = "securePassword123",
            firstName = "Marie",
            lastName = "Coiffeuse",
            userType = UserType.salon_owner
        )

        every { userRepository.existsByEmail("owner@salon.com") } returns false
        every { passwordEncoder.encode(any()) } returns "hashedPassword"
        every { userRepository.save(any()) } answers { firstArg() }

        // When
        val result = userService.registerUser(request)

        // Then
        assertEquals(UserType.salon_owner, result.userType)
    }

    @Test
    fun `registerUser devrait créer un coiffeur`() {
        // Given
        val request = RegisterRequest(
            email = "coiffeur@salon.com",
            password = "securePassword123",
            firstName = "Pierre",
            lastName = "Styliste",
            userType = UserType.hairstylist
        )

        every { userRepository.existsByEmail("coiffeur@salon.com") } returns false
        every { passwordEncoder.encode(any()) } returns "hashedPassword"
        every { userRepository.save(any()) } answers { firstArg() }

        // When
        val result = userService.registerUser(request)

        // Then
        assertEquals(UserType.hairstylist, result.userType)
    }

    // ========================================
    // Tests: checkPassword
    // ========================================

    @Test
    fun `checkPassword devrait retourner true pour un mot de passe correct`() {
        // Given
        val rawPassword = "mySecretPassword"
        val encodedPassword = "\$2a\$10\$hashedValue"

        every { passwordEncoder.matches(rawPassword, encodedPassword) } returns true

        // When
        val result = userService.checkPassword(rawPassword, encodedPassword)

        // Then
        assertTrue(result)
        verify { passwordEncoder.matches(rawPassword, encodedPassword) }
    }

    @Test
    fun `checkPassword devrait retourner false pour un mot de passe incorrect`() {
        // Given
        val rawPassword = "wrongPassword"
        val encodedPassword = "\$2a\$10\$hashedValue"

        every { passwordEncoder.matches(rawPassword, encodedPassword) } returns false

        // When
        val result = userService.checkPassword(rawPassword, encodedPassword)

        // Then
        assertFalse(result)
    }

    // ========================================
    // Tests: updateUserAvatar
    // ========================================

    @Test
    fun `updateUserAvatar devrait mettre à jour l'avatar d'un utilisateur existant`() {
        // Given
        val userId = "user-001"
        val avatarUrl = "https://storage.example.com/avatar.jpg"
        val existingUser = User(
            id = userId,
            email = "user@example.com",
            passwordHash = "hash",
            userType = UserType.client,
            firstName = "Jean",
            lastName = "Dupont",
            isActive = true,
            avatarUrl = null
        )

        every { userRepository.findById(userId) } returns Optional.of(existingUser)
        every { userRepository.save(any()) } answers { firstArg() }

        // When
        val result = userService.updateUserAvatar(userId, avatarUrl)

        // Then
        assertEquals(avatarUrl, result.avatarUrl)
        verify { userRepository.save(any()) }
    }

    @Test
    fun `updateUserAvatar devrait lever une exception pour un utilisateur inexistant`() {
        // Given
        val userId = "non-existent-user"
        val avatarUrl = "https://storage.example.com/avatar.jpg"

        every { userRepository.findById(userId) } returns Optional.empty()

        // When / Then
        val exception = assertThrows<IllegalArgumentException> {
            userService.updateUserAvatar(userId, avatarUrl)
        }

        assertTrue(exception.message!!.contains(userId))
        verify(exactly = 0) { userRepository.save(any()) }
    }

    // ========================================
    // Tests: getAllUsers
    // ========================================

    @Test
    fun `getAllUsers devrait retourner tous les utilisateurs`() {
        // Given
        val users = listOf(
            User(
                id = "user-001",
                email = "user1@example.com",
                passwordHash = "hash1",
                userType = UserType.client,
                firstName = "Jean",
                lastName = "Dupont",
                isActive = true
            ),
            User(
                id = "user-002",
                email = "user2@example.com",
                passwordHash = "hash2",
                userType = UserType.salon_owner,
                firstName = "Marie",
                lastName = "Martin",
                isActive = true
            )
        )

        every { userRepository.findAll() } returns users

        // When
        val result = userService.getAllUsers()

        // Then
        assertEquals(2, result.size)
        assertEquals("user1@example.com", result[0].email)
        assertEquals("user2@example.com", result[1].email)
    }

    @Test
    fun `getAllUsers devrait retourner une liste vide si aucun utilisateur`() {
        // Given
        every { userRepository.findAll() } returns emptyList()

        // When
        val result = userService.getAllUsers()

        // Then
        assertTrue(result.isEmpty())
    }
}

