package com.frollot.exception

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.time.LocalDateTime

/**
 * Gestionnaire global des exceptions de l'application.
 *
 * Cette classe intercepte toutes les exceptions levées par les contrôleurs
 * et les transforme en réponses HTTP standardisées et lisibles.
 */
@RestControllerAdvice
class GlobalExceptionHandler {

    /**
     * Gère les erreurs de validation @Valid sur les DTOs.
     *
     * @param ex L'exception levée lors de la validation
     * @return Réponse HTTP 400 avec les détails des erreurs de validation
     */
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(ex: MethodArgumentNotValidException): ResponseEntity<ValidationErrorResponse> {
        val errors = ex.bindingResult.fieldErrors.associate { 
            it.field to (it.defaultMessage ?: "Valeur invalide")
        }
        
        val errorResponse = ValidationErrorResponse(
            status = HttpStatus.BAD_REQUEST.value(),
            error = "Validation Failed",
            message = "Erreurs de validation dans la requête",
            errors = errors,
            timestamp = LocalDateTime.now()
        )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse)
    }

    /**
     * Gère les cas où l'email est déjà utilisé.
     *
     * @param ex L'exception levée
     * @return Réponse HTTP 409 (Conflict) avec un message d'erreur clair
     */
    @ExceptionHandler(EmailAlreadyExistsException::class)
    fun handleEmailAlreadyExists(ex: EmailAlreadyExistsException): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            status = HttpStatus.CONFLICT.value(),
            error = "Conflict",
            message = ex.message ?: "Email déjà utilisé",
            timestamp = LocalDateTime.now()
        )
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse)
    }

    /**
     * Gère les erreurs de validation (email invalide, mot de passe trop court, etc.).
     *
     * @param ex L'exception levée
     * @return Réponse HTTP 400 (Bad Request) avec un message d'erreur clair
     */
    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(ex: IllegalArgumentException): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            status = HttpStatus.BAD_REQUEST.value(),
            error = "Bad Request",
            message = ex.message ?: "Données invalides",
            timestamp = LocalDateTime.now()
        )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse)
    }

    /**
     * Gère les erreurs d'accès non autorisé.
     *
     * @param ex L'exception levée
     * @return Réponse HTTP 403 (Forbidden)
     */
    @ExceptionHandler(org.springframework.security.access.AccessDeniedException::class)
    fun handleAccessDenied(ex: org.springframework.security.access.AccessDeniedException): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            status = HttpStatus.FORBIDDEN.value(),
            error = "Forbidden",
            message = "Accès refusé",
            timestamp = LocalDateTime.now()
        )
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse)
    }

    /**
     * Gère les ressources non trouvées.
     *
     * @param ex L'exception levée
     * @return Réponse HTTP 404 (Not Found)
     */
    @ExceptionHandler(NoSuchElementException::class)
    fun handleNotFound(ex: NoSuchElementException): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            status = HttpStatus.NOT_FOUND.value(),
            error = "Not Found",
            message = ex.message ?: "Ressource non trouvée",
            timestamp = LocalDateTime.now()
        )
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse)
    }

    /**
     * Gère toutes les autres exceptions non prévues.
     *
     * @param ex L'exception levée
     * @return Réponse HTTP 500 (Internal Server Error)
     */
    @ExceptionHandler(Exception::class)
    fun handleGenericException(ex: Exception): ResponseEntity<ErrorResponse> {
        // Log l'erreur pour le debugging (en production, utiliser un logger)
        ex.printStackTrace()
        
        val errorResponse = ErrorResponse(
            status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
            error = "Internal Server Error",
            message = "Une erreur inattendue s'est produite",
            timestamp = LocalDateTime.now()
        )
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse)
    }
}

/**
 * Format standardisé des réponses d'erreur.
 */
data class ErrorResponse(
    val status: Int,
    val error: String,
    val message: String,
    val timestamp: LocalDateTime
)

/**
 * Format des réponses d'erreur de validation avec détails des champs.
 */
data class ValidationErrorResponse(
    val status: Int,
    val error: String,
    val message: String,
    val errors: Map<String, String>,
    val timestamp: LocalDateTime
)
