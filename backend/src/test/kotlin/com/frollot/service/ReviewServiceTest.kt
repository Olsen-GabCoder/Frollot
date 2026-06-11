package com.frollot.service

import io.mockk.impl.annotations.MockK
import com.frollot.dto.CreateReviewRequest
import com.frollot.model.*
import com.frollot.repository.*
import io.mockk.*
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*
import com.frollot.model.SalonService as SalonServiceEntity

@ExtendWith(MockKExtension::class)
class ReviewServiceTest {

    @MockK
    private lateinit var reviewRepository: ReviewRepository

    @MockK
    private lateinit var bookingRepository: BookingRepository

    @MockK
    private lateinit var salonRepository: SalonRepository

    @MockK
    private lateinit var userRepository: UserRepository

    private lateinit var reviewService: ReviewService

    private lateinit var salon: Salon
    private lateinit var client: User
    private lateinit var booking: Booking
    private lateinit var service: SalonServiceEntity

    @BeforeEach
    fun setUp() {
        reviewService = ReviewService(
            reviewRepository,
            bookingRepository,
            salonRepository,
            userRepository
        )
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

        booking = Booking(
            id = "booking-001",
            salon = salon,
            client = client,
            service = service,
            bookingDatetime = LocalDateTime.now().minusDays(1),
            status = BookingStatus.completed
        )
    }

    @Test
    fun `createReview devrait creer un avis valide`() {
        // Given
        val request = CreateReviewRequest(
            salonId = "salon-001",
            bookingId = "booking-001",
            rating = 5,
            title = "Excellent service",
            content = "Tres satisfait"
        )

        every { bookingRepository.findById("booking-001") } returns Optional.of(booking)
        every { salonRepository.findById("salon-001") } returns Optional.of(salon)
        every { userRepository.findById("client-001") } returns Optional.of(client)
        every { reviewRepository.existsByBookingId("booking-001") } returns false
        every { reviewRepository.save(any()) } returnsArgument 0
        every { reviewRepository.findAverageRatingBySalonId("salon-001") } returns BigDecimal("5.00")
        every { reviewRepository.countBySalonIdAndIsVisibleTrue("salon-001") } returns 1L
        every { reviewRepository.findBySalonIdAndIsVisibleTrueOrderByCreatedAtDesc("salon-001") } returns emptyList()
        every { salonRepository.save(any()) } returnsArgument 0

        // When
        val result = reviewService.createReview(request, "client-001")

        // Then
        assert(result.id != null)
        assert(result.rating == 5)
        assert(result.title == "Excellent service")
        verify { reviewRepository.save(any()) }
    }

    @Test
    fun `createReview devrait rejeter si booking non termine`() {
        // Given
        val pendingBooking = Booking(
            id = booking.id,
            salon = booking.salon,
            client = booking.client,
            service = booking.service,
            bookingDatetime = booking.bookingDatetime,
            status = BookingStatus.pending
        )

        val request = CreateReviewRequest(
            salonId = "salon-001",
            bookingId = "booking-001",
            rating = 5
        )

        every { bookingRepository.findById("booking-001") } returns Optional.of(pendingBooking)

        // When / Then
        assertThrows<ReviewService.InvalidReviewException> {
            reviewService.createReview(request, "client-001")
        }
    }

    @Test
    fun `createReview devrait rejeter si non-proprietaire du booking`() {
        // Given
        val request = CreateReviewRequest(
            salonId = "salon-001",
            bookingId = "booking-001",
            rating = 5
        )

        every { bookingRepository.findById("booking-001") } returns Optional.of(booking)

        // When / Then
        assertThrows<ReviewService.UnauthorizedAccessException> {
            reviewService.createReview(request, "other-client-001")
        }
    }

    @Test
    fun `createReview devrait rejeter si avis deja existant`() {
        // Given
        val request = CreateReviewRequest(
            salonId = "salon-001",
            bookingId = "booking-001",
            rating = 5
        )

        every { bookingRepository.findById("booking-001") } returns Optional.of(booking)
        every { reviewRepository.existsByBookingId("booking-001") } returns true

        // When / Then
        assertThrows<ReviewService.InvalidReviewException> {
            reviewService.createReview(request, "client-001")
        }
    }

    @Test
    fun `getSalonReviews devrait retourner les avis avec pagination`() {
        // Given
        val review = Review(
            id = "review-001",
            salon = salon,
            client = client,
            booking = booking,
            rating = 5,
            title = "Great",
            isVisible = true
        )

        val pageable = PageRequest.of(0, 20)
        val reviewsPage = PageImpl(listOf(review), pageable, 1)

        every { salonRepository.existsById("salon-001") } returns true
        every { reviewRepository.findBySalonIdAndIsVisibleTrueOrderByCreatedAtDesc("salon-001", pageable) } returns reviewsPage

        // When
        val result = reviewService.getSalonReviews("salon-001", pageable)

        // Then
        assert(result.content.size == 1)
        assert(result.content[0].id == "review-001")
    }

    @Test
    fun `updateSalonReviewStats devrait mettre a jour la moyenne`() {
        // Given
        every { salonRepository.findById("salon-001") } returns Optional.of(salon)
        every { reviewRepository.findAverageRatingBySalonId("salon-001") } returns BigDecimal("4.50")
        every { reviewRepository.countBySalonIdAndIsVisibleTrue("salon-001") } returns 10L
        every { salonRepository.save(any()) } returnsArgument 0

        // When
        reviewService.updateSalonReviewStats("salon-001")

        // Then
        verify { salonRepository.save(any()) }
    }
}