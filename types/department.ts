// TypeScript definitions for Department entities and DTOs

export interface Department {
  id: string;
  name: string;
  _links?: {
    self: { href: string };
    department: { href: string };
    batches: { href: string };
  };
}

export interface DepartmentResponse {
  id: string;
  name: string;
}

export interface CreateDepartmentRequest {
  name: string;
}

export interface UpdateDepartmentRequest {
  name?: string;
}

export interface DepartmentBatchResponse {
  id: string;
  name: string;
  graduationYear: string;
  section: string;
}

export interface PaginatedDepartmentResponse {
  data: DepartmentResponse[];
  pagination: {
    current_page: number;
    per_page: number;
    total_pages: number;
    total_count: number;
  };
}
