package nl.csarotterdam.techlab

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class TechlabAPIApplication : SpringBootServletInitializer()

fun main(args: Array<String>) {
    runApplication<TechlabAPIApplication>(*args)
}