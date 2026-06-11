package com.frollot.service

import com.frollot.dto.CreateBookingRequest
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
import java.time.LocalDateTime
import java.util.*
import com.frollot.model.SalonService as SalonServiceEntity

@ExtendWith(MockKExtension::class)
class BookingServiceTest {

    @MockK
    private lateinit var bookingRepository: BookingRepository

    @MockK
    private lateinit var salonRepository: SalonRepository

    @MockK
    private lateinit var userRepository: UserRepository

    @MockK
    private lateinit var salonServiceRepository: SalonServiceRepository

    @MockK
    private lateinit var salonStaffRepository: SalonStaffRepository

    private lateinit var bookingService: BookingService

    private lateinit var salon: Salon
    private lateinit var client: User
    private lateinit var service: SalonServiceEntity
    private lateinit var staff: SalonStaff
    private lateinit var futureDateTime: LocalDateTime

    @BeforeEach
    fun setUp() {
        bookingService = BookingService(
            bookingRepository,
            salonRepository,
            userRepository,
            salonServiceRepository,
            salonStaffRepository
        )
        futureDateTime = LocalDateTime.now().plusDays(1).withHour(14).withMinute(0)

        salon = Salon(
            id = "salon-001",
            name = "Test Salon",
            address = "123 Test St",
            city = "Test City",
            postalCode = "12345",
            slug = "test-salon"
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

        service = SalonServiceEntity(
            id = "service-001",
            name = "Coupe",
            description = "Coupe de cheveux",
            durationMinutes = 30,
            price = BigDecimal("50.00"),
            category = ServiceCategory.COUPE,
            salon = salon
        )

        val staffUser = User(
            id = "staff-user-001",
            email = "staff@test.com",
            passwordHash = "hash",
            userType = UserType.hairstylist,
            firstName = "Jane",
            lastName = "Smith",
            isActive = true
        )

        staff = SalonStaff(
            id = "staff-001",
            salon = salon,
            user = staffUser,
            specialties = mutableListOf(ServiceCategory.COUPE),
            isActive = true
        )
    }

    @Test
    fun `createBooking devrait creer une reservation valide`() {
        // Given
        val request = CreateBookingRequest(
            salonId = "salon-001",
            clientId = "client-001",
            serviceId = "service-001",
            bookingDatetime = futureDateTime
        )

        every { salonRepository.findById("salon-001") } returns Optional.of(salon)
        every { userRepository.findById("client-001") } returns Optional.of(client)
        every { salonServiceRepository.findById("service-001") } returns Optional.of(service)
        every { bookingRepository.hasClientConflict(any(), any(), any()) } returns 0L
        every { bookingRepository.save(any()) } returnsArgument 0

        // When
        val result = bookingService.createBooking(request)

        // Then
        assert(result.id != null)
        assert(result.salonId == "salon-001")
        assert(result.clientId == "client-001")
        assert(result.serviceId == "service-001")
        verify { bookingRepository.save(any()) }
    }

    @Test
    fun `createBooking devrait detecter un conflit client`() {
        // Given
        val request = CreateBookingRequest(
            salonId = "salon-001",
            clientId = "client-001",
            serviceId = "service-001",
            bookingDatetime = futureDateTime
        )

        every { salonRepository.findById("salon-001") } returns Optional.of(salon)
        every { userRepository.findById("client-001") } returns Optional.of(client)
        every { salonServiceRepository.findById("service-001") } returns Optional.of(service)
        every { bookingRepository.hasClientConflict(any(), any(), any()) } returns 1L

        // When / Then
        assertThrows<BookingService.SlotUnavailableException> {
            bookingService.createBooking(request)
        }
    }

    @Test
    fun `createBooking devrait detecter un conflit staff`() {
        // Given
        val request = CreateBookingRequest(
            salonId = "salon-001",
            clientId = "client-001",
            staffId = "staff-001",
            serviceId = "service-001",
            bookingDatetime = futureDateTime
        )

        every { salonRepository.findById("salon-001") } returns Optional.of(salon)
        every { userRepository.findById("client-001") } returns Optional.of(client)
        every { salonServiceRepository.findById("service-001") } returns Optional.of(service)
        every { salonStaffRepository.findById("staff-001") } returns Optional.of(staff)
        every { bookingRepository.hasClientConflict(any(), any(), any()) } returns 0L
        every { bookingRepository.hasStaffConflict(any(), any(), any()) } returns 1L

        // When / Then
        assertThrows<BookingService.SlotUnavailableException> {
            bookingService.createBooking(request)
        }
    }

    @Test
    fun `createBooking devrait rejeter une reservation dans le passe`() {
        // Given
        val pastDateTime = LocalDateTime.now().minusHours(1)
        val request = CreateBookingRequest(
            salonId = "salon-001",
            clientId = "client-001",
            serviceId = "service-001",
            bookingDatetime = pastDateTime
        )

        every { salonRepository.findById("salon-001") } returns Optional.of(salon)
        every { userRepository.findById("client-001") } returns Optional.of(client)
        every { salonServiceRepository.findById("service-001") } returns Optional.of(service)

        // When / Then
        assertThrows<BookingService.InvalidBookingException> {
            bookingService.createBooking(request)
        }
    }

    @Test
    fun `createBooking devrait valider que le service appartient au salon`() {
        // Given
        val otherSalon = Salon(
            id = "salon-002",
            name = "Other Salon",
            address = "456 St",
            city = "City",
            postalCode = "54321",
            slug = "other-salon"
        )

        val serviceFromOtherSalon = SalonServiceEntity(
            id = "service-001",
            name = service.name,
            description = service.description,
            durationMinutes = service.durationMinutes,
            price = service.price,
            category = service.category,
            salon = otherSalon
        )

        val request = CreateBookingRequest(
            salonId = "salon-001",
            clientId = "client-001",
            serviceId = "service-001",
            bookingDatetime = futureDateTime
        )

        every { salonRepository.findById("salon-001") } returns Optional.of(salon)
        every { userRepository.findById("client-001") } returns Optional.of(client)
        every { salonServiceRepository.findById("service-001") } returns Optional.of(serviceFromOtherSalon)

        // When / Then
        assertThrows<BookingService.InvalidBookingException> {
            bookingService.createBooking(request)
        }
    }

    @Test
    fun `cancelBooking devrait annuler une reservation par le client proprietaire`() {
        // Given
        val booking = Booking(
            id = "booking-001",
            salon = salon,
            client = client,
            service = service,
            bookingDatetime = futureDateTime,
            status = BookingStatus.confirmed
        )

        every { bookingRepository.findById("booking-001") } returns Optional.of(booking)
        every { bookingRepository.save(any()) } returnsArgument 0

        // When
        val result = bookingService.cancelBooking("booking-001", "client-001")

        // Then
        assert(result.status == BookingStatus.cancelled)
        assert(result.id == "booking-001")
        verify { bookingRepository.save(any()) }
    }

    @Test
    fun `cancelBooking devrait rejeter l'annulation par un non-proprietaire`() {
        // Given
        val booking = Booking(
            id = "booking-001",
            salon = salon,
            client = client,
            service = service,
            bookingDatetime = futureDateTime,
            status = BookingStatus.confirmed
        )

        every { bookingRepository.findById("booking-001") } returns Optional.of(booking)

        // When / Then
        assertThrows<BookingService.UnauthorizedAccessException> {
            bookingService.cancelBooking("booking-001", "other-client-001")
        }
    }

    @Test
    fun `updateBookingStatus devrait mettre a jour le statut correctement`() {
        // Given
        val booking = Booking(
            id = "booking-001",
            salon = salon,
            client = client,
            service = service,
            bookingDatetime = futureDateTime,
            status = BookingStatus.pending
        )

        val updateRequest = com.frollot.dto.UpdateBookingStatusRequest(
            status = BookingStatus.confirmed
        )

        every { bookingRepository.findById("booking-001") } returns Optional.of(booking)
        every { bookingRepository.save(any()) } returnsArgument 0

        // When
        val result = bookingService.updateBookingStatus("booking-001", updateRequest, "client-001")

        // Then
        assert(result.status == BookingStatus.confirmed)
        verify { bookingRepository.save(any()) }
    }
}