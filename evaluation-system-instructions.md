# Evaluation System - Frontend Integration Guide

This document provides instructions for integrating with the Evaluation API in the DevLabs backend system. This system allows faculty, managers, and admins to evaluate projects as part of reviews.

## Overview

The Evaluation API allows authorized users to:

- Retrieve evaluation criteria for a specific review
- Submit evaluations for projects
- View evaluation results for projects

Key features:

- Faculty can evaluate projects that are part of their courses
- Evaluations consist of scores and comments for each criterion defined in the review's rubric
- Results show individual evaluations and aggregate statistics
- Support for draft and submitted evaluations

## API Endpoints

### 1. Get Evaluation Criteria for a Review

Retrieves all criteria associated with a specific review, which evaluators will need to score.

**Endpoint:** `GET /api/evaluations/review/{reviewId}/criteria`

**Parameters:**

- `reviewId` (path parameter): UUID of the review

**Response:**

```json
{
  "reviewId": "uuid-string",
  "reviewName": "Final Project Evaluation",
  "criteria": [
    {
      "id": "criterion-uuid-string",
      "name": "Code Quality",
      "description": "Assesses the quality and cleanliness of code",
      "maxScore": 10.0,
      "isCommon": false
    },
    {
      "id": "criterion-uuid-string",
      "name": "Documentation",
      "description": "Evaluates the completeness and clarity of documentation",
      "maxScore": 5.0,
      "isCommon": false
    }
  ]
}
```

**Notes:**

- This endpoint should be called first when loading the evaluation form
- The returned criteria list should be used to dynamically generate form fields

### 2. Submit Evaluation

Creates or updates an evaluation for a project in a review.

**Endpoint:** `POST /api/evaluations/submit?userId={userId}`

**Parameters:**

- `userId` (query parameter): UUID of the user submitting the evaluation

**Request Body:**

```json
{
  "reviewId": "review-uuid-string",
  "projectId": "project-uuid-string",
  "comments": "Overall good project with solid implementation",
  "criterionScores": [
    {
      "criterionId": "criterion-uuid-string",
      "score": 8.5,
      "comment": "Good code quality but lacks comprehensive documentation"
    },
    {
      "criterionId": "criterion-uuid-string",
      "score": 9.0,
      "comment": "Excellent functionality"
    }
  ]
}
```

**Response:**

```json
{
  "id": "evaluation-uuid-string",
  "reviewId": "review-uuid-string",
  "reviewName": "Final Project Evaluation",
  "projectId": "project-uuid-string",
  "projectTitle": "AI-Powered Task Manager",
  "evaluatorId": "user-uuid-string",
  "evaluatorName": "John Doe",
  "comments": "Overall good project with solid implementation",
  "criterionScores": [
    {
      "id": "score-uuid-string",
      "criterionId": "criterion-uuid-string",
      "criterionName": "Code Quality",
      "score": 8.5,
      "maxScore": 10.0,
      "comment": "Good code quality but lacks comprehensive documentation"
    },
    {
      "id": "score-uuid-string",
      "criterionId": "criterion-uuid-string",
      "criterionName": "Documentation",
      "score": 9.0,
      "maxScore": 10.0,
      "comment": "Excellent functionality"
    }
  ],
  "totalScore": 17.5,
  "maxPossibleScore": 20.0,
  "status": "SUBMITTED",
  "createdAt": "2025-06-11T15:30:00Z",
  "updatedAt": "2025-06-11T15:30:00Z"
}
```

**Notes:**

- Only faculty, managers, and admins can submit evaluations
- Faculty can only evaluate projects that are part of their courses
- Scores must be between 0 and the criterion's maxScore
- This endpoint handles both new evaluations and updates to existing ones

### 3. Get Evaluation Results

Retrieves evaluation results for a specific project in a review.

**Endpoint:** `GET /api/evaluations/results?reviewId={reviewId}&projectId={projectId}`

**Parameters:**

- `reviewId` (query parameter): UUID of the review
- `projectId` (query parameter): UUID of the project

**Response:**

```json
{
  "reviewId": "review-uuid-string",
  "reviewName": "Final Project Evaluation",
  "projectId": "project-uuid-string",
  "projectTitle": "AI-Powered Task Manager",
  "evaluations": [
    {
      "id": "evaluation-uuid-string",
      "evaluatorId": "user-uuid-string",
      "evaluatorName": "John Doe",
      "totalScore": 17.5,
      "status": "SUBMITTED",
      "updatedAt": "2025-06-11T15:30:00Z"
    },
    {
      "id": "evaluation-uuid-string",
      "evaluatorId": "user-uuid-string",
      "evaluatorName": "Jane Smith",
      "totalScore": 18.0,
      "status": "SUBMITTED",
      "updatedAt": "2025-06-11T16:45:00Z"
    }
  ],
  "averageScore": 17.75,
  "maxPossibleScore": 20.0
}
```

**Notes:**

- Only submitted evaluations are included in the results
- The average score is calculated across all evaluations
- This endpoint can be used for both faculty viewing their own evaluations and students viewing results

### 4. Get Specific Evaluation

Retrieves a single evaluation by its ID.

**Endpoint:** `GET /api/evaluations/{evaluationId}`

**Parameters:**

- `evaluationId` (path parameter): UUID of the evaluation

**Response:** Same format as the POST response above.

**Notes:**

- Use this endpoint to load an existing evaluation for editing
- Also useful for viewing detailed results of a specific evaluation

### 5. Check if Project has Reviews Assigned

Determines whether a project has any reviews assigned to it (directly or indirectly).

**Endpoint:** `GET /api/projects/{projectId}/reviews`

**Parameters:**

- `projectId` (path parameter): UUID of the project to check

**Response:**

```json
{
  "hasReview": true,
  "assignmentType": "DIRECT",
  "liveReviews": [
    {
      "id": "review-uuid-string",
      "name": "Final Project Evaluation",
      "startDate": "2025-06-01T00:00:00Z",
      "endDate": "2025-06-30T00:00:00Z",
      "courses": [
        {
          "id": "course-uuid-string",
          "name": "Advanced Software Engineering",
          "code": "CSE401",
          "semesterInfo": {
            "id": "semester-uuid-string",
            "name": "Spring 2025",
            "year": 2025,
            "isActive": true
          }
        }
      ],
      "projects": [
        {
          "id": "project-uuid-string",
          "title": "AI-Powered Task Manager",
          "teamName": "Innovators",
          "teamMembers": [
            {
              "id": "member-uuid-string",
              "name": "John Doe"
            }
          ]
        }
      ],
      "rubricsInfo": {
        "id": "rubrics-uuid-string",
        "name": "Project Evaluation Rubric",
        "criteria": [
          {
            "id": "criterion-uuid-string",
            "name": "Code Quality",
            "description": "Assesses the quality and cleanliness of code",
            "maxScore": 10.0,
            "isCommon": false
          }
        ]
      }
    }
  ],
  "completedReviews": []
}
```

**Notes:**

- The `assignmentType` field indicates how the project is related to the review:
  - `DIRECT`: Project is directly assigned to the review
  - `COURSE`: Project's course is associated with the review
  - `BATCH`: Project's team members' batch is associated with the review
  - `SEMESTER`: Project's course's semester has courses associated with the review
  - `NONE`: No reviews are associated with the project
- The response contains separate arrays for `liveReviews` (ongoing) and `completedReviews`
- Live reviews are those where the current date falls between startDate and endDate
- Completed reviews are those where the endDate is in the past
- Use this endpoint before showing evaluation options to determine if a project has any reviews

## Implementation Guidelines

### Evaluation Workflow

1. **Review Selection:**

   - Display a list of reviews the user has access to
   - Allow user to select a review to begin evaluations

2. **Project Selection:**

   - Display projects that are part of the selected review
   - For faculty, only show projects from their courses
   - Filter by status (not evaluated, in progress, completed)

3. **Evaluation Form:**

   - Call `GET /api/evaluations/review/{reviewId}/criteria` to get criteria
   - Dynamically generate form fields for each criterion
   - Include validation for score ranges (0 to maxScore)
   - Allow saving as draft or submitting final evaluation
   - If editing an existing evaluation, pre-populate the form

4. **Results View:**
   - Display summary statistics (average score, maximum possible score)
   - Show list of evaluators with their total scores
   - Optionally allow drilling down into individual evaluations
   - Consider visualizations like charts or graphs for the results

### UI/UX Recommendations

1. **Evaluation Form:**

   - Use sliders or number inputs with clear min/max boundaries for scores
   - Display criterion description as help text or tooltip
   - Show running total score and percentage as user enters scores
   - Include progress indicator for large rubrics
   - Implement auto-save for draft evaluations

2. **Results Display:**

   - Use charts to visualize scores across criteria
   - If multiple evaluations exist, show comparison chart
   - Consider radar/spider charts for multi-criteria comparison
   - Color-code scores (red/yellow/green) based on score ranges

3. **Navigation:**
   - Implement breadcrumbs for easy navigation (Reviews > Projects > Evaluation)
   - Provide batch actions for evaluators with many projects to assess
   - Include sorting and filtering options for project lists

### Role-Based Features

1. **Faculty View:**

   - Show only courses and projects they teach
   - Focus on evaluation creation and submission
   - Limited view of other faculty evaluations

2. **Manager/Admin View:**

   - Access to all evaluations
   - Comprehensive analytics and reports
   - Ability to reassign evaluations if needed

3. **Student View (if applicable):**
   - Read-only view of evaluation results
   - Focus on feedback and overall scores
   - No access to evaluator identities if anonymity is required

### Error Handling

Common error responses to handle:

- 404 Not Found: Review, project, or evaluation doesn't exist
- 403 Forbidden: User doesn't have permission for the operation
- 400 Bad Request: Invalid input data (e.g., score out of range)

Implement appropriate error messages and fallback UI states for these scenarios.

## Data Models

### Evaluation

| Field            | Type      | Description                            |
| ---------------- | --------- | -------------------------------------- |
| id               | UUID      | Unique identifier                      |
| reviewId         | UUID      | Review this evaluation belongs to      |
| projectId        | UUID      | Project being evaluated                |
| evaluatorId      | UUID      | User who created the evaluation        |
| comments         | String    | Overall comments for the evaluation    |
| criterionScores  | Array     | List of scores for individual criteria |
| totalScore       | Float     | Sum of all criterion scores            |
| maxPossibleScore | Float     | Maximum possible score                 |
| status           | String    | DRAFT or SUBMITTED                     |
| createdAt        | Timestamp | Creation date and time                 |
| updatedAt        | Timestamp | Last update date and time              |

### CriterionScore

| Field         | Type   | Description                          |
| ------------- | ------ | ------------------------------------ |
| id            | UUID   | Unique identifier                    |
| criterionId   | UUID   | The criterion being scored           |
| criterionName | String | Name of the criterion                |
| score         | Float  | Score given for this criterion       |
| maxScore      | Float  | Maximum possible score for criterion |
| comment       | String | Optional comment for this score      |

## Example User Flows

### Faculty Evaluation Flow

1. Faculty selects a review from their dashboard
2. System displays all projects for that review that are in the faculty's courses
3. Faculty selects a project to evaluate
4. System loads evaluation criteria and any existing evaluation data
5. Faculty fills in scores and comments for each criterion
6. Faculty submits the completed evaluation
7. System confirms submission and returns to project list

### Manager/Admin Results Review Flow

1. Manager selects a review from the dashboard
2. System displays all projects for that review
3. Manager selects a project to view results
4. System displays all evaluations for that project with summary statistics
5. Manager can drill down into individual evaluations for detailed analysis
6. System provides options for exporting or sharing results

## Considerations for Future Extensions

1. **Evaluation Templates**

   - Save common evaluation patterns for reuse
   - Pre-filled scores and comments for standard feedback

2. **Batch Evaluations**

   - Evaluate multiple projects with similar criteria simultaneously
   - Copy evaluations between similar projects with minor adjustments

3. **Peer Evaluations**

   - Allow students to evaluate each other's work
   - Anonymous feedback collection

4. **Integration with Feedback System**
   - Connect evaluations to a feedback delivery system
   - Automated notifications when evaluations are complete
