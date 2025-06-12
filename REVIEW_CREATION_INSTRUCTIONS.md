# Review Creation Instructions

## Overview

This document provides step-by-step instructions for creating reviews in the DevLabs Backend system.

## Database Setup (One-time fixes)

**Important**: If you encounter errors when creating reviews, you may need to apply these fixes:

### 1. Unique Constraint Error on `rubrics_id`

If you see an error about "duplicate key value violates unique constraint" on `rubrics_id`, run this SQL command:

```sql
ALTER TABLE reviews DROP CONSTRAINT IF EXISTS ukdxrhwpyvdl8a343bluojde5y5;
```

### 2. LazyInitializationException Error

If you see an error about "failed to lazily initialize a collection", this has been fixed in the code by ensuring rubrics criteria are loaded within the transaction. Make sure you're using the latest version of the code.

This allows multiple reviews to use the same rubrics, which is the intended behavior.

## Endpoint

**POST** `/api/review`

## Prerequisites

- You must have one of the following roles: `ADMIN`, `MANAGER`, or `FACULTY`
- For faculty users: You can only create reviews for courses where you are an instructor
- You need a valid `rubricsId` (rubrics must exist in the system)
- You need a valid `userId` (your user ID)

## Required Fields

- `name`: String - Name/title of the review
- `startDate`: LocalDate - When the review starts (format: "YYYY-MM-DD")
- `endDate`: LocalDate - When the review ends (format: "YYYY-MM-DD")
- `rubricsId`: UUID - ID of the rubrics to use for evaluation
- `userId`: UUID - ID of the user creating the review

## Optional Assignment Fields

You can assign the review to any combination of:

- `courseIds`: List<UUID> - Assign to specific courses
- `semesterIds`: List<UUID> - Assign to all courses in specific semesters
- `batchIds`: List<UUID> - Assign to all courses in specific batches
- `projectIds`: List<UUID> - Assign to specific projects directly
- `sections`: List<String> - Assign to specific section names

## Example Requests

### 1. Create a Review for Specific Courses

```bash
curl -X POST http://localhost:8080/api/review \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Mid-term Project Review",
    "startDate": "2025-06-15",
    "endDate": "2025-06-30",
    "rubricsId": "your-rubrics-id-here",
    "userId": "your-user-id-here",
    "courseIds": ["course-id-1", "course-id-2"]
  }'
```

### 2. Create a Review for Specific Projects

```bash
curl -X POST http://localhost:8080/api/review \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Final Project Review",
    "startDate": "2025-07-01",
    "endDate": "2025-07-15",
    "rubricsId": "your-rubrics-id-here",
    "userId": "your-user-id-here",
    "projectIds": ["project-id-1", "project-id-2"]
  }'
```

### 3. Create a Review for an Entire Batch

```bash
curl -X POST http://localhost:8080/api/review \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Batch Assessment Review",
    "startDate": "2025-06-20",
    "endDate": "2025-07-05",
    "rubricsId": "your-rubrics-id-here",
    "userId": "your-user-id-here",
    "batchIds": ["batch-id-here"]
  }'
```

### 4. Create a Review for Multiple Assignment Types

```bash
curl -X POST http://localhost:8080/api/review \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Comprehensive Review",
    "startDate": "2025-06-25",
    "endDate": "2025-07-10",
    "rubricsId": "your-rubrics-id-here",
    "userId": "your-user-id-here",
    "courseIds": ["course-id-1"],
    "projectIds": ["project-id-1"],
    "sections": ["Section A", "Section B"]
  }'
```

## Response

On successful creation, you'll receive a `201 Created` status with the review details:

```json
{
  "id": "generated-review-id",
  "name": "Mid-term Project Review",
  "startDate": "2025-06-15",
  "endDate": "2025-06-30",
  "courses": [...],
  "projects": [...],
  "rubricsInfo": {...}
}
```

## Error Responses

- `400 Bad Request` - Invalid request data or validation errors
- `403 Forbidden` - Insufficient permissions or trying to assign courses you don't instruct
- `404 Not Found` - Referenced entities (rubrics, courses, etc.) not found
- `500 Internal Server Error` - Server error

## Assignment Logic

When a review is created, it will be automatically detected by projects through these relationships:

1. **Direct Project Assignment**: Projects listed in `projectIds`
2. **Course-based Assignment**: Projects assigned to courses in `courseIds`
3. **Batch-based Assignment**: Projects assigned to courses that belong to batches in `batchIds`
4. **Semester-based Assignment**: Projects assigned to courses that belong to semesters in `semesterIds`

## Notes

- Reviews can be checked for any project using: `GET /projects/{projectId}/reviews`
- Only `ONGOING` and `PROPOSED` projects are included in reviews
- Faculty users have restricted access based on their course assignments
- Date validation ensures `endDate` is after `startDate`

## Testing Your Review

After creating a review, you can test if it's properly assigned to a project:

```bash
curl http://localhost:8080/projects/{your-project-id}/reviews
```

This should return `"hasReview": true` if the assignment was successful.
