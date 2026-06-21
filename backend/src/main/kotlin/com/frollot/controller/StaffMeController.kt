package com.frollot.controller

import com.frollot.model.ServiceCategory
import com.frollot.model.User
import com.frollot.repository.SalonStaffRepository
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*

data class UpdateMySpecialtiesRequest(
    val specialties: List<String> = emptyList()
)

@RestController
@RequestMapping("/api/staff/me")
@CrossOrigin(
    origins = ["http://localhost:3000", "http://localhost:8090", "http://localhost:8081", "http://localhost:8082", "http://10.0.2.2:8090"],
    allowCredentials = "true"
)
class StaffMeController(
    private val salonStaffRepository: SalonStaffRepository
) {
    private fun getAuthenticatedUserId(): String {
        val authentication = SecurityContextHolder.getContext().authentication
        if (authentication == null || !authentication.isAuthenticated) {
            throw IllegalStateException("Aucun utilisateur authentifie")
        }
        return (authentication.principal as User).id!!
    }

    @PutMapping("/specialties")
    @PreAuthorize("isAuthenticated()")
    fun updateMySpecialties(@RequestBody request: UpdateMySpecialtiesRequest): ResponseEntity<Any> {
        val userId = getAuthenticatedUserId()

        // Resolve the user's active staff membership (mono-salon)
        val staffList = salonStaffRepository.findByUserId(userId)
            .filter { it.isActive }
        if (staffList.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(mapOf("message" to "Aucun rattachement salon actif"))
        }
        val staff = staffList.first()

        // Validate and deduplicate specialties
        val parsed = mutableSetOf<ServiceCategory>()
        for (raw in request.specialties) {
            val cat = try {
                ServiceCategory.valueOf(raw.uppercase().trim())
            } catch (_: IllegalArgumentException) {
                return ResponseEntity.badRequest()
                    .body(mapOf("message" to "Categorie invalide : '$raw'. Valeurs acceptees : ${ServiceCategory.entries.joinToString { it.name }}"))
            }
            parsed.add(cat)
        }

        // Persist
        staff.specialties = parsed.toMutableList()
        salonStaffRepository.save(staff)

        return ResponseEntity.ok(mapOf(
            "specialties" to staff.specialties.map { it.name },
            "message" to if (staff.specialties.isEmpty()) "Generaliste (toutes prestations)" else "${staff.specialties.size} specialite(s) enregistree(s)"
        ))
    }

    @GetMapping("/specialties")
    @PreAuthorize("isAuthenticated()")
    fun getMySpecialties(): ResponseEntity<Any> {
        val userId = getAuthenticatedUserId()

        val staffList = salonStaffRepository.findByUserId(userId)
            .filter { it.isActive }
        if (staffList.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(mapOf("message" to "Aucun rattachement salon actif"))
        }
        val staff = staffList.first()

        return ResponseEntity.ok(mapOf(
            "specialties" to staff.specialties.map { it.name },
            "allCategories" to ServiceCategory.entries.map { it.name }
        ))
    }
}
