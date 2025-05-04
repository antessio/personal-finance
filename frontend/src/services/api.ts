import { RestPersonalFinanceService } from './restPersonalFinanceService';
import { MockPersonalFinanceService } from './mockPersonalFinanceService';

// Use mock service in development, REST service in production
export const service = process.env.NODE_ENV === 'development'
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
} = service; 