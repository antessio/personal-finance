import { Transaction, Category, TransactionFilters, BulkUpdatePayload, UploadFile, PaginatedResponse, Budget, Account, CategorySpending, MonthlyData, MacroCategoryMonthlyData, AccountFlowData, CategoryTrendsData } from '../types';

export interface PersonalFinanceService {


  getCategorySpending(year: number, month?: number): Promise<CategorySpending[]>;
  getMonthlyData(year: number, month?: number): Promise<MonthlyData[]>;
  getMacroCategoriesMontlyData(year: number, month?: number): Promise<MacroCategoryMonthlyData[]>;
  getCategoryTrendsData(year: number, month?: number): Promise<CategoryTrendsData[]>;
  getAccountFlowData(year: number, month?: number): Promise<AccountFlowData[]>;

  // Transaction methods
  getTotalIncome(year: number, month: number | undefined): Promise<number>;
  getTotalExpenses(year: number, month: number | undefined): Promise<number>;
  getTotalSavings(year: number, month: number | undefined): Promise<number>;
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
  getAllBudgets(): Promise<Budget[]>;
  getBudgets(year: string): Promise<Budget[]>;
  bulkCreateBudgets(budgets: Omit<Budget, 'id'>[]): Promise<Budget[]>;
  createBudget(budget: Omit<Budget, 'id'>): Promise<Budget>;
  updateBudget(id: string, budget: Partial<Budget>): Promise<Budget>;
  deleteBudget(id: string): Promise<void>;
  getIncomeBudget(year: number, month: number | undefined): Promise<number>;
  getExpenseBudget(year: number, month: number | undefined): Promise<number>;
  getSavingsBudget(year: number, month: number | undefined): Promise<number>;

  // Upload methods
  getUploads(filters?: { limit?: number; cursor?: string }): Promise<PaginatedResponse<UploadFile>>;
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