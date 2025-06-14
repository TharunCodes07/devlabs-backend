package com.devlabs.devlabsbackend.config

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component

@Component
class DatabaseMigrationRunner @Autowired constructor(
    private val jdbcTemplate: JdbcTemplate
) : ApplicationRunner {

    private val logger = LoggerFactory.getLogger(DatabaseMigrationRunner::class.java)

    override fun run(args: ApplicationArguments?) {
        try {
            // Check if is_published column exists in review table
            val columnExists = checkIfColumnExists("review", "is_published")

            if (!columnExists) {
                logger.info("Column 'is_published' not found in 'review' table. Adding it now...")
                addIsPublishedColumn()
                logger.info("Successfully added 'is_published' column to 'review' table")
            } else {
                logger.debug("Column 'is_published' already exists in 'review' table")
                // Check for null values and fix them
                fixNullValuesInIsPublished()
            }
        } catch (e: Exception) {
            logger.error("Error during database migration: ${e.message}", e)
            // Don't throw exception to prevent application startup failure
        }
    }

    private fun checkIfColumnExists(tableName: String, columnName: String): Boolean {
        return try {
            val sql = """
                SELECT COUNT(*)
                FROM information_schema.columns
                WHERE table_name = ?
                AND column_name = ?
                AND table_schema = 'public'
            """.trimIndent()

            val count = jdbcTemplate.queryForObject(sql, Int::class.java, tableName, columnName) ?: 0
            count > 0
        } catch (e: Exception) {
            logger.warn("Could not check if column exists: ${e.message}")
            false
        }
    }

    private fun addIsPublishedColumn() {
        val sql = """
            ALTER TABLE public.review
            ADD COLUMN is_published BOOLEAN NOT NULL DEFAULT false
        """.trimIndent()

        jdbcTemplate.execute(sql)

        // Add comment for documentation
        val commentSql = """
            COMMENT ON COLUMN public.review.is_published
            IS 'Indicates whether the review results are published to students'
        """.trimIndent()

        jdbcTemplate.execute(commentSql)
    }

    private fun fixNullValuesInIsPublished() {
        try {
            // Check if there are any null values
            val nullCountSql = "SELECT COUNT(*) FROM public.review WHERE is_published IS NULL"
            val nullCount = jdbcTemplate.queryForObject(nullCountSql, Int::class.java) ?: 0

            if (nullCount > 0) {
                logger.info("Found $nullCount records with null is_published values. Fixing them...")

                // Update null values to false
                val updateSql = "UPDATE public.review SET is_published = false WHERE is_published IS NULL"
                val updatedRows = jdbcTemplate.update(updateSql)

                logger.info("Updated $updatedRows records: set is_published = false for null values")

                // Ensure NOT NULL constraint is set
                try {
                    val constraintSql = "ALTER TABLE public.review ALTER COLUMN is_published SET NOT NULL"
                    jdbcTemplate.execute(constraintSql)
                    logger.info("Set NOT NULL constraint on is_published column")
                } catch (e: Exception) {
                    logger.debug("NOT NULL constraint may already exist: ${e.message}")
                }

                // Set default value
                try {
                    val defaultSql = "ALTER TABLE public.review ALTER COLUMN is_published SET DEFAULT false"
                    jdbcTemplate.execute(defaultSql)
                    logger.info("Set default value for is_published column")
                } catch (e: Exception) {
                    logger.debug("Default value may already exist: ${e.message}")
                }
            } else {
                logger.debug("No null values found in is_published column")
            }
        } catch (e: Exception) {
            logger.warn("Could not fix null values in is_published column: ${e.message}")
        }
    }
}
