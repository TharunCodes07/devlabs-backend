package com.devlabs.devlabsbackend.review.controller;

import com.devlabs.devlabsbackend.review.service.ReviewService
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/review")
public class ReviewController(
    val reviewService: ReviewService,
){

}
