package com.frollot.model

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(name = "salons")
data class Salon(
    @Id
    @Column(length = 36, columnDefinition = "CHAR(36)")
    var id: String? = null,

    @Column(nullable = false, length = 100)
    var name: String = "",

    @Column(nullable = false)
    var address: String = "",

    @Column(nullable = false, length = 100)
    var city: String = "",

    @Column(name = "postal_code", nullable = false, length = 10)
    var postalCode: String = "",

    @Column(precision = 10, scale = 8, nullable = true)
    var latitude: BigDecimal? = null,

    @Column(precision = 11, scale = 8, nullable = true)
    var longitude: BigDecimal? = null,

    @Column(columnDefinition = "TEXT")
    var description: String? = null,

    // AJOUTER CE CHAMP :
    @Column(length = 255, nullable = false)
    var slug: String = "", // Slug pour les URLs (ex: "salon-elegance-paris")

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    var owner: User? = null,

    @Column(
        name = "rating_average",
        precision = 3,
        scale = 2,
        nullable = false
    )
    var ratingAverage: BigDecimal = BigDecimal.ZERO,

    @Column(
        name = "total_reviews",
        nullable = false
    )
    var totalReviews: Int = 0,

    @Column(name = "cover_photo_url", length = 500)
    var coverPhotoUrl: String? = null,

    // Phase E.2 - Profil Salon Social
    @Column(name = "social_description", columnDefinition = "TEXT")
    var socialDescription: String? = null,

    @Column(name = "social_cover_image", length = 500)
    var socialCoverImage: String? = null,

    // L5 — Contact fields (columns already exist in DB)
    @Column(name = "phone_number", length = 20)
    var phoneNumber: String? = null,

    @Column(name = "email", length = 255)
    var email: String? = null,

    @Column(name = "website_url", length = 500)
    var websiteUrl: String? = null,

    // Phase H.2 - Vérification Salons/Coiffeurs
    @Column(name = "is_verified", nullable = false)
    var isVerified: Boolean = false,

    @Enumerated(EnumType.STRING)
    @Column(name = "verification_type", length = 20)
    var verificationType: VerificationType? = null,

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    var createdAt: LocalDateTime? = null,

    @UpdateTimestamp
    @Column(name = "updated_at")
    var updatedAt: LocalDateTime? = null
)