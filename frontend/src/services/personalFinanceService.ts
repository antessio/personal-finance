import { Transaction, Category, TransactionFilters, BulkUpdatePayload, UploadFile, PaginatedResponse, Budget, Account, CategorySpending, MonthlyData } from '../types';

export interface PersonalFinanceService {

  
  getCategorySpending(year: string): Promise<CategorySpending[]>;
  getMonthlyData(year: string): Promise<MonthlyData[]>;

  // Transaction methods
  getTotalIncome(year: number): Promise<number>;
  getTotalExpenses(year: number): Promise<number>;
  getTotalSavings(year: number): Promise<number>;
  getTransactions(filters: TransactionFilters): Promise<PaginatedResponse<Transaction>>;
  uploadTransactions(file: File): Promise<void>;
  bulkUpdateTransactions(payload: BulkUpdatePayload): Promise<void>;
  categorizeTransactions(transactionIds: string[]): Promise<void>;

  // Category methods
  
  getCategories(filters?: { limit?: number; cursor?: string }): Promise<PaginatedResponse<Category>>;
  getAllCategories(): Promise<Category[]>;
  createCategory(category: Omit<Category, 'id'>): Promise<Category>;
  updateCategory(id: string, category: Partial<Category>): Promise<Category>;
  deleteCategory(id: string): Promise<void>;

  // Budget methods
  getAllBudgets(year: string): Promise<Budget[]>;
  getBudgets(year: string, limit: number): Promise<PaginatedResponse<Budget>>;
  createBudget(budget: Omit<Budget, 'id'>): Promise<Budget>;
  updateBudget(id: string, budget: Partial<Budget>): Promise<Budget>;
  deleteBudget(id: string): Promise<void>;
  getIncomeBudget(year: string): Promise<number>;
  getExpenseBudget(year: string): Promise<number>;
  getSavingsBudget(year: string): Promise<number>;

  // Upload methods
  getUploads(): Promise<PaginatedResponse<UploadFile>>;
  uploadFile(file: { file: File; account: string }): Promise<void>;
  processUpload(id: string): Promise<void>;
  deleteUpload(id: string): Promise<void>;

  // Auth methods
  login(email: string, password: string): Promise<{ user: { id: string; name: string; email: string } }>;
  logout(): Promise<void>;
  signup(userData: { email: string; password: string; name: string }): Promise<void>;
  getCurrentUser(): Promise<{ id: string; name: string; email: string }>;

  // Token management (optional for implementations)
  setToken?(token: string): void;
  isAuthenticated?(): boolean;

  getAccounts(): Promise<Account[]>;
} 