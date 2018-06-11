package com.tripledip.wordherdserver

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class WordHerdServerApplication

fun main(args: Array<String>) {
    runApplication<WordHerdServerApplication>(*args)
}
