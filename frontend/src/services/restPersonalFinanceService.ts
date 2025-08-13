import axios, { AxiosInstance } from 'axios';
import { Transaction, Category, TransactionFilters, BulkUpdatePayload, PaginatedResponse, Budget, UploadFile } from '../types';
import { PersonalFinanceService } from './personalFinanceService';

export class RestPersonalFinanceService implements PersonalFinanceService {
  private api: AxiosInstance;

  constructor() {
    this.api = axios.create({
      baseURL: process.env.NEXT_PUBLIC_API_URL || 'http://localhost:3001',
      withCredentials: true,
    });

    // Add request interceptor to include auth token
    this.api.interceptors.request.use(
      (config) => {
        const token = this.getAuthToken();
        if (token) {
          config.headers.Authorization = `Bearer ${token}`;
        }
        return config;
      },
      (error) => {
        return Promise.reject(error);
      }
    );

    // Add response interceptor to handle auth errors
    this.api.interceptors.response.use(
      (response) => response,
      (error) => {
        if (error.response?.status === 401) {
          // Clear token and redirect to login
          this.clearAuthToken();
          if (typeof window !== 'undefined') {
            window.location.href = '/login';
          }
        }
        return Promise.reject(error);
      }
    );
  }

  private getAuthToken(): string | null {
    if (typeof window === 'undefined') return null;
    
    // Try to get token from localStorage first
    const storedToken = localStorage.getItem('auth-token');
    if (storedToken) return storedToken;
    
    // Fallback to reading from cookie
    const cookies = document.cookie.split(';');
    const authCookie = cookies.find(cookie => cookie.trim().startsWith('auth-token='));
    return authCookie ? authCookie.split('=')[1] : null;
  }

  private setAuthToken(token: string): void {
    if (typeof window === 'undefined') return;
    
    // Store in localStorage
    localStorage.setItem('auth-token', token);
    
    // Also set as cookie for middleware compatibility
    document.cookie = `auth-token=${token}; path=/; secure; samesite=strict`;
  }

  private clearAuthToken(): void {
    if (typeof window === 'undefined') return;
    
    // Clear from localStorage
    localStorage.removeItem('auth-token');
    
    // Clear cookie
    document.cookie = 'auth-token=; path=/; expires=Thu, 01 Jan 1970 00:00:00 GMT';
  }

  // Public method to manually set token if needed
  public setToken(token: string): void {
    this.setAuthToken(token);
  }

  // Public method to check if user is authenticated
  public isAuthenticated(): boolean {
    return !!this.getAuthToken();
  }

  // Transaction methods
  async getTransactions(filters: TransactionFilters): Promise<PaginatedResponse<Transaction>> {
    const response = await this.api.get<PaginatedResponse<Transaction>>('/api/transactions', { params: filters });
    return response.data;
  }

  async uploadTransactions(file: File): Promise<void> {
    const formData = new FormData();
    formData.append('file', file);
    await this.api.post('/transactions/upload', formData);
  }

  async bulkUpdateTransactions(payload: BulkUpdatePayload): Promise<void> {
    await this.api.patch('/transactions/bulk-update', payload);
  }

  async categorizeTransactions(transactionIds: string[]): Promise<void> {
    await this.api.post('/transactions/categorize', { transactionIds });
  }

  // Category methods
  async getCategories(): Promise<Category[]> {
    const response = await this.api.get<Category[]>('/categories');
    return response.data;
  }

  async createCategory(category: Omit<Category, 'id'>): Promise<Category> {
    const response = await this.api.post<Category>('/categories', category);
    return response.data;
  }

  async updateCategory(id: string, category: Partial<Category>): Promise<Category> {
    const response = await this.api.patch<Category>(`/categories/${id}`, category);
    return response.data;
  }

  async deleteCategory(id: string): Promise<void> {
    await this.api.delete(`/categories/${id}`);
  }

  // Budget methods
  async getBudgets(year: string): Promise<Budget[]> {
    const response = await this.api.get<Budget[]>(`/budgets?year=${year}`);
    return response.data;
  }

  async createBudget(budget: Omit<Budget, 'id'>): Promise<Budget> {
    const response = await this.api.post<Budget>('/budgets', budget);
    return response.data;
  }

  async updateBudget(id: string, budget: Partial<Budget>): Promise<Budget> {
    const response = await this.api.patch<Budget>(`/budgets/${id}`, budget);
    return response.data;
  }

  async deleteBudget(id: string): Promise<void> {
    await this.api.delete(`/budgets/${id}`);
  }

  // Auth methods
  async login(email: string, password: string): Promise<{ user: { id: string; name: string; email: string } }> {
    const response = await this.api.post('/public/api/users/login', { username: email, password });
    
    // Extract token from response (adjust based on your API response structure)
    const token = response.data.token || response.headers['authorization']?.replace('Bearer ', '');
    
    if (token) {
      this.setAuthToken(token);
    }
    
    return response.data;
  }

  async logout(): Promise<void> {
      this.clearAuthToken();
    
  }

  async signup(userData: { email: string; password: string; name: string }): Promise<void> {
    await this.api.post('/public/api/users/register', userData);
  }

  async getCurrentUser(): Promise<{ id: string; name: string; email: string }> {
    const response = await this.api.get('/api/users/me');
    return response.data;
  }

  // Upload methods
  async getUploads(): Promise<UploadFile[]> {
    const response = await this.api.get<UploadFile[]>('/uploads');
    return response.data;
  }

  async uploadFile(file: { file: File, account: string}): Promise<void> {
    const formData = new FormData();
    formData.append('file', file.file);
    formData.append('sourceType', file.account);
    
    // The Authorization header will be automatically added by the request interceptor
    // But we need to ensure Content-Type is handled correctly for file uploads
    await this.api.post('/api/transaction-imports/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
        'Authorization': `Bearer ${this.getAuthToken()}`
      },
    });
  }

  async processUpload(id: string): Promise<void> {
    await this.api.post(`/uploads/${id}/process`);
  }

  async deleteUpload(id: string): Promise<void> {
    await this.api.delete(`/uploads/${id}`);
  }
} 