import { Transaction, Category, TransactionFilters, BulkUpdatePayload, UploadFile, PaginatedResponse, Budget } from '../types';

export interface PersonalFinanceService {
  // Transaction methods
  getTransactions(filters: TransactionFilters): Promise<PaginatedResponse<Transaction>>;
  uploadTransactions(file: File): Promise<void>;
  bulkUpdateTransactions(payload: BulkUpdatePayload): Promise<void>;
  categorizeTransactions(transactionIds: string[]): Promise<void>;

  // Category methods
  getCategories(): Promise<Category[]>;
  createCategory(category: Omit<Category, 'id'>): Promise<Category>;
  updateCategory(id: string, category: Partial<Category>): Promise<Category>;
  deleteCategory(id: string): Promise<void>;

  // Budget methods
  getBudgets(year: string): Promise<Budget[]>;
  createBudget(budget: Omit<Budget, 'id'>): Promise<Budget>;
  updateBudget(id: string, budget: Partial<Budget>): Promise<Budget>;
  deleteBudget(id: string): Promise<void>;

  // Upload methods
  getUploads(): Promise<UploadFile[]>;
  uploadFile(file: { file: File; account: string }): Promise<void>;
  processUpload(id: string): Promise<void>;
  deleteUpload(id: string): Promise<void>;

  // Auth methods
  login(email: string, password: string): Promise<{ user: { id: string; name: string; email: string } }>;
  logout(): Promise<void>;
  signup(userData: { email: string; password: string; name: string }): Promise<void>;
  getCurrentUser(): Promise<{ id: string; name: string; email: string }>;
} 