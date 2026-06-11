package com.frollot.exception

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

/**
 * Exception levée lorsqu'un utilisateur tente de s'inscrire avec un email déjà utilisé.
 *
 * Cette exception est automatiquement convertie en réponse HTTP 409 (Conflict)
 * par Spring Boot grâce à l'annotation @ResponseStatus.
 */
@ResponseStatus(HttpStatus.CONFLICT)
class EmailAlreadyExistsException(email: String) :
    RuntimeException("L'email '$email' est déjà utilisé par un autre compte")