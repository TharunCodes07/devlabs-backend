# Course-Specific Evaluation System Implementation

## Overview

Successfully implemented a course-specific evaluation system where faculty can only evaluate projects for courses they teach, with separate scores and evaluations per course.

## Key Changes Made

### 1. Enhanced IndividualScore Entity

- **File**: `src/main/kotlin/com/devlabs/devlabsbackend/individualscore/domain/IndividualScore.kt`
- **Changes**: Added optional `course` field with `@ManyToOne` relationship to Course entity
- **Purpose**: Enables course-specific score storage

### 2. Updated IndividualScoreRepository

- **File**: `src/main/kotlin/com/devlabs/devlabsbackend/individualscore/repository/IndividualScoreRepository.kt`
- **New Methods**:
  - `findByParticipantAndCriterionAndReviewAndProjectAndCourse()` - Find course-specific scores
  - `findByReviewAndProjectAndCourse()` - Get all scores for a project in a specific course
  - `findDistinctParticipantsByReviewAndProjectAndCourse()` - Count participants evaluated in a course
  - `deleteByParticipantAndReviewAndProjectAndCourse()` - Delete course-specific scores

### 3. Enhanced DTOs

- **File**: `src/main/kotlin/com/devlabs/devlabsbackend/individualscore/domain/DTO/ScoreDTO.kt`
- **New DTOs**:
  - `SubmitCourseScoreRequest` - Submit scores for a specific course
  - `CourseEvaluationInfo` - Information about course evaluations
  - `ProjectEvaluationSummary` - Summary of evaluations by course for a project
  - `CourseEvaluationSummary` - Summary for each course
  - `AvailableEvaluationRequest/Response` - Get available evaluations for faculty

### 4. Enhanced IndividualScoreService

- **File**: `src/main/kotlin/com/devlabs/devlabsbackend/individualscore/service/IndividualScoreService.kt`
- **New Methods**:
  - `submitCourseScores()` - Faculty submit scores for courses they teach
  - `getAvailableEvaluations()` - Get evaluations available to current user based on role
  - `getCourseScoresForParticipant()` - Get course-specific scores for a participant
  - `getCourseScoresForProject()` - Get course-specific scores for all project members
  - `getProjectEvaluationSummary()` - Get evaluation summary by course for a project
  - `deleteCourseScoresForParticipant()` - Delete course-specific scores with permissions

### 5. New Controller Endpoints

- **File**: `src/main/kotlin/com/devlabs/devlabsbackend/individualscore/controller/IndividualScoreController.kt`
- **New Endpoints**:
  - `POST /api/individual-score/course` - Submit course-specific scores
  - `POST /api/individual-score/evaluations/available` - Get available evaluations
  - `GET /api/individual-score/review/{reviewId}/project/{projectId}/course/{courseId}/participant/{participantId}` - Get course-specific scores
  - `DELETE /api/individual-score/review/{reviewId}/project/{projectId}/course/{courseId}/participant/{participantId}` - Delete course-specific scores

### 6. Fixed Review Assignment Detection

- **File**: `src/main/kotlin/com/devlabs/devlabs-backend/review/repository/ReviewRepository.kt`
- **Changes**: Added `findAllWithAssociations()` and `findByCourses()` methods
- **Purpose**: Eager loading to prevent LazyInitializationException in review assignment checks

### 7. Database Migration

- **File**: `add-course-to-individual-score.sql`
- **Purpose**: Adds `course_id` column to `individual_score` table with proper indexes

## Permission System

### Faculty Permissions

- Can only submit/view/delete scores for courses they teach
- Cannot access scores for courses they don't instruct
- Can see their available evaluations across all their courses

### Admin/Manager Permissions

- Can submit/view/delete scores for any course
- Can see all available evaluations
- Have full access to all evaluation functions

## Key Features

### Course-Specific Evaluation Workflow

1. Faculty logs in and requests available evaluations
2. System returns only evaluations for courses they teach
3. Faculty selects a project and course combination
4. Faculty submits scores specific to that course
5. Scores are stored with course association

### Backward Compatibility

- Existing scores (without course association) remain unchanged
- New course-specific scores have `course_id` populated
- Both systems can coexist during transition

### Data Integrity

- Foreign key constraints ensure data consistency
- Proper indexing for performance
- Validation prevents invalid course associations

## API Usage Examples

### 1. Get Available Evaluations for Faculty

```http
POST /api/individual-score/evaluations/available
Content-Type: application/json

{
  "userId": "faculty-uuid-here"
}
```

### 2. Submit Course-Specific Scores

```http
POST /api/individual-score/course
Content-Type: application/json
X-User-Id: faculty-uuid-here

{
  "reviewId": "review-uuid",
  "projectId": "project-uuid",
  "courseId": "course-uuid",
  "scores": [
    {
      "participantId": "student-uuid",
      "criterionScores": [
        {
          "criterionId": "criterion-uuid",
          "score": 85.0,
          "comment": "Excellent work"
        }
      ]
    }
  ]
}
```

### 3. Get Course-Specific Scores

```http
GET /api/individual-score/review/{reviewId}/project/{projectId}/course/{courseId}/participant/{participantId}
```

## Database Migration Instructions

1. **Run the migration script**:

   ```sql
   -- Execute the contents of add-course-to-individual-score.sql
   -- This adds course_id column and indexes to individual_score table
   ```

2. **Verify migration**:

   ```sql
   -- Check if column was added
   DESCRIBE individual_score;

   -- Check indexes
   SHOW INDEX FROM individual_score;
   ```

## Testing Strategy

### 1. Unit Tests

- Test course permission validation
- Test score submission with course association
- Test retrieval of course-specific scores

### 2. Integration Tests

- Test faculty can only access their courses
- Test admin/manager can access all courses
- Test backward compatibility with existing scores

### 3. API Tests

- Test all new endpoints with different user roles
- Test permission enforcement
- Test error handling for invalid course associations

## Next Steps

1. **Run database migration** in all environments
2. **Update frontend** to use new course-specific evaluation workflow
3. **Test thoroughly** with different user roles
4. **Consider consolidation** of evaluation systems (IndividualScore vs Evaluation)
5. **Monitor performance** with new indexes and queries

## Benefits Achieved

✅ **Course-specific evaluations** - Faculty can only evaluate projects for courses they teach
✅ **Separate scores per course** - Each course gets evaluated separately by its instructor  
✅ **Role-based permissions** - Proper access control based on user roles
✅ **Backward compatibility** - Existing evaluation system continues to work
✅ **Performance optimized** - Proper indexing for course-specific queries
✅ **Data integrity** - Foreign key constraints and validation
✅ **Comprehensive API** - Full CRUD operations for course-specific evaluations

The implementation successfully addresses all requirements while maintaining system integrity and performance.
