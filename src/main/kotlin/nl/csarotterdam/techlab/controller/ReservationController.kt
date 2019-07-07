package nl.csarotterdam.techlab.controller

import nl.csarotterdam.techlab.model.misc.AUTHORIZATION
import nl.csarotterdam.techlab.service.ReservationService
import org.springframework.web.bind.annotation.*

@CrossOrigin
@RestController
@RequestMapping("/reservation")
class ReservationController(
        private val reservationService: ReservationService
) {

    @GetMapping("/list/current")
    fun listCurrent(
            @RequestHeader(AUTHORIZATION) token: String
    ) = reservationService.listCurrent(token)

    @GetMapping("/list")
    fun list(
            @RequestHeader(AUTHORIZATION) token: String
    ) = reservationService.list(token)

    @GetMapping("/id/{id}")
    fun readById(
            @RequestHeader(AUTHORIZATION) token: String,
            @PathVariable id: String
    ) = reservationService.readById(token, id)

    @GetMapping("/user/{userId}")
    fun readByUserId(
            @RequestHeader(AUTHORIZATION) token: String,
            @PathVariable userId: String
    ) = reservationService.readByUserId(token, userId)

    @DeleteMapping("/delete/id/{id}")
    fun setDeleted(
            @RequestHeader(AUTHORIZATION) token: String,
            @PathVariable id: String
    ) = reservationService.setDeleted(token, id)
}