package com.devlabs.devlabsbackend

import com.devlabs.devlabsbackend.core.config.DotenvLoader
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching
import org.springframework.scheduling.annotation.EnableAsync

@SpringBootApplication
@EnableCaching
@EnableAsync
class DevlabsBackendApplication

fun main(args: Array<String>) {
    DotenvLoader.load()
    runApplication<DevlabsBackendApplication>(*args)
}
