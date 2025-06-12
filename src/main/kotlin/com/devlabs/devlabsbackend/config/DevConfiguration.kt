package com.devlabs.devlabsbackend.config

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.core.io.ClassPathResource
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.transaction.annotation.Transactional
import jakarta.annotation.PostConstruct
import java.nio.charset.StandardCharsets

@Configuration
@Profile("dev")
class DevConfiguration {

    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    @PostConstruct
    @Transactional
    fun recreateReviewTable() {
        try {
            val resource = ClassPathResource("recreate-reviews-table.sql")
            val sql = resource.inputStream.readBytes().toString(StandardCharsets.UTF_8)
            
            // Execute the SQL script
            val statements = sql.split(";").filter { it.trim().isNotEmpty() }
            statements.forEach { statement ->
                if (statement.trim().isNotEmpty()) {
                    jdbcTemplate.execute(statement.trim())
                }
            }
            
            println("✓ Review table recreated successfully in dev mode")
        } catch (e: Exception) {
            println("⚠ Warning: Could not recreate review table: ${e.message}")
            // Don't fail the startup, just log the warning
        }
    }
}
