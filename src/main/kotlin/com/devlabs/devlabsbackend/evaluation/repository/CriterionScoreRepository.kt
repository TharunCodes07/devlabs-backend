package com.devlabs.devlabsbackend.evaluation.repository

import com.devlabs.devlabsbackend.criterion.domain.Criterion
import com.devlabs.devlabsbackend.evaluation.domain.CriterionScore
import com.devlabs.devlabsbackend.evaluation.domain.Evaluation
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface CriterionScoreRepository : JpaRepository<CriterionScore, UUID> {
    fun findByEvaluation(evaluation: Evaluation): List<CriterionScore>
    
    fun findByEvaluationAndCriterion(evaluation: Evaluation, criterion: Criterion): Optional<CriterionScore>
    
    @Query("SELECT cs FROM CriterionScore cs WHERE cs.evaluation.id = :evaluationId")
    fun findByEvaluationId(@Param("evaluationId") evaluationId: UUID): List<CriterionScore>
}
