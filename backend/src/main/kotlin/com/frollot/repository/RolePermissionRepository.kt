package com.frollot.repository

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository

/**
 * Repository pour lire les permissions par rôle.
 * Utilise JdbcTemplate (pas JPA) car les tables permissions/role_permissions
 * sont des tables de référence sans entité Hibernate associée.
 */
@Repository
class RolePermissionRepository(
    private val jdbcTemplate: JdbcTemplate
) {
    /**
     * Charge toutes les permissions pour un rôle donné.
     */
    fun findPermissionsByRole(role: String): Set<String> {
        return jdbcTemplate.queryForList(
            "SELECT permission_key FROM role_permissions WHERE role = ?",
            String::class.java,
            role
        ).toSet()
    }

    /**
     * Charge la matrice complète role -> permissions (pour le cache au démarrage).
     */
    fun findAllRolePermissions(): Map<String, Set<String>> {
        val result = mutableMapOf<String, MutableSet<String>>()
        jdbcTemplate.query(
            "SELECT role, permission_key FROM role_permissions"
        ) { rs, _ ->
            val role = rs.getString("role")
            val perm = rs.getString("permission_key")
            result.getOrPut(role) { mutableSetOf() }.add(perm)
        }
        return result
    }
}
