export interface Transaction {
  id: string;
  date: string;
  description: string;
  amount: number;
  categoryId?: string;
  account: string;
  included: boolean;
}

export interface Category {
  id: string;
  name: string;
  macroCategory: string;
  regexPatterns: string[];
}

export interface TransactionFilters {
  month?: string;
  included?: boolean;
  account?: string;
  categoryId?: string;
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