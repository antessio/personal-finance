import { Transaction, Category, UploadFile, Budget } from '../types';

export const mockUsers = [
  {
    id: '1',
    name: 'John Doe',
    email: 'john@example.com',
    password: 'password123',
  },
  {
    id: '2',
    name: 'Jane Smith',
    email: 'jane@example.com',
    password: 'password123',
  },
  {
    id: '3',
    name: 'Bob Johnson',
    email: 'bob@example.com',
    password: 'password123',
  },
];

export const mockCategories = [
  // INCOME
  { id: 'cat-income-1', name: 'Salary', macroCategory: 'INCOME', regexPatterns: ['salary', 'stipendio'] },
  { id: 'cat-income-2', name: 'Interest', macroCategory: 'INCOME', regexPatterns: ['interest', 'interessi'] },

  // EXPENSE
  { id: 'cat-expense-1', name: 'Groceries', macroCategory: 'EXPENSE', regexPatterns: ['grocery', 'supermercato'] },
  { id: 'cat-expense-2', name: 'Dining Out', macroCategory: 'EXPENSE', regexPatterns: ['restaurant', 'ristorante'] },

  // BILLS
  { id: 'cat-bills-1', name: 'Electricity Bill', macroCategory: 'BILLS', regexPatterns: ['electricity', 'luce'] },
  { id: 'cat-bills-2', name: 'Internet Bill', macroCategory: 'BILLS', regexPatterns: ['internet', 'fibra'] },

  // SAVINGS
  { id: 'cat-savings-1', name: 'Emergency Fund', macroCategory: 'SAVINGS', regexPatterns: ['emergency', 'fondo emergenza'] },
  { id: 'cat-savings-2', name: 'Retirement', macroCategory: 'SAVINGS', regexPatterns: ['retirement', 'pensione'] },

  // SUBSCRIPTIONS
  { id: 'cat-subs-1', name: 'Netflix', macroCategory: 'SUBSCRIPTIONS', regexPatterns: ['netflix'] },
  { id: 'cat-subs-2', name: 'Spotify', macroCategory: 'SUBSCRIPTIONS', regexPatterns: ['spotify'] },

  // DEBTS
  { id: 'cat-debts-1', name: 'Mortgage', macroCategory: 'DEBTS', regexPatterns: ['mortgage', 'mutuo'] },
  { id: 'cat-debts-2', name: 'Credit Card', macroCategory: 'DEBTS', regexPatterns: ['credit card', 'carta di credito'] },
];

export const mockTransactions: Transaction[] = [
  {
    id: '1',
    date: '2024-03-15',
    description: 'Whole Foods Market',
    amount: -85.32,
    account: 'Chase Checking',
    categoryId: 'cat-expense-1',
    included: true,
  },
  {
    id: '2',
    date: '2024-03-14',
    description: 'Starbucks Coffee',
    amount: -4.95,
    account: 'Chase Checking',
    categoryId: 'cat-expense-2',
    included: true,
  },
  {
    id: '3',
    date: '2024-03-13',
    description: 'Uber Ride',
    amount: -15.50,
    account: 'Chase Checking',
    categoryId: 'cat-expense-2',
    included: true,
  },
  {
    id: '4',
    date: '2024-03-12',
    description: 'Electric Bill',
    amount: -120.00,
    account: 'Chase Checking',
    categoryId: 'cat-bills-1',
    included: true,
  },
  {
    id: '5',
    date: '2024-03-11',
    description: 'Netflix Subscription',
    amount: -14.99,
    account: 'Chase Checking',
    categoryId: 'cat-subs-1',
    included: true,
  },
  {
    id: '6',
    date: '2024-03-10',
    description: 'Trader Joe\'s',
    amount: -65.78,
    account: 'Chase Checking',
    categoryId: 'cat-expense-1',
    included: true,
  },
  {
    id: '7',
    date: '2024-03-09',
    description: 'Italian Restaurant',
    amount: -45.20,
    account: 'Chase Checking',
    categoryId: 'cat-expense-2',
    included: true,
  },
  {
    id: '8',
    date: '2024-03-08',
    description: 'Public Transport',
    amount: -2.75,
    account: 'Chase Checking',
    categoryId: 'cat-expense-2',
    included: true,
  },
  {
    id: '9',
    date: '2024-03-07',
    description: 'Internet Bill',
    amount: -79.99,
    account: 'Chase Checking',
    categoryId: 'cat-bills-2',
    included: true,
  },
  {
    id: '10',
    date: '2024-03-06',
    description: 'Spotify Premium',
    amount: -9.99,
    account: 'Chase Checking',
    categoryId: 'cat-subs-2',
    included: true,
  },
  {
    id: '11',
    date: '2024-03-05',
    description: 'Local Grocery Store',
    amount: -32.45,
    account: 'Chase Checking',
    categoryId: 'cat-expense-1',
    included: false,
  },
  {
    id: '12',
    date: '2024-03-04',
    description: 'Coffee Shop',
    amount: -3.50,
    account: 'Chase Checking',
    categoryId: 'cat-expense-2',
    included: false,
  },
  {
    id: '13',
    date: '2024-03-03',
    description: 'Lyft Ride',
    amount: -12.30,
    account: 'Chase Checking',
    categoryId: 'cat-expense-2',
    included: false,
  },
  {
    id: '14',
    date: '2024-03-02',
    description: 'Phone Bill',
    amount: -45.00,
    account: 'Chase Checking',
    categoryId: 'cat-bills-1',
    included: false,
  },
  {
    id: '15',
    date: '2024-03-01',
    description: 'Movie Theater',
    amount: -25.00,
    account: 'Chase Checking',
    categoryId: 'cat-subs-1',
    included: false,
  },
  {
    id: '16',
    date: '2024-03-15',
    description: 'March Salary',
    amount: 3500.00,
    account: 'Chase Checking',
    categoryId: 'cat-income-1',
    included: true,
  },
  {
    id: '17',
    date: '2024-03-10',
    description: 'Freelance Business Income',
    amount: 1200.00,
    account: 'Chase Checking',
    categoryId: 'cat-income-2',
    included: true,
  },
  {
    id: '18',
    date: '2024-03-05',
    description: 'Investment Dividend',
    amount: 250.00,
    account: 'Chase Savings',
    categoryId: 'cat-income-2',
    included: true,
  },
];

export const mockUploads: UploadFile[] = [
  {
    id: '1',
    filename: 'chase_march_2024.csv',
    account: 'Chase Checking',
    status: 'completed',
    uploadedAt: '2024-03-15T10:30:00Z',
    processedAt: '2024-03-15T10:35:00Z',
  },
  {
    id: '2',
    filename: 'amex_april_2024.csv',
    account: 'Amex Platinum',
    status: 'pending',
    uploadedAt: '2024-04-01T09:15:00Z',
  },
  {
    id: '3',
    filename: 'chase_feb_2024.csv',
    account: 'Chase Checking',
    status: 'error',
    uploadedAt: '2024-02-28T14:20:00Z',
    errorMessage: 'Invalid date format in row 12',
  },
  {
    id: '4',
    filename: 'savings_q1_2024.xlsx',
    account: 'Chase Savings',
    status: 'processing',
    uploadedAt: '2024-04-02T11:45:00Z',
  },
];

export const mockBudgets: Budget[] = [
  {
    id: '1',
    categoryId: '1', // Groceries
    amount: 6000,
    period: 'annual',
    year: '2024',
  },
  {
    id: '2',
    categoryId: '2', // Dining Out
    amount: 2400,
    period: 'annual',
    year: '2024',
  },
  {
    id: '3',
    categoryId: '3', // Transportation
    amount: 300,
    period: 'monthly',
    month: '2024-01',
    year: '2024',
  },
  {
    id: '4',
    categoryId: '4', // Entertainment
    amount: 200,
    period: 'monthly',
    month: '2024-01',
    year: '2024',
  },
  {
    id: '5',
    categoryId: '5', // Shopping
    amount: 500,
    period: 'monthly',
    month: '2024-01',
    year: '2024',
  },
  {
    id: '6',
    categoryId: '6', // Utilities
    amount: 1800,
    period: 'annual',
    year: '2024',
  },
  {
    id: '7',
    categoryId: '7', // Rent
    amount: 12000,
    period: 'annual',
    year: '2024',
  },
  {
    id: '8',
    categoryId: '8', // Healthcare
    amount: 1200,
    period: 'annual',
    year: '2024',
  },
  {
    id: '9',
    categoryId: '9', // Travel
    amount: 3000,
    period: 'annual',
    year: '2024',
  },
  {
    id: '10',
    categoryId: '10', // Education
    amount: 5000,
    period: 'annual',
    year: '2024',
  },
]; 