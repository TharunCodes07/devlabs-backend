package com.devlabs.devlabsbackend.project.service

data class PaginatedResponse<T>(
    val data: List<T>,
    val pagination: PaginationInfo
)

data class PaginationInfo(
    val current_page: Int,
    val per_page: Int,
    val total_pages: Int,
    val total_count: Int
)
