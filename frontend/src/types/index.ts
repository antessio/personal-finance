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
}

export interface Budget {
  id: string;
  categoryId: string;
  amount: number;
  period: 'monthly' | 'annual';
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