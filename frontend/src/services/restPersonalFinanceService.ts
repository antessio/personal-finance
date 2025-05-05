import axios from 'axios';
import { Transaction, Category, TransactionFilters, BulkUpdatePayload, PaginatedResponse, Budget, UploadFile } from '../types';
import { PersonalFinanceService } from './personalFinanceService';

export class RestPersonalFinanceService implements PersonalFinanceService {
  private api = axios.create({
    baseURL: process.env.NEXT_PUBLIC_API_URL || 'http://localhost:3001',
    withCredentials: true,
  });

  // Transaction methods
  async getTransactions(filters: TransactionFilters): Promise<PaginatedResponse<Transaction>> {
    const response = await this.api.get<PaginatedResponse<Transaction>>('/transactions', { params: filters });
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
    const response = await this.api.post('/auth/login', { email, password });
    return response.data;
  }

  async logout(): Promise<void> {
    await this.api.post('/auth/logout');
  }

  async signup(userData: { email: string; password: string; name: string }): Promise<void> {
    await this.api.post('/auth/signup', userData);
  }

  async getCurrentUser(): Promise<{ id: string; name: string; email: string }> {
    const response = await this.api.get('/auth/me');
    return response.data;
  }

  // Upload methods
  async getUploads(): Promise<UploadFile[]> {
    const response = await this.api.get<UploadFile[]>('/uploads');
    return response.data;
  }

  async uploadFile(file: { file: File; account: string }): Promise<void> {
    const formData = new FormData();
    formData.append('file', file.file);
    formData.append('account', file.account);
    await this.api.post('/uploads', formData);
  }

  async processUpload(id: string): Promise<void> {
    await this.api.post(`/uploads/${id}/process`);
  }

  async deleteUpload(id: string): Promise<void> {
    await this.api.delete(`/uploads/${id}`);
  }
} 