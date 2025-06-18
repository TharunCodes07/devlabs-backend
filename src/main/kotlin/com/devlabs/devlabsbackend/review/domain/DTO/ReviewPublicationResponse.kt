package com.devlabs.devlabsbackend.review.domain.DTO

import java.time.LocalDate
import java.util.*


data class ReviewPublicationResponse(
    val reviewId: UUID,
    val reviewName: String,
    val isPublished: Boolean,
    val publishDate: LocalDate? = null,
    val canPublish: Boolean = false
)
