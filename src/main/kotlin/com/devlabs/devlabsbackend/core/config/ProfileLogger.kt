package com.devlabs.devlabsbackend.core.config

import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.core.env.Environment
import org.springframework.stereotype.Component

@Component
class ProfileLogger(private val env: Environment) : ApplicationRunner {
    override fun run(args: ApplicationArguments?) {
        val activeProfiles = env.activeProfiles
        println("🚀 Server is starting with active profile(s): ${activeProfiles.joinToString(", ")}")
    }
}
