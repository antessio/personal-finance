import { RestPersonalFinanceService } from './restPersonalFinanceService';
import { MockPersonalFinanceService } from './mockPersonalFinanceService';

// Use the real backend by default; set NEXT_PUBLIC_USE_MOCK=true to force the
// in-memory mock service (e.g. for a public demo build with no backend).
export const service = process.env.NEXT_PUBLIC_USE_MOCK === 'true'
  ? new MockPersonalFinanceService()
  : new RestPersonalFinanceService();

export const {
  getTransactions,
  uploadTransactions,
  bulkUpdateTransactions,
  categorizeTransactions,
  getCategories,
  createCategory,
  updateCategory,
  deleteCategory,
  login,
  logout,
  signup,
  getCurrentUser,
  getAccounts,
} = service; 