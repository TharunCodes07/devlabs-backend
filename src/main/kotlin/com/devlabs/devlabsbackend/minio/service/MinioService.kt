package com.devlabs.devlabsbackend.s3.service

import io.minio.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.InputStream
import java.util.*

@Service
class MinioService @Autowired constructor(
    private val minioClient: MinioClient,
    @Qualifier("minioBucketName") private val bucketName: String
) {
    // List of allowed file types for upload
    private val allowedFileTypes = listOf(
        "image/jpeg", "image/png", "image/gif", "image/webp",
        "application/pdf", "application/msword",
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
        "text/plain", "application/zip", "application/x-rar-compressed"
    )

    /**
     * Upload a file to Minio storage
     * @param file The file to upload
     * @param customName Optional custom name for the file
     * @param directoryPath Optional directory path for organizing files
     * @return The object name (key) in Minio
     */
    fun uploadFile(file: MultipartFile, customName: String? = null, directoryPath: String? = null): String {
        // Validate file type
        if (!isValidFileType(file.contentType)) {
            throw IllegalArgumentException("File type not allowed: ${file.contentType}")
        }

        // Generate object name with directory path
        val fileName = if (customName.isNullOrBlank()) {
            // Default naming with UUID if no custom name provided
            UUID.randomUUID().toString() + "_" + file.originalFilename?.replace(" ", "_")
        } else {
            // Use custom name but check if it already exists
            generateUniqueObjectName(customName, directoryPath)
        }

        // Build full object path
        val objectName = if (directoryPath.isNullOrBlank()) {
            fileName
        } else {
            "${directoryPath.trim('/')}/$fileName"
        }

        // Upload the file to Minio
        minioClient.putObject(
            PutObjectArgs.builder()
                .bucket(bucketName)
                .`object`(objectName)
                .stream(file.inputStream, file.size, -1)
                .contentType(file.contentType)
                .build()
        )

        return objectName
    }

    /**
     * Generate a unique object name by checking if the custom name already exists in the bucket
     * If it exists, append a UUID to make it unique
     * @param customName The custom name to check
     * @param directoryPath Optional directory path for the file
     * @return A unique object name
     */
    private fun generateUniqueObjectName(customName: String, directoryPath: String? = null): String {
        val sanitizedName = customName.replace(" ", "_")
        
        // Build full path for existence check
        val fullPath = if (directoryPath.isNullOrBlank()) {
            sanitizedName
        } else {
            "${directoryPath.trim('/')}/$sanitizedName"
        }

        // Check if object already exists
        val exists = objectExists(fullPath)

        // If exists, append UUID to make it unique
        return if (exists) {
            "$sanitizedName-${UUID.randomUUID()}"
        } else {
            sanitizedName
        }
    }

    /**
     * Check if an object exists in the bucket
     * @param objectName The name of the object to check
     * @return true if the object exists, false otherwise
     */
    private fun objectExists(objectName: String): Boolean {
        return try {
            minioClient.statObject(
                StatObjectArgs.builder()
                    .bucket(bucketName)
                    .`object`(objectName)
                    .build()
            )
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Delete a file from Minio storage
     * @param objectName The name of the object to delete
     */
    fun deleteFile(objectName: String) {
        minioClient.removeObject(
            RemoveObjectArgs.builder()
                .bucket(bucketName)
                .`object`(objectName)
                .build()
        )
    }

    /**
     * Check if file type is allowed
     * @param contentType The content type to check
     * @return true if the content type is allowed, false otherwise
     */
    private fun isValidFileType(contentType: String?): Boolean {
        if (contentType == null) return false
        return allowedFileTypes.contains(contentType)
    }

    /**
     * Get a file from Minio storage
     * @param objectName The name of the object to get
     * @return The input stream of the file
     */
    fun getFile(objectName: String): InputStream {
        return minioClient.getObject(
            GetObjectArgs.builder()
                .bucket(bucketName)
                .`object`(objectName)
                .build()
        )
    }

    /**
     * Generate a pre-signed URL for accessing an object
     * @param objectName The name of the object
     * @param expirySeconds How long the URL should be valid for (in seconds)
     * @return The pre-signed URL for the object
     */
    fun getObjectUrl(objectName: String, expirySeconds: Int = 7 * 24 * 3600): String {
        return minioClient.getPresignedObjectUrl(
            GetPresignedObjectUrlArgs.builder()
                .bucket(bucketName)
                .`object`(objectName)
                .method(io.minio.http.Method.GET)
                .expiry(expirySeconds)
                .build()
        )
    }

    /**
     * List files in a specific directory path
     * @param directoryPath The directory path to list files from
     * @return List of file information
     */
    fun listFiles(directoryPath: String): List<Map<String, Any>> {
        val prefix = if (directoryPath.isBlank()) "" else "${directoryPath.trim('/')}/"
        
        val objects = minioClient.listObjects(
            ListObjectsArgs.builder()
                .bucket(bucketName)
                .prefix(prefix)
                .recursive(true)
                .build()
        )

        return objects.map { item ->
            val result = item.get()
            mapOf(
                "objectName" to result.objectName(),
                "size" to result.size(),
                "lastModified" to result.lastModified().toString(),
                "etag" to result.etag(),
                "fileName" to result.objectName().substringAfterLast('/'),
                "directory" to result.objectName().substringBeforeLast('/', "")
            )
        }.toList()
    }

    /**
     * Download all files in a directory as a ZIP
     * @param directoryPath The directory path to download files from
     * @return InputStream of the ZIP file
     */
    fun downloadDirectoryAsZip(directoryPath: String): InputStream {
        val prefix = if (directoryPath.isBlank()) "" else "${directoryPath.trim('/')}/"
        
        val objects = minioClient.listObjects(
            ListObjectsArgs.builder()
                .bucket(bucketName)
                .prefix(prefix)
                .recursive(true)
                .build()
        )

        val tempZipFile = java.io.File.createTempFile("minio-download", ".zip")
        val zipOutputStream = java.util.zip.ZipOutputStream(java.io.FileOutputStream(tempZipFile))

        try {
            objects.forEach { item ->
                val result = item.get()
                val objectStream = minioClient.getObject(
                    GetObjectArgs.builder()
                        .bucket(bucketName)
                        .`object`(result.objectName())
                        .build()
                )

                // Use only the filename for the ZIP entry (remove directory structure)
                val fileName = result.objectName().substringAfterLast('/')
                val zipEntry = java.util.zip.ZipEntry(fileName)
                zipOutputStream.putNextEntry(zipEntry)
                
                objectStream.copyTo(zipOutputStream)
                objectStream.close()
                zipOutputStream.closeEntry()
            }
        } finally {
            zipOutputStream.close()
        }

        return java.io.FileInputStream(tempZipFile)
    }

    /**
     * Delete all files in a directory
     * @param directoryPath The directory path to delete files from
     * @return Number of files deleted
     */
    fun deleteDirectory(directoryPath: String): Int {
        val prefix = if (directoryPath.isBlank()) "" else "${directoryPath.trim('/')}/"
        
        val objects = minioClient.listObjects(
            ListObjectsArgs.builder()
                .bucket(bucketName)
                .prefix(prefix)
                .recursive(true)
                .build()
        )

        val objectNames = objects.map { it.get().objectName() }.toList()
        
        if (objectNames.isNotEmpty()) {
            val deleteObjects = objectNames.map { 
                io.minio.messages.DeleteObject(it) 
            }
            
            minioClient.removeObjects(
                RemoveObjectsArgs.builder()
                    .bucket(bucketName)
                    .objects(deleteObjects)
                    .build()
            )
        }

        return objectNames.size
    }
}
