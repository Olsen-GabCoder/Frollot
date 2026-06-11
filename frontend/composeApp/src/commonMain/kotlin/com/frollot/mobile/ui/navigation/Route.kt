package com.frollot.mobile.ui.navigation

import kotlinx.serialization.Serializable

sealed class Route {
    @Serializable
    data object Login : Route()

    @Serializable
    data object Register : Route()

    @Serializable
    data object ForgotPassword : Route()

    @Serializable
    data class ResetPassword(val token: String? = null) : Route()

    @Serializable
    data class EmailVerification(val email: String? = null, val token: String? = null) : Route()

    @Serializable
    data object Home : Route()

    @Serializable
    data class CreateSalon(val ownerId: String) : Route()

    @Serializable
    data class SalonDetail(val salonId: String) : Route()

    @Serializable
    data class Booking(
        val salonId: String,
        val serviceId: String
    ) : Route()

    @Serializable
    data object MyBookings : Route()

    @Serializable
    data class BookingDetail(val bookingId: String) : Route()

    @Serializable
    data object SocialFeed : Route()

    @Serializable
    data object Profile : Route()

    @Serializable
    data class CreatePost(val salonId: String? = null) : Route() // salonId optionnel pour pré-remplir le tag salon

    @Serializable
    data class CreateService(val salonId: String) : Route()

    @Serializable
    data class CreateStaff(val salonId: String) : Route()

    // ✅ NOUVEAU: Route pour créer un avis
    @Serializable
    data class CreateReview(
        val salonId: String,
        val salonName: String,
        val bookingId: String,
        val serviceName: String
    ) : Route()

    @Serializable
    data class QueueManagement(val salonId: String) : Route()

    @Serializable
    data class OwnerBookingsManagement(val salonId: String) : Route()

    @Serializable
    data class Payment(
        val bookingId: String
    ) : Route()

    @Serializable
    data class Comments(val postId: String) : Route()

    @Serializable
    data class PostDetail(val postId: String) : Route() // Écran de détail d'un post

    @Serializable
    data class Favorites(val userId: String) : Route()

    @Serializable
    data class Archives(val userId: String) : Route()

    @Serializable
    data class CreatePortfolio(val ownerId: String, val ownerType: String) : Route() // ownerType: "coiffeur" ou "salon"
    
    @Serializable
    data class Report(
        val reportedEntityType: String, // "POST", "COMMENT", "USER", "SALON"
        val reportedEntityId: String
    ) : Route()

    @Serializable
    data class EditPortfolio(val portfolioId: String) : Route()

    @Serializable
    data class PortfolioDetail(val portfolioId: String) : Route()

    @Serializable
    data class PortfoliosList(val ownerId: String, val ownerType: String) : Route() // ownerType: "coiffeur" ou "salon"

    @Serializable
    data class Search(val query: String = "") : Route() // Phase C.1 - Recherche spécialisée coiffure

    @Serializable
    data class SalonPosts(val salonId: String) : Route() // Phase C.2 - Feed par Salon

    @Serializable
    data object Trending : Route() // Phase C.3 - Trending Coiffure

    @Serializable
    data class CoiffeurProfile(val coiffeurId: String) : Route() // Phase E.1 - Profil Coiffeur Enrichi

    @Serializable
    data class SalonSocialProfile(val salonId: String) : Route() // Phase E.2 - Profil Salon Social

    @Serializable
    data class ClientProfile(val clientId: String) : Route() // Phase E.4 - Profil Client

    @Serializable
    data class SalonOwnerProfile(val ownerId: String) : Route() // Phase E.5 - Profil Propriétaire de Salon

    @Serializable
    data class Collections(val userId: String) : Route() // Phase F.1 - Collections Thématiques

    @Serializable
    data class CollectionDetail(val collectionId: String) : Route() // Phase F.1 - Collections Thématiques

    @Serializable
    data object Settings : Route() // Paramètres de l'application
    
    @Serializable
    data object SecuritySettings : Route() // Paramètres de sécurité
    
    @Serializable
    data object ChangeEmail : Route() // Modifier l'email
    
    @Serializable
    data object ChangePhone : Route() // Modifier le téléphone
    
    @Serializable
    data object BlockedUsers : Route() // Utilisateurs bloqués
    
    @Serializable
    data object PaymentMethods : Route() // Méthodes de paiement
    
    @Serializable
    data object HelpCenter : Route() // Centre d'aide
    
    @Serializable
    data object ContactSupport : Route() // Contacter le support
    
    @Serializable
    data object TermsOfService : Route() // Conditions d'utilisation
    
    @Serializable
    data object PrivacyPolicy : Route() // Politique de confidentialité
}
