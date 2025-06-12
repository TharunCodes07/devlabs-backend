package com.devlabs.devlabsbackend.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.core.io.ClassPathResource
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.util.FileCopyUtils
import java.nio.charset.StandardCharsets

@Configuration
@Profile("dev")
class DevDatabaseConfig {

    @Bean
    fun recreateReviewsTable(jdbcTemplate: JdbcTemplate): CommandLineRunner {
        return CommandLineRunner {
            try {
                println("🔄 [DEV MODE] Recreating reviews table...")
                
                // Read the SQL script
                val resource = ClassPathResource("recreate-reviews-table.sql")
                val sqlScript = String(FileCopyUtils.copyToByteArray(resource.inputStream), StandardCharsets.UTF_8)
                
                // Execute the script
                sqlScript.split(";")
                    .map { it.trim() }
                    .filter { it.isNotEmpty() && !it.startsWith("--") }
                    .forEach { sql ->
                        if (sql.isNotBlank()) {
                            try {
                                jdbcTemplate.execute(sql)
                                println("✅ Executed: ${sql.take(50)}...")
                            } catch (e: Exception) {
                                println("⚠️  Warning executing SQL: ${e.message}")
                            }
                        }
                    }
                
                println("✅ [DEV MODE] Reviews table recreation completed!")
                println("📝 Hibernate will now recreate the table with the correct structure.")
                
            } catch (e: Exception) {
                println("❌ [DEV MODE] Error recreating reviews table: ${e.message}")
            }
        }
    }
}
