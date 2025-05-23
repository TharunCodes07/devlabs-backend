package com.devlabs.devlabsbackend

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class DevlabsBackendApplication

fun main(args: Array<String>) {
    runApplication<DevlabsBackendApplication>(*args)
}