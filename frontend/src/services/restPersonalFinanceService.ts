import axios, { AxiosInstance } from 'axios';
import { Transaction, Category, TransactionFilters, BulkUpdatePayload, PaginatedResponse, Budget, UploadFile, Account, CategorySpending, MonthlyData, MacroCategoryMonthlyData } from '../types';
import { PersonalFinanceService } from './personalFinanceService';
import { BudgetRest, CategoryRest, CategorySpendingRest, MonthlyDataRest, PaginatedResponseRest, TransactionRest, UploadFilRest } from './rest/types';
import { isAuthEnabled } from '../config/auth';

export class RestPersonalFinanceService implements PersonalFinanceService {
  private api: AxiosInstance;

  constructor() {
    this.api = axios.create({
      baseURL: process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080',
      withCredentials: true,
    });

    // Add request interceptor to include auth token
    this.api.interceptors.request.use(
      (config) => {
        if (isAuthEnabled()) {
          const token = this.getAuthToken();
          if (token) {
            config.headers.Authorization = `Bearer ${token}`;
          }
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
        if (isAuthEnabled() && error.response?.status === 401) {
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
    if (!isAuthEnabled() || typeof window === 'undefined') return null;

    // Try to get token from localStorage first
    const storedToken = localStorage.getItem('auth-token');
    if (storedToken) return storedToken;

    // Fallback to reading from cookie
    const cookies = document.cookie.split(';');
    const authCookie = cookies.find(cookie => cookie.trim().startsWith('auth-token='));
    return authCookie ? authCookie.split('=')[1] : null;
  }

  private setAuthToken(token: string): void {
    if (!isAuthEnabled() || typeof window === 'undefined') return;

    // Store in localStorage
    localStorage.setItem('auth-token', token);

    // Also set as cookie for middleware compatibility
    document.cookie = `auth-token=${token}; path=/; secure; samesite=strict`;
  }

  private clearAuthToken(): void {
    if (!isAuthEnabled() || typeof window === 'undefined') return;

    // Clear from localStorage
    localStorage.removeItem('auth-token');

    // Clear cookie
    document.cookie = 'auth-token=; path=/; expires=Thu, 01 Jan 1970 00:00:00 GMT';
  }

  // Public method to manually set token if needed
  public setToken(token: string): void {
    if (isAuthEnabled()) {
      this.setAuthToken(token);
    }
  }

  // Public method to check if user is authenticated
  public isAuthenticated(): boolean {
    if (!isAuthEnabled()) return true; // Always authenticated when auth is disabled
    return !!this.getAuthToken();
  }

  async getCategorySpending(year: string): Promise<CategorySpending[]> {
    const fromDate = `${year}-01-01`;
    const toDate = `${year}-12-31`;
    const response = await this.api.get<CategorySpendingRest[]>(`/api/transactions/category-spending?fromDate=${fromDate}&toDate=${toDate}`);
    return response.data.map(spending => ({
      categoryName: spending.category.name + ' ' + spending.category.emoji,
      totalSpent: spending.totalSpent,
      budgetedAmount: spending.budgetAmount || 0,
      percentage: spending.budgetAmount ? (spending.totalSpent / spending.budgetAmount) * 100 : 0,
    }));
  }

  async getMacroCategoriesMontlyData(year: string): Promise<MacroCategoryMonthlyData[]> {
    const fromDate = `${year}-01-01`;
    const toDate = `${year}-12-31`;
    const response = await this.api.get<MacroCategoryMonthlyData[]>(`/api/transactions/expenses-monthly-data?fromDate=${fromDate}&toDate=${toDate}`);
    console.log("Macro categories monthly data response:", response.data);
    return response.data;
  }
  async getMonthlyData(year: string): Promise<MonthlyData[]> {
    const fromDate = `${year}-01-01`;
    const toDate = `${year}-12-31`;
    const response = await this.api.get<MonthlyDataRest[]>(`/api/transactions/monthly-data?fromDate=${fromDate}&toDate=${toDate}`);
    return response.data.map(data => ({
      year: data.yearMonth.split('-')[0],
      month: data.yearMonth.split('-')[1],
      totalIncome: data.totalIncome,
      totalExpenses: data.totalExpenses,
      totalSavings: data.totalSavings,
    }));
  }

  async getTotalIncome(year: number): Promise<number> {
    const fromDate = `${year}-01-01`;
    const toDate = `${year}-12-31`;
    const response = await this.api.get<number>(`/api/transactions/total-income?fromDate=${fromDate}&toDate=${toDate}`);
    return response.data;
  }

  async getTotalExpenses(year: number): Promise<number> {
    const fromDate = `${year}-01-01`;
    const toDate = `${year}-12-31`;
    const response = await this.api.get<number>(`/api/transactions/total-expenses?fromDate=${fromDate}&toDate=${toDate}`);
    return response.data;
  }

  async getTotalSavings(year: number): Promise<number> {
    const fromDate = `${year}-01-01`;
    const toDate = `${year}-12-31`;
    const response = await this.api.get<number>(`/api/transactions/total-savings?fromDate=${fromDate}&toDate=${toDate}`);
    return response.data;
  }

  // Transaction methods
  async getTransactions(filters: TransactionFilters): Promise<PaginatedResponse<Transaction>> {

    const response = await this.api.get<PaginatedResponseRest<TransactionRest>>('/api/transactions', { params: this.convertFilters(filters) });
    return {
      data: response.data.data.map(transaction => ({
        id: transaction.id,
        date: transaction.date,
        description: transaction.description,
        categoryId: transaction.category?.id,
        category: transaction.category ? {
          id: transaction.category.id,
          name: transaction.category.name,
          macroCategory: transaction.category.macroCategory,
          regexPatterns: transaction.category.matchers,
        } : undefined,
        amount: transaction.amount,
        account: transaction.source,
        included: !transaction.skip
      })),
      hasMore: response.data.hasNext,
      nextCursor: response.data.data.length > 0 ? response.data.data[response.data.data.length - 1].id : undefined
    }
  }

  convertFilters(filters: TransactionFilters): Record<string, any> {
    const params: Record<string, any> = {};
    if (filters.month) params.targetDate = filters.month;
    if (filters.included !== undefined) params.skip = !filters.included;
    if (filters.account) params.source = filters.account.toLowerCase();
    if (filters.categoryId) {
      if (filters.categoryId == "uncategorized") {
        params.uncategorized = true;
      } else if (filters.categoryId == "categorized") {
        params.uncategorized = false;
      } else {
        params.categoryId = filters.categoryId;
      }
    }
    if (filters.limit) params.limit = filters.limit;
    if (filters.cursor) params.cursor = filters.cursor;
    return params;
  }

  async uploadTransactions(file: File): Promise<void> {
    const formData = new FormData();
    formData.append('file', file);
    await this.api.post('/transactions/upload', formData);
  }

  async bulkUpdateTransactions(payload: BulkUpdatePayload): Promise<void> {

    try {
      const requestData = {
        transactionIds: payload.transactionIds,
        categoryId: payload.updates.categoryId,
        skip: payload.updates.included != undefined ? !payload.updates.included : undefined,
      };

      if (!this.api) {
        throw new Error("API instance is not initialized");
      }
      const response = await this.api.patch('/api/transactions/bulk-update', requestData);

    } catch (error) {
      console.error("Bulk update error:", error);
      throw error;
    }
  }

  async categorizeTransactions(transactionIds: string[]): Promise<void> {
    await this.api.post('/transactions/categorize', { transactionIds });
  }

  // Category methods
  async getAllCategories(): Promise<Category[]> {
    return (await this.getCategories({ limit: 200 })).data;
  }
  async getCategories(filters?: { limit?: number; cursor?: string }): Promise<PaginatedResponse<Category>> {
    const params: any = {
      limit: filters?.limit || 20
    };

    if (filters?.cursor) {
      params.cursor = filters.cursor;
    }

    const response = await this.api.get<PaginatedResponseRest<CategoryRest>>('/api/categories', { params });
    return {
      data: response.data.data.map(category => ({
        id: category.id,
        name: category.name,
        macroCategory: category.macroCategory,
        regexPatterns: category.matchers,
      })),
      hasMore: response.data.hasNext,
      nextCursor: response.data.hasNext ? response.data.data[response.data.data.length - 1].id : undefined
    };
  }

  async createCategory(category: Omit<Category, 'id'>): Promise<Category> {
    const response = await this.api.post<Category>('/api/categories', category);
    return response.data;
  }

  async updateCategory(id: string, category: Partial<Category>): Promise<Category> {
    const response = await this.api.patch<Category>(`/categories/${id}`, category);
    return response.data;
  }

  async deleteCategory(id: string): Promise<void> {
    await this.api.delete(`/api/categories/${id}`);
  }

  // Budget methods
  async getAllBudgets(): Promise<Budget[]> {
    return (await this.getBudgets(new Date().getFullYear().toString()));
  }
  async getBudgets(year: string): Promise<Budget[]> {
    const response = await this.api.get<BudgetRest[]>(`/api/budgets`, { params: { year } });
    return response.data.map(budget => ({
      id: budget.id,
      categoryId: budget.categoryId,
      amount: budget.amount,
      year: (budget.year || year )+ '',
      month: (budget.month || '') + '',
      period: this.convertBudgetType(budget.type),
    }));
  }
  convertBudgetType(type: 'DEFAULT' | 'YEARLY' | 'MONTHLY'): 'annual' | 'monthly' | 'default' {
    switch (type) {
      case 'YEARLY':
        return 'annual';
      case 'MONTHLY':
        return 'monthly';
      default:
        return 'default'; // Default to annual for DEFAULT type
    }
  }

  async getIncomeBudget(year: number): Promise<number> {
    const fromDate = `${year}-01-01`;
    const toDate = `${year}-12-31`;
    const response = await this.api.get<number>(`/api/budgets/total-income?fromDate=${fromDate}&toDate=${toDate}`);
    return response.data;
  }
  async getExpenseBudget(year: number): Promise<number> {
    const fromDate = `${year}-01-01`;
    const toDate = `${year}-12-31`;
    const response = await this.api.get<number>(`/api/budgets/total-expenses?fromDate=${fromDate}&toDate=${toDate}`);
    return response.data;
  }
  async getSavingsBudget(year: number): Promise<number> {
    const fromDate = `${year}-01-01`;
    const toDate = `${year}-12-31`;
    const response = await this.api.get<number>(`/api/budgets/total-savings?fromDate=${fromDate}&toDate=${toDate}`);
    return response.data;
  }
  async createBudget(budget: Omit<Budget, 'id'>): Promise<Budget> {
    var response: any;
    console.log("Creating budget", budget);
    if (budget.period && budget.period == 'monthly') {
      response = await this.api.post<Budget>('/api/budgets/monthly', {
        amount: budget.amount,
        categoryId: budget.categoryId,
        yearMonth: budget.month
      });
    } else {
      response = await this.api.post<Budget>('/api/budgets/annual', {
        amount: budget.amount,
        categoryId: budget.categoryId
      });
    }
    return response.data;
  }

  async bulkCreateBudgets(budgets: Omit<Budget, 'id'>[]): Promise<Budget[]> {

    const response = await this.api.post<Budget[]>('/api/budgets/bulk',
      budgets.map(budget => ({
        categoryId: budget.categoryId,
        amount: budget.amount,
      })));
    return response.data;
  }

  async updateBudget(id: string, budget: Partial<Budget>): Promise<Budget> {
    const response = await this.api.patch<Budget>(`/api/budgets/${id}`, budget);
    return response.data;
  }

  async deleteBudget(id: string): Promise<void> {
    await this.api.delete(`/api/budgets/${id}`);
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
  async getUploads(filters?: { limit?: number; cursor?: string }): Promise<PaginatedResponse<UploadFile>> {
    const params: any = {
      limit: filters?.limit || 20
    };

    if (filters?.cursor) {
      params.cursor = filters.cursor;
    }

    const response = await this.api.get<PaginatedResponseRest<UploadFilRest>>('/api/transaction-imports', { params });
    return {
      data: response.data.data.map(upload => ({
        id: upload.id,
        status: this.convertUploadStatus(upload.status),
        filename: upload.filePath,
        account: upload.sourceType,
        uploadedAt: upload.insertedAt,
        processedAt: upload.updatedAt || upload.insertedAt, // Assuming updatedAt is used for processed time
        errorMessage: upload.status === 'error' ? 'Error processing file' : undefined,
      })),
      hasMore: response.data.hasNext,
      nextCursor: response.data.hasNext ? response.data.data[response.data.data.length - 1].id : undefined
    };
  }

  convertUploadStatus(status: string): 'pending' | 'processing' | 'completed' | 'error' {
    switch (status) {
      case 'PENDING':
        return 'pending';
      case 'PROCESSING':
        return 'processing';
      case 'SUCCESS':
        return 'completed';
      case 'ERROR':
        return 'error';
      default:
        return 'pending'; // Default case if status is unknown
    }
  }
  async uploadFile(file: { file: File, account: string }): Promise<void> {
    const formData = new FormData();
    formData.append('file', file.file);
    formData.append('sourceType', file.account);

    // The Authorization header will be automatically added by the request interceptor
    // But we need to ensure Content-Type is handled correctly for file uploads
    await this.api.post('/api/transaction-imports/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      },
    });
  }

  async processUpload(id: string): Promise<void> {
    await this.api.post(`/uploads/${id}/process`);
  }

  async deleteUpload(id: string): Promise<void> {
    await this.api.delete(`/uploads/${id}`);
  }

  async getAccounts(): Promise<Account[]> {
    const response = await this.api.get("/api/configurations/accounts");
    return response.data;
  }
} 