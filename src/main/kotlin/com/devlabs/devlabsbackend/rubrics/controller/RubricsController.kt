package com.devlabs.devlabsbackend.rubrics.controller

import com.devlabs.devlabsbackend.rubrics.service.RubricsService
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/rubrics")
class RubricsController(
    val rubricsService: RubricsService
) {
}