package com.devlabs.devlabsbackend

import com.devlabs.devlabsbackend.core.config.DotenvLoader
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class DevlabsBackendApplication

fun main(args: Array<String>) {
    DotenvLoader.load()
    runApplication<DevlabsBackendApplication>(*args)
}