import { Transaction, Category, TransactionFilters, BulkUpdatePayload, UploadFile, PaginatedResponse, Budget } from '../types';
import { PersonalFinanceService } from './personalFinanceService';
import { mockTransactions, mockCategories, mockUsers, mockUploads, mockBudgets } from './mockData';

const simulateDelay = () => new Promise(resolve => setTimeout(resolve, 500));

interface MockUser {
  id: string;
  name: string;
  email: string;
  password: string;
}

export class MockPersonalFinanceService implements PersonalFinanceService {
  private transactions: Transaction[];
  private categories: Category[];
  private users: MockUser[];
  private currentUser: { id: string; name: string; email: string } | null;
  private uploads: UploadFile[];
  private budgets: Budget[];

  constructor() {
    this.transactions = [...mockTransactions];
    this.categories = [...mockCategories];
    this.users = [...mockUsers];
    this.currentUser = this.getStoredUser();
    this.uploads = [...mockUploads];
    this.budgets = [...mockBudgets];
  }

  private getStoredUser() {
    if (typeof window === 'undefined') return null;
    const stored = sessionStorage.getItem('currentUser');
    console.log("Getting stored user:", stored);
    return stored ? JSON.parse(stored) : null;
  }

  private setStoredUser(user: { id: string; name: string; email: string } | null) {
    if (typeof window === 'undefined') return;
    if (user) {
      console.log("Setting stored user:", user);
      sessionStorage.setItem('currentUser', JSON.stringify(user));
      // Set auth-token cookie
      document.cookie = 'auth-token=1; path=/';
    } else {
      console.log("Removing stored user");
      sessionStorage.removeItem('currentUser');
      // Remove auth-token cookie
      document.cookie = 'auth-token=; path=/; expires=Thu, 01 Jan 1970 00:00:00 GMT';
    }
  }

  // Transaction methods
  async getTransactions(filters: TransactionFilters): Promise<PaginatedResponse<Transaction>> {
    await simulateDelay();
    
    // Filter transactions based on criteria
    let filteredTransactions = this.transactions.filter(transaction => {
      if (filters.month && !transaction.date.startsWith(filters.month)) {
        return false;
      }
      if (filters.included !== undefined && transaction.included !== filters.included) {
        return false;
      }
      if (filters.account && transaction.account !== filters.account) {
        return false;
      }
      if (filters.categoryId && transaction.categoryId !== filters.categoryId) {
        return false;
      }
      return true;
    });

    // Sort transactions by date (newest first)
    filteredTransactions.sort((a, b) => new Date(b.date).getTime() - new Date(a.date).getTime());

    // Handle cursor-based pagination
    const limit = filters.limit || 10;
    let startIndex = 0;
    
    if (filters.cursor) {
      const cursorIndex = filteredTransactions.findIndex(t => t.id === filters.cursor);
      if (cursorIndex !== -1) {
        startIndex = cursorIndex + 1;
      }
    }
    
    const paginatedTransactions = filteredTransactions.slice(startIndex, startIndex + limit);
    const hasMore = startIndex + limit < filteredTransactions.length;
    const nextCursor = hasMore ? paginatedTransactions[paginatedTransactions.length - 1].id : undefined;

    return {
      data: paginatedTransactions,
      nextCursor,
      hasMore
    };
  }

  async uploadTransactions(file: File): Promise<void> {
    await simulateDelay();
    // Simulate file processing
    console.log('File uploaded:', file.name);
    return Promise.resolve();
  }

  async bulkUpdateTransactions(payload: BulkUpdatePayload): Promise<void> {
    await simulateDelay();
    this.transactions = this.transactions.map(transaction => {
      if (payload.transactionIds.includes(transaction.id)) {
        return { ...transaction, ...payload.updates };
      }
      return transaction;
    });
    return Promise.resolve();
  }

  async categorizeTransactions(transactionIds: string[]): Promise<void> {
    await simulateDelay();
    // Simulate categorization logic
    console.log('Categorizing transactions:', transactionIds);
    return Promise.resolve();
  }

  // Category methods
  async getCategories(): Promise<Category[]> {
    await simulateDelay();
    return Promise.resolve(this.categories);
  }

  async createCategory(category: Omit<Category, 'id'>): Promise<Category> {
    await simulateDelay();
    const newCategory: Category = {
      ...category,
      id: (this.categories.length + 1).toString(),
    };
    this.categories.push(newCategory);
    return Promise.resolve(newCategory);
  }

  async updateCategory(id: string, category: Partial<Category>): Promise<Category> {
    await simulateDelay();
    const index = this.categories.findIndex(c => c.id === id);
    if (index === -1) {
      return Promise.reject(new Error('Category not found'));
    }
    this.categories[index] = { ...this.categories[index], ...category };
    return Promise.resolve(this.categories[index]);
  }

  async deleteCategory(id: string): Promise<void> {
    await simulateDelay();
    this.categories = this.categories.filter(category => category.id !== id);
    return Promise.resolve();
  }

  // Auth methods
  async login(email: string, password: string): Promise<{ user: { id: string; name: string; email: string } }> {
    await simulateDelay();
    console.log("Login attempt:", email);
    const user = this.users.find(u => u.email === email && u.password === password);
    if (!user) {
      console.log("Login failed: invalid credentials");
      return Promise.reject(new Error('Invalid credentials'));
    }
    this.currentUser = { id: user.id, name: user.name, email: user.email };
    this.setStoredUser(this.currentUser);
    console.log("Login successful:", this.currentUser);
    return Promise.resolve({ user: this.currentUser });
  }

  async logout(): Promise<void> {
    await simulateDelay();
    console.log("Logout");
    this.currentUser = null;
    this.setStoredUser(null);
    return Promise.resolve();
  }

  async signup(userData: { email: string; password: string; name: string }): Promise<void> {
    await simulateDelay();
    if (this.users.some(u => u.email === userData.email)) {
      return Promise.reject(new Error('Email already exists'));
    }
    const newUser = {
      ...userData,
      id: (this.users.length + 1).toString(),
    };
    this.users.push(newUser);
    return Promise.resolve();
  }

  async getCurrentUser(): Promise<{ id: string; name: string; email: string }> {
    await simulateDelay();
    console.log("Getting current user:", this.currentUser);
    if (!this.currentUser) {
      console.log("No current user found");
      return Promise.reject(new Error('Not authenticated'));
    }
    return Promise.resolve(this.currentUser);
  }

  // Upload methods
  async getUploads(): Promise<UploadFile[]> {
    await simulateDelay();
    return Promise.resolve(this.uploads);
  }

  async uploadFile(file: { file: File; account: string }): Promise<void> {
    await simulateDelay();
    const newUpload: UploadFile = {
      id: Math.random().toString(36).substr(2, 9),
      filename: file.file.name,
      account: file.account,
      status: 'pending',
      uploadedAt: new Date().toISOString(),
    };
    this.uploads.push(newUpload);
    return Promise.resolve();
  }

  async processUpload(id: string): Promise<void> {
    await simulateDelay();
    const upload = this.uploads.find(u => u.id === id);
    if (upload) {
      upload.status = 'processing';
      // Simulate processing delay
      await new Promise(resolve => setTimeout(resolve, 1000));
      upload.status = 'completed';
      upload.processedAt = new Date().toISOString();
    }
    return Promise.resolve();
  }

  async deleteUpload(id: string): Promise<void> {
    await simulateDelay();
    this.uploads = this.uploads.filter(u => u.id !== id);
    return Promise.resolve();
  }

  // Budget methods
  async getBudgets(year: string): Promise<Budget[]> {
    await simulateDelay();
    return this.budgets.filter(budget => budget.year === year);
  }

  async createBudget(budget: Omit<Budget, 'id'>): Promise<Budget> {
    await simulateDelay();
    const newBudget: Budget = {
      ...budget,
      id: Math.random().toString(36).substr(2, 9),
    };
    this.budgets.push(newBudget);
    return newBudget;
  }

  async updateBudget(id: string, budget: Partial<Budget>): Promise<Budget> {
    await simulateDelay();
    const index = this.budgets.findIndex(b => b.id === id);
    if (index === -1) {
      throw new Error('Budget not found');
    }
    this.budgets[index] = { ...this.budgets[index], ...budget };
    return this.budgets[index];
  }

  async deleteBudget(id: string): Promise<void> {
    await simulateDelay();
    this.budgets = this.budgets.filter(budget => budget.id !== id);
  }
} 