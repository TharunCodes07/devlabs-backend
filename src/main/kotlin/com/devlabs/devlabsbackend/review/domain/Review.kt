package com.devlabs.devlabsbackend.review.domain

import com.devlabs.devlabsbackend.project.domain.Project
import com.devlabs.devlabsbackend.rubric.domain.Rubric
import com.devlabs.devlabsbackend.user.domain.User
import jakarta.persistence.*
import java.sql.Timestamp
import java.time.Instant
import java.util.*

@Entity
@Table(name = "review")
class Review(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)    val id: UUID? = null,
    var title: String,
    var description: String? = null,
    
    // Exam-like scheduling with start and end dates
    var startDate: Timestamp? = null,
    var endDate: Timestamp? = null,
    
    // Project being reviewed
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    var project: Project,
    
    // Reviewer (instructor/faculty)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewer_id")
    var reviewer: User,
      // Rubric for evaluation
    @OneToOne(mappedBy = "review", fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    var rubric: Rubric? = null,
    
    // Individual team member scores
    @OneToMany(mappedBy = "review", fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    var teamMemberScores: MutableSet<TeamMemberScore> = mutableSetOf(),
    
    // Overall scores and feedback
    var totalScore: Double? = null,
    var maxPossibleScore: Double? = null,
    var percentageScore: Double? = null,
    
    @Column(columnDefinition = "TEXT")
    var overallFeedback: String? = null,
    
    @Column(columnDefinition = "TEXT")
    var strengths: String? = null,
    
    @Column(columnDefinition = "TEXT")
    var improvements: String? = null,
    
    @Column(columnDefinition = "TEXT")
    var nextSteps: String? = null,
    
    // Meeting/presentation details (if applicable)
    var presentationDate: Timestamp? = null,
    var presentationDuration: Int? = null, // in minutes
    var presentationLocation: String? = null,
    var presentationNotes: String? = null,
    
    // Attendance tracking for team members
    @ElementCollection
    @CollectionTable(
        name = "review_attendance",
        joinColumns = [JoinColumn(name = "review_id")]
    )
    @MapKeyJoinColumn(name = "user_id")
    @Column(name = "attended")
    var attendance: MutableMap<User, Boolean> = mutableMapOf(),
      val createdAt: Timestamp = Timestamp.from(Instant.now()),
    var updatedAt: Timestamp = Timestamp.from(Instant.now())
) {
    // Helper method to check if review is currently active (between start and end dates)
    fun isActive(): Boolean {
        val now = Timestamp.from(Instant.now())
        return when {
            startDate == null || endDate == null -> false
            now.after(startDate) && now.before(endDate) -> true
            else -> false
        }
    }
    
    // Helper method to check if review has ended
    fun hasEnded(): Boolean {
        val now = Timestamp.from(Instant.now())
        return endDate?.before(now) == true
    }
    
    // Helper method to check if review hasn't started yet
    fun hasNotStarted(): Boolean {
        val now = Timestamp.from(Instant.now())
        return startDate?.after(now) == true
    }

    // Helper method to calculate percentage score
    fun calculatePercentageScore(): Double? {
        return if (totalScore != null && maxPossibleScore != null && maxPossibleScore!! > 0) {
            (totalScore!! / maxPossibleScore!!) * 100
        } else null
    }
}