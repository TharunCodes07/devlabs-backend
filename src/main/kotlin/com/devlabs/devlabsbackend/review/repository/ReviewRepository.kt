package com.devlabs.devlabsbackend.review.repository

import com.devlabs.devlabsbackend.review.domain.Review
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.rest.core.annotation.RepositoryRestResource
import java.util.UUID

@RepositoryRestResource(path = "reviews")
interface ReviewRepository: JpaRepository<Review, UUID> {
}