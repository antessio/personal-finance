import { Transaction, Category, TransactionFilters, BulkUpdatePayload, UploadFile, PaginatedResponse, Budget, Account, CategorySpending, MonthlyData, MacroCategoryMonthlyData } from '../types';
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
    return stored ? JSON.parse(stored) : null;
  }

  private setStoredUser(user: { id: string; name: string; email: string } | null) {
    if (typeof window === 'undefined') return;
    if (user) {
      sessionStorage.setItem('currentUser', JSON.stringify(user));
      // Set auth-token cookie
      document.cookie = 'auth-token=1; path=/';
    } else {
      sessionStorage.removeItem('currentUser');
      // Remove auth-token cookie
      document.cookie = 'auth-token=; path=/; expires=Thu, 01 Jan 1970 00:00:00 GMT';
    }
  }

  async getMacroCategoriesMontlyData(year: number, month?: number): Promise<MacroCategoryMonthlyData[]> {
    await simulateDelay();
    const macroCategories = ['INCOME', 'EXPENSE', 'BILLS', 'SAVINGS', 'SUBSCRIPTIONS', 'DEBTS'];
    const monthlyData: MacroCategoryMonthlyData[] = [];

    for (const macroCategory of macroCategories) {
      for (let month = 1; month <= 12; month++) {
        const monthStr = `${year}-${month.toString().padStart(2, '0')}`;
        
        // Generate realistic mock data for each macro category
        let total = 0;
        const baseMultiplier = 1 + (month * 0.05); // Gradual increase throughout year
        const seasonalVariation = Math.sin((month / 12) * 2 * Math.PI) * 0.2; // Seasonal variation
        
        switch (macroCategory) {
          case 'INCOME':
            total = Math.round((2500 + (Math.random() * 500 - 250)) * baseMultiplier * (1 + seasonalVariation));
            break;
          case 'EXPENSE':
            total = Math.round((1800 + (Math.random() * 400 - 200)) * baseMultiplier * (1 + seasonalVariation * 0.5));
            break;
          case 'BILLS':
            total = Math.round((600 + (Math.random() * 100 - 50)) * baseMultiplier * (1 + seasonalVariation * 0.3));
            break;
          case 'SAVINGS':
            total = Math.round((400 + (Math.random() * 200 - 100)) * baseMultiplier * (1 + seasonalVariation * 0.8));
            break;
          case 'SUBSCRIPTIONS':
            total = Math.round((150 + (Math.random() * 50 - 25)) * baseMultiplier * (1 + seasonalVariation * 0.1));
            break;
          case 'DEBTS':
            total = Math.round((200 + (Math.random() * 100 - 50)) * baseMultiplier * (1 + seasonalVariation * 0.2));
            break;
        }

        monthlyData.push({ macroCategory, year: Number(year), month, week: 1, total });
      }
    }
    return monthlyData;
  }

  // Category spending methods
  async getCategorySpending(year: number, month?: number): Promise<CategorySpending[]> {
    await simulateDelay();
    if(month){
      return this.getCategorySpendingByMonth(year, month);
    }
    const categorySpending: CategorySpending[] = this.categories.map(category => {
      const totalSpent = this.transactions
        .filter(tx => tx.categoryId === category.id && tx.date.startsWith(year.toString()) && tx.included)
        .reduce((sum, tx) => sum + Math.abs(tx.amount), 0);

      const budget = this.budgets
        .find(budget => budget.categoryId === category.id && budget.year === year.toString());

      return {
        categoryName: category.name,
        totalSpent,
        budgetedAmount: budget?.amount || 0,
        percentage: totalSpent / (budget?.amount || 1) * 100,
        categoryType: category.type, // Add category type for 50-30-20 breakdown
        macroCategory: category.macroCategory, // Add macro category for additional filtering
      };
    });
    return categorySpending;
  }

  async getMonthlyData(year: number, month?: number): Promise<MonthlyData[]> {
    await simulateDelay();
    const monthlyData: MonthlyData[] = [];
    
    // Get category IDs for each macro category
    const incomeCategoryIds = this.categories.filter(c => c.macroCategory === 'INCOME').map(c => c.id);
    const expenseCategoryIds = this.categories.filter(c => c.macroCategory === 'EXPENSE').map(c => c.id);
    const savingsCategoryIds = this.categories.filter(c => c.macroCategory === 'SAVINGS').map(c => c.id);
    
    // If month is specified, return data for just that month, otherwise all months
    const monthsToProcess = month ? [month] : Array.from({length: 12}, (_, i) => i + 1);
    
    for (const monthNum of monthsToProcess) {
      const monthStr = `${year}-${monthNum.toString().padStart(2, '0')}`;
      
      const totalIncome = this.transactions
        .filter(tx => {
          const txDate = new Date(tx.date);
          return tx.date.startsWith(monthStr) && 
                 tx.categoryId && 
                 incomeCategoryIds.includes(tx.categoryId) && 
                 tx.included;
        })
        .reduce((sum, tx) => sum + tx.amount, 0);
        
      const totalExpenses = this.transactions
        .filter(tx => {
          const txDate = new Date(tx.date);
          return tx.date.startsWith(monthStr) && 
                 tx.categoryId && 
                 expenseCategoryIds.includes(tx.categoryId) && 
                 tx.included;
        })
        .reduce((sum, tx) => sum + Math.abs(tx.amount), 0);
        
      const totalSavings = this.transactions
        .filter(tx => {
          const txDate = new Date(tx.date);
          return tx.date.startsWith(monthStr) && 
                 tx.categoryId && 
                 savingsCategoryIds.includes(tx.categoryId) && 
                 tx.included;
        })
        .reduce((sum, tx) => sum + Math.abs(tx.amount), 0);
        
      monthlyData.push({
        year: year.toString(),
        month: monthStr,
        week: 1, // Assuming week 1 for simplicity
        totalIncome,
        totalExpenses,
        totalSavings,
      });
    }
    return monthlyData;
  }

  async getTotalIncome(year: number, month?: number): Promise<number> {
    await simulateDelay();

    const incomeCategoryIds = this.categories
      .filter(c => c.macroCategory === 'INCOME')
      .map(c => c.id);
    
    return this.transactions
      .filter(tx => {
        const transactionDate = new Date(tx.date);
        const transactionYear = transactionDate.getFullYear();
        const transactionMonth = transactionDate.getMonth() + 1;
        
        return tx.included && 
               tx.categoryId && 
               incomeCategoryIds.includes(tx.categoryId) &&
               transactionYear === year &&
               (month === undefined || transactionMonth === month);
      })
      .reduce((sum, tx) => sum + tx.amount, 0);
  }

  async getTotalExpenses(year: number, month?: number): Promise<number> {
    await simulateDelay();
    const expenseCategoryIds = this.categories
      .filter(c => c.macroCategory === 'EXPENSE')
      .map(c => c.id);
    
    return this.transactions
      .filter(tx => {
        const transactionDate = new Date(tx.date);
        const transactionYear = transactionDate.getFullYear();
        const transactionMonth = transactionDate.getMonth() + 1;
        
        return tx.included && 
               tx.categoryId && 
               expenseCategoryIds.includes(tx.categoryId) &&
               transactionYear === year &&
               (month === undefined || transactionMonth === month);
      })
      .reduce((sum, tx) => sum + Math.abs(tx.amount), 0);
  }
  
  async getTotalSavings(year: number, month?: number): Promise<number> {
    await simulateDelay();
    const savingsCategoryIds = this.categories
      .filter(c => c.macroCategory === 'SAVINGS')
      .map(c => c.id);
    
    return this.transactions
      .filter(tx => {
        const transactionDate = new Date(tx.date);
        const transactionYear = transactionDate.getFullYear();
        const transactionMonth = transactionDate.getMonth() + 1;
        
        return tx.included && 
               tx.categoryId && 
               savingsCategoryIds.includes(tx.categoryId) &&
               transactionYear === year &&
               (month === undefined || transactionMonth === month);
      })
      .reduce((sum, tx) => sum + Math.abs(tx.amount), 0);
  }

  async getTotalIncomeByMonth(year: number, month: string): Promise<number> {
    await simulateDelay();
    const incomeCategoryIds = this.categories
      .filter(c => c.macroCategory === 'INCOME')
      .map(c => c.id);
    return this.transactions
      .filter(tx => {
        const transactionDate = new Date(tx.date);
        const transactionMonth = (transactionDate.getMonth() + 1).toString().padStart(2, '0');
        return tx.included && 
               transactionDate.getFullYear() === year && 
               transactionMonth === month && 
               tx.categoryId && 
               incomeCategoryIds.includes(tx.categoryId);
      })
      .reduce((sum, tx) => sum + tx.amount, 0);
  }

  async getTotalExpensesByMonth(year: number, month: string): Promise<number> {
    await simulateDelay();
    const expenseCategoryIds = this.categories
      .filter(c => c.macroCategory === 'EXPENSE')
      .map(c => c.id);
    return this.transactions
      .filter(tx => {
        const transactionDate = new Date(tx.date);
        const transactionMonth = (transactionDate.getMonth() + 1).toString().padStart(2, '0');
        return tx.included && 
               transactionDate.getFullYear() === year && 
               transactionMonth === month && 
               tx.categoryId && 
               expenseCategoryIds.includes(tx.categoryId);
      })
      .reduce((sum, tx) => sum + Math.abs(tx.amount), 0);
  }

  async getTotalSavingsByMonth(year: number, month: string): Promise<number> {
    await simulateDelay();
    const savingsCategoryIds = this.categories
      .filter(c => c.macroCategory === 'SAVINGS')
      .map(c => c.id);
    return this.transactions
      .filter(tx => {
        const transactionDate = new Date(tx.date);
        const transactionMonth = (transactionDate.getMonth() + 1).toString().padStart(2, '0');
        return tx.included && 
               transactionDate.getFullYear() === year && 
               transactionMonth === month && 
               tx.categoryId && 
               savingsCategoryIds.includes(tx.categoryId);
      })
      .reduce((sum, tx) => sum + Math.abs(tx.amount), 0);
  }

  async getCategorySpendingByMonth(year: number, month: number): Promise<CategorySpending[]> {
    await simulateDelay();
    const monthlyTransactions = this.transactions.filter(tx => {
      const transactionDate = new Date(tx.date);
      const transactionMonth = transactionDate.getMonth() + 1; // getMonth() returns 0-11, we need 1-12
      return tx.included && 
             transactionDate.getFullYear() === year && 
             transactionMonth === month;
    });

    const categorySpending: { [key: string]: { 
      totalSpent: number; 
      budgetedAmount: number; 
      category: Category;
    } } = {};
    
    monthlyTransactions.forEach(transaction => {
      if (transaction.categoryId) {
        const category = this.categories.find(c => c.id === transaction.categoryId);
        if (category) {
          if (!categorySpending[category.name]) {
            // Get budget for this category and month
            const budget = this.budgets.find(b => 
              b.categoryId === category.id && 
              b.year === year.toString() && 
              (b.period === 'monthly' && b.month === month.toString().padStart(2, '0') || 
               b.period === 'annual')
            );
            const budgetAmount = budget ? (budget.period === 'annual' ? budget.amount / 12 : budget.amount) : 0;
            
            categorySpending[category.name] = { 
              totalSpent: 0, 
              budgetedAmount: budgetAmount,
              category: category
            };
          }
          categorySpending[category.name].totalSpent += Math.abs(transaction.amount);
        }
      }
    });

    return Object.entries(categorySpending).map(([categoryName, data]) => ({
      categoryName,
      totalSpent: data.totalSpent,
      budgetedAmount: data.budgetedAmount,
      percentage: (data.totalSpent / data.budgetedAmount) * 100,
      categoryType: data.category.type, // Add category type for 50-30-20 breakdown
      macroCategory: data.category.macroCategory, // Add macro category for additional filtering
    }));
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
    return Promise.resolve();
  }

  // Category methods
  async getCategories(): Promise<PaginatedResponse<Category>> {
    await simulateDelay();
    return Promise.resolve({data: this.categories, hasMore: false, nextCursor: undefined});
  }

  async getAllCategories(): Promise<Category[]> {
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
    const user = this.users.find(u => u.email === email && u.password === password);
    if (!user) {
      return Promise.reject(new Error('Invalid credentials'));
    }
    this.currentUser = { id: user.id, name: user.name, email: user.email };
    this.setStoredUser(this.currentUser);
    return Promise.resolve({ user: this.currentUser });
  }

  async logout(): Promise<void> {
    await simulateDelay();
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
    if (!this.currentUser) {
      return Promise.reject(new Error('Not authenticated'));
    }
    return Promise.resolve(this.currentUser);
  }

  // Upload methods
  async getUploads(): Promise<PaginatedResponse<UploadFile>> {
    await simulateDelay();
    return Promise.resolve({data: this.uploads, hasMore: false, nextCursor: undefined});
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
  async getAllBudgets(): Promise<Budget[]> {
    await simulateDelay();
    return Promise.resolve((await this.getBudgets(new Date().getFullYear().toString())));
  }
  async getBudgets(year: string): Promise<Budget[]> {
    await simulateDelay();
    return Promise.resolve(this.budgets.filter(budget => budget.year === year));
  }

  async getIncomeBudget(year: number, month?: number): Promise<number> {
    await simulateDelay();
    const incomeCategoryIds = this.categories
      .filter(c => c.macroCategory === 'INCOME')
      .map(c => c.id);
    
    if (month !== undefined) {
      const monthStr = month.toString().padStart(2, '0');
      const monthlyBudgets = this.budgets.filter(budget => 
        incomeCategoryIds.includes(budget.categoryId) && 
        budget.year === year.toString() &&
        budget.period === 'monthly' && 
        budget.month === monthStr
      );
      const annualBudgets = this.budgets.filter(budget => 
        incomeCategoryIds.includes(budget.categoryId) && 
        budget.year === year.toString() &&
        budget.period === 'annual'
      );
      const monthlyTotal = monthlyBudgets.reduce((sum, budget) => sum + budget.amount, 0);
      const annualTotal = annualBudgets.reduce((sum, budget) => sum + budget.amount, 0) / 12;
      return monthlyTotal || annualTotal;
    }
    
    const incomeBudgets = this.budgets.filter(budget => 
      incomeCategoryIds.includes(budget.categoryId) && budget.year === year.toString()
    );
    return incomeBudgets.reduce((sum, budget) => sum + budget.amount, 0);
  }

  async getExpenseBudget(year: number, month?: number): Promise<number> {
    await simulateDelay();
    const expenseCategoryIds = this.categories
      .filter(c => c.macroCategory === 'EXPENSE')
      .map(c => c.id);
    
    if (month !== undefined) {
      const monthStr = month.toString().padStart(2, '0');
      const monthlyBudgets = this.budgets.filter(budget => 
        expenseCategoryIds.includes(budget.categoryId) && 
        budget.year === year.toString() &&
        budget.period === 'monthly' && 
        budget.month === monthStr
      );
      const annualBudgets = this.budgets.filter(budget => 
        expenseCategoryIds.includes(budget.categoryId) && 
        budget.year === year.toString() &&
        budget.period === 'annual'
      );
      const monthlyTotal = monthlyBudgets.reduce((sum, budget) => sum + budget.amount, 0);
      const annualTotal = annualBudgets.reduce((sum, budget) => sum + budget.amount, 0) / 12;
      return monthlyTotal || annualTotal;
    }
    
    const expenseBudgets = this.budgets.filter(budget => 
      expenseCategoryIds.includes(budget.categoryId) && budget.year === year.toString()
    );
    return expenseBudgets.reduce((sum, budget) => sum + budget.amount, 0);
  } 

  async getSavingsBudget(year: number, month?: number): Promise<number> {
    await simulateDelay();
    const savingsCategoryIds = this.categories
      .filter(c => c.macroCategory === 'SAVINGS')
      .map(c => c.id);
    
    if (month !== undefined) {
      const monthStr = month.toString().padStart(2, '0');
      const monthlyBudgets = this.budgets.filter(budget => 
        savingsCategoryIds.includes(budget.categoryId) && 
        budget.year === year.toString() &&
        budget.period === 'monthly' && 
        budget.month === monthStr
      );
      const annualBudgets = this.budgets.filter(budget => 
        savingsCategoryIds.includes(budget.categoryId) && 
        budget.year === year.toString() &&
        budget.period === 'annual'
      );
      const monthlyTotal = monthlyBudgets.reduce((sum, budget) => sum + budget.amount, 0);
      const annualTotal = annualBudgets.reduce((sum, budget) => sum + budget.amount, 0) / 12;
      return monthlyTotal || annualTotal;
    }
    
    const savingsBudgets = this.budgets.filter(budget => 
      savingsCategoryIds.includes(budget.categoryId) && budget.year === year.toString()
    );
    return savingsBudgets.reduce((sum, budget) => sum + budget.amount, 0);
  }

  async getIncomeBudgetByMonth(year: number, month: string): Promise<number> {
    await simulateDelay();
    const incomeCategoryIds = this.categories
      .filter(c => c.macroCategory === 'INCOME')
      .map(c => c.id);
    const monthlyBudgets = this.budgets.filter(budget => 
      incomeCategoryIds.includes(budget.categoryId) && 
      budget.year === year.toString() &&
      (budget.period === 'monthly' && budget.month === month)
    );
    const annualBudgets = this.budgets.filter(budget => 
      incomeCategoryIds.includes(budget.categoryId) && 
      budget.year === year.toString() &&
      budget.period === 'annual'
    );
    // Monthly budgets take precedence, otherwise use annual budget divided by 12
    const monthlyTotal = monthlyBudgets.reduce((sum, budget) => sum + budget.amount, 0);
    const annualTotal = annualBudgets.reduce((sum, budget) => sum + budget.amount, 0) / 12;
    return monthlyTotal || annualTotal;
  }

  async getExpenseBudgetByMonth(year: number, month: string): Promise<number> {
    await simulateDelay();
    const expenseCategoryIds = this.categories
      .filter(c => c.macroCategory === 'EXPENSE')
      .map(c => c.id);
    const monthlyBudgets = this.budgets.filter(budget => 
      expenseCategoryIds.includes(budget.categoryId) && 
      budget.year === year.toString() &&
      (budget.period === 'monthly' && budget.month === month)
    );
    const annualBudgets = this.budgets.filter(budget => 
      expenseCategoryIds.includes(budget.categoryId) && 
      budget.year === year.toString() &&
      budget.period === 'annual'
    );
    const monthlyTotal = monthlyBudgets.reduce((sum, budget) => sum + budget.amount, 0);
    const annualTotal = annualBudgets.reduce((sum, budget) => sum + budget.amount, 0) / 12;
    return monthlyTotal || annualTotal;
  }

  async getSavingsBudgetByMonth(year: number, month: string): Promise<number> {
    await simulateDelay();
    const savingsCategoryIds = this.categories
      .filter(c => c.macroCategory === 'SAVINGS')
      .map(c => c.id);
    const monthlyBudgets = this.budgets.filter(budget => 
      savingsCategoryIds.includes(budget.categoryId) && 
      budget.year === year.toString() &&
      (budget.period === 'monthly' && budget.month === month)
    );
    const annualBudgets = this.budgets.filter(budget => 
      savingsCategoryIds.includes(budget.categoryId) && 
      budget.year === year.toString() &&
      budget.period === 'annual'
    );
    const monthlyTotal = monthlyBudgets.reduce((sum, budget) => sum + budget.amount, 0);
    const annualTotal = annualBudgets.reduce((sum, budget) => sum + budget.amount, 0) / 12;
    return monthlyTotal || annualTotal;
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

  async bulkCreateBudgets(budgets: Omit<Budget, 'id'>[]): Promise<Budget[]> {
    await simulateDelay();
    const newBudgets: Budget[] = budgets.map(budget => ({
      ...budget,
      id: Math.random().toString(36).substr(2, 9),
    }));
    this.budgets.push(...newBudgets);
    return newBudgets;
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

  async getAccounts(): Promise<Account[]> {
    await simulateDelay();
    const accounts: Account[] = [
        {
          id: "widiba",
          name: "Widiba"
        },
        {
          id: "intesa",
          name: "Intesa",
        },
        {
          id: "satispay",
          name: "Satispay",
        },
        {
          id: "paypal",
          name: "PayPal"
        }
    ]
    return accounts;
      
  }
} 