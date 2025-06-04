package com.devlabs.devlabsbackend.review.service;

import com.devlabs.devlabsbackend.review.repository.ReviewRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Service;

@Service
public class ReviewService(
        private val reviewRepository: ReviewRepository
) {

}