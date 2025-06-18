package com.devlabs.devlabsbackend.exception

import com.devlabs.devlabsbackend.core.DTO.NotFoundExceptionDTO
import com.devlabs.devlabsbackend.core.exception.NotFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class ExceptionHandler {

    @ExceptionHandler(NotFoundException::class)
    fun handleNotFoundException(ex: NotFoundException): ResponseEntity<Any> {
        return ResponseEntity(NotFoundExceptionDTO(message = ex.message.toString(), statusCode = 404), HttpStatus.NOT_FOUND)
    }

}