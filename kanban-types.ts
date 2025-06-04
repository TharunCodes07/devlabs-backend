// TypeScript interfaces for Kanban board functionality

export interface User {
  id: string;
  name: string;
  email: string;
  profileId?: string;
  image?: string;
  role: "STUDENT" | "ADMIN" | "FACULTY" | "MANAGER";
  phoneNumber: string;
  isActive: boolean;
}

export interface KanbanTask {
  id: string;
  title: string;
  description?: string;
  position: number;
  createdBy: User;
  assignedTo?: User;
  createdAt: string; // ISO timestamp
  updatedAt: string; // ISO timestamp
}

export interface KanbanColumn {
  id: string;
  name: string;
  position: number;
  tasks: KanbanTask[];
  createdAt: string; // ISO timestamp
  updatedAt: string; // ISO timestamp
}

export interface KanbanBoard {
  id: string;
  projectId: string;
  columns: KanbanColumn[];
  createdAt: string; // ISO timestamp
  updatedAt: string; // ISO timestamp
}

// Request interfaces
export interface CreateTaskRequest {
  title: string;
  description?: string;
  columnId: string;
  assignedToId?: string;
  userId: string;
}

export interface UpdateTaskRequest {
  title?: string;
  description?: string;
  assignedToId?: string;
  userId: string;
}

export interface MoveTaskRequest {
  columnId: string;
  position: number;
  userId: string;
}

// DeleteTaskRequest removed - delete only needs taskId and accessToken

// API Response wrapper (if you're using the same pattern as other endpoints)
export interface ApiResponse<T> {
  data?: T;
  error?: string;
}

// API Functions
const API_BASE_URL = "http://localhost:8080";

export const kanbanApi = {
  // Get kanban board for a project
  getKanbanBoard: async (
    projectId: string,
    accessToken: string
  ): Promise<KanbanBoard> => {
    const response = await fetch(
      `${API_BASE_URL}/kanban/project/${projectId}`,
      {
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${accessToken}`,
        },
      }
    );

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    return await response.json();
  },
  // Create a new task
  createTask: async (
    request: CreateTaskRequest,
    accessToken: string
  ): Promise<KanbanTask> => {
    const response = await fetch(`${API_BASE_URL}/kanban/tasks`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${accessToken}`,
      },
      body: JSON.stringify(request),
    });

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    return await response.json();
  },
  // Update an existing task
  updateTask: async (
    taskId: string,
    request: UpdateTaskRequest,
    accessToken: string
  ): Promise<KanbanTask> => {
    const response = await fetch(`${API_BASE_URL}/kanban/tasks/${taskId}`, {
      method: "PUT",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${accessToken}`,
      },
      body: JSON.stringify(request),
    });

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    return await response.json();
  },
  // Move a task to different column/position
  moveTask: async (
    taskId: string,
    request: MoveTaskRequest,
    accessToken: string
  ): Promise<KanbanTask> => {
    const response = await fetch(
      `${API_BASE_URL}/kanban/tasks/${taskId}/move`,
      {
        method: "PUT",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${accessToken}`,
        },
        body: JSON.stringify(request),
      }
    );

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    return await response.json();
  }, // Delete a task
  deleteTask: async (
    taskId: string,
    accessToken: string
  ): Promise<{ message: string }> => {
    const response = await fetch(`${API_BASE_URL}/kanban/tasks/${taskId}`, {
      method: "DELETE",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${accessToken}`,
      },
      // No body needed - user identity comes from access token
    });

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    return await response.json();
  },

  // Get a specific task
  getTask: async (taskId: string, accessToken: string): Promise<KanbanTask> => {
    const response = await fetch(`${API_BASE_URL}/kanban/tasks/${taskId}`, {
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${accessToken}`,
      },
    });

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }
    return await response.json();
  },
};
