package com.frollot.service

import com.frollot.dto.JoinQueueRequest
import com.frollot.dto.LeaveQueueRequest
import com.frollot.model.*
import com.frollot.repository.*
import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import java.math.BigDecimal
import java.util.*
import com.frollot.model.SalonService as SalonServiceEntity

@ExtendWith(MockKExtension::class)
class QueueServiceTest {

    @MockK
    private lateinit var waitingQueueRepository: WaitingQueueRepository

    @MockK
    private lateinit var queueEntryRepository: QueueEntryRepository

    @MockK
    private lateinit var salonRepository: SalonRepository

    @MockK
    private lateinit var userRepository: UserRepository

    @MockK
    private lateinit var salonServiceRepository: SalonServiceRepository

    private lateinit var queueService: QueueService

    private lateinit var salon: Salon
    private lateinit var owner: User
    private lateinit var client: User
    private lateinit var queue: WaitingQueue
    private lateinit var service: SalonServiceEntity

    @BeforeEach
    fun setUp() {
        queueService = QueueService(
            waitingQueueRepository,
            queueEntryRepository,
            salonRepository,
            userRepository,
            salonServiceRepository
        )

        // Créer le propriétaire du salon
        owner = User(
            id = "owner-001",
            email = "owner@test.com",
            passwordHash = "hash",
            userType = UserType.salon_owner,
            firstName = "Owner",
            lastName = "Test",
            isActive = true
        )

        salon = Salon(
            id = "salon-001",
            name = "Test Salon",
            address = "123 Test St",
            city = "Test City",
            postalCode = "12345",
            slug = "test-salon",
            owner = owner  // Définir le propriétaire
        )

        client = User(
            id = "client-001",
            email = "client@test.com",
            passwordHash = "hash",
            userType = UserType.client,
            firstName = "John",
            lastName = "Doe",
            isActive = true
        )

        queue = WaitingQueue(
            id = "queue-001",
            salon = salon
        )

        service = SalonServiceEntity(
            id = "service-001",
            name = "Coupe",
            description = "Coupe de cheveux",
            durationMinutes = 30,
            price = BigDecimal("50.00"),
            category = ServiceCategory.COUPE,
            salon = salon
        )
    }

    @Test
    fun `joinQueue devrait creer une entree valide`() {
        // Given
        val request = JoinQueueRequest(
            salonId = "salon-001",
            userId = "client-001"
        )

        every { salonRepository.findById("salon-001") } returns Optional.of(salon)
        every { userRepository.findById("client-001") } returns Optional.of(client)
        every { waitingQueueRepository.findBySalonId("salon-001") } returns queue
        every { queueEntryRepository.existsByQueueIdAndClientIdAndStatusIn(queue.id!!, any(), any()) } returns false
        every { queueEntryRepository.save(any()) } returnsArgument 0
        every { queueEntryRepository.findByQueueIdAndStatusInOrderByJoinedAtAsc(any(), any()) } returns emptyList()

        // When
        val result = queueService.joinQueue(request)

        // Then
        assert(result.entryId != null)
        verify { queueEntryRepository.save(any()) }
    }

    @Test
    fun `joinQueue devrait rejeter si deja dans la queue`() {
        // Given
        val request = JoinQueueRequest(
            salonId = "salon-001",
            userId = "client-001"
        )

        every { salonRepository.findById("salon-001") } returns Optional.of(salon)
        every { userRepository.findById("client-001") } returns Optional.of(client)
        every { waitingQueueRepository.findBySalonId("salon-001") } returns queue
        every { queueEntryRepository.existsByQueueIdAndClientIdAndStatusIn(queue.id!!, any(), any()) } returns true

        // When / Then
        assertThrows<QueueService.AlreadyInQueueException> {
            queueService.joinQueue(request)
        }
    }

    @Test
    fun `leaveQueue devrait retirer une entree valide`() {
        // Given
        val entry = QueueEntry(
            id = "entry-001",
            queue = queue,
            client = client,
            status = QueueEntryStatus.waiting
        )

        val request = LeaveQueueRequest(
            entryId = "entry-001",
            userId = "client-001"
        )

        every { queueEntryRepository.findById("entry-001") } returns Optional.of(entry)
        every { queueEntryRepository.save(any()) } returnsArgument 0
        every { queueEntryRepository.findByQueueIdAndStatusInOrderByJoinedAtAsc(any(), any()) } returns emptyList()

        // When
        val result = queueService.leaveQueue("salon-001", request)

        // Then
        assert(result.status == QueueEntryStatus.cancelled)
        verify { queueEntryRepository.save(any()) }
    }

    @Test
    fun `callNextClient devrait appeler le prochain client`() {
        // Given
        val entry = QueueEntry(
            id = "entry-001",
            queue = queue,
            client = client,
            status = QueueEntryStatus.waiting
        )

        every { waitingQueueRepository.findBySalonId("salon-001") } returns queue
        every { queueEntryRepository.findFirstByQueueIdAndStatusOrderByJoinedAtAsc(queue.id!!, any()) } returns entry
        every { queueEntryRepository.save(any()) } returnsArgument 0
        every { queueEntryRepository.findByQueueIdAndStatusInOrderByJoinedAtAsc(queue.id!!, any()) } returns listOf(entry)

        // When
        val result = queueService.callNextClient("salon-001", "owner-001")

        // Then
        assert(result.status == QueueEntryStatus.called)
        verify { queueEntryRepository.save(any()) }
    }

    @Test
    fun `callNextClient devrait rejeter si queue vide`() {
        // Given
        every { waitingQueueRepository.findBySalonId("salon-001") } returns queue
        every { queueEntryRepository.findFirstByQueueIdAndStatusOrderByJoinedAtAsc(queue.id!!, any()) } returns null

        // When / Then
        assertThrows<QueueService.InvalidQueueOperationException> {
            queueService.callNextClient("salon-001", "owner-001")
        }
    }

    @Test
    fun `getQueueStatus devrait retourner le statut de la queue`() {
        // Given
        val entry = QueueEntry(
            id = "entry-001",
            queue = queue,
            client = client,
            status = QueueEntryStatus.waiting,
            requestedDurationMinutes = 30
        )

        every { waitingQueueRepository.findBySalonId("salon-001") } returns queue
        every { queueEntryRepository.findByQueueIdAndStatusInOrderByJoinedAtAsc(queue.id!!, any()) } returns listOf(entry)

        // When
        val result = queueService.getQueueStatus("salon-001")

        // Then
        assert(result.salonId == "salon-001")
        assert(result.entries.size == 1)
        assert(result.entries[0].entryId == "entry-001")
    }
}