package com.devlabs.devlabsbackend.individualscore.service;

import com.devlabs.devlabsbackend.individualscore.repository.IndividualScoreRepository;
import org.springframework.stereotype.Service;

@Service
public class IndividualScoreService (
        private  val individualScoreRepository: IndividualScoreRepository
){
}
