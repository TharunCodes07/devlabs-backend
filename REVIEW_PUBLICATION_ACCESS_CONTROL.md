# Review Publication and Access Control Guide

## Overview

This document outlines the review publication workflow and access control rules for the Devlabs evaluation system. The system enforces specific permissions to ensure that evaluation results are only visible to the appropriate users at the appropriate time.

## Review Publication Workflow

### Key Concepts

- **Review Status**: Reviews can be in either "Draft" or "Published" state
- **Publication**: The act of making review results visible to students
- **Access Control**: Restrictions on who can see evaluation results

### Review States

1. **Draft State (Default)**
   - Only faculty, managers, and admins can see evaluation results
   - Students cannot access any evaluation data
   - Evaluations can be modified, added, or deleted

2. **Published State**
   - Students can see only their own individual scores
   - Faculty, managers, and admins can see all evaluation results
   - Evaluations become read-only (cannot be modified or deleted)

## User Roles and Permissions

### Admin/Manager
- Can create, edit, and delete reviews
- Can view all evaluations for all projects and students
- Can publish review results
- Can unpublish review results (reverting to draft state)
- Can view aggregated statistics for all projects

### Faculty
- Can create, edit, and delete evaluations for courses they teach
- Can view all evaluations for projects in their courses
- Cannot publish or unpublish review results (admin/manager only)
- Can view aggregated statistics for projects in their courses

### Students
- Can only view their own individual scores after review publication
- Cannot see scores of other students
- Cannot see evaluations in draft state
- Can view feedback and comments related to their own evaluations

## Publication API

### Publish Review

Publish a review, making results visible to students.

**Endpoint:**
```
PUT /api/reviews/{reviewId}/publish
```

**Request Headers:**
```
X-User-Id: UUID
```

**Response:**
```json
{
  "success": true,
  "message": "Review published successfully",
  "reviewId": "UUID",
  "reviewName": "String",
  "publishDate": "YYYY-MM-DD"
}
```

**Access:** Admin/Manager only

### Unpublish Review

Revert a review to draft state, hiding results from students.

**Endpoint:**
```
PUT /api/reviews/{reviewId}/unpublish
```

**Request Headers:**
```
X-User-Id: UUID
```

**Response:**
```json
{
  "success": true,
  "message": "Review unpublished successfully",
  "reviewId": "UUID",
  "reviewName": "String"
}
```

**Access:** Admin/Manager only

### Check Publication Status

Check if a review is published.

**Endpoint:**
```
GET /api/reviews/{reviewId}/publication-status
```

**Response:**
```json
{
  "reviewId": "UUID",
  "reviewName": "String",
  "isPublished": true,
  "publishDate": "YYYY-MM-DD",
  "publishedBy": {
    "id": "UUID",
    "name": "String"
  }
}
```

**Access:** Admin/Manager/Faculty

## Student Access API

### Get Student's Own Evaluations

Retrieve a student's own evaluations for a published review.

**Endpoint:**
```
GET /api/reviews/{reviewId}/my-evaluations
```

**Request Headers:**
```
X-User-Id: UUID
```

**Response:**
```json
{
  "reviewId": "UUID",
  "reviewName": "String",
  "isPublished": true,
  "publishDate": "YYYY-MM-DD",
  "projects": [
    {
      "projectId": "UUID",
      "projectTitle": "String",
      "teamName": "String",
      "courses": [
        {
          "courseId": "UUID",
          "courseName": "String",
          "evaluations": [
            {
              "criterionId": "UUID",
              "criterionName": "String",
              "maxScore": "Number",
              "score": "Number",
              "comment": "String"
            }
          ],
          "totalScore": "Number",
          "maxPossibleScore": "Number",
          "percentage": "Number"
        }
      ]
    }
  ]
}
```

**Access:** All authenticated users (each user only sees their own data)

## Implementation Rules

1. **Publication Check**:
   - All evaluation endpoints must check if the review is published before returning data to students
   - Admins, managers, and faculty bypass this check

2. **Data Filtering**:
   - When returning data to students, filter to show only their own scores
   - Include course and project context for proper organization

3. **Modification Protection**:
   - Block all modification attempts (PUT, POST, DELETE) for published reviews
   - Require unpublishing before allowing modifications

4. **Audit Trail**:
   - Log all publish/unpublish actions with timestamp and user information
   - Include publication status in review summary responses

## UI Implementation Guidelines

1. **Publication Controls**:
   - Add "Publish" and "Unpublish" buttons to review detail pages (admin/manager only)
   - Display publication status prominently on review pages

2. **Status Indicators**:
   - Use visual indicators (icons, colors) to show publication status
   - Display "Published" badge on published reviews

3. **Student View**:
   - Create dedicated "My Evaluations" page for students
   - Organize by review > project > course > criteria
   - Show clear messaging when no published evaluations exist

4. **Confirmation Dialogs**:
   - Require confirmation before publishing/unpublishing
   - Include warning about student visibility on publish
   - Include warning about data hiding on unpublish

## Testing Checklist

- Verify that only admins/managers can publish/unpublish reviews
- Confirm students can only see published reviews
- Verify students can only see their own evaluations
- Test that published reviews cannot be modified
- Verify appropriate error messages for unauthorized access attempts
- Test publication status display across the application
