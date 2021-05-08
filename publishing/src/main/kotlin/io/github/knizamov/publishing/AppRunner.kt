package io.github.knizamov.publishing

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
internal class AppRunner

fun main(args: Array<String>) {
    runApplication<AppRunner>(*args)
}