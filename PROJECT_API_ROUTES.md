# Project API Routes Guide

## Overview

This document provides information about available API routes for accessing project data in the Devlabs backend system, including a note about the newly added `/projects/active` endpoint.

## Available Project Routes

### Project Listing Endpoints

1. **Active/Ongoing Projects**
   - `/projects/active` - Get active projects with pagination
   - `/projects/ongoing` - Get ongoing projects with pagination (functionally equivalent to `/active`)

2. **Project Filtering**
   - `/projects/team/{teamId}` - Get projects for a specific team
   - `/projects/course/{courseId}` - Get projects for a specific course
   - `/projects/user/{userId}` - Get projects for a specific user
   - `/projects/pending-approval` - Get projects that need approval

3. **Project Search**
   - `/projects/search?query={searchTerm}` - Search all projects
   - `/projects/team/{teamId}/search?query={searchTerm}` - Search projects within a team
   - `/projects/course/{courseId}/search?query={searchTerm}` - Search projects within a course

4. **Project Details**
   - `/projects/{projectId}` - Get detailed information about a specific project
   - `/projects/{projectId}/reviews` - Get reviews associated with a project (including publication status)

### Project Management Endpoints

1. **Create and Update**
   - `POST /projects` - Create a new project
   - `PUT /projects/{projectId}` - Update an existing project
   - `PUT /projects/{projectId}/courses` - Update the courses associated with a project

2. **Project Status Changes**
   - `PUT /projects/{projectId}/approve` - Approve a project
   - `PUT /projects/{projectId}/reject` - Reject a project
   - `PUT /projects/{projectId}/complete` - Mark a project as completed
   - `POST /projects/auto-complete` - Auto-complete projects for inactive courses

3. **Delete Projects**
   - `DELETE /projects/{projectId}` - Delete a project

## Common Query Parameters

Most listing endpoints support the following query parameters:

- `page`: Page number (zero-based, default: 0)
- `size`: Number of items per page (default: 10)
- `sortBy`: Field to sort by (default varies by endpoint)
- `sortOrder`: Sort direction, either "asc" or "desc" (default varies by endpoint)

## Note About Project Statuses

Projects can have the following statuses:
- `PROPOSED`: Newly created projects that haven't been approved yet
- `ONGOING`: Active projects that are currently in progress
- `COMPLETED`: Projects that have been finished
- `REJECTED`: Projects that were not approved

The `/projects/active` and `/projects/ongoing` endpoints both return projects with the `ONGOING` status.

## Additional API Endpoints

The system also has API endpoints at different base paths:

- `/api/project/active` - Get all active projects without pagination
- `/api/project/semester/{semesterId}/active` - Get active projects for a specific semester
- `/api/project/batch/{batchId}/active` - Get active projects for a specific batch

## Error Handling

All endpoints return appropriate HTTP status codes:
- 200 OK: Successful request
- 400 Bad Request: Invalid parameters
- 404 Not Found: Resource not found
- 500 Internal Server Error: Server-side error

Error responses have the following format:
```json
{
  "error": "Error message description"
}
```
