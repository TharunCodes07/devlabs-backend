package com.devlabs.devlabsbackend.project.domain.DTO

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonSetter
import com.fasterxml.jackson.annotation.Nulls
import java.util.*

@JsonIgnoreProperties(ignoreUnknown = true)
data class CreateProjectRequest(
    val title: String,
    val description: String,
    val objectives: String? = null,
    val githubUrl: String? = null,
    val teamId: UUID,
    
    @JsonSetter(nulls = Nulls.SKIP)
    val courseIds: List<UUID> = emptyList()
)

data class UpdateProjectRequest(
    val userId: String,
    val title: String? = null,
    val description: String? = null,
    val objectives: String? = null,
    val githubUrl: String? = null
)
