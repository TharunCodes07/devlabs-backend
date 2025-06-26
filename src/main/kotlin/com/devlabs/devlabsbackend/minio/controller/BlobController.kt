package com.devlabs.devlabsbackend.s3.controller

import com.devlabs.devlabsbackend.s3.service.MinioService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

/**
 * MinIO Blob Storage Controller
 * 
 * This controller provides endpoints for file upload with organized directory structure.
 * 
 * Directory Structure:
 * - Files can be organized in a hierarchical structure: teamId/projectId-projectName/reviewId-reviewName/
 * - All parameters are optional - provide only what you need for your directory structure
 * - If no teamId is provided, files are stored at the bucket root
 * 
 * Available Endpoints:
 * 
 * UPLOAD:
 * - POST /blob/upload - Upload a file with optional directory structure
 * - POST /blob/upload-structured - Alternative upload endpoint with detailed response
 * 
 * RETRIEVE:
 * - GET /blob/list - List all files in a specific directory
 * - GET /blob/file-info?objectName=... - Get information about a specific file
 * - GET /blob/download-zip - Download all files in a directory as ZIP
 * 
 * DELETE:
 * - DELETE /blob/delete?objectName=... - Delete a specific file
 * - DELETE /blob/delete-directory - Delete all files in a directory
 * 
 * Examples:
 * 1. Upload to review: POST /blob/upload with teamId, projectId, projectName, reviewId, reviewName
 * 2. List review files: GET /blob/list?teamId=team1&projectId=proj1&reviewId=review1
 * 3. Download all review files: GET /blob/download-zip?teamId=team1&projectId=proj1&reviewId=review1
 * 4. Delete review directory: DELETE /blob/delete-directory?teamId=team1&projectId=proj1&reviewId=review1
 * 
 * Response includes:
 * - objectName: Full path/name of the uploaded file in MinIO
 * - url: Pre-signed URL for accessing the file
 * - directoryPath: The directory structure used
 */
@RestController
@RequestMapping("/blob")
class BlobController @Autowired constructor(
    private val minioService: MinioService
) {

    /**
     * Upload a file to Minio storage
     * @param file The file to upload
     * @param customName Optional custom name for the file
     * @param teamId Optional team ID for directory structure
     * @param projectId Optional project ID for directory structure
     * @param projectName Optional project name for directory structure
     * @param reviewId Optional review ID for directory structure
     * @param reviewName Optional review name for directory structure
     * @return The object name (key) and URL in Minio
     */
    @PostMapping("/upload")
    fun uploadFile(
        @RequestParam("file") file: MultipartFile,
        @RequestParam("customName", required = false) customName: String?,
        @RequestParam("teamId", required = false) teamId: String?,
        @RequestParam("projectId", required = false) projectId: String?,
        @RequestParam("projectName", required = false) projectName: String?,
        @RequestParam("reviewId", required = false) reviewId: String?,
        @RequestParam("reviewName", required = false) reviewName: String?
    ): ResponseEntity<Map<String, Any>> {
        return try {
            if (file.isEmpty) {
                return ResponseEntity.badRequest().body(mapOf("error" to "File is empty"))
            }

            // Build directory path from provided parameters
            val directoryPath = buildDirectoryPath(teamId, projectId, projectName, reviewId, reviewName)

            val objectName = minioService.uploadFile(file, customName, directoryPath)
            val objectUrl = minioService.getObjectUrl(objectName)
            ResponseEntity.ok(mapOf(
                "objectName" to objectName, 
                "url" to objectUrl,
                "directoryPath" to (directoryPath ?: "root")
            ))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(mapOf("error" to e.message.toString()))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "Failed to upload file: ${e.message}"))
        }
    }

    /**
     * Upload a file to Minio storage using structured context
     * @param file The file to upload
     * @param customName Optional custom name for the file
     * @param context Upload context containing directory structure parameters
     * @return The object name (key) and URL in Minio
     */
    @PostMapping("/upload-structured")
    fun uploadFileStructured(
        @RequestParam("file") file: MultipartFile,
        @RequestParam("customName", required = false) customName: String?,
        @RequestParam("teamId", required = false) teamId: String?,
        @RequestParam("projectId", required = false) projectId: String?,
        @RequestParam("projectName", required = false) projectName: String?,
        @RequestParam("reviewId", required = false) reviewId: String?,
        @RequestParam("reviewName", required = false) reviewName: String?
    ): ResponseEntity<Map<String, Any>> {
        return try {
            if (file.isEmpty) {
                return ResponseEntity.badRequest().body(mapOf("error" to "File is empty"))
            }

            // Create upload context
            val context = UploadContext(teamId, projectId, projectName, reviewId, reviewName)
            val directoryPath = context.toDirectoryPath()

            val objectName = minioService.uploadFile(file, customName, directoryPath)
            val objectUrl = minioService.getObjectUrl(objectName)
            
            ResponseEntity.ok(mapOf(
                "objectName" to objectName, 
                "url" to objectUrl,
                "directoryPath" to (directoryPath ?: "root"),
                "context" to mapOf(
                    "teamId" to (teamId ?: ""),
                    "projectId" to (projectId ?: ""),
                    "projectName" to (projectName ?: ""),
                    "reviewId" to (reviewId ?: ""),
                    "reviewName" to (reviewName ?: "")
                )
            ))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(mapOf("error" to e.message.toString()))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "Failed to upload file: ${e.message}"))
        }
    }

    /**
     * Delete a file from Minio storage
     * @param objectName The name of the object to delete
     */
    @DeleteMapping("/delete")
    fun deleteFile(@RequestParam("objectName") objectName: String): ResponseEntity<Map<String, String>> {
        return try {
            minioService.deleteFile(objectName)
            ResponseEntity.ok(mapOf("message" to "File deleted successfully"))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "Failed to delete file: ${e.message}"))
        }
    }

    /**
     * List files for a specific review/project/team
     * @param teamId Optional team ID
     * @param projectId Optional project ID  
     * @param projectName Optional project name
     * @param reviewId Optional review ID
     * @param reviewName Optional review name
     * @return List of files in the specified directory
     */
    @GetMapping("/list")
    fun listFiles(
        @RequestParam("teamId", required = false) teamId: String?,
        @RequestParam("projectId", required = false) projectId: String?,
        @RequestParam("projectName", required = false) projectName: String?,
        @RequestParam("reviewId", required = false) reviewId: String?,
        @RequestParam("reviewName", required = false) reviewName: String?
    ): ResponseEntity<Map<String, Any>> {
        return try {
            val directoryPath = buildDirectoryPath(teamId, projectId, projectName, reviewId, reviewName)
            val files = minioService.listFiles(directoryPath ?: "")
            
            ResponseEntity.ok(mapOf(
                "files" to files,
                "directoryPath" to (directoryPath ?: "root"),
                "count" to files.size
            ))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "Failed to list files: ${e.message}"))
        }
    }

    /**
     * Download all files in a directory as ZIP
     * @param teamId Optional team ID
     * @param projectId Optional project ID
     * @param projectName Optional project name  
     * @param reviewId Optional review ID
     * @param reviewName Optional review name
     * @return ZIP file containing all files in the directory
     */
    @GetMapping("/download-zip")
    fun downloadDirectoryAsZip(
        @RequestParam("teamId", required = false) teamId: String?,
        @RequestParam("projectId", required = false) projectId: String?,
        @RequestParam("projectName", required = false) projectName: String?,
        @RequestParam("reviewId", required = false) reviewId: String?,
        @RequestParam("reviewName", required = false) reviewName: String?
    ): ResponseEntity<Any> {
        return try {
            val directoryPath = buildDirectoryPath(teamId, projectId, projectName, reviewId, reviewName)
            
            if (directoryPath.isNullOrBlank()) {
                return ResponseEntity.badRequest()
                    .body(mapOf("error" to "At least teamId must be provided"))
            }

            val zipStream = minioService.downloadDirectoryAsZip(directoryPath)
            val zipFileName = "${directoryPath.replace("/", "-")}.zip"

            ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"$zipFileName\"")
                .header("Content-Type", "application/zip")
                .body(org.springframework.core.io.InputStreamResource(zipStream))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "Failed to download files: ${e.message}"))
        }
    }

    /**
     * Delete all files in a specific directory
     * @param teamId Optional team ID
     * @param projectId Optional project ID
     * @param projectName Optional project name
     * @param reviewId Optional review ID  
     * @param reviewName Optional review name
     * @return Number of files deleted
     */
    @DeleteMapping("/delete-directory")
    fun deleteDirectory(
        @RequestParam("teamId", required = false) teamId: String?,
        @RequestParam("projectId", required = false) projectId: String?,
        @RequestParam("projectName", required = false) projectName: String?,
        @RequestParam("reviewId", required = false) reviewId: String?,
        @RequestParam("reviewName", required = false) reviewName: String?
    ): ResponseEntity<Map<String, Any>> {
        return try {
            val directoryPath = buildDirectoryPath(teamId, projectId, projectName, reviewId, reviewName)
            
            if (directoryPath.isNullOrBlank()) {
                return ResponseEntity.badRequest()
                    .body(mapOf("error" to "At least teamId must be provided"))
            }

            val deletedCount = minioService.deleteDirectory(directoryPath)
            ResponseEntity.ok(mapOf(
                "message" to "Directory deleted successfully",
                "deletedFiles" to deletedCount,
                "directoryPath" to directoryPath
            ))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "Failed to delete directory: ${e.message}"))
        }
    }

    /**
     * Get file information by object name
     * @param objectName The full object name/path
     * @return File information and download URL
     */
    @GetMapping("/file-info")
    fun getFileInfo(@RequestParam("objectName") objectName: String): ResponseEntity<Map<String, Any>> {
        return try {
            val fileUrl = minioService.getObjectUrl(objectName)
            
            ResponseEntity.ok(mapOf(
                "objectName" to objectName,
                "url" to fileUrl,
                "fileName" to objectName.substringAfterLast('/'),
                "directory" to objectName.substringBeforeLast('/', "")
            ))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "Failed to get file info: ${e.message}"))
        }
    }

    /**
     * Build directory path from provided parameters
     * Creates a hierarchical structure: teamId/projectId-projectName/reviewId-reviewName
     * @param teamId The team ID
     * @param projectId The project ID
     * @param projectName The project name
     * @param reviewId The review ID
     * @param reviewName The review name
     * @return The constructed directory path or null if no teamId provided
     */
    private fun buildDirectoryPath(
        teamId: String?,
        projectId: String?,
        projectName: String?,
        reviewId: String?,
        reviewName: String?
    ): String? {
        if (teamId.isNullOrBlank()) return null

        return buildString {
            append(teamId.trim())

            if (!projectId.isNullOrBlank()) {
                append("/")
                append(projectId.trim())
                
                if (!projectName.isNullOrBlank()) {
                    append("-")
                    append(projectName.trim().replace(" ", "_"))
                }

                if (!reviewId.isNullOrBlank()) {
                    append("/")
                    append(reviewId.trim())
                    
                    if (!reviewName.isNullOrBlank()) {
                        append("-")
                        append(reviewName.trim().replace(" ", "_"))
                    }
                }
            }
        }
    }
}

/**
 * Data class for upload context parameters
 */
data class UploadContext(
    val teamId: String? = null,
    val projectId: String? = null,
    val projectName: String? = null,
    val reviewId: String? = null,
    val reviewName: String? = null
) {
    /**
     * Convert upload context to directory path
     * @return The constructed directory path or null if no teamId provided
     */
    fun toDirectoryPath(): String? {
        if (teamId.isNullOrBlank()) return null

        return buildString {
            append(teamId.trim())

            if (!projectId.isNullOrBlank()) {
                append("/")
                append(projectId.trim())
                
                if (!projectName.isNullOrBlank()) {
                    append("-")
                    append(projectName.trim().replace(" ", "_"))
                }

                if (!reviewId.isNullOrBlank()) {
                    append("/")
                    append(reviewId.trim())
                    
                    if (!reviewName.isNullOrBlank()) {
                        append("-")
                        append(reviewName.trim().replace(" ", "_"))
                    }
                }
            }
        }
    }
}
