import axios from 'axios';
import { Transaction, Category, TransactionFilters, BulkUpdatePayload } from '../types';
import { PersonalFinanceService } from './personalFinanceService';

export class RestPersonalFinanceService implements PersonalFinanceService {
  private api = axios.create({
    baseURL: process.env.NEXT_PUBLIC_API_URL || 'http://localhost:3001',
    withCredentials: true,
  });

  // Transaction methods
  async getTransactions(filters: TransactionFilters): Promise<Transaction[]> {
    const response = await this.api.get<Transaction[]>('/transactions', { params: filters });
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
} 