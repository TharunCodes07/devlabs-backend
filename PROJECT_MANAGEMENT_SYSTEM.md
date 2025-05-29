# Project Management System - Simplified Teams & Projects

This document outlines the simplified project management system that has been implemented in the college management platform. The system supports streamlined team-based project management with a focus on simplicity and essential functionality.

## Overview

The simplified project management system consists of five main entities:

1. **Teams** - Democratic groups of students that collaborate on projects
2. **Projects** - Work assignments created by teams and assigned to courses
3. **Reviews** - Evaluations of projects by instructors
4. **TeamMemberScores** - Individual scoring of team members by staff
5. **Rubrics** - Evaluation criteria and scoring frameworks

## Key Simplifications

### Teams

- **No Leader Hierarchy**: All team members have equal permissions
- **No Size Limits**: Teams can have any number of members
- **No Status Management**: Teams are either active or inactive
- **Immutable Membership**: Team members are set during creation and cannot be changed later
- **Create New for Changes**: If team composition needs to change, create a new team

### Projects

- **Simplified Status Workflow**: PROPOSED → ONGOING → COMPLETED
- **No Timeline Fields**: Removed start dates, deadlines, and expected completion dates
- **No Documentation URLs**: Removed repository, documentation, and live URL fields
- **No Progress Tracking**: Removed percentage-based progress monitoring
- **No Priority Levels**: All projects have equal priority
- **Auto-completion**: Projects automatically complete when semester becomes inactive
- **Auto-start**: Projects automatically start when approved (no separate approval step)

## Entity Relationships

```
User (Student) ←→ Team ←→ Project ←→ Review ←→ TeamMemberScore
                    ↓        ↓           ↓
                 Course   RubricTemplate  Rubric
```

## Features

### Team Management

- **Simple Team Creation**: Students can create teams with initial members during creation
- **Fixed Membership**: Team membership is immutable after creation
- **No Membership Changes**: To change team composition, create a new team
- **Course Integration**: Teams work on projects for specific courses

### Project Management

- **Streamlined Workflow**: Clear progression from proposal to completion
- **Course Integration**: Projects are linked to specific courses
- **Automatic Completion**: Projects complete when semesters end
- **Basic Information**: Title, description, and objectives only
- **Team-based Creation**: Any team member can create projects

### Review System

- **Exam-like Scheduling**: Reviews have simple start and end dates, like exams
- **No Status Management**: Reviews don't have complex status workflows
- **Flexible Scheduling**: Start dates, end dates, presentation slots
- **Attendance Tracking**: Monitor team member participation
- **Comprehensive Feedback**: Strengths, improvements, next steps
- **Simple Management**: Create, update, complete reviews without status complexity

### Rubric Framework

- **Template System**: Reusable rubric templates for instructors
- **Public/Private Templates**: Share templates across faculty
- **Flexible Criteria Types**: Scale, Percentage, Boolean, Text feedback
- **Weighted Scoring**: Different importance levels for criteria
- **Custom Rubrics**: Create ad-hoc evaluation criteria

## API Endpoints

### Teams (`/teams`)

- `POST /teams` - Create a new team with initial members
- `GET /teams/user/{userId}` - Get teams for user
- `GET /teams/search` - Search teams by name

### Projects (`/projects`)

- `POST /projects` - Create a new project
- `PUT /projects/{id}` - Update project basic information
- `PUT /projects/{id}/approve` - Approve and start project (instructors)
- `PUT /projects/{id}/reject` - Reject project (instructors)
- `PUT /projects/{id}/complete` - Complete project
- `POST /projects/auto-complete` - Auto-complete projects for inactive semesters
- `GET /projects/team/{teamId}` - Get projects by team
- `GET /projects/course/{courseId}` - Get projects by course
- `GET /projects/pending-approval` - Get projects needing approval
- `GET /projects/ongoing` - Get ongoing projects

### Reviews (`/reviews`)

- `POST /reviews` - Create a new review
- `PUT /reviews/{id}` - Update review
- `PUT /reviews/{id}/complete` - Complete review
- `PUT /reviews/{id}/attendance` - Mark attendance
- `GET /reviews/project/{projectId}` - Get reviews for project
- `GET /reviews/reviewer` - Get reviews for current user

### Individual Team Member Scoring (`/reviews/{reviewId}/team-members`)

- `POST /{teamMemberId}/score` - Score individual team member
- `PUT /{teamMemberId}/score` - Update team member score
- `GET /scores` - Get all team member scores for review
- `GET /{teamMemberId}/score` - Get specific team member score
- `DELETE /{teamMemberId}/score` - Delete team member score
- `GET /team-members/{teamMemberId}/scores/history` - Get score history for member
- `GET /team-members/{teamMemberId}/scores/average` - Get average score for member

### Rubrics (`/rubrics`)

- `POST /rubrics/templates` - Create rubric template
- `PUT /rubrics/templates/{id}` - Update template
- `GET /rubrics/templates/available` - Get available templates
- `POST /rubrics/review/{reviewId}/from-template/{templateId}` - Create rubric from template
- `POST /rubrics/review/{reviewId}/custom` - Create custom rubric
- `PUT /rubrics/items/{itemId}/score` - Score rubric item

## Business Rules & Simplified Logic

### Team Management

1. **Democratic Structure**: All team members have equal permissions to manage the team
2. **No Size Limits**: Teams can have any number of members
3. **Course Enrollment**: Team members must be enrolled in the course to create projects
4. **Immutable Membership**: Team membership cannot be changed after creation
5. **Simple Structure**: No leader roles, status tracking, or complex hierarchies

### Project Management

1. **Team-Course Validation**: Projects can only be created if team has members enrolled in the course
2. **Edit Restrictions**: Only proposed and rejected projects can be edited
3. **Equal Rights**: Any team member can create and update projects
4. **Instructor Approval**: Only course instructors can approve and start projects
5. **Simplified Status Workflow**: PROPOSED → ONGOING → COMPLETED
6. **Auto-completion**: Projects automatically complete when semester becomes inactive
7. **Basic Information Only**: Title, description, and objectives
8. **Auto-start**: Projects automatically start when approved by instructors

### Review System

1. **Instructor-Only Creation**: Only course instructors can create reviews
2. **Review Ownership**: Only assigned reviewer can modify review
3. **Attendance Validation**: Only team members can be marked for attendance
4. **Simple Scheduling**: Reviews have start and end dates like exams
5. **Date-Based Operation**: Reviews operate within their scheduled time windows
6. **Manual Completion**: Instructors complete reviews when evaluation is finished
7. **No Status Workflows**: Simple creation, active period, and completion cycle

### Rubric System

1. **Template Ownership**: Only template creators can modify their templates
2. **Public Templates**: Public templates are available to all instructors
3. **Review-Rubric Linking**: Each review can have only one rubric
4. **Score Validation**: Scores must be within defined min/max bounds
5. **Auto-calculation**: Review scores automatically update when rubric items are scored

## Security Considerations

1. **Role-based Access**: Different permissions for students, faculty, and admin
2. **Ownership Validation**: Users can only modify entities they own or have permission to access
3. **Course Authorization**: Operations validated against course enrollment/instruction
4. **Team Membership**: Actions restricted to team members
5. **Review Authorization**: Only assigned reviewers can evaluate projects

## Database Schema

The system uses JPA/Hibernate entities with proper relationships:

- One-to-Many and Many-to-Many relationships
- Cascade operations for dependent entities
- Lazy loading for performance optimization
- UUID primary keys for all entities
- Timestamp tracking for audit trails

## Project Status Workflow

```
PROPOSED → ONGOING → COMPLETED
    ↓          ↓
 REJECTED   (Auto-complete when semester ends)
```

### Status Transitions

- **PROPOSED**: Initial state when project is created
- **ONGOING**: Instructor approves the project proposal (automatically starts)
- **REJECTED**: Instructor rejects the project (can be re-proposed)
- **COMPLETED**: Project is finished (manual or automatic)

## Auto-completion Logic

Projects in ONGOING status are automatically completed when:

- The semester associated with the project's course becomes inactive (`isActive = false`)
- This ensures projects don't remain open indefinitely

## Review Workflow - Exam-like System

The review system operates like an exam with simple start and end dates:

```
Create Review → Active Period → Complete
     ↓              ↓              ↓
 Set Dates     (Between start    Manual
              and end dates)   Completion
```

### Simple Date-Based Operation

Reviews work within defined time windows:

- **Before Start**: Review is not yet active, can be modified
- **Active Period**: Review is ongoing (between `startDate` and `endDate`)
- **After End**: Review period has concluded, can be completed

### Date-Based Logic

The system uses simple date comparisons:

1. **Start Date**: When the review period begins
2. **End Date**: When the review period concludes
3. **Current Time**: Determines if review is active, pending, or concluded
4. **Manual Completion**: Instructors can complete reviews at any time

### Key Features

- **Simple Scheduling**: Clear start and end times like exam periods
- **No Status Complexity**: No automatic status transitions or complex workflows
- **Flexible Timing**: Instructors control when reviews begin and end
- **Date-Based Queries**: Easy filtering by time periods
- **Manual Control**: Complete discretion over review completion timing

## Usage Examples

### Creating a Team

```json
POST /teams
{
  "name": "Alpha Development Team",
  "description": "Full-stack development team focused on web applications",
  "memberIds": ["member1-uuid", "member2-uuid", "member3-uuid"]
}
```

### Creating a Project

```json
POST /projects
{
  "title": "E-commerce Platform",
  "description": "Building a modern e-commerce solution with React and Spring Boot",
  "objectives": "Learn full-stack development, implement secure payment processing",
  "teamId": "team-uuid",
  "courseId": "course-uuid"
}
```

### Approving a Project

```http
PUT /projects/{projectId}/approve
Authorization: Bearer {instructor-token}
```

_Note: Projects automatically start when approved - no separate start action needed._

### Creating a Review

```json
POST /reviews
{
  "title": "Midterm Project Review",
  "description": "Comprehensive evaluation of project progress and team performance",
  "startDate": "2024-03-15T09:00:00",
  "endDate": "2024-03-15T17:00:00",
  "projectId": "project-uuid",
  "reviewerId": "instructor-uuid"
}
```

### Updating a Review

```json
PUT /reviews/{reviewId}
{
  "title": "Updated Project Review",
  "description": "Modified evaluation criteria and extended time window",
  "startDate": "2024-03-15T08:00:00",
  "endDate": "2024-03-15T18:00:00"
}
```

This simplified system maintains essential functionality while reducing complexity and administrative overhead.

## Individual Team Member Scoring System

1. **Individual Evaluation**: Staff can score each team member separately within a review
2. **Detailed Feedback**: Each score includes individual feedback, strengths, and improvement areas
3. **Flexible Scoring**: Supports numerical scores, percentages, and qualitative feedback
4. **Score History**: Maintains complete scoring history for each team member
5. **Average Calculation**: Automatic calculation of average scores across all reviews
6. **Review Integration**: Individual scores are linked to the main project review

#### Individual Score Components

- **Individual Score**: Numerical score earned by the team member
- **Max Possible Score**: Maximum points available for the evaluation
- **Percentage Score**: Calculated automatically (individual/max \* 100)
- **Individual Feedback**: Specific feedback for the team member
- **Strengths**: Areas where the team member excelled
- **Improvements**: Areas for development and growth
