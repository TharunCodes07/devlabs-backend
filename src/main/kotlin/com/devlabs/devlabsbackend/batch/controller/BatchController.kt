package com.devlabs.devlabsbackend.batch.controller

import com.devlabs.devlabsbackend.batch.service.BatchService
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/batch")
class BatchController(
    private val batchService: BatchService
) {

    @PutMapping("/{batchId}/add-students")
    fun addStudents(@RequestBody userIds: List<UUID>, @PathVariable batchId: UUID) {
        batchService.addStudentsToBatch(batchId, userIds)
    }

    @PutMapping("/{batchId}/delete-students")
    fun deleteStudents(@RequestBody userIds: List<UUID>, @PathVariable batchId: UUID) {
        batchService.removeStudentsFromBatch(batchId, userIds)
    }

    @PutMapping("/{batchId}/add-semesters")
    fun addSemesters(@RequestBody semesterIds: List<UUID>, @PathVariable batchId: UUID) {
        batchService.addSemestersToBatches(batchId, semesterIds)
    }

    @PutMapping("/{batchId}/delete-semesters")
    fun deleteSemesters(@RequestBody semesterIds: List<UUID>, @PathVariable batchId: UUID) {
        batchService.removeSemestersFromBatch(batchId, semesterIds)
    }

    @PutMapping("/{batchId}/assign-managers")
    fun assignManagers(@RequestBody userIds: List<UUID>, @PathVariable batchId: UUID) {
        batchService.addManagersToBatch(batchId, userIds)
    }

    @PutMapping("/{batchId}/remove-managers")
    fun removeManagers(@RequestBody userIds: List<UUID>, @PathVariable batchId: UUID) {
        batchService.removeManagersFromBatch(batchId, userIds)
    }
}
