export interface UserResponse {
  id: string;
  email: string;
  userName: string;
  createdAt: string;
}

export interface RegisterRequest {
  email: string;
  password: string;
  userName: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}
