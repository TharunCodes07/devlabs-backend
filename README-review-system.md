# DevLabs Review System Documentation

## Overview

The DevLabs Review System provides a comprehensive solution for creating and managing project reviews with individual scoring of team members. This document describes the available API endpoints and their functionality.

## Authentication and Authorization

All API endpoints require authentication. The user ID must be provided in the `X-User-Id` header for endpoints that require user authorization.

Role-based access control is implemented with the following roles:

- **Admin/Manager**: Full access to all features
- **Faculty**: Access to their courses, projects, and related reviews
- **Student**: Access to their own projects and reviews

## Review Endpoints

### Create a Review

- **Endpoint**: `POST /api/review`
- **Description**: Creates a new review
- **Required Role**: Admin, Manager, or Faculty
- **Request Body**: `CreateReviewRequest`
- **Response**: Created review details

### Update a Review

- **Endpoint**: `PUT /api/review/{reviewId}`
- **Description**: Updates an existing review
- **Required Role**: Admin, Manager, or Faculty (with access to the review)
- **Request Body**: `UpdateReviewRequest`
- **Response**: Updated review details

### Get a Review by ID

- **Endpoint**: `GET /api/review/{reviewId}`
- **Description**: Retrieves a specific review by ID
- **Response**: Review details

### Get All Reviews

- **Endpoint**: `GET /api/review`
- **Description**: Retrieves all reviews with pagination
- **Query Parameters**:
  - `page`: Page number (default: 0)
  - `size`: Page size (default: 10)
  - `sortBy`: Field to sort by (default: startDate)
  - `sortOrder`: Sort direction (asc/desc, default: desc)
- **Response**: Paginated list of reviews

### Get Reviews for User

- **Endpoint**: `GET /api/review/user`
- **Description**: Retrieves reviews based on user role (all for admin/manager, course-related for faculty, project-related for students)
- **Required Header**: `X-User-Id`
- **Query Parameters**:
  - `page`: Page number (default: 0)
  - `size`: Page size (default: 10)
- **Response**: Paginated list of reviews

### Get All Reviews for User (Non-paginated)

- **Endpoint**: `GET /api/review/allForUser`
- **Description**: Retrieves all reviews for the user without pagination
- **Required Header**: `X-User-Id`
- **Response**: List of reviews

### Get Recently Completed Reviews

- **Endpoint**: `GET /api/review/recentlyCompleted`
- **Description**: Retrieves reviews that have ended (endDate is before current date)
- **Query Parameters**:
  - `page`: Page number (default: 0)
  - `size`: Page size (default: 10)
- **Response**: Paginated list of recently completed reviews

### Get Live Reviews

- **Endpoint**: `GET /api/review/live`
- **Description**: Retrieves currently active reviews (current date is between startDate and endDate)
- **Query Parameters**:
  - `page`: Page number (default: 0)
  - `size`: Page size (default: 10)
- **Response**: Paginated list of live reviews

### Get Upcoming Reviews

- **Endpoint**: `GET /api/review/upcoming`
- **Description**: Retrieves future reviews (startDate is after current date)
- **Query Parameters**:
  - `page`: Page number (default: 0)
  - `size`: Page size (default: 10)
- **Response**: Paginated list of upcoming reviews

### Get Reviews for Project

- **Endpoint**: `GET /api/review/project/{projectId}`
- **Description**: Retrieves all reviews associated with a specific project
- **Response**: List of reviews

### Get Active Reviews for Course

- **Endpoint**: `GET /api/review/course/{courseId}/active`
- **Description**: Retrieves active reviews for a specific course
- **Response**: List of active reviews

### Delete a Review

- **Endpoint**: `DELETE /api/review/{reviewId}`
- **Description**: Deletes a review
- **Required Role**: Admin, Manager, or Faculty (with access to the review)
- **Required Header**: `X-User-Id`
- **Response**: Success status

### Search Reviews

- **Endpoint**: `GET /api/review/search`
- **Description**: Searches for reviews with various filters
- **Query Parameters**:
  - `name`: Filter by review name (optional)
  - `courseId`: Filter by course ID (optional)
  - `status`: Filter by status (live, completed, upcoming) (optional)
  - `page`: Page number (default: 0)
  - `size`: Page size (default: 10)
  - `sortBy`: Field to sort by (default: startDate)
  - `sortOrder`: Sort direction (asc/desc, default: desc)
- **Response**: Paginated list of filtered reviews

## Individual Score Endpoints

### Submit Scores

- **Endpoint**: `POST /api/individualScore`
- **Description**: Submits scores for participants in a review
- **Required Role**: Admin, Manager, or Faculty (with access to the project)
- **Required Header**: `X-User-Id`
- **Request Body**: `SubmitScoreRequest`
- **Response**: Success status and count of saved scores

### Get Score by ID

- **Endpoint**: `GET /api/individualScore/{scoreId}`
- **Description**: Retrieves a specific individual score by ID
- **Response**: Score details

### Get Scores for Participant

- **Endpoint**: `GET /api/individualScore/review/{reviewId}/project/{projectId}/participant/{participantId}`
- **Description**: Retrieves scores for a specific participant in a review and project
- **Response**: Participant's scores summary

### Get Scores for Project

- **Endpoint**: `GET /api/individualScore/review/{reviewId}/project/{projectId}`
- **Description**: Retrieves scores for all participants in a review and project
- **Response**: List of participants' scores summaries

### Delete Scores for Participant

- **Endpoint**: `DELETE /api/individualScore/review/{reviewId}/project/{projectId}/participant/{participantId}`
- **Description**: Deletes all scores for a specific participant in a review and project
- **Required Role**: Admin, Manager, or Faculty (with access to the project)
- **Required Header**: `X-User-Id`
- **Response**: Success status

## Rubrics Endpoints

### Get All Rubrics

- **Endpoint**: `GET /api/rubrics`
- **Description**: Retrieves all rubrics with pagination
- **Query Parameters**:
  - `page`: Page number (default: 0)
  - `size`: Page size (default: 10)
  - `sortBy`: Field to sort by (default: name)
  - `sortDir`: Sort direction (ASC/DESC, default: ASC)
- **Response**: Paginated list of rubrics

### Get Rubrics by ID

- **Endpoint**: `GET /api/rubrics/{id}`
- **Description**: Retrieves a specific rubrics by ID
- **Response**: Rubrics details

### Create Rubrics

- **Endpoint**: `POST /api/rubrics`
- **Description**: Creates a new rubrics
- **Required Role**: Admin or Manager
- **Required Header**: `X-User-Id`
- **Request Body**: `CreateRubricsRequest`
- **Response**: Created rubrics details

### Update Rubrics

- **Endpoint**: `PUT /api/rubrics/{id}`
- **Description**: Updates an existing rubrics
- **Required Role**: Admin or Manager
- **Required Header**: `X-User-Id`
- **Request Body**: `UpdateRubricsRequest`
- **Response**: Updated rubrics details

### Delete Rubrics

- **Endpoint**: `DELETE /api/rubrics/{id}`
- **Description**: Deletes a rubrics
- **Required Role**: Admin or Manager
- **Required Header**: `X-User-Id`
- **Response**: Success status

## Criterion Endpoints

### Get Criterion by ID

- **Endpoint**: `GET /api/criterion/{id}`
- **Description**: Retrieves a specific criterion by ID
- **Response**: Criterion details

### Get Criteria by Rubrics ID

- **Endpoint**: `GET /api/criterion/rubrics/{rubricsId}`
- **Description**: Retrieves all criteria for a specific rubrics
- **Response**: List of criteria

### Delete Criterion

- **Endpoint**: `DELETE /api/criterion/{id}`
- **Description**: Deletes a criterion
- **Required Role**: Admin or Manager
- **Required Header**: `X-User-Id`
- **Response**: Success status

## Data Models

### Review

- `id`: UUID
- `name`: String
- `startDate`: LocalDate
- `endDate`: LocalDate
- `courses`: List of associated courses
- `projects`: List of associated projects
- `rubrics`: Associated rubrics for evaluation

### IndividualScore

- `id`: UUID
- `participant`: User being scored
- `criterion`: Criterion being evaluated
- `score`: Double
- `comment`: String (optional)
- `review`: Associated review
- `project`: Associated project

### Rubrics

- `id`: UUID
- `name`: String
- `criteria`: List of criteria

### Criterion

- `id`: UUID
- `name`: String
- `description`: String
- `maxScore`: Float
- `isCommon`: Boolean
- `rubrics`: Associated rubrics

## Request and Response DTOs

### Review DTOs

- `CreateReviewRequest`: Data for creating a new review
- `UpdateReviewRequest`: Data for updating an existing review
- `ReviewResponse`: Review data returned by the API

### Individual Score DTOs

- `SubmitScoreRequest`: Data for submitting scores
- `IndividualScoreResponse`: Individual score data returned by the API
- `ParticipantScoresSummary`: Summary of scores for a participant

### Rubrics DTOs

- `CreateRubricsRequest`: Data for creating new rubrics
- `UpdateRubricsRequest`: Data for updating existing rubrics
- `RubricsResponse`: Rubrics data returned by the API

### Criterion DTOs

- `CriterionResponse`: Criterion data returned by the API
