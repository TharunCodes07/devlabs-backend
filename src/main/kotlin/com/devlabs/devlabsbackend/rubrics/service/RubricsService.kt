package com.devlabs.devlabsbackend.rubrics.service

import com.devlabs.devlabsbackend.rubrics.repository.RubricsRepository
import org.springframework.stereotype.Service

@Service
class RubricsService(
    private val rubricsRepository: RubricsRepository
) {
}