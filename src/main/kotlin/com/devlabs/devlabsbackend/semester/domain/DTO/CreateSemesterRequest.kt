package com.devlabs.devlabsbackend.semester.domain.DTO

data class CreateSemesterRequest(
    val name: String,
    val year: Int,
    val isActive: Boolean = true
)
