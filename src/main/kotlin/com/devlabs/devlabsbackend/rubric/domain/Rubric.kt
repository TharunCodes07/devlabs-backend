package com.devlabs.devlabsbackend.rubric.domain

import com.devlabs.devlabsbackend.user.domain.User
import com.devlabs.devlabsbackend.review.domain.Review
import jakarta.persistence.*
import java.sql.Timestamp
import java.time.Instant
import java.util.*

enum class RubricItemType {
    SCALE,      // 1-5, 1-10, etc.
    PERCENTAGE, // 0-100%
    BOOLEAN,    // Pass/Fail
    TEXT        // Free text feedback
}

@Entity
@Table(name = "rubric_template")
class RubricTemplate(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null,
    
    var name: String,
    var description: String? = null,
    var isPublic: Boolean = false, // If true, can be used by other instructors
    
    // Creator of this template (faculty/staff)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    var createdBy: User,
    
    // Rubric items/criteria
    @OneToMany(mappedBy = "template", fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    var items: MutableSet<RubricTemplateItem> = mutableSetOf(),
    
    val createdAt: Timestamp = Timestamp.from(Instant.now()),
    var updatedAt: Timestamp = Timestamp.from(Instant.now())
) {
    // Helper method to calculate total possible score
    fun getTotalPossibleScore(): Double {
        return items.filter { it.type == RubricItemType.SCALE || it.type == RubricItemType.PERCENTAGE }
            .sumOf { it.maxScore ?: 0.0 }
    }
}

@Entity
@Table(name = "rubric_template_item")
class RubricTemplateItem(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null,
    
    var criteriaName: String,
    var description: String? = null,
    var type: RubricItemType,
    var weight: Double = 1.0, // Weight/importance of this criteria
    var minScore: Double? = null, // For SCALE and PERCENTAGE types
    var maxScore: Double? = null, // For SCALE and PERCENTAGE types
    var isRequired: Boolean = true,
    var orderIndex: Int = 0, // For ordering criteria
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id")
    var template: RubricTemplate
)

@Entity
@Table(name = "rubric")
class Rubric(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null,
    
    // Review this rubric belongs to
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id")
    var review: Review,
    
    // Template used (optional, can be custom rubric)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id")
    var template: RubricTemplate? = null,
    
    // Actual rubric items with scores
    @OneToMany(mappedBy = "rubric", fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    var items: MutableSet<RubricItem> = mutableSetOf(),
    
    val createdAt: Timestamp = Timestamp.from(Instant.now()),
    var updatedAt: Timestamp = Timestamp.from(Instant.now())
) {
    // Helper method to calculate total score
    fun getTotalScore(): Double {
        return items.mapNotNull { it.score }.sum()
    }
    
    // Helper method to calculate weighted score
    fun getWeightedScore(): Double {
        return items.mapNotNull { item -> 
            item.score?.let { it * item.weight } 
        }.sum()
    }
}

@Entity
@Table(name = "rubric_item")
class RubricItem(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null,
    
    var criteriaName: String,
    var description: String? = null,
    var type: RubricItemType,
    var weight: Double = 1.0,
    var minScore: Double? = null,
    var maxScore: Double? = null,
    var score: Double? = null, // Actual score given
    var feedback: String? = null, // Text feedback for this criteria
    var orderIndex: Int = 0,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rubric_id")
    var rubric: Rubric
)
