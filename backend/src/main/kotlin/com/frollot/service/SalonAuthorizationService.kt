package com.frollot.service

import com.frollot.repository.RolePermissionRepository
import com.frollot.repository.SalonRepository
import com.frollot.repository.SalonStaffRepository
import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

/**
 * Service central d'autorisation pour les actions sur un salon.
 *
 * Logique (ordre important) :
 *   1. Si userId == salon.owner.id -> TRUE (toutes permissions, cas spécial)
 *   2. Sinon, cherche le SalonStaff de ce user pour ce salon
 *   3. Pas de staff -> FALSE
 *   4. Vérifie si le rôle du staff a la permission demandée (via cache)
 *
 * Cache : la matrice role->permissions est chargée une fois au démarrage
 * (données de référence stables). Le lien user->salon->role est résolu à chaque
 * appel via SalonStaffRepository (déjà en cache Hibernate L1 dans la transaction).
 * Si la matrice change (très rare), un redémarrage ou un appel à reloadPermissions() suffit.
 */
@Service
class SalonAuthorizationService(
    private val salonRepository: SalonRepository,
    private val salonStaffRepository: SalonStaffRepository,
    private val rolePermissionRepository: RolePermissionRepository
) {
    private val logger = LoggerFactory.getLogger(SalonAuthorizationService::class.java)

    // Cache de la matrice role -> Set<permission_key>, chargé au démarrage
    @Volatile
    private var rolePermissionsCache: Map<String, Set<String>> = emptyMap()

    class PermissionDeniedException(userId: String, salonId: String, permission: String) :
        ResponseStatusException(
            HttpStatus.FORBIDDEN,
            "L'utilisateur '$userId' n'a pas la permission '$permission' sur le salon '$salonId'"
        )

    @PostConstruct
    fun loadPermissions() {
        rolePermissionsCache = rolePermissionRepository.findAllRolePermissions()
        val total = rolePermissionsCache.values.sumOf { it.size }
        logger.info("Permissions chargées : ${rolePermissionsCache.size} rôles, $total entrées role_permissions")
    }

    /**
     * Recharge la matrice depuis la base (en cas de modification sans redémarrage).
     */
    fun reloadPermissions() {
        loadPermissions()
    }

    /**
     * Vérifie si un utilisateur a une permission donnée sur un salon.
     *
     * @return true si autorisé, false sinon
     */
    fun hasPermission(userId: String, salonId: String, permissionKey: String): Boolean {
        // 1. Cas spécial : l'owner a TOUTES les permissions
        val salon = salonRepository.findById(salonId).orElse(null) ?: return false
        if (salon.owner?.id == userId) {
            return true
        }

        // 2. Chercher le staff de ce user dans ce salon
        val staff = salonStaffRepository.findBySalonIdAndUserId(salonId, userId) ?: return false
        if (!staff.isActive) {
            return false
        }

        // 3. Vérifier la permission dans le cache
        val role = staff.role.lowercase()
        val permissions = rolePermissionsCache[role] ?: return false
        return permissionKey in permissions
    }

    /**
     * Vérifie la permission et lance une exception 403 si refusée.
     */
    fun requirePermission(userId: String, salonId: String, permissionKey: String) {
        if (!hasPermission(userId, salonId, permissionKey)) {
            throw PermissionDeniedException(userId, salonId, permissionKey)
        }
    }

    /**
     * Retourne le rôle effectif d'un utilisateur sur un salon.
     * "owner" si propriétaire, le rôle du staff sinon, null si aucun lien.
     */
    fun getUserRole(userId: String, salonId: String): String? {
        val salon = salonRepository.findById(salonId).orElse(null) ?: return null
        if (salon.owner?.id == userId) {
            return "owner"
        }
        val staff = salonStaffRepository.findBySalonIdAndUserId(salonId, userId) ?: return null
        if (!staff.isActive) return null
        return staff.role.lowercase()
    }

    /**
     * Retourne toutes les permissions effectives d'un utilisateur sur un salon.
     */
    fun getUserPermissions(userId: String, salonId: String): Set<String> {
        val salon = salonRepository.findById(salonId).orElse(null) ?: return emptySet()

        // Owner = toutes les permissions
        if (salon.owner?.id == userId) {
            return rolePermissionsCache.values.flatten().toSet() +
                // Ajouter les permissions qui ne sont dans aucun rôle (owner-only)
                setOf(
                    "staff.add", "staff.update", "staff.remove",
                    "invitation.search", "invitation.create", "invitation.cancel",
                    "booking.manage_payment", "portfolio.delete",
                    "payment.refund", "payment.view_salon", "verification.request"
                )
        }

        val staff = salonStaffRepository.findBySalonIdAndUserId(salonId, userId) ?: return emptySet()
        if (!staff.isActive) return emptySet()

        return rolePermissionsCache[staff.role.lowercase()] ?: emptySet()
    }
}
