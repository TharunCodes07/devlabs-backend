package com.devlabs.devlabsbackend.batch.controller

import com.devlabs.devlabsbackend.batch.service.BatchService
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/batch")
class BatchController(
    private val batchService: BatchService
)
{
    @PostMapping("/add")
    fun addStudents(@RequestBody batchId: UUID, studentId: List<UUID>){
        batchService.addStudentsToBatch(batchId, studentId)
    }

    @DeleteMapping("/delete")
    fun deleteStudents(@RequestBody batchId: UUID, studentId: List<UUID>){
        batchService.removeStudentsFromBatch(batchId, studentId)
    }
}