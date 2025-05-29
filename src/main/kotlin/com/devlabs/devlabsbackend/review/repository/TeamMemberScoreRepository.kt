package com.devlabs.devlabsbackend.review.repository

import com.devlabs.devlabsbackend.review.domain.Review
import com.devlabs.devlabsbackend.review.domain.TeamMemberScore
import com.devlabs.devlabsbackend.user.domain.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface TeamMemberScoreRepository : JpaRepository<TeamMemberScore, UUID> {
    
    fun findByReview(review: Review): List<TeamMemberScore>
    
    fun findByReviewAndTeamMember(review: Review, teamMember: User): TeamMemberScore?
    
    fun findByTeamMember(teamMember: User): List<TeamMemberScore>
    
    @Query("SELECT tms FROM TeamMemberScore tms WHERE tms.review.project.id = :projectId")
    fun findByProjectId(@Param("projectId") projectId: UUID): List<TeamMemberScore>
    
    @Query("SELECT tms FROM TeamMemberScore tms WHERE tms.review.reviewer.id = :reviewerId")
    fun findByReviewerId(@Param("reviewerId") reviewerId: UUID): List<TeamMemberScore>
    
    @Query("SELECT AVG(tms.percentageScore) FROM TeamMemberScore tms WHERE tms.teamMember.id = :teamMemberId AND tms.percentageScore IS NOT NULL")
    fun findAverageScoreByTeamMember(@Param("teamMemberId") teamMemberId: UUID): Double?
}
