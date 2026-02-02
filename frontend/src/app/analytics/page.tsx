'use client';

import { Box, Paper, Typography, FormControl, InputLabel, Select, MenuItem, Grid, useTheme, Chip, LinearProgress } from '@mui/material';
import Layout from '../../components/Layout';
import { useQuery } from '@tanstack/react-query';
import { service } from '../../services/api';
import { useState } from 'react';
import MonthlyDataTable from '../../components/charts/MonthlyDataTable';
import { BarChart, Bar, XAxis, YAxis, Tooltip, Legend, ResponsiveContainer, CartesianGrid } from 'recharts';
import { CheckCircle, Warning } from '@mui/icons-material';

export default function AnalyticsPage() {
  const theme = useTheme();
  const isDark = theme.palette.mode === 'dark';
  const currentYear = new Date().getFullYear();
  const currentMonth = new Date().getMonth() + 1; // getMonth() returns 0-11, we need 1-12

  // Calculate previous month (handling year boundary)
  const getPreviousMonth = () => {
    if (currentMonth === 1) {
      return { month: 12, year: currentYear - 1 };
    }
    return { month: currentMonth - 1, year: currentYear };
  };

  const previousMonth = getPreviousMonth();

  const [selectedYear, setSelectedYear] = useState<number>(previousMonth.year);
  const [selectedMonth, setSelectedMonth] = useState<number | undefined>(previousMonth.month);

  // Year options for the selector (current year and 4 years back)
  const yearOptions = [];
  for (let year = currentYear; year >= currentYear - 4; year--) {
    yearOptions.push({ value: year, label: year.toString() });
  }

  // Month options
  const monthOptions = [
    { value: undefined, label: 'All Year' },
    { value: 1, label: 'January' },
    { value: 2, label: 'February' },
    { value: 3, label: 'March' },
    { value: 4, label: 'April' },
    { value: 5, label: 'May' },
    { value: 6, label: 'June' },
    { value: 7, label: 'July' },
    { value: 8, label: 'August' },
    { value: 9, label: 'September' },
    { value: 10, label: 'October' },
    { value: 11, label: 'November' },
    { value: 12, label: 'December' },
  ];

  const months = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];
  const monthNumberMap: { [key: string]: number } = {
    'Jan': 1, 'Feb': 2, 'Mar': 3, 'Apr': 4, 'May': 5, 'Jun': 6, 'Jul': 7, 'Aug': 8, 'Sep': 9, 'Oct': 10, 'Nov': 11, 'Dec': 12
  };
  // Fetch data
  const { data: monthlyData = [] } = useQuery({
    queryKey: ['monthlyData', selectedYear, selectedMonth],
    queryFn: async () => {
      const data = await service.getMonthlyData(selectedYear, selectedMonth);
      return data;
    }
  });

  const { data: categorySpending = [] } = useQuery({
    queryKey: ['categorySpending', selectedYear, selectedMonth],
    queryFn: async () => {
      const data = await service.getCategorySpending(selectedYear, selectedMonth);
      return data;
    },
  });
  const { data: categoryIncome = [] } = useQuery({
    queryKey: ['categoryIncome', selectedYear, selectedMonth],
    queryFn: async () => {
      const data = await service.getCategoryIncome(selectedYear, selectedMonth);
      return data;
    },
  });
  const { data: categorySavings = [] } = useQuery({
    queryKey: ['categorySavings', selectedYear, selectedMonth],
    queryFn: async () => {
      const data = await service.getCategorySavings(selectedYear, selectedMonth);
      return data;
    },
  });


  // Prepare pie chart data for Income
  const incomeData = categoryIncome

    .map(cat => ({
      name: cat.categoryName,
      value: cat.totalSpent,
      budget: cat.budgetedAmount || 0
    }));

  // Prepare pie chart data for Expenses
  const expenseData = categorySpending
    .map(cat => ({
      name: cat.categoryName,
      value: Math.abs(cat.totalSpent),
      budget: cat.budgetedAmount || 0
    }));

  // Prepare pie chart data for Savings
  const savingsData = categorySavings
    .map(cat => ({
      name: cat.categoryName,
      value: Math.abs(cat.totalSpent),
      budget: cat.budgetedAmount || 0
    }));

  // Prepare monthly income data for table
  const monthlyIncomeData = months.map((month, index) => {
    const monthNumber = monthNumberMap[month].toString().padStart(2, '0');
    const monthData = monthlyData.find(d => d.month === monthNumber.toString() && d.year === selectedYear.toString());
    return {
      month: `${month}-${selectedYear}`,
      budget: monthData?.incomeBudget || 0, // Mock budget data
      actual: monthData ? monthData.totalIncome : 0
    };
  });

  // Prepare monthly expenses data for table
  const monthlyExpensesData = months.map((month, index) => {
    const monthNumber = monthNumberMap[month].toString().padStart(2, '0');
    const monthData = monthlyData.find(d => d.month === monthNumber.toString() && d.year === selectedYear.toString());
    return {
      month: `${month}-${selectedYear}`,
      budget: monthData?.expensesBudget || 0,
      actual: monthData ? Math.abs(monthData.totalExpenses) : 0
    };
  });
  // Prepare monthly savings data for table
  const monthlySavingsData = months.map((month, index) => {
    const monthNumber = monthNumberMap[month].toString().padStart(2, '0');
    const monthDataSavings = monthlyData.find(d => d.month === monthNumber && d.year === selectedYear.toString());

    return {
      month: `${month}-${selectedYear}`,
      budget: monthDataSavings?.savingsBudget || 0,
      actual: monthDataSavings ? Math.abs(monthDataSavings.totalSavings) : 0
    };
  });

  // Calculate totals
  const totalIncomeBudget = monthlyIncomeData.reduce((sum, d) => sum + d.budget, 0);
  const totalIncomeActual = monthlyIncomeData.reduce((sum, d) => sum + d.actual, 0);
  const totalExpensesBudget = monthlyExpensesData.reduce((sum, d) => sum + d.budget, 0);
  const totalExpensesActual = monthlyExpensesData.reduce((sum, d) => sum + d.actual, 0);
  const totalSavingsBudget = monthlySavingsData.reduce((sum, d) => sum + d.budget, 0);
  const totalSavingsActual = monthlySavingsData.reduce((sum, d) => sum + d.actual, 0);

  // Prepare income vs savings bar chart data - monthly only
  const incomeVsSavingsData = months.map((month, index) => {
    const monthStr = monthNumberMap[month].toString().padStart(2, '0');
    const monthData = monthlyData.find(d => d.month === monthStr && d.year === selectedYear.toString());
    return {
      month,
      Income: monthData ? monthData.totalIncome : 0,
      Savings: monthData ? Math.abs(monthData.totalSavings) : 0
    };
  });

  // Prepare income vs expense bar chart data - monthly only
  const incomeVsExpenseData = months.map((month, index) => {
    const monthStr = monthNumberMap[month].toString().padStart(2, '0');
    const monthData = monthlyData.find(d => d.month === monthStr && d.year === selectedYear.toString());
    return {
      month,
      Income: monthData ? monthData.totalIncome : 0,
      Expense: monthData ? Math.abs(monthData.totalExpenses) : 0
    };
  });

  // Prepare Bills, Debts, Subscriptions data
  const billsData = categorySpending
    .filter(cat => cat.macroCategory === 'BILLS');

  const debtsData = categorySpending
    .filter(cat => cat.macroCategory === 'DEBTS');

  const subscriptionsData = categorySpending
    .filter(cat => cat.macroCategory === 'SUBSCRIPTIONS');

  const calculatePercentage = (actual: number, budget: number, isExpense: boolean) => {
    if (budget === 0) return 0;
    var percentage = isExpense ? (actual / budget) * 100 : (budget / actual) * 100;
    return isExpense ? percentage : 100 - percentage;
  };

  return (
    <Layout>
      <Box sx={{ bgcolor: '#f5f6fa', minHeight: '100vh', p: { xs: 2, md: 4 } }}>
        {/* Header */}
        <Box sx={{ mb: 4, display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: 2 }}>
          <Box>
            <Typography variant="h4" fontWeight={700} color="text.primary" gutterBottom>
              {selectedMonth ? `${monthOptions.find(m => m.value === selectedMonth)?.label} ${selectedYear}` : `${selectedYear}`} Analytics
            </Typography>
            <Typography variant="body1" color="text.secondary">
              {selectedMonth ? 'Monthly financial breakdown and insights' : 'Annual financial breakdown and insights'}
            </Typography>
          </Box>
          <Box sx={{ display: 'flex', gap: 2 }}>
            <FormControl sx={{ minWidth: 150 }}>
              <InputLabel id="year-selector-label">Year</InputLabel>
              <Select
                labelId="year-selector-label"
                value={selectedYear}
                label="Year"
                onChange={(e) => setSelectedYear(e.target.value as number)}
                sx={{ bgcolor: 'white' }}
              >
                {yearOptions.map((option) => (
                  <MenuItem key={option.value} value={option.value}>
                    {option.label}
                  </MenuItem>
                ))}
              </Select>
            </FormControl>
            <FormControl sx={{ minWidth: 180 }}>
              <InputLabel id="month-selector-label">Month</InputLabel>
              <Select
                labelId="month-selector-label"
                value={selectedMonth === undefined ? '' : selectedMonth}
                label="Month"
                onChange={(e) => setSelectedMonth(e.target.value === '' ? undefined : e.target.value as number)}
                sx={{ bgcolor: 'white' }}
              >
                {monthOptions.map((option) => (
                  <MenuItem key={option.label} value={option.value === undefined ? '' : option.value}>
                    {option.label}
                  </MenuItem>
                ))}
              </Select>
            </FormControl>
          </Box>
        </Box>

        {/* Category Budget Comparison Widget */}
        <Paper elevation={4} sx={{ p: 3, borderRadius: 4, boxShadow: '0 4px 24px rgba(0,0,0,0.08)', mb: 4, background: 'linear-gradient(135deg, #f3e5f5 0%, #ffffff 100%)' }}>
          <Typography variant="h6" fontWeight={700} color="text.primary" mb={3} textAlign="center">
            CATEGORY BUDGET COMPARISON
          </Typography>
          {(() => {
            // Group categories by macro category
            const allCategories = [...categoryIncome, ...categorySpending, ...categorySavings];

            const groupedByMacro = allCategories.reduce((acc, cat) => {
              const macro = cat.macroCategory || 'OTHER';
              if (!acc[macro]) acc[macro] = [];
              acc[macro].push(cat);
              return acc;
            }, {} as { [key: string]: typeof allCategories });

            // Define macro category colors and labels
            const macroCategoryConfig: { [key: string]: { color: string; label: string; bgcolor: string } } = {
              'INCOME': { color: '#4caf50', label: 'Income', bgcolor: 'rgba(76, 175, 80, 0.08)' },
              'EXPENSE': { color: '#f44336', label: 'Expenses', bgcolor: 'rgba(244, 67, 54, 0.08)' },
              'BILLS': { color: '#ff9800', label: 'Bills', bgcolor: 'rgba(255, 152, 0, 0.08)' },
              'SUBSCRIPTIONS': { color: '#9c27b0', label: 'Subscriptions', bgcolor: 'rgba(156, 39, 176, 0.08)' },
              'DEBTS': { color: '#795548', label: 'Debts', bgcolor: 'rgba(121, 85, 72, 0.08)' },
              'SAVINGS': { color: '#2196f3', label: 'Savings', bgcolor: 'rgba(33, 150, 243, 0.08)' },
              'OTHER': { color: '#607d8b', label: 'Other', bgcolor: 'rgba(96, 125, 139, 0.08)' }
            };

            return Object.entries(groupedByMacro).map(([macroCategory, categories]) => {
              const config = macroCategoryConfig[macroCategory] || macroCategoryConfig['OTHER'];

              return (
                <Box key={macroCategory} sx={{ mb: 3 }}>
                  <Box sx={{
                    bgcolor: config.color,
                    p: 1.5,
                    mb: 1,
                    borderRadius: 2
                  }}>
                    <Typography variant="subtitle1" fontWeight={700} color="white">
                      {config.label}
                    </Typography>
                  </Box>
                  {categories.sort((a, b) => b.totalSpent - a.totalSpent).map((category) => {
                    const isExpense = categorySpending.some(c => c.categoryName === category.categoryName);

                    const actualAmount = Math.abs(category.totalSpent);
                    const budgetAmount = category.budgetedAmount || 0;

                    // Check if budget is zero
                    const targetedBudget = budgetAmount === 0;

                    // For expenses: under budget is good (green)
                    // For income/savings: over budget is good (green)
                    const difference = isExpense ? (budgetAmount - actualAmount) : (actualAmount - budgetAmount);
                    const isGood = !targetedBudget && difference > 0;
                    const percentage = calculatePercentage(actualAmount, budgetAmount, isExpense);

                    return (
                      <Box key={category.categoryName} sx={{
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'space-between',
                        p: 2,
                        mb: 1,
                        borderRadius: 2,
                        bgcolor: 'white',
                        borderLeft: `4px solid ${targetedBudget ? '#9e9e9e' : (isGood ? '#4caf50' : '#f44336')}`,
                        boxShadow: '0 1px 4px rgba(0,0,0,0.05)',
                        transition: 'transform 0.2s, box-shadow 0.2s',
                        '&:hover': {
                          transform: 'translateX(4px)',
                          boxShadow: '0 2px 8px rgba(0,0,0,0.1)',
                        }
                      }}>
                        <Box sx={{ flex: 1, minWidth: 0 }}>
                          <Typography variant="body1" fontWeight={600} noWrap>
                            {category.categoryName}
                          </Typography>
                          <Box sx={{ display: 'flex', gap: 2, mt: 0.5 }}>
                            <Typography variant="caption" color="text.secondary">
                              Actual: <strong>€{actualAmount.toLocaleString()}</strong>
                            </Typography>
                            <Typography variant="caption" color="text.secondary">
                              Budget: <strong>{targetedBudget ? 'N/A' : `€${budgetAmount.toLocaleString()}`}</strong>
                            </Typography>
                            {!targetedBudget && (
                              <Typography variant="caption" color={isGood ? '#4caf50' : '#f44336'}>
                                <strong>{Math.round(percentage)}%</strong>
                              </Typography>
                            )}
                          </Box>
                        </Box>
                        <Box sx={{ ml: 2 }}>
                          {targetedBudget ? (
                            <Chip
                              label="Targeted Budget"
                              size="small"
                              sx={{
                                bgcolor: '#9e9e9e',
                                color: 'white',
                                fontWeight: 600
                              }}
                            />
                          ) : (
                            <Chip
                              icon={isGood ? <CheckCircle /> : <Warning />}
                              label={`€${Math.abs(difference).toLocaleString()}`}
                              size="small"
                              sx={{
                                bgcolor: isGood ? '#4caf50' : '#f44336',
                                color: 'white',
                                fontWeight: 600,
                                '& .MuiChip-icon': {
                                  color: 'white'
                                }
                              }}
                            />
                          )}
                        </Box>
                      </Box>
                    );
                  })}
                </Box>
              );
            });
          })()}
        </Paper>

        {/* Income vs Savings Bar Chart */}
        <Box sx={{ mb: 4 }}>
          <Paper elevation={4} sx={{ p: 3, borderRadius: 4, boxShadow: '0 4px 24px #b2dfdb33', background: 'linear-gradient(135deg, #e8f5e9 0%, #ffffff 100%)' }}>
            <Typography variant="h6" fontWeight={700} color="text.primary" mb={2} textAlign="center">
              INCOME vs SAVINGS (Monthly)
            </Typography>
            <Box sx={{ width: '100%', height: 300 }}>
              <ResponsiveContainer width="100%" height="100%">
                <BarChart data={incomeVsSavingsData} margin={{ top: 20, right: 30, left: 20, bottom: 5 }}>
                  <CartesianGrid strokeDasharray="3 3" stroke={isDark ? '#444' : '#ccc'} />
                  <XAxis
                    dataKey="month"
                    tick={{ fontSize: 12, fill: theme.palette.text.primary }}
                    stroke={theme.palette.text.secondary}
                  />
                  <YAxis
                    tick={{ fontSize: 12, fill: theme.palette.text.secondary }}
                    tickFormatter={(value) => `€${value.toLocaleString()}`}
                    stroke={theme.palette.text.secondary}
                  />
                  <Tooltip
                    formatter={(value) => `€${Number(value).toLocaleString()}`}
                    contentStyle={{
                      backgroundColor: isDark ? '#2c2c2c' : '#ffffff',
                      border: `1px solid ${isDark ? '#444' : '#ccc'}`,
                      borderRadius: '8px',
                      color: isDark ? '#ffffff' : '#000000'
                    }}
                  />
                  <Legend wrapperStyle={{ color: theme.palette.text.primary }} />
                  <Bar dataKey="Income" fill="#4caf50" radius={[4, 4, 0, 0]} />
                  <Bar dataKey="Savings" fill="#2196f3" radius={[4, 4, 0, 0]} />
                </BarChart>
              </ResponsiveContainer>
            </Box>
          </Paper>
        </Box>

        {/* Savings Breakdown Section */}
        <Box sx={{ mb: 4 }}>
          <Paper elevation={4} sx={{ p: 3, borderRadius: 4, boxShadow: '0 4px 24px rgba(33,150,243,0.15)', background: 'linear-gradient(135deg, #e3f2fd 0%, #ffffff 100%)' }}>
            <Typography variant="h6" fontWeight={700} color="info.main" mb={3} textAlign="center">
              SAVINGS BY CATEGORY
            </Typography>

            {savingsData.length > 0 ? (
              <>
                {/* Total Savings Summary */}
                <Box sx={{
                  bgcolor: '#2196f3',
                  p: 3,
                  borderRadius: 3,
                  mb: 3,
                  boxShadow: '0 2px 8px rgba(0,0,0,0.1)',
                  textAlign: 'center'
                }}>
                  <Typography variant="h6" fontWeight={700} color="white">
                    TOTAL SAVINGS
                  </Typography>
                  <Typography variant="h4" fontWeight={700} color="white" mt={1}>
                    € {savingsData.reduce((sum, cat) => sum + cat.value, 0).toLocaleString()}
                  </Typography>
                </Box>

                {/* Savings Categories Grid */}
                <Box sx={{
                  display: 'grid',
                  gridTemplateColumns: {
                    xs: '1fr',
                    sm: 'repeat(2, 1fr)',
                    md: 'repeat(3, 1fr)',
                  },
                  gap: 2
                }}>
                  {savingsData.sort((a, b) => b.value - a.value).map((item, index) => {
                    const difference = item.value - item.budget;
                    const isOverBudget = difference > 0;
                    const percentage = item.budget > 0 ? (item.value / item.budget) * 100 : 0;

                    return (
                      <Box key={index} sx={{
                        p: 2,
                        bgcolor: 'white',
                        borderRadius: 2,
                        boxShadow: '0 1px 4px rgba(0,0,0,0.05)',
                        transition: 'all 0.2s',
                        '&:hover': {
                          transform: 'translateY(-2px)',
                          boxShadow: '0 2px 8px rgba(0,0,0,0.1)'
                        }
                      }}>
                        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 1 }}>
                          <Typography variant="body2" fontWeight={600} noWrap sx={{ maxWidth: '60%' }}>
                            {item.name}
                          </Typography>
                          <Chip
                            label={`${Math.round(percentage)}%`}
                            size="small"
                            sx={{
                              bgcolor: percentage >= 100 ? 'success.light' : 'warning.light',
                              color: percentage >= 100 ? 'success.dark' : 'warning.dark',
                              fontWeight: 700,
                              height: 20,
                              fontSize: '0.7rem'
                            }}
                          />
                        </Box>

                        <Box sx={{ mb: 1.5 }}>
                          <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 0.5 }}>
                            <Typography variant="caption" color="text.secondary">Saved</Typography>
                            <Typography variant="body2" fontWeight={700} color="info.main">
                              €{item.value.toLocaleString()}
                            </Typography>
                          </Box>
                          <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 1 }}>
                            <Typography variant="caption" color="text.secondary">Goal</Typography>
                            <Typography variant="body2" fontWeight={600} color="text.secondary">
                              €{item.budget.toLocaleString()}
                            </Typography>
                          </Box>
                        </Box>

                        {item.budget > 0 && (
                          <>
                            <LinearProgress
                              variant="determinate"
                              value={Math.min(100, percentage)}
                              sx={{
                                height: 8,
                                borderRadius: 1,
                                bgcolor: 'grey.200',
                                mb: 1,
                                '& .MuiLinearProgress-bar': {
                                  bgcolor: percentage >= 100 ? 'success.main' : 'info.main',
                                },
                              }}
                            />
                            <Box sx={{ display: 'flex', justifyContent: 'center' }}>
                              <Chip
                                icon={isOverBudget ? <CheckCircle /> : <Warning />}
                                label={`€${Math.abs(difference).toLocaleString()}`}
                                size="small"
                                sx={{
                                  bgcolor: isOverBudget ? '#4caf50' : '#ff9800',
                                  color: 'white',
                                  fontWeight: 600,
                                  height: 20,
                                  '& .MuiChip-icon': {
                                    color: 'white',
                                    fontSize: '0.9rem'
                                  },
                                  '& .MuiChip-label': {
                                    fontSize: '0.7rem',
                                    px: 1
                                  }
                                }}
                              />
                            </Box>
                          </>
                        )}
                      </Box>
                    );
                  })}
                </Box>
              </>
            ) : (
              <Box sx={{ textAlign: 'center', py: 4 }}>
                <Typography variant="body1" color="text.secondary">
                  No savings data available for this period
                </Typography>
              </Box>
            )}
          </Paper>
        </Box>

        {/* Monthly Data Tables Row - Only show when viewing full year */}
        {!selectedMonth && (
          <Grid container spacing={3} sx={{ mb: 4 }}>
            <Grid item xs={12} md={4}>
              <MonthlyDataTable
                title="SALES"
                data={monthlyIncomeData}
                totalBudget={totalIncomeBudget}
                totalActual={totalIncomeActual}
                headerColor="#4caf50"
                type="income"
              />
            </Grid>
            <Grid item xs={12} md={4}>
              <MonthlyDataTable
                title="TOTAL EXPENSES"
                data={monthlyExpensesData}
                totalBudget={totalExpensesBudget}
                totalActual={totalExpensesActual}
                headerColor="#f44336"
                type="expense"
              />
            </Grid>
            <Grid item xs={12} md={4}>
              <MonthlyDataTable
                title="SAVINGS"
                data={monthlySavingsData}
                totalBudget={totalSavingsBudget}
                totalActual={totalSavingsActual}
                headerColor="#2196f3"
                type="savings"
              />
            </Grid>
          </Grid>
        )}
        {/* Income vs Expense Chart */}
        <Box sx={{ mb: 4 }}>
          <Paper elevation={4} sx={{ p: 3, borderRadius: 4, boxShadow: '0 4px 24px #b2dfdb33', background: 'linear-gradient(135deg, #ffebee 0%, #ffffff 100%)' }}>
            <Typography variant="h6" fontWeight={700} color="text.primary" mb={2} textAlign="center">
              INCOME vs EXPENSE (Monthly)
            </Typography>
            <Box sx={{ width: '100%', height: 300 }}>
              <ResponsiveContainer width="100%" height="100%">
                <BarChart data={incomeVsExpenseData} margin={{ top: 20, right: 30, left: 20, bottom: 5 }}>
                  <CartesianGrid strokeDasharray="3 3" stroke={isDark ? '#444' : '#ccc'} />
                  <XAxis
                    dataKey="month"
                    tick={{ fontSize: 12, fill: theme.palette.text.primary }}
                    stroke={theme.palette.text.secondary}
                  />
                  <YAxis
                    tick={{ fontSize: 12, fill: theme.palette.text.secondary }}
                    tickFormatter={(value) => `€${value.toLocaleString()}`}
                    stroke={theme.palette.text.secondary}
                  />
                  <Tooltip
                    formatter={(value) => `€${Number(value).toLocaleString()}`}
                    contentStyle={{
                      backgroundColor: isDark ? '#2c2c2c' : '#ffffff',
                      border: `1px solid ${isDark ? '#444' : '#ccc'}`,
                      borderRadius: '8px',
                      color: isDark ? '#ffffff' : '#000000'
                    }}
                  />
                  <Legend wrapperStyle={{ color: theme.palette.text.primary }} />
                  <Bar dataKey="Income" fill="#4caf50" radius={[4, 4, 0, 0]} />
                  <Bar dataKey="Expense" fill="#f44336" radius={[4, 4, 0, 0]} />
                </BarChart>
              </ResponsiveContainer>
            </Box>
          </Paper>
        </Box>

        {/* Total Expenses */}
        <Box sx={{ mb: 4 }}>
          <Paper elevation={4} sx={{ p: 3, borderRadius: 4, boxShadow: '0 4px 24px rgba(255,87,34,0.15)' }}>
            <Box sx={{ bgcolor: '#ff5722', p: 3, borderRadius: 3, boxShadow: '0 2px 8px rgba(0,0,0,0.1)' }}>
              <Typography variant="h6" fontWeight={700} color="white" textAlign="center">
                TOTAL EXPENSES
              </Typography>
              <Typography variant="h4" fontWeight={700} color="white" textAlign="center" mt={1}>
                € {expenseData.reduce((sum, cat) => sum + cat.value, 0).toLocaleString()}
              </Typography>
            </Box>
          </Paper>
        </Box>
      </Box>
    </Layout>
  );
}
