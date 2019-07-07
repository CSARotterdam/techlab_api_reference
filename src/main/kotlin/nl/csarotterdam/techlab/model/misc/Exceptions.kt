package nl.csarotterdam.techlab.model.misc

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.BAD_REQUEST)
class BadRequestException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)

@ResponseStatus(HttpStatus.UNAUTHORIZED)
class UnauthorizedException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)

@ResponseStatus(HttpStatus.NOT_FOUND)
class NotFoundException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)