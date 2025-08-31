export interface Transaction {
  id: string;
  date: string;
  description: string;
  amount: number;
  categoryId?: string;
  category?: Category;
  account: string;
  included: boolean;
}

export interface Category {
  id: string;
  name: string;
  macroCategory: string;
  regexPatterns: string[];
  type: 'NEEDS' | 'WANTS' | 'SAVINGS_DEBTS';
}

export interface Budget {
  id: string;
  categoryId: string;
  amount: number;
  period: 'monthly' | 'annual' | 'default';
  month?: string; // Format: 'YYYY-MM', required only for monthly budgets
  year: string; // Format: 'YYYY'
}

export interface TransactionFilters {
  month?: string;
  included?: boolean;
  account?: string;
  categoryId?: string;
  cursor?: string;
  limit?: number;
}

export interface PaginatedResponse<T> {
  data: T[];
  nextCursor?: string;
  hasMore: boolean;
}

export interface BulkUpdatePayload {
  transactionIds: string[];
  updates: Partial<Transaction>;
}

export interface UploadFile {
  id: string;
  filename: string;
  account: string;
  status: 'pending' | 'processing' | 'completed' | 'error';
  uploadedAt: string;
  processedAt?: string;
  errorMessage?: string;
} 

export interface Account{
  id: string;
  name: string;
}

export interface  AccountFlowData {
  accountName: string;
  period: string;
  total: number;
}

export interface CategorySpending{
  categoryName: string;
  totalSpent: number;
  budgetedAmount?: number;
  percentage: number;
  categoryType?: 'NEEDS' | 'WANTS' | 'SAVINGS_DEBTS'; 
  macroCategory?: string;
  percentageOfIncome?: number; // New field for % of income
}

export interface CumulativeSpendingData {
  categoryName: string;
  period: string; // "Day 1", "Day 2", etc. for monthly view or "Week 1", "Week 2" for yearly
  cumulativeAmount: number;
  budgetCumulative: number;
  percentageOfBudget: number;
}

export interface LargestExpenseItem {
  id: string;
  date: string;
  description: string;
  amount: number;
  categoryName?: string;
  account: string;
}

export interface MonthlyData {
  year: string;
  month: string;
  week: number;
  totalIncome: number;
  totalExpenses: number;
  totalSavings: number;
}

export interface MacroCategoryMonthlyData{
  macroCategory: string;
  year: number;
  month: number;
  week: number;
  total: number;
}

export interface CategoryTrendsData{
  categoryName: string;
  year: number;
  month: number;
  week: number;
  total: number;
}