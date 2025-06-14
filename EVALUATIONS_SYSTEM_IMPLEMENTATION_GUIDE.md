# Evaluations System Implementation Guide

## Overview

The Evaluations System allows faculty members, administrators, and managers to review and score projects across different courses. This document provides detailed specifications for the frontend implementation of the evaluations features.

## Key Concepts

- **Review**: A formal evaluation period with start and end dates, associated with specific rubrics
- **Project**: A student team project that can be associated with multiple courses
- **Course**: An academic course with instructors and students
- **Individual Score**: An evaluation score given to an individual student for a specific criterion
- **Course-specific Evaluation**: Evaluations that are specific to how a project performs in the context of a particular course

## User Roles and Access

- **Faculty**: Can only evaluate projects in courses they teach
- **Admin/Manager**: Can evaluate any project across all courses and publish/unpublish reviews
- **Students**: Can only view their own scores for published reviews

### Automatic Role Detection

The system automatically determines the user's role and permissions based on the provided user ID:

1. The user ID is included in each request body as a field (`userId`)
2. The backend retrieves the user's role (ADMIN, MANAGER, FACULTY, or STUDENT)
3. For faculty users, the system also identifies which courses they teach
4. Based on this information, the system enforces appropriate access controls
5. No explicit role information needs to be passed in API requests

## Publication Workflow

Reviews follow a publication workflow:

1. **Draft State (Default)**: Only faculty, admins, and managers can see results
2. **Published State**: Students can see their own scores only

Only admins and managers can publish or unpublish reviews.

### Publication API Endpoints

**Get Publication Status:**

```
GET /api/review/{reviewId}/publication
```

Request Body:

```json
{
  "userId": "UUID"
}
```

Response:

```json
{
  "reviewId": "UUID",
  "reviewName": "String",
  "isPublished": "Boolean",
  "publishDate": "YYYY-MM-DD", // if published
  "canPublish": "Boolean" // true for admin/manager
}
```

**Publish Review (Admin/Manager only):**

```
POST /api/review/{reviewId}/publish
```

Request Body:

```json
{
  "userId": "UUID"
}
```

**Unpublish Review (Admin/Manager only):**

```
POST /api/review/{reviewId}/unpublish
```

Request Body:

```json
{
  "userId": "UUID"
}
```

The system automatically checks the user's role from the userId in the request body when handling publication requests.

## Main Features

### 1. Available Evaluations List

This page shows evaluations available to the current user.

#### API Endpoint

```
POST /api/individualScore/evaluations/available
```

#### Request Body

```json
{
  "userId": "UUID",
  "semester": "UUID" (optional)
}
```

The system will automatically determine:

- For faculty: Only evaluations for courses they teach
- For admin/manager: All evaluations across courses
- For students: Only their own evaluations in published reviews

#### Response

```json
{
  "evaluations": [
    {
      "reviewId": "UUID",
      "reviewName": "String",
      "projectId": "UUID",
      "projectTitle": "String",
      "courseId": "UUID",
      "courseName": "String",
      "teamName": "String",
      "startDate": "YYYY-MM-DD",
      "endDate": "YYYY-MM-DD",
      "hasExistingEvaluation": "Boolean"
    }
  ],
  "totalCount": "Number"
}
```

#### UI Components

- Filter by semester (optional)
- Sortable columns (by reviewName, projectTitle, courseName, startDate, endDate)
- Status indicator showing if evaluation has been completed
- Buttons to "Start Evaluation" or "View/Edit Evaluation"

### 2. Evaluation Form

This page allows users to submit scores for individual students in a project team.

#### API Endpoints

**Get Course Evaluation Data:**

```
GET /api/individualScore/review/{reviewId}/project/{projectId}/course/{courseId}/data
```

Request Body:

```json
{
  "userId": "UUID"
}
```

This returns the `CourseEvaluationData` object containing team members, criteria, and any existing scores.

The system will automatically:

- Verify the user has access to the requested course
- For faculty: Only allow access to courses they teach
- For admin/manager: Allow access to all courses
- For students: Only allow access to their own evaluations in published reviews

**Submit Course-specific Scores:**

```
POST /api/individualScore/course
```

The system will use the authenticated user's ID from the request body to:

- Validate permissions based on user role
- Check course teaching assignments for faculty users
- Record the evaluator ID with the submitted scores

#### Request Body for Submission

```json
{
  "userId": "UUID",
  "reviewId": "UUID",
  "projectId": "UUID",
  "courseId": "UUID",
  "scores": [
    {
      "participantId": "UUID",
      "criterionScores": [
        {
          "criterionId": "UUID",
          "score": "Number",
          "comment": "String" (optional)
        }
      ]
    }
  ]
}
```

#### UI Components

- Tabbed interface to switch between team members
- Grid with criteria rows and score input columns
- Comment fields for each criterion
- Total score calculation showing running total and percentage
- Save/Submit buttons
- Cancel button to return to evaluations list

### 3. Evaluation Summary

This page shows a summary of all evaluations for a project across different courses.

#### API Endpoint

```
GET /api/individualScore/review/{reviewId}/project/{projectId}/summary
```

Request Body:

```json
{
  "userId": "UUID"
}
```

#### Response

```json
{
  "reviewId": "UUID",
  "reviewName": "String",
  "projectId": "UUID",
  "projectTitle": "String",
  "teamName": "String",
  "courseEvaluations": [
    {
      "courseId": "UUID",
      "courseName": "String",
      "instructors": ["String"],
      "hasEvaluation": "Boolean",
      "evaluationCount": "Number"
    }
  ]
}
```

#### UI Components

- Project details header
- List of courses with evaluation status
- Links to view detailed evaluations for each course
- Summary statistics (average scores, completion status)

### 4. Individual Student Evaluation

This page shows evaluation details for a specific student.

#### API Endpoint

```
GET /api/individualScore/review/{reviewId}/project/{projectId}/course/{courseId}/participant/{participantId}
```

Request Body:

```json
{
  "userId": "UUID"
}
```

#### Response

```json
{
  "participantId": "UUID",
  "participantName": "String",
  "criterionScores": [
    {
      "criterionId": "UUID",
      "criterionName": "String",
      "maxScore": "Number",
      "score": "Number",
      "comment": "String" (optional)
    }
  ],
  "totalScore": "Number",
  "maxPossibleScore": "Number",
  "percentage": "Number"
}
```

#### UI Components

- Student information header
- Table of criteria, scores, and comments
- Visual indicators of performance (progress bars, color coding)
- Summary section with total score and percentage

## Navigation Flow

1. User accesses "Evaluations" section in the main navigation
2. User views list of available evaluations
3. User selects a project/course to evaluate
4. User completes evaluation form for each team member
5. User submits evaluation
6. User is returned to evaluations list with updated status

## States and Transitions

### Evaluation States

- **Not Started**: No scores have been submitted yet
- **In Progress**: Some scores have been submitted but not for all team members
- **Complete**: Scores have been submitted for all team members

### Time-based States

- **Upcoming**: Review start date is in the future
- **Active**: Current date is between review start and end dates
- **Expired**: Review end date has passed

## Error Handling

- Display appropriate error messages for:
  - Unauthorized access attempts
  - Missing required fields
  - Invalid score values (outside allowed range)
  - Server errors
  - Network issues

## UI/UX Guidelines

- Use consistent color coding:
  - Green for completed evaluations
  - Yellow/Orange for in-progress evaluations
  - Blue for upcoming evaluations
  - Gray for expired evaluations
- Provide clear feedback after form submission
- Include help text and tooltips for complex fields
- Implement responsive design for mobile/tablet compatibility
- Add confirmation dialogs for destructive actions (like deleting an evaluation)

## Technical Notes

### Data Refreshing

- Implement automatic saving of form data to prevent loss of work
- Add "last updated" timestamps to show evaluation recency

### Performance Considerations

- Implement pagination for lists with many evaluations
- Use client-side caching for frequently accessed data
- Lazy load team member data when switching tabs

## Reviews Project Integration

When viewing a project's details, you can fetch associated reviews using:

```
GET /projects/{projectId}/reviews
```

Request Body:

```json
{
  "userId": "UUID"
}
```

This returns:

```json
{
  "hasReview": "Boolean",
  "assignmentType": "String",
  "liveReviews": ["ReviewResponse"],
  "upcomingReviews": ["ReviewResponse"],
  "completedReviews": ["ReviewResponse"]
}
```

Where `assignmentType` is one of:

- "DIRECT": Project directly assigned to review
- "COURSE": Project's course assigned to review
- "BATCH": Project's team members' batch assigned to review
- "SEMESTER": Assigned through semester relationship
- "NONE": No reviews found

### Active Projects

To get a list of active projects that can be reviewed:

```
GET /projects/active
```

Request Body:

```json
{
  "userId": "UUID",
  "page": 0, // optional, zero-based, default: 0
  "size": 10 // optional, default: 10
}
```

Response:

```json
{
  "content": [
    {
      "id": "UUID",
      "title": "String",
      "description": "String",
      "status": "String",
      "team": { "id": "UUID", "name": "String" },
      "courses": [{ "id": "UUID", "name": "String", "code": "String" }]
    }
  ],
  "totalElements": "Number",
  "totalPages": "Number",
  "number": "Number",
  "size": "Number"
}
```

## API Details

### Course Evaluation Data

When working with course-specific evaluations, the system provides detailed data through this endpoint:

```
GET /api/individualScore/review/{reviewId}/project/{projectId}/course/{courseId}/data
```

Request Body:

```json
{
  "userId": "UUID"
}
```

Response:

```json
{
  "courseId": "UUID",
  "courseName": "String",
  "projectId": "UUID",
  "reviewId": "UUID",
  "teamMembers": [
    {
      "id": "UUID",
      "name": "String",
      "email": "String",
      "role": "String"
    }
  ],
  "criteria": [
    {
      "id": "UUID",
      "name": "String",
      "description": "String",
      "maxScore": "Number",
      "courseSpecific": "Boolean"
    }
  ],
  "existingScores": [
    {
      "participantId": "UUID",
      "criterionScores": [
        {
          "criterionId": "UUID",
          "score": "Number",
          "comment": "String"
        }
      ]
    }
  ],
  "isPublished": "Boolean"
}
```

Use this data to populate the evaluation form for a specific course, showing team members, criteria, and any existing scores.

### Common Response Structure

All API responses follow this general pattern:

- Success responses: HTTP 200/201 with data
- Error responses: Appropriate HTTP status code with error object:
  ```json
  {
    "error": "Error message"
  }
  ```

### Authentication Requirements

All API calls require the user ID to be included in the request body:

```json
{
  "userId": "UUID"
  // other request parameters
}
```

This user ID is used to:

1. Determine the user's role (ADMIN, MANAGER, FACULTY, or STUDENT)
2. Filter available evaluations based on role and course assignments
3. Enforce access control restrictions
4. Track who submitted or modified evaluations

## Implementation Timeline Recommendations

1. **Phase 1**: Implement Available Evaluations List and basic navigation
2. **Phase 2**: Implement Evaluation Form with score submission
3. **Phase 3**: Implement Evaluation Summary views
4. **Phase 4**: Add advanced features (filtering, sorting, statistics)

## Testing Checklist

- Verify form validation for all input fields
- Test accessibility with screen readers
- Confirm mobile responsiveness
- Verify proper handling of concurrent edits
- Test with various screen sizes and resolutions
- Verify that faculty can only access courses they teach
- Test navigation paths between all related screens

---

## Appendix: Data Structures

### Review

```typescript
interface Review {
  id: string;
  name: string;
  startDate: string; // YYYY-MM-DD
  endDate: string; // YYYY-MM-DD
  rubrics: Rubrics;
  courses: Course[];
  projects: Project[];
}
```

### Criterion

```typescript
interface Criterion {
  id: string;
  name: string;
  description: string;
  maxScore: number;
  isCommon: boolean;
}
```

### ParticipantScore

```typescript
interface ParticipantScore {
  participantId: string;
  criterionScores: CriterionScore[];
}

interface CriterionScore {
  criterionId: string;
  score: number;
  comment?: string;
}
```

### CourseEvaluationInfo

```typescript
interface CourseEvaluationInfo {
  reviewId: string;
  reviewName: string;
  projectId: string;
  projectTitle: string;
  courseId: string;
  courseName: string;
  teamName: string;
  startDate: string; // YYYY-MM-DD
  endDate: string; // YYYY-MM-DD
  hasExistingEvaluation: boolean;
}
```

### CourseEvaluationData

```typescript
interface CourseEvaluationData {
  courseId: string;
  courseName: string;
  projectId: string;
  reviewId: string;
  teamMembers: {
    id: string;
    name: string;
    email: string;
    role: string;
  }[];
  criteria: {
    id: string;
    name: string;
    description: string;
    maxScore: number;
    courseSpecific: boolean;
  }[];
  existingScores?: {
    participantId: string;
    criterionScores: {
      criterionId: string;
      score: number;
      comment?: string;
    }[];
  }[];
  isPublished: boolean;
}
```

### Review Publication

```typescript
interface ReviewPublicationStatus {
  reviewId: string;
  reviewName: string;
  isPublished: boolean;
  publishDate?: string; // YYYY-MM-DD
  publishedBy?: {
    id: string;
    name: string;
    role: string;
  };
}

// Response when checking if a review is published
interface ReviewPublicationResponse {
  reviewId: string;
  reviewName: string;
  isPublished: boolean;
  publishDate?: string; // YYYY-MM-DD
  canPublish: boolean; // True for admin/manager roles only
}
```
