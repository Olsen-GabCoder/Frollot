package com.frollot.model

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

/**
 * Convertisseur pour PostVisibility entre la base de données (minuscules) et l'enum Kotlin (majuscules).
 * Phase F.3 - Visibilité des Posts
 * 
 * La base de données stocke les valeurs en minuscules ("public", "followers", "private")
 * mais l'enum Kotlin utilise des majuscules ("PUBLIC", "FOLLOWERS", "PRIVATE").
 */
@Converter(autoApply = true)
class PostVisibilityConverter : AttributeConverter<PostVisibility, String> {
    
    override fun convertToDatabaseColumn(attribute: PostVisibility?): String? {
        return attribute?.name?.lowercase()
    }
    
    override fun convertToEntityAttribute(dbData: String?): PostVisibility? {
        if (dbData == null) {
            return null
        }
        return try {
            PostVisibility.valueOf(dbData.uppercase())
        } catch (e: IllegalArgumentException) {
            // Valeur par défaut si la valeur n'est pas reconnue
            PostVisibility.PUBLIC
        }
    }
}

