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

export const mockCategories: Category[] = [
  // INCOME
  { id: 'cat-income-1', name: 'Salary', macroCategory: 'INCOME', type: 'NEEDS', regexPatterns: ['salary', 'stipendio', 'payroll', 'wages'] },
  { id: 'cat-income-2', name: 'Interest', macroCategory: 'INCOME', type: 'NEEDS', regexPatterns: ['interest', 'interessi', 'bank interest'] },
  { id: 'cat-income-3', name: 'Freelance', macroCategory: 'INCOME', type: 'NEEDS', regexPatterns: ['freelance', 'consulting', 'contractor'] },
  { id: 'cat-income-4', name: 'Investment Returns', macroCategory: 'INCOME', type: 'NEEDS', regexPatterns: ['dividend', 'investment', 'returns'] },
  { id: 'cat-income-5', name: 'Bonus', macroCategory: 'INCOME', type: 'NEEDS', regexPatterns: ['bonus', 'commission', 'incentive'] },

  // EXPENSE - NEEDS
  { id: 'cat-expense-1', name: 'Groceries', macroCategory: 'EXPENSE', type: 'NEEDS', regexPatterns: ['grocery', 'supermercato', 'whole foods', 'trader joe', 'walmart', 'target'] },
  { id: 'cat-expense-2', name: 'Transportation', macroCategory: 'EXPENSE', type: 'NEEDS', regexPatterns: ['gas', 'fuel', 'public transport', 'metro', 'bus', 'train'] },
  { id: 'cat-expense-3', name: 'Healthcare', macroCategory: 'EXPENSE', type: 'NEEDS', regexPatterns: ['doctor', 'pharmacy', 'medical', 'hospital', 'dentist'] },
  { id: 'cat-expense-4', name: 'Insurance', macroCategory: 'EXPENSE', type: 'NEEDS', regexPatterns: ['insurance', 'premium', 'coverage'] },
  { id: 'cat-expense-5', name: 'Childcare', macroCategory: 'EXPENSE', type: 'NEEDS', regexPatterns: ['daycare', 'babysitter', 'childcare', 'school'] },

  // EXPENSE - WANTS
  { id: 'cat-expense-6', name: 'Dining Out', macroCategory: 'EXPENSE', type: 'WANTS', regexPatterns: ['restaurant', 'ristorante', 'fast food', 'cafe', 'coffee', 'starbucks'] },
  { id: 'cat-expense-7', name: 'Entertainment', macroCategory: 'EXPENSE', type: 'WANTS', regexPatterns: ['movie', 'theater', 'concert', 'entertainment', 'cinema'] },
  { id: 'cat-expense-8', name: 'Shopping', macroCategory: 'EXPENSE', type: 'WANTS', regexPatterns: ['amazon', 'shopping', 'clothes', 'fashion', 'mall'] },
  { id: 'cat-expense-9', name: 'Travel', macroCategory: 'EXPENSE', type: 'WANTS', regexPatterns: ['hotel', 'airbnb', 'flight', 'airline', 'vacation', 'travel'] },
  { id: 'cat-expense-10', name: 'Personal Care', macroCategory: 'EXPENSE', type: 'WANTS', regexPatterns: ['salon', 'spa', 'barber', 'cosmetics', 'beauty'] },

  // BILLS
  { id: 'cat-bills-1', name: 'Electricity Bill', macroCategory: 'BILLS', type: 'NEEDS', regexPatterns: ['electricity', 'luce', 'electric', 'power company'] },
  { id: 'cat-bills-2', name: 'Internet Bill', macroCategory: 'BILLS', type: 'NEEDS', regexPatterns: ['internet', 'fibra', 'broadband', 'wifi'] },
  { id: 'cat-bills-3', name: 'Phone Bill', macroCategory: 'BILLS', type: 'NEEDS', regexPatterns: ['phone', 'mobile', 'cell', 'verizon', 'att'] },
  { id: 'cat-bills-4', name: 'Water Bill', macroCategory: 'BILLS', type: 'NEEDS', regexPatterns: ['water', 'aqua', 'utility'] },
  { id: 'cat-bills-5', name: 'Gas Bill', macroCategory: 'BILLS', type: 'NEEDS', regexPatterns: ['gas bill', 'heating', 'natural gas'] },
  { id: 'cat-bills-6', name: 'Rent/Mortgage', macroCategory: 'BILLS', type: 'NEEDS', regexPatterns: ['rent', 'mortgage', 'housing', 'apartment'] },

  // SAVINGS
  { id: 'cat-savings-1', name: 'Emergency Fund', macroCategory: 'SAVINGS', type: 'SAVINGS_DEBTS', regexPatterns: ['emergency', 'fondo emergenza', 'emergency fund'] },
  { id: 'cat-savings-2', name: 'Retirement', macroCategory: 'SAVINGS', type: 'SAVINGS_DEBTS', regexPatterns: ['retirement', 'pensione', '401k', 'ira'] },
  { id: 'cat-savings-3', name: 'Investment', macroCategory: 'SAVINGS', type: 'SAVINGS_DEBTS', regexPatterns: ['investment', 'stocks', 'bonds', 'mutual fund'] },
  { id: 'cat-savings-4', name: 'Education Fund', macroCategory: 'SAVINGS', type: 'SAVINGS_DEBTS', regexPatterns: ['education', 'college', 'tuition', 'school fund'] },

  // SUBSCRIPTIONS
  { id: 'cat-subs-1', name: 'Netflix', macroCategory: 'SUBSCRIPTIONS', type: 'WANTS', regexPatterns: ['netflix'] },
  { id: 'cat-subs-2', name: 'Spotify', macroCategory: 'SUBSCRIPTIONS', type: 'WANTS', regexPatterns: ['spotify'] },
  { id: 'cat-subs-3', name: 'Amazon Prime', macroCategory: 'SUBSCRIPTIONS', type: 'WANTS', regexPatterns: ['amazon prime', 'prime'] },
  { id: 'cat-subs-4', name: 'Gym Membership', macroCategory: 'SUBSCRIPTIONS', type: 'WANTS', regexPatterns: ['gym', 'fitness', 'planet fitness', 'la fitness'] },
  { id: 'cat-subs-5', name: 'Cloud Storage', macroCategory: 'SUBSCRIPTIONS', type: 'WANTS', regexPatterns: ['icloud', 'google drive', 'dropbox', 'cloud'] },
  { id: 'cat-subs-6', name: 'News/Magazines', macroCategory: 'SUBSCRIPTIONS', type: 'WANTS', regexPatterns: ['news', 'magazine', 'newspaper', 'subscription'] },

  // DEBTS
  { id: 'cat-debts-1', name: 'Mortgage', macroCategory: 'DEBTS', type: 'SAVINGS_DEBTS', regexPatterns: ['mortgage', 'mutuo', 'home loan'] },
  { id: 'cat-debts-2', name: 'Credit Card', macroCategory: 'DEBTS', type: 'SAVINGS_DEBTS', regexPatterns: ['credit card', 'carta di credito', 'visa', 'mastercard'] },
  { id: 'cat-debts-3', name: 'Student Loan', macroCategory: 'DEBTS', type: 'SAVINGS_DEBTS', regexPatterns: ['student loan', 'education loan', 'college debt'] },
  { id: 'cat-debts-4', name: 'Car Loan', macroCategory: 'DEBTS', type: 'SAVINGS_DEBTS', regexPatterns: ['car loan', 'auto loan', 'vehicle loan'] },
];

export const mockTransactions: Transaction[] = [
  // 2025 Transactions - January
  { id: 't-2025-001', date: '2025-01-31', description: 'January Salary', amount: 3200.00, account: 'Chase Checking', categoryId: 'cat-income-1', included: true },
  { id: 't-2025-002', date: '2025-01-30', description: 'Whole Foods Market', amount: -125.45, account: 'Chase Checking', categoryId: 'cat-expense-1', included: true },
  { id: 't-2025-003', date: '2025-01-29', description: 'Starbucks Coffee', amount: -6.85, account: 'Chase Checking', categoryId: 'cat-expense-6', included: true },
  { id: 't-2025-004', date: '2025-01-28', description: 'Gas Station', amount: -45.20, account: 'Chase Checking', categoryId: 'cat-expense-2', included: true },
  { id: 't-2025-005', date: '2025-01-27', description: 'Netflix Subscription', amount: -17.99, account: 'Chase Checking', categoryId: 'cat-subs-1', included: true },
  { id: 't-2025-006', date: '2025-01-26', description: 'Electric Bill', amount: -135.00, account: 'Chase Checking', categoryId: 'cat-bills-1', included: true },
  { id: 't-2025-007', date: '2025-01-25', description: 'Target Shopping', amount: -89.99, account: 'Chase Checking', categoryId: 'cat-expense-8', included: true },
  { id: 't-2025-008', date: '2025-01-24', description: 'Doctor Visit', amount: -150.00, account: 'Chase Checking', categoryId: 'cat-expense-3', included: true },
  { id: 't-2025-009', date: '2025-01-23', description: 'Freelance Project', amount: 850.00, account: 'Chase Checking', categoryId: 'cat-income-3', included: true },
  { id: 't-2025-010', date: '2025-01-22', description: 'Restaurant Dinner', amount: -65.40, account: 'Chase Checking', categoryId: 'cat-expense-6', included: true },
  { id: 't-2025-011', date: '2025-01-21', description: 'Gym Membership', amount: -29.99, account: 'Chase Checking', categoryId: 'cat-subs-4', included: true },
  { id: 't-2025-012', date: '2025-01-20', description: 'Grocery Store', amount: -78.32, account: 'Chase Checking', categoryId: 'cat-expense-1', included: true },
  { id: 't-2025-013', date: '2025-01-19', description: 'Phone Bill', amount: -55.00, account: 'Chase Checking', categoryId: 'cat-bills-3', included: true },
  { id: 't-2025-014', date: '2025-01-18', description: 'Amazon Purchase', amount: -145.67, account: 'Chase Checking', categoryId: 'cat-expense-8', included: true },
  { id: 't-2025-015', date: '2025-01-17', description: 'Movie Theater', amount: -28.00, account: 'Chase Checking', categoryId: 'cat-expense-7', included: true },
  { id: 't-2025-016', date: '2025-01-16', description: 'Emergency Fund Transfer', amount: -500.00, account: 'Chase Checking', categoryId: 'cat-savings-1', included: true },
  { id: 't-2025-017', date: '2025-01-15', description: 'Internet Bill', amount: -79.99, account: 'Chase Checking', categoryId: 'cat-bills-2', included: true },
  { id: 't-2025-018', date: '2025-01-14', description: 'Coffee Shop', amount: -4.75, account: 'Chase Checking', categoryId: 'cat-expense-6', included: true },
  { id: 't-2025-019', date: '2025-01-13', description: 'Spotify Premium', amount: -10.99, account: 'Chase Checking', categoryId: 'cat-subs-2', included: true },
  { id: 't-2025-020', date: '2025-01-12', description: 'Water Bill', amount: -35.00, account: 'Chase Checking', categoryId: 'cat-bills-4', included: true },

  // 2024 Transactions - December
  { id: 't-2024-120', date: '2024-12-31', description: 'December Salary', amount: 3200.00, account: 'Chase Checking', categoryId: 'cat-income-1', included: true },
  { id: 't-2024-121', date: '2024-12-30', description: 'Year-End Bonus', amount: 1500.00, account: 'Chase Checking', categoryId: 'cat-income-5', included: true },
  { id: 't-2024-122', date: '2024-12-29', description: 'Holiday Shopping', amount: -350.00, account: 'Chase Checking', categoryId: 'cat-expense-8', included: true },
  { id: 't-2024-123', date: '2024-12-28', description: 'Restaurant', amount: -95.50, account: 'Chase Checking', categoryId: 'cat-expense-6', included: true },
  { id: 't-2024-124', date: '2024-12-27', description: 'Gas Station', amount: -42.30, account: 'Chase Checking', categoryId: 'cat-expense-2', included: true },
  { id: 't-2024-125', date: '2024-12-26', description: 'Groceries', amount: -156.78, account: 'Chase Checking', categoryId: 'cat-expense-1', included: true },
  { id: 't-2024-126', date: '2024-12-25', description: 'Gift Shopping', amount: -200.00, account: 'Chase Checking', categoryId: 'cat-expense-8', included: true },
  { id: 't-2024-127', date: '2024-12-24', description: 'Electric Bill', amount: -142.50, account: 'Chase Checking', categoryId: 'cat-bills-1', included: true },
  { id: 't-2024-128', date: '2024-12-23', description: 'Retirement Contribution', amount: -600.00, account: 'Chase Checking', categoryId: 'cat-savings-2', included: true },
  { id: 't-2024-129', date: '2024-12-22', description: 'Amazon Prime', amount: -14.99, account: 'Chase Checking', categoryId: 'cat-subs-3', included: true },

  // 2024 Transactions - November
  { id: 't-2024-100', date: '2024-11-30', description: 'November Salary', amount: 3200.00, account: 'Chase Checking', categoryId: 'cat-income-1', included: true },
  { id: 't-2024-101', date: '2024-11-29', description: 'Grocery Shopping', amount: -98.45, account: 'Chase Checking', categoryId: 'cat-expense-1', included: true },
  { id: 't-2024-102', date: '2024-11-28', description: 'Thanksgiving Dinner', amount: -125.00, account: 'Chase Checking', categoryId: 'cat-expense-6', included: true },
  { id: 't-2024-103', date: '2024-11-27', description: 'Gas', amount: -38.90, account: 'Chase Checking', categoryId: 'cat-expense-2', included: true },
  { id: 't-2024-104', date: '2024-11-26', description: 'Black Friday Shopping', amount: -275.99, account: 'Chase Checking', categoryId: 'cat-expense-8', included: true },
  { id: 't-2024-105', date: '2024-11-25', description: 'Internet Bill', amount: -79.99, account: 'Chase Checking', categoryId: 'cat-bills-2', included: true },
  { id: 't-2024-106', date: '2024-11-24', description: 'Phone Bill', amount: -55.00, account: 'Chase Checking', categoryId: 'cat-bills-3', included: true },
  { id: 't-2024-107', date: '2024-11-23', description: 'Movie Night', amount: -32.50, account: 'Chase Checking', categoryId: 'cat-expense-7', included: true },
  { id: 't-2024-108', date: '2024-11-22', description: 'Freelance Payment', amount: 750.00, account: 'Chase Checking', categoryId: 'cat-income-3', included: true },
  { id: 't-2024-109', date: '2024-11-21', description: 'Healthcare Premium', amount: -285.00, account: 'Chase Checking', categoryId: 'cat-expense-4', included: true },

  // 2024 Transactions - October
  { id: 't-2024-080', date: '2024-10-31', description: 'October Salary', amount: 3200.00, account: 'Chase Checking', categoryId: 'cat-income-1', included: true },
  { id: 't-2024-081', date: '2024-10-30', description: 'Halloween Costume', amount: -45.00, account: 'Chase Checking', categoryId: 'cat-expense-8', included: true },
  { id: 't-2024-082', date: '2024-10-29', description: 'Grocery Store', amount: -115.67, account: 'Chase Checking', categoryId: 'cat-expense-1', included: true },
  { id: 't-2024-083', date: '2024-10-28', description: 'Car Insurance', amount: -125.00, account: 'Chase Checking', categoryId: 'cat-expense-4', included: true },
  { id: 't-2024-084', date: '2024-10-27', description: 'Restaurant', amount: -58.30, account: 'Chase Checking', categoryId: 'cat-expense-6', included: true },
  { id: 't-2024-085', date: '2024-10-26', description: 'Gas Station', amount: -41.20, account: 'Chase Checking', categoryId: 'cat-expense-2', included: true },
  { id: 't-2024-086', date: '2024-10-25', description: 'Electric Bill', amount: -128.75, account: 'Chase Checking', categoryId: 'cat-bills-1', included: true },
  { id: 't-2024-087', date: '2024-10-24', description: 'Investment Dividend', amount: 125.00, account: 'Chase Checking', categoryId: 'cat-income-4', included: true },
  { id: 't-2024-088', date: '2024-10-23', description: 'Coffee', amount: -5.25, account: 'Chase Checking', categoryId: 'cat-expense-6', included: true },
  { id: 't-2024-089', date: '2024-10-22', description: 'Gym Membership', amount: -29.99, account: 'Chase Checking', categoryId: 'cat-subs-4', included: true },

  // 2024 Transactions - September
  { id: 't-2024-060', date: '2024-09-30', description: 'September Salary', amount: 3200.00, account: 'Chase Checking', categoryId: 'cat-income-1', included: true },
  { id: 't-2024-061', date: '2024-09-29', description: 'Back to School Shopping', amount: -189.99, account: 'Chase Checking', categoryId: 'cat-expense-8', included: true },
  { id: 't-2024-062', date: '2024-09-28', description: 'Groceries', amount: -92.34, account: 'Chase Checking', categoryId: 'cat-expense-1', included: true },
  { id: 't-2024-063', date: '2024-09-27', description: 'Gas', amount: -39.50, account: 'Chase Checking', categoryId: 'cat-expense-2', included: true },
  { id: 't-2024-064', date: '2024-09-26', description: 'Water Bill', amount: -32.00, account: 'Chase Checking', categoryId: 'cat-bills-4', included: true },
  { id: 't-2024-065', date: '2024-09-25', description: 'Internet Bill', amount: -79.99, account: 'Chase Checking', categoryId: 'cat-bills-2', included: true },
  { id: 't-2024-066', date: '2024-09-24', description: 'Emergency Fund', amount: -500.00, account: 'Chase Checking', categoryId: 'cat-savings-1', included: true },
  { id: 't-2024-067', date: '2024-09-23', description: 'Restaurant', amount: -72.85, account: 'Chase Checking', categoryId: 'cat-expense-6', included: true },
  { id: 't-2024-068', date: '2024-09-22', description: 'Netflix', amount: -15.99, account: 'Chase Checking', categoryId: 'cat-subs-1', included: true },
  { id: 't-2024-069', date: '2024-09-21', description: 'Phone Bill', amount: -55.00, account: 'Chase Checking', categoryId: 'cat-bills-3', included: true },

  // 2024 August
  { id: 't-2024-040', date: '2024-08-31', description: 'August Salary', amount: 3200.00, account: 'Chase Checking', categoryId: 'cat-income-1', included: true },
  { id: 't-2024-041', date: '2024-08-30', description: 'Summer Vacation', amount: -1200.00, account: 'Chase Checking', categoryId: 'cat-expense-9', included: true },
  { id: 't-2024-042', date: '2024-08-29', description: 'Hotel Stay', amount: -350.00, account: 'Chase Checking', categoryId: 'cat-expense-9', included: true },
  { id: 't-2024-043', date: '2024-08-28', description: 'Airport Restaurant', amount: -45.00, account: 'Chase Checking', categoryId: 'cat-expense-6', included: true },
  { id: 't-2024-044', date: '2024-08-27', description: 'Groceries', amount: -110.45, account: 'Chase Checking', categoryId: 'cat-expense-1', included: true },
  { id: 't-2024-045', date: '2024-08-26', description: 'Gas', amount: -44.20, account: 'Chase Checking', categoryId: 'cat-expense-2', included: true },
  { id: 't-2024-046', date: '2024-08-25', description: 'Electric Bill', amount: -165.00, account: 'Chase Checking', categoryId: 'cat-bills-1', included: true },
  { id: 't-2024-047', date: '2024-08-24', description: 'Spa Day', amount: -180.00, account: 'Chase Checking', categoryId: 'cat-expense-10', included: true },
  { id: 't-2024-048', date: '2024-08-23', description: 'Concert Tickets', amount: -95.00, account: 'Chase Checking', categoryId: 'cat-expense-7', included: true },
  { id: 't-2024-049', date: '2024-08-22', description: 'Freelance Project', amount: 900.00, account: 'Chase Checking', categoryId: 'cat-income-3', included: true },

  // 2023 Transactions for comparison
  { id: 't-2023-001', date: '2023-12-31', description: 'December 2023 Salary', amount: 3000.00, account: 'Chase Checking', categoryId: 'cat-income-1', included: true },
  { id: 't-2023-002', date: '2023-12-30', description: 'Year-End Shopping', amount: -280.00, account: 'Chase Checking', categoryId: 'cat-expense-8', included: true },
  { id: 't-2023-003', date: '2023-12-29', description: 'Holiday Dinner', amount: -125.00, account: 'Chase Checking', categoryId: 'cat-expense-6', included: true },
  { id: 't-2023-004', date: '2023-11-30', description: 'November 2023 Salary', amount: 3000.00, account: 'Chase Checking', categoryId: 'cat-income-1', included: true },
  { id: 't-2023-005', date: '2023-11-29', description: 'Thanksgiving Groceries', amount: -150.00, account: 'Chase Checking', categoryId: 'cat-expense-1', included: true },
  { id: 't-2023-006', date: '2023-10-31', description: 'October 2023 Salary', amount: 3000.00, account: 'Chase Checking', categoryId: 'cat-income-1', included: true },
  { id: 't-2023-007', date: '2023-10-30', description: 'Halloween Expenses', amount: -65.00, account: 'Chase Checking', categoryId: 'cat-expense-8', included: true },
  { id: 't-2023-008', date: '2023-09-30', description: 'September 2023 Salary', amount: 3000.00, account: 'Chase Checking', categoryId: 'cat-income-1', included: true },
  { id: 't-2023-009', date: '2023-08-31', description: 'August 2023 Salary', amount: 3000.00, account: 'Chase Checking', categoryId: 'cat-income-1', included: true },
  { id: 't-2023-010', date: '2023-07-31', description: 'July 2023 Salary', amount: 3000.00, account: 'Chase Checking', categoryId: 'cat-income-1', included: true },
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
  // INCOME budgets for 2024
  {
    id: 'budget-income-1',
    categoryId: 'cat-income-1', // Salary
    amount: 35000,
    period: 'annual',
    year: '2024',
  },
  {
    id: 'budget-income-2',
    categoryId: 'cat-income-2', // Interest
    amount: 500,
    period: 'annual',
    year: '2024',
  },

  // EXPENSE budgets for 2024
  {
    id: 'budget-expense-1',
    categoryId: 'cat-expense-1', // Groceries
    amount: 6000,
    period: 'annual',
    year: '2024',
  },
  {
    id: 'budget-expense-2',
    categoryId: 'cat-expense-2', // Dining Out
    amount: 2400,
    period: 'annual',
    year: '2024',
  },

  // BILLS budgets for 2024
  {
    id: 'budget-bills-1',
    categoryId: 'cat-bills-1', // Electricity Bill
    amount: 1200,
    period: 'annual',
    year: '2024',
  },
  {
    id: 'budget-bills-2',
    categoryId: 'cat-bills-2', // Internet Bill
    amount: 600,
    period: 'annual',
    year: '2024',
  },

  // SAVINGS budgets for 2024
  {
    id: 'budget-savings-1',
    categoryId: 'cat-savings-1', // Emergency Fund
    amount: 5000,
    period: 'annual',
    year: '2024',
  },
  {
    id: 'budget-savings-2',
    categoryId: 'cat-savings-2', // Retirement
    amount: 8000,
    period: 'annual',
    year: '2024',
  },

  // SUBSCRIPTIONS budgets for 2024
  {
    id: 'budget-subs-1',
    categoryId: 'cat-subs-1', // Netflix
    amount: 180,
    period: 'annual',
    year: '2024',
  },
  {
    id: 'budget-subs-2',
    categoryId: 'cat-subs-2', // Spotify
    amount: 120,
    period: 'annual',
    year: '2024',
  },

  // DEBTS budgets for 2024
  {
    id: 'budget-debts-1',
    categoryId: 'cat-debts-1', // Mortgage
    amount: 15000,
    period: 'annual',
    year: '2024',
  },
  {
    id: 'budget-debts-2',
    categoryId: 'cat-debts-2', // Credit Card
    amount: 2400,
    period: 'annual',
    year: '2024',
  },

  // INCOME budgets for 2025
  {
    id: 'budget-income-1-2025',
    categoryId: 'cat-income-1', // Salary
    amount: 37000,
    period: 'annual',
    year: '2025',
  },
  {
    id: 'budget-income-2-2025',
    categoryId: 'cat-income-2', // Interest
    amount: 600,
    period: 'annual',
    year: '2025',
  },

  // EXPENSE budgets for 2025
  {
    id: 'budget-expense-1-2025',
    categoryId: 'cat-expense-1', // Groceries
    amount: 6300,
    period: 'annual',
    year: '2025',
  },
  {
    id: 'budget-expense-2-2025',
    categoryId: 'cat-expense-2', // Dining Out
    amount: 2500,
    period: 'annual',
    year: '2025',
  },

  // BILLS budgets for 2025
  {
    id: 'budget-bills-1-2025',
    categoryId: 'cat-bills-1', // Electricity Bill
    amount: 1250,
    period: 'annual',
    year: '2025',
  },
  {
    id: 'budget-bills-2-2025',
    categoryId: 'cat-bills-2', // Internet Bill
    amount: 650,
    period: 'annual',
    year: '2025',
  },

  // SAVINGS budgets for 2025
  {
    id: 'budget-savings-1-2025',
    categoryId: 'cat-savings-1', // Emergency Fund
    amount: 5500,
    period: 'annual',
    year: '2025',
  },
  {
    id: 'budget-savings-2-2025',
    categoryId: 'cat-savings-2', // Retirement
    amount: 8500,
    period: 'annual',
    year: '2025',
  },

  // SUBSCRIPTIONS budgets for 2025
  {
    id: 'budget-subs-1-2025',
    categoryId: 'cat-subs-1', // Netflix
    amount: 200,
    period: 'annual',
    year: '2025',
  },
  {
    id: 'budget-subs-2-2025',
    categoryId: 'cat-subs-2', // Spotify
    amount: 130,
    period: 'annual',
    year: '2025',
  },

  // DEBTS budgets for 2025
  {
    id: 'budget-debts-1-2025',
    categoryId: 'cat-debts-1', // Mortgage
    amount: 15500,
    period: 'annual',
    year: '2025',
  },
  {
    id: 'budget-debts-2-2025',
    categoryId: 'cat-debts-2', // Credit Card
    amount: 2000,
    period: 'annual',
    year: '2025',
  },

  // Monthly budget examples for January 2025
  {
    id: 'budget-expense-1-jan-2025',
    categoryId: 'cat-expense-1', // Groceries
    amount: 550,
    period: 'monthly',
    month: '2025-01',
    year: '2025',
  },
  {
    id: 'budget-expense-2-jan-2025',
    categoryId: 'cat-expense-2', // Dining Out
    amount: 220,
    period: 'monthly',
    month: '2025-01',
    year: '2025',
  },
  {
    id: 'budget-bills-1-jan-2025',
    categoryId: 'cat-bills-1', // Electricity Bill
    amount: 110,
    period: 'monthly',
    month: '2025-01',
    year: '2025',
  },
  {
    id: 'budget-bills-2-jan-2025',
    categoryId: 'cat-bills-2', // Internet Bill
    amount: 55,
    period: 'monthly',
    month: '2025-01',
    year: '2025',
  },

  // Additional budget entries for new categories
  // 2025 budgets for new expense categories
  {
    id: 'budget-expense-3-2025',
    categoryId: 'cat-expense-3', // Healthcare
    amount: 2400,
    period: 'annual',
    year: '2025',
  },
  {
    id: 'budget-expense-4-2025',
    categoryId: 'cat-expense-4', // Insurance
    amount: 3600,
    period: 'annual',
    year: '2025',
  },
  {
    id: 'budget-expense-7-2025',
    categoryId: 'cat-expense-7', // Entertainment
    amount: 1200,
    period: 'annual',
    year: '2025',
  },
  {
    id: 'budget-expense-8-2025',
    categoryId: 'cat-expense-8', // Shopping
    amount: 2000,
    period: 'annual',
    year: '2025',
  },
  {
    id: 'budget-expense-9-2025',
    categoryId: 'cat-expense-9', // Travel
    amount: 3000,
    period: 'annual',
    year: '2025',
  },
  {
    id: 'budget-expense-10-2025',
    categoryId: 'cat-expense-10', // Personal Care
    amount: 800,
    period: 'annual',
    year: '2025',
  },

  // 2025 budgets for new bill categories
  {
    id: 'budget-bills-3-2025',
    categoryId: 'cat-bills-3', // Phone Bill
    amount: 660,
    period: 'annual',
    year: '2025',
  },
  {
    id: 'budget-bills-4-2025',
    categoryId: 'cat-bills-4', // Water Bill
    amount: 420,
    period: 'annual',
    year: '2025',
  },
  {
    id: 'budget-bills-5-2025',
    categoryId: 'cat-bills-5', // Gas Bill
    amount: 800,
    period: 'annual',
    year: '2025',
  },
  {
    id: 'budget-bills-6-2025',
    categoryId: 'cat-bills-6', // Rent/Mortgage
    amount: 18000,
    period: 'annual',
    year: '2025',
  },

  // 2025 budgets for new savings categories
  {
    id: 'budget-savings-3-2025',
    categoryId: 'cat-savings-3', // Investment
    amount: 6000,
    period: 'annual',
    year: '2025',
  },
  {
    id: 'budget-savings-4-2025',
    categoryId: 'cat-savings-4', // Education Fund
    amount: 2000,
    period: 'annual',
    year: '2025',
  },

  // 2025 budgets for new subscription categories
  {
    id: 'budget-subs-3-2025',
    categoryId: 'cat-subs-3', // Amazon Prime
    amount: 180,
    period: 'annual',
    year: '2025',
  },
  {
    id: 'budget-subs-4-2025',
    categoryId: 'cat-subs-4', // Gym Membership
    amount: 360,
    period: 'annual',
    year: '2025',
  },
  {
    id: 'budget-subs-5-2025',
    categoryId: 'cat-subs-5', // Cloud Storage
    amount: 120,
    period: 'annual',
    year: '2025',
  },
  {
    id: 'budget-subs-6-2025',
    categoryId: 'cat-subs-6', // News/Magazines
    amount: 240,
    period: 'annual',
    year: '2025',
  },

  // 2025 budgets for new debt categories
  {
    id: 'budget-debts-3-2025',
    categoryId: 'cat-debts-3', // Student Loan
    amount: 3600,
    period: 'annual',
    year: '2025',
  },
  {
    id: 'budget-debts-4-2025',
    categoryId: 'cat-debts-4', // Car Loan
    amount: 4800,
    period: 'annual',
    year: '2025',
  },

  // 2024 budgets for new categories
  {
    id: 'budget-expense-3-2024',
    categoryId: 'cat-expense-3', // Healthcare
    amount: 2200,
    period: 'annual',
    year: '2024',
  },
  {
    id: 'budget-expense-4-2024',
    categoryId: 'cat-expense-4', // Insurance
    amount: 3400,
    period: 'annual',
    year: '2024',
  },
  {
    id: 'budget-expense-7-2024',
    categoryId: 'cat-expense-7', // Entertainment
    amount: 1000,
    period: 'annual',
    year: '2024',
  },
  {
    id: 'budget-expense-8-2024',
    categoryId: 'cat-expense-8', // Shopping
    amount: 1800,
    period: 'annual',
    year: '2024',
  },
  {
    id: 'budget-expense-9-2024',
    categoryId: 'cat-expense-9', // Travel
    amount: 2500,
    period: 'annual',
    year: '2024',
  },
  {
    id: 'budget-bills-3-2024',
    categoryId: 'cat-bills-3', // Phone Bill
    amount: 600,
    period: 'annual',
    year: '2024',
  },
  {
    id: 'budget-bills-4-2024',
    categoryId: 'cat-bills-4', // Water Bill
    amount: 400,
    period: 'annual',
    year: '2024',
  },
  {
    id: 'budget-subs-4-2024',
    categoryId: 'cat-subs-4', // Gym Membership
    amount: 330,
    period: 'annual',
    year: '2024',
  },
]; 