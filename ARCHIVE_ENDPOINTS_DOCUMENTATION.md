# Archive Endpoints Documentation

## Overview

The archive endpoints provide access to completed projects with role-based filtering and pagination. These endpoints allow users to view and search through their historical project data based on their role in the system.

## Endpoints

### 1. Get Archived Projects

**Endpoint:** `GET /projects/user/{userId}/archive`

**Description:** Retrieves all completed projects for a specific user with role-based filtering and pagination.

**Parameters:**

- `userId` (path parameter): UUID of the user
- `page` (query parameter, optional): Page number (default: 0)
- `size` (query parameter, optional): Page size (default: 10)

**Role-based Logic:**

- **Students**: Returns only completed projects where they were team members
- **Faculty/Staff**: Returns completed projects from courses they have taught/handled
- **Admin/Manager**: Returns all completed projects in the system

**Example Request:**

```http
GET /projects/user/123e4567-e89b-12d3-a456-426614174000/archive?page=0&size=10
```

**Example Response:**

```json
{
  "content": [
    {
      "id": "project-uuid",
      "title": "Project Title",
      "description": "Project Description",
      "status": "COMPLETED",
      "team": {
        "id": "team-uuid",
        "name": "Team Name",
        "members": [...]
      },
      "courses": [
        {
          "id": "course-uuid",
          "title": "Course Title",
          "code": "CS101"
        }
      ],
      "createdAt": "2024-01-15T10:30:00Z",
      "updatedAt": "2024-05-20T14:45:00Z"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 10,
    "sort": {
      "sorted": true,
      "unsorted": false
    }
  },
  "totalElements": 25,
  "totalPages": 3,
  "last": false,
  "first": true,
  "numberOfElements": 10
}
```

### 2. Search Archived Projects

**Endpoint:** `GET /projects/user/{userId}/archive/search`

**Description:** Search through completed projects for a specific user with the same role-based filtering as the archive endpoint.

**Parameters:**

- `userId` (path parameter): UUID of the user
- `query` (query parameter): Search term to filter projects by title (case-insensitive)
- `page` (query parameter, optional): Page number (default: 0)
- `size` (query parameter, optional): Page size (default: 10)

**Example Request:**

```http
GET /projects/user/123e4567-e89b-12d3-a456-426614174000/archive/search?query=mobile&page=0&size=10
```

**Example Response:**
Same structure as the archive endpoint but filtered by the search query.

## Error Responses

### 400 Bad Request

```json
{
  "error": "Invalid page or size parameter"
}
```

### 404 Not Found

```json
{
  "error": "User not found"
}
```

### 500 Internal Server Error

```json
{
  "error": "Failed to get archived projects: [error message]"
}
```

## Implementation Details

### Role-Based Access Control

The endpoints automatically filter results based on the user's role:

1. **Students (`STUDENT` role):**

   - Only see projects where they were team members
   - Query: `SELECT p FROM Project p JOIN p.team.members m WHERE p.status = COMPLETED AND m = user`

2. **Faculty/Staff (`FACULTY` role):**

   - Only see projects from courses they have instructed
   - Query: `SELECT p FROM Project p JOIN p.courses.instructors i WHERE p.status = COMPLETED AND i = user`

3. **Admin/Manager (`ADMIN`, `MANAGER` roles):**
   - See all completed projects in the system
   - Query: `SELECT p FROM Project p WHERE p.status = COMPLETED`

### Pagination

Both endpoints return paginated results using Spring Boot's standard pagination structure:

- `content`: Array of project objects
- `pageable`: Pagination metadata
- `totalElements`: Total number of projects
- `totalPages`: Total number of pages
- `first`/`last`: Boolean flags for first/last page
- `numberOfElements`: Number of elements in current page

### Search Functionality

The search endpoint performs case-insensitive partial matching on project titles using SQL `LIKE` with wildcards.

## Frontend Integration Notes

1. **Authentication**: Ensure the user is authenticated and their role is properly determined before calling these endpoints.

2. **Pagination**: Use the pagination metadata to implement proper page navigation in the UI.

3. **Search**: Implement debounced search to avoid excessive API calls while typing.

4. **Error Handling**: Handle all error responses appropriately with user-friendly messages.

5. **Loading States**: Show loading indicators while fetching data.

6. **Empty States**: Handle cases where no archived projects are found.

## Example Usage in Frontend

### Fetch Archived Projects

```javascript
const fetchArchivedProjects = async (userId, page = 0, size = 10) => {
  try {
    const response = await fetch(
      `/api/projects/user/${userId}/archive?page=${page}&size=${size}`
    );
    if (!response.ok) {
      throw new Error("Failed to fetch archived projects");
    }
    return await response.json();
  } catch (error) {
    console.error("Error fetching archived projects:", error);
    throw error;
  }
};
```

### Search Archived Projects

```javascript
const searchArchivedProjects = async (userId, query, page = 0, size = 10) => {
  try {
    const response = await fetch(
      `/api/projects/user/${userId}/archive/search?query=${encodeURIComponent(
        query
      )}&page=${page}&size=${size}`
    );
    if (!response.ok) {
      throw new Error("Failed to search archived projects");
    }
    return await response.json();
  } catch (error) {
    console.error("Error searching archived projects:", error);
    throw error;
  }
};
```

## Testing

### Test Cases to Consider

1. **Role-based access**: Verify each role sees only appropriate projects
2. **Pagination**: Test with different page sizes and navigation
3. **Search**: Test with various search terms and edge cases
4. **Error handling**: Test with invalid user IDs and parameters
5. **Empty results**: Test with users who have no completed projects
6. **Performance**: Test with large datasets to ensure proper pagination

### Sample Test Data

- Create users with different roles
- Create completed projects associated with different courses and teams
- Verify the correct projects are returned for each role
