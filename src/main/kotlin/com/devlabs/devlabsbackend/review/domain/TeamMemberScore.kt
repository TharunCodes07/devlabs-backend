package com.devlabs.devlabsbackend.review.domain

import com.devlabs.devlabsbackend.user.domain.User
import jakarta.persistence.*
import java.sql.Timestamp
import java.time.Instant
import java.util.*

@Entity
@Table(name = "team_member_score")
class TeamMemberScore(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null,
    
    // The review this score belongs to
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id")
    var review: Review,
    
    // The team member being scored
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_member_id")
    var teamMember: User,
    
    // Individual scores
    var individualScore: Double? = null,
    var maxPossibleScore: Double? = null,
    var percentageScore: Double? = null,
    
    // Individual feedback
    @Column(columnDefinition = "TEXT")
    var individualFeedback: String? = null,
    
    @Column(columnDefinition = "TEXT")
    var strengths: String? = null,
    
    @Column(columnDefinition = "TEXT")
    var improvements: String? = null,
    
    val createdAt: Timestamp = Timestamp.from(Instant.now()),
    var updatedAt: Timestamp = Timestamp.from(Instant.now())
)
