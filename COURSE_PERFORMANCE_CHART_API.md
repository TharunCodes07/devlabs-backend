# Course Performance Chart API

## Overview

This API endpoint provides performance chart data for students, showing their scores across different reviews in a specific course. It's designed to create performance visualization charts similar to quiz progression charts.

## Endpoint

### Get Student Course Performance Chart

```
GET /api/course/student/{studentId}/course/{courseId}/performance-chart
```

## Parameters

- `studentId` (UUID): The unique identifier of the student
- `courseId` (UUID): The unique identifier of the course

## Response Format

The API returns an array of review performance data points, similar to your friend's quiz platform:

```json
[
  {
    "reviewId": "af3f839c-c3de-4b89-b9a3-b86f7358d600",
    "reviewName": "Mid-term Project Review",
    "startDate": "2025-01-15",
    "endDate": "2025-01-20",
    "status": "completed",
    "showResult": true,
    "score": 85.5,
    "totalScore": 100.0,
    "scorePercentage": 85.5,
    "courseName": "Software Engineering",
    "courseCode": "CS401"
  },
  {
    "reviewId": "e60d655e-0565-46cb-84a0-4b5473290d36",
    "reviewName": "Final Project Review",
    "startDate": "2025-02-15",
    "endDate": "2025-02-20",
    "status": "completed",
    "showResult": true,
    "score": 92.0,
    "totalScore": 100.0,
    "scorePercentage": 92.0,
    "courseName": "Software Engineering",
    "courseCode": "CS401"
  },
  {
    "reviewId": "b70d655e-0565-46cb-84a0-4b5473290d37",
    "reviewName": "Viva Voce",
    "startDate": "2025-03-01",
    "endDate": "2025-03-05",
    "status": "missed",
    "showResult": false,
    "score": null,
    "totalScore": null,
    "scorePercentage": null,
    "courseName": "Software Engineering",
    "courseCode": "CS401"
  }
]
```

## Response Fields

### Required Fields

- `reviewId`: Unique identifier for the review
- `reviewName`: Name of the review/evaluation
- `startDate`: Review start date (YYYY-MM-DD format)
- `endDate`: Review end date (YYYY-MM-DD format)
- `status`: Review status ("completed", "missed", "ongoing", "upcoming")
- `showResult`: Boolean indicating if results are published
- `courseName`: Name of the course
- `courseCode`: Course code

### Optional Fields (null for missed/upcoming reviews)

- `score`: Student's actual score in the review
- `totalScore`: Maximum possible score for the review
- `scorePercentage`: Score as a percentage (score/totalScore \* 100)

## Status Values

- `completed`: Student has submitted and scores are recorded
- `missed`: Review period ended but student has no scores
- `ongoing`: Review is currently active
- `upcoming`: Review hasn't started yet

## Usage Example

### Request

```bash
curl -X GET "http://localhost:8090/api/course/student/74161a18-dfec-4e89-9b30-02adb4f5d996/course/af3f839c-c3de-4b89-b9a3-b86f7358d600/performance-chart"
```

### Chart Implementation

You can use this data to create a performance chart with:

- **X-axis**: Review names (reviewName) or dates (startDate)
- **Y-axis**: Score percentage (scorePercentage) from 0% to 100%
- **Data points**: Each review represents a point on the chart
- **Status indicators**: Use different colors/styles for different status values

### Chart Data Processing

```javascript
// Example JavaScript processing
const chartData = apiResponse.map((review) => ({
  x: review.reviewName,
  y: review.scorePercentage,
  status: review.status,
  date: review.startDate,
}));

// Filter only completed reviews for the chart
const completedReviews = chartData.filter(
  (point) => point.status === "completed"
);
```

## Error Responses

### 404 Not Found

```json
{
  "error": "Student with id [studentId] not found"
}
```

```json
{
  "error": "Course with id [courseId] not found"
}
```

### 400 Bad Request

```json
{
  "error": "User is not a student"
}
```

```json
{
  "error": "Student is not enrolled in this course"
}
```

### 500 Internal Server Error

```json
{
  "error": "Failed to retrieve performance chart data: [error details]"
}
```

## Prerequisites

1. Student must exist in the system
2. Course must exist in the system
3. Student must be enrolled in the course (either directly or through batch enrollment)
4. Reviews must be associated with the course

## Notes

- Results are sorted by review start date (chronological order)
- Only reviews where the student has some involvement are included
- The `showResult` field controls whether scores should be displayed to the student
- Missing or null scores indicate reviews that weren't completed or are upcoming
