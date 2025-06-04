package com.devlabs.devlabsbackend.individualscore.controller

import com.devlabs.devlabsbackend.individualscore.service.IndividualScoreService
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/individual_score")
class IndividualScoreController (
    private val individualScoreService: IndividualScoreService
){
}