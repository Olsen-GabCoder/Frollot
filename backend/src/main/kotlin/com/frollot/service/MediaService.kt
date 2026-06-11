package com.frollot.service

import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.util.*

/**
 * Service de gestion des fichiers média (images).
 *
 * Gère :
 * - Upload de fichiers vers le dossier uploads/
 * - Génération de noms de fichiers uniques
 * - Validation des types de fichiers
 */
@Service
class MediaService {

    companion object {
        private const val UPLOAD_DIR = "uploads"
        private const val MAX_FILE_SIZE = 10 * 1024 * 1024 // 10 MB
        private val ALLOWED_EXTENSIONS = setOf("jpg", "jpeg", "png", "gif", "webp")
    }

    init {
        // Créer le dossier uploads au démarrage de l'application
        val uploadDir = File(UPLOAD_DIR)
        if (!uploadDir.exists()) {
            uploadDir.mkdirs()
            println("✅ Dossier uploads créé: ${uploadDir.absolutePath}")
        } else {
            println("✅ Dossier uploads existe déjà: ${uploadDir.absolutePath}")
        }
    }

    /**
     * Sauvegarde un fichier uploadé et retourne l'URL publique.
     *
     * @param file Le fichier à sauvegarder
     * @return L'URL publique du fichier (ex: /uploads/filename.jpg)
     * @throws IllegalArgumentException si le fichier est invalide
     */
    fun saveFile(file: MultipartFile): String {
        println("📤 MediaService: Réception fichier - ${file.originalFilename} (${file.size} bytes)")

        // Validation
        validateFile(file)

        // Créer le dossier uploads s'il n'existe pas
        val uploadDir = File(UPLOAD_DIR)
        if (!uploadDir.exists()) {
            uploadDir.mkdirs()
            println("📁 Dossier uploads créé: ${uploadDir.absolutePath}")
        }

        // Générer un nom de fichier unique
        val originalFilename = file.originalFilename ?: "file"
        val extension = getFileExtension(originalFilename)
        val uniqueFilename = "${UUID.randomUUID()}.$extension"

        // Chemin complet du fichier
        val filePath = Paths.get(UPLOAD_DIR, uniqueFilename)

        // Sauvegarder le fichier avec gestion d'erreur améliorée
        try {
            // Utiliser inputStream + copy pour meilleure performance
            Files.copy(file.inputStream, filePath, StandardCopyOption.REPLACE_EXISTING)
            println("✅ Fichier sauvegardé: ${filePath.toAbsolutePath()}")
        } catch (e: Exception) {
            println("❌ Erreur sauvegarde: ${e.message}")
            e.printStackTrace()
            throw RuntimeException("Erreur lors de la sauvegarde du fichier: ${e.message}", e)
        }

        // Retourner l'URL publique (sans le domaine, sera géré par le contrôleur)
        val publicUrl = "/uploads/$uniqueFilename"
        println("🔗 URL publique générée: $publicUrl")
        return publicUrl
    }

    /**
     * Valide un fichier uploadé.
     */
    private fun validateFile(file: MultipartFile) {
        println("🔍 Validation du fichier...")

        if (file.isEmpty) {
            println("❌ Validation échouée: Fichier vide")
            throw IllegalArgumentException("Le fichier est vide")
        }

        if (file.size > MAX_FILE_SIZE) {
            println("❌ Validation échouée: Fichier trop volumineux (${file.size} bytes)")
            throw IllegalArgumentException("Le fichier est trop volumineux (max ${MAX_FILE_SIZE / 1024 / 1024}MB)")
        }

        val originalFilename = file.originalFilename ?: ""
        val extension = getFileExtension(originalFilename).lowercase()

        if (extension !in ALLOWED_EXTENSIONS) {
            println("❌ Validation échouée: Extension non autorisée ($extension)")
            throw IllegalArgumentException("Type de fichier non autorisé. Types acceptés: ${ALLOWED_EXTENSIONS.joinToString(", ")}")
        }

        // Vérifier le content type
        val contentType = file.contentType ?: ""
        if (!contentType.startsWith("image/")) {
            println("❌ Validation échouée: Content-Type invalide ($contentType)")
            throw IllegalArgumentException("Le fichier doit être une image")
        }

        println("✅ Validation réussie: $originalFilename ($contentType, ${file.size} bytes)")
    }

    /**
     * Extrait l'extension d'un nom de fichier.
     */
    private fun getFileExtension(filename: String): String {
        val lastDot = filename.lastIndexOf('.')
        return if (lastDot >= 0 && lastDot < filename.length - 1) {
            filename.substring(lastDot + 1).lowercase()
        } else {
            "jpg" // Extension par défaut
        }
    }

    /**
     * Supprime un fichier.
     */
    fun deleteFile(fileUrl: String): Boolean {
        return try {
            val filename = fileUrl.substringAfterLast("/")
            val filePath = Paths.get(UPLOAD_DIR, filename)
            val deleted = Files.deleteIfExists(filePath)
            if (deleted) {
                println("🗑️ Fichier supprimé: $filename")
            } else {
                println("⚠️ Fichier introuvable: $filename")
            }
            deleted
        } catch (e: Exception) {
            println("❌ Erreur lors de la suppression du fichier $fileUrl: ${e.message}")
            false
        }
    }
}