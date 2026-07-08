'use client';

import { Box, Paper, Typography, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, LinearProgress, Chip, FormControl, InputLabel, Select, MenuItem, Grid, useTheme, ToggleButton, ToggleButtonGroup, Tooltip as MuiTooltip, Skeleton } from '@mui/material';
import Layout from '../components/Layout';
import MacroCategoryBudgetTrend from '../components/charts/MacroCategoryBudgetTrend';
import ChartSkeleton from '../components/skeletons/ChartSkeleton';
import ListRowsSkeleton from '../components/skeletons/ListRowsSkeleton';
import { TrendingUp, TrendingDown, Savings, BarChart as MuiBarChart, Timeline, CheckCircle, Warning } from '@mui/icons-material';
import { Bar, BarChart, XAxis, YAxis, Tooltip, Legend, ResponsiveContainer, CartesianGrid, Line, ComposedChart, LabelList } from 'recharts';
import { useQuery } from '@tanstack/react-query';
import { service } from '../services/api';
import { MonthlyData } from '../types';
import { useState } from 'react';

export default function HomePage() {
  const theme = useTheme();
  const isDark = theme.palette.mode === 'dark';
  // const { user } = useAuth();
  const currentYear = new Date().getFullYear();
  const [selectedYear, setSelectedYear] = useState<number>(currentYear);
  const [categoryBreakdownView, setCategoryBreakdownView] = useState<'categories' | 'groups'>('categories');
  const [showAllCategoryBreakdown, setShowAllCategoryBreakdown] = useState(false);
  const [showAllSavingsBreakdown, setShowAllSavingsBreakdown] = useState(false);
  const [showAllInvestmentsBreakdown, setShowAllInvestmentsBreakdown] = useState(false);
  const BREAKDOWN_LIST_LIMIT = 8;
  // Year options for the selector (current year and 4 years back)
  const yearOptions = [];
  for (let year = currentYear; year >= currentYear - 4; year--) {
    yearOptions.push({ value: year, label: year.toString() });
  }

  // Fetch data with month filtering
  const { data: totalIncome = 0, isLoading: isLoadingTotalIncome } = useQuery({
    queryKey: ['totalIncome', selectedYear],
    queryFn: () => service.getTotalIncome(selectedYear),
  });

  const { data: totalExpenses = 0, isLoading: isLoadingTotalExpenses } = useQuery({
    queryKey: ['totalExpenses', selectedYear],
    queryFn: () => service.getTotalExpenses(selectedYear),
  });

  const { data: totalSavings = 0, isLoading: isLoadingTotalSavings } = useQuery({
    queryKey: ['totalSavings', selectedYear],
    queryFn: () => service.getTotalSavings(selectedYear),
  });

  const { data: incomeBudget = 0, isLoading: isLoadingIncomeBudget } = useQuery({
    queryKey: ['incomeBudget', selectedYear],
    queryFn: () => service.getIncomeBudget(selectedYear),
  });

  const { data: expenseBudget = 0, isLoading: isLoadingExpenseBudget } = useQuery({
    queryKey: ['expenseBudget', selectedYear],
    queryFn: () => service.getExpenseBudget(selectedYear),
  });

  const { data: savingsBudget = 0, isLoading: isLoadingSavingsBudget } = useQuery({
    queryKey: ['savingsBudget', selectedYear],
    queryFn: () => service.getSavingsBudget(selectedYear),
  });


  const { data: categorySpending = [], isLoading: isLoadingCategorySpending } = useQuery({
    queryKey: ['categorySpending', selectedYear],
    queryFn: () => service.getCategorySpending(selectedYear),
  });

  const { data: categorySavings = [], isLoading: isLoadingCategorySavings } = useQuery({
    queryKey: ['categorySavings', selectedYear],
    queryFn: () => service.getCategorySavings(selectedYear),
  });

  const { data: totalInvestments = 0, isLoading: isLoadingTotalInvestments } = useQuery({
    queryKey: ['totalInvestments', selectedYear],
    queryFn: () => service.getTotalInvestments(selectedYear),
  });

  const { data: investmentsBudget = 0, isLoading: isLoadingInvestmentsBudget } = useQuery({
    queryKey: ['investmentsBudget', selectedYear],
    queryFn: () => service.getInvestmentsBudget(selectedYear),
  });

  const { data: categoryInvestments = [], isLoading: isLoadingCategoryInvestments } = useQuery({
    queryKey: ['categoryInvestments', selectedYear],
    queryFn: () => service.getCategoryInvestments(selectedYear),
  });

  // Calculate budget data
  const totalBudget = incomeBudget + expenseBudget + savingsBudget + investmentsBudget;

  // Money Flow data - show different data based on month selection
  const months = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];
  const { data: monthlyData = [], isLoading: isLoadingMonthlyData } = useQuery({
    queryKey: ['monthlyData', selectedYear],
    queryFn: () => service.getMonthlyData(selectedYear)
  });

  // Prepare income vs savings / income vs expense bar chart data - always all 12 months
  const incomeVsSavingsData = months.map((month, index) => {
    const monthStr = (index + 1).toString().padStart(2, '0');
    const monthData = monthlyData.find((d: MonthlyData) => d.month === monthStr && d.year === selectedYear.toString());
    return {
      month,
      Income: monthData ? monthData.totalIncome : 0,
      Savings: monthData ? Math.abs(monthData.totalSavings) : 0
    };
  });

  const incomeVsExpenseData = months.map((month, index) => {
    const monthStr = (index + 1).toString().padStart(2, '0');
    const monthData = monthlyData.find((d: MonthlyData) => d.month === monthStr && d.year === selectedYear.toString());
    return {
      month,
      Income: monthData ? monthData.totalIncome : 0,
      Expense: monthData ? Math.abs(monthData.totalExpenses) : 0
    };
  });

  // Account Flow data
  const { data: accountFlowData = [], isLoading: isLoadingAccountFlowData } = useQuery({
    queryKey: ['accountFlowData', selectedYear],
    queryFn: () => service.getAccountFlowData(selectedYear)
  });

  // Fetch accounts from API
  const { data: accounts = [], isLoading: isLoadingAccounts } = useQuery({
    queryKey: ['accounts'],
    queryFn: () => service.getAccounts()
  });

  const { data: macroCategoryBudgetTrend = [], isLoading: isLoadingMacroCategoryBudgetTrend } = useQuery({
    queryKey: ['macroCategoryBudgetTrend', selectedYear],
    queryFn: () => service.getMacroCategoryBudgetTrend(selectedYear)
  });

  // Per-section loading flags, derived from the queries each section actually depends on
  const isLoadingAnnualSummary = isLoadingIncomeBudget || isLoadingExpenseBudget || isLoadingSavingsBudget || isLoadingInvestmentsBudget || isLoadingTotalIncome || isLoadingTotalExpenses || isLoadingTotalSavings || isLoadingTotalInvestments;
  const isLoadingIncomeCard = isLoadingTotalIncome || isLoadingIncomeBudget;
  const isLoadingExpensesCard = isLoadingTotalExpenses || isLoadingExpenseBudget;
  const isLoadingSavingsCard = isLoadingTotalSavings || isLoadingSavingsBudget;
  const isLoadingInvestmentsCard = isLoadingTotalInvestments || isLoadingInvestmentsBudget;
  const isLoadingAccountFlow = isLoadingAccountFlowData || isLoadingAccounts;
  const isLoadingBudgetTrend = isLoadingMacroCategoryBudgetTrend || isLoadingCategorySpending;

  // Define colors for each macro category
  const macroCategoryColors: { [key: string]: string } = {
    'EXPENSE': '#f44336',
    'BILLS': '#ff9800',
    'SAVINGS': '#2196f3',
    'SUBSCRIPTIONS': '#9c27b0',
    'DEBTS': '#795548',
    'INCOME': '#4caf50',
    'INVESTMENTS': '#00897b'
  };

  // Transform account flow data for chart
  const transformedAccountData = (() => {
    if (!accountFlowData || accountFlowData.length === 0) return [];

    // Group data by period and aggregate by account
    const periodGroups: { [period: string]: { [account: string]: { total: number } } } = {};

    accountFlowData.forEach(item => {
      if (!periodGroups[item.period]) {
        periodGroups[item.period] = {};
      }
      periodGroups[item.period][item.accountName] = {
        total: item.total
      };
    });

    // Convert to chart format and add total line
    return Object.keys(periodGroups).map(period => {
      const result: { [key: string]: string | number } = { period };
      let totalAcrossAccounts = 0;

      Object.keys(periodGroups[period]).forEach(account => {
        const accountTotal = periodGroups[period][account].total;
        result[`${account}_Total`] = accountTotal;
        totalAcrossAccounts += accountTotal;
      });

      // Add total line data
      result['Total_All_Accounts'] = totalAcrossAccounts;

      return result;
    });
  })();

  // Get unique accounts for the account chart
  const uniqueAccounts = [...new Set(accountFlowData.map(item => item.accountName))];

  // Generate dynamic colors for accounts based on API data
  const generateAccountColors = (accounts: { name: string }[]) => {
    const colors = ['#4c93afff', '#2196f3', '#ff9800', '#9c27b0', '#f44336', '#795548', '#607d8b'];
    const accountColors: { [key: string]: string } = {};
    accounts.forEach((account, index) => {
      accountColors[account.name] = colors[index % colors.length];
    });
    return accountColors;
  };

  const accountColors = generateAccountColors(accounts);

  // --- 50-30-20 Budget Fake Data ---
  type BudgetKey = 'needs' | 'wants' | 'savingsDebts';
  type BudgetKeyWithTotal = BudgetKey | 'total';

  const budget502010Map = categorySpending
    .filter(c => c.categoryType)
    .reduce((acc, curr) => {
      if (curr.categoryType) {
        acc[curr.categoryType] = {
          totalSpent: curr.totalSpent + acc[curr.categoryType]?.totalSpent || 0,
          budget: curr.budgetedAmount || 0 + acc[curr.categoryType]?.budget || 0
        }
        acc['TOTAL'] = {
          totalSpent: (acc['TOTAL']?.totalSpent || 0) + (curr.totalSpent || 0),
          budget: (acc['TOTAL']?.budget || 0) + (curr.budgetedAmount || 0)
        }
      }
      return acc;
    }, {
      'TOTAL': {
        totalSpent: 0,
        budget: 0

      },
      'NEEDS': {
        totalSpent: 0,
        budget: 0
      },
      'WANTS': {
        totalSpent: 0,
        budget: 0
      },
      'SAVINGS_DEBTS': {
        totalSpent: 0,
        budget: 0
      }
    });

  const budget502010 = {
    goal: {
      needs: 50,
      wants: 30,
      savingsDebts: 20,
    },
    actual: {
      needs: Math.round(((budget502010Map['NEEDS']?.totalSpent || 0) * 100 / (budget502010Map['TOTAL']?.totalSpent || 1)) * 100) / 100,
      wants: Math.round(((budget502010Map['WANTS']?.totalSpent || 0) * 100 / (budget502010Map['TOTAL']?.totalSpent || 1)) * 100) / 100,
      savingsDebts: Math.round(((budget502010Map['SAVINGS_DEBTS']?.totalSpent || 0) * 100 / (budget502010Map['TOTAL']?.totalSpent || 1)) * 100) / 100,
    },
    amount: {
      needs: {
        budget: budget502010Map['NEEDS']?.budget || 0,
        actual: budget502010Map['NEEDS']?.totalSpent || 0
      },
      wants: {
        budget: budget502010Map['WANTS']?.budget || 0,
        actual: budget502010Map['WANTS']?.totalSpent || 0
      },
      savingsDebts: {
        budget: budget502010Map['SAVINGS_DEBTS']?.budget || 0,
        actual: budget502010Map['SAVINGS_DEBTS']?.totalSpent || 0
      },
      total: {
        budget: budget502010Map['TOTAL']?.budget || 0,
        actual: budget502010Map['TOTAL']?.totalSpent || 0
      },
    },
  };

  // Calculate left to spend

  const leftToSpend = totalIncome + totalSavings + totalInvestments - totalExpenses;

  // --- Category Breakdown (bar chart + detail list) ---
  const categoryPalette = ['#4c93af', '#2196f3', '#ff9800', '#9c27b0', '#f44336', '#795548', '#607d8b', '#4caf50', '#00897b', '#e91e63', '#3f51b5', '#8bc34a'];
  const categoryColorMap: { [name: string]: string } = {};
  categorySpending.forEach((c, index) => {
    categoryColorMap[c.categoryName] = categoryPalette[index % categoryPalette.length];
  });

  const categoryBreakdownData = categoryBreakdownView === 'categories'
    ? categorySpending
      .map(c => ({
        name: c.categoryName,
        totalSpent: c.totalSpent,
        budgetedAmount: c.budgetedAmount || 0,
        color: categoryColorMap[c.categoryName],
      }))
      .sort((a, b) => b.totalSpent - a.totalSpent)
    : Object.values(
      categorySpending.reduce((acc, c) => {
        const key = c.macroCategory || 'OTHER';
        if (!acc[key]) acc[key] = { name: key, totalSpent: 0, budgetedAmount: 0 };
        acc[key].totalSpent += c.totalSpent;
        acc[key].budgetedAmount += c.budgetedAmount || 0;
        return acc;
      }, {} as { [key: string]: { name: string; totalSpent: number; budgetedAmount: number } })
    )
      .map(g => ({ ...g, color: macroCategoryColors[g.name] || '#607d8b' }))
      .sort((a, b) => b.totalSpent - a.totalSpent);

  const totalCategoryBreakdownSpend = categoryBreakdownData.reduce((sum, d) => sum + d.totalSpent, 0);
  const totalCategoryBreakdownBudget = categoryBreakdownData.reduce((sum, d) => sum + d.budgetedAmount, 0);
  const categoryBreakdownBarMax = Math.max(totalCategoryBreakdownSpend, totalCategoryBreakdownBudget, 1);

  // --- Savings / Investments Breakdown (fund-style: totals are net activity, not a growth signal - see below) ---
  const buildFundBreakdownData = (items: { categoryName: string; totalSpent: number; budgetedAmount?: number }[]) => items
    .map((c, index) => ({
      name: c.categoryName,
      amount: Math.abs(c.totalSpent),
      goal: c.budgetedAmount || 0,
      color: categoryPalette[index % categoryPalette.length],
    }))
    .sort((a, b) => b.amount - a.amount);

  const savingsBreakdownData = buildFundBreakdownData(categorySavings);
  const totalSavingsBreakdown = savingsBreakdownData.reduce((sum, d) => sum + d.amount, 0);
  const investmentsBreakdownData = buildFundBreakdownData(categoryInvestments);
  const totalInvestmentsBreakdown = investmentsBreakdownData.reduce((sum, d) => sum + d.amount, 0);

  // Note: the backend nets deposits against withdrawals per category but then takes the absolute
  // value of the final total, so a fund that grew and one that shrank by the same amount are
  // indistinguishable here. We show activity + a goal reference, not a directional judgement.
  const renderFundBreakdown = (
    data: { name: string; amount: number; goal: number; color: string }[],
    total: number,
    activityLabel: string,
    showAll: boolean,
    onToggleShowAll: () => void
  ) => (
    <>
      <Box sx={{ mb: 3 }}>
        <Typography variant="overline" color="text.secondary">
          Total {activityLabel}
        </Typography>
        <Typography variant="h4" fontWeight={800} color="text.primary">
          €{total.toLocaleString()}
        </Typography>
      </Box>
      <Box sx={{ display: 'flex', flexDirection: 'column' }}>
        {(showAll ? data : data.slice(0, BREAKDOWN_LIST_LIMIT)).map((item, index) => {
          const share = total > 0 ? (item.amount / total) * 100 : 0;
          return (
            <Box key={item.name} sx={{ py: 1.25, borderBottom: '1px solid', borderColor: 'grey.200' }}>
              <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', gap: 1 }}>
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, minWidth: 0 }}>
                  <Typography variant="caption" color="text.secondary" sx={{ width: 20, flexShrink: 0 }}>
                    {index + 1}
                  </Typography>
                  <Box sx={{ width: 10, height: 10, borderRadius: '50%', bgcolor: item.color, flexShrink: 0 }} />
                  <Typography variant="body2" fontWeight={700} noWrap>
                    {item.name}
                  </Typography>
                </Box>
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, flexShrink: 0 }}>
                  {item.goal > 0 && (
                    <Chip
                      variant="outlined"
                      size="small"
                      label={`Goal €${item.goal.toLocaleString()}`}
                      sx={{ height: 20, fontSize: '0.7rem', '& .MuiChip-label': { px: 1 } }}
                    />
                  )}
                  <Typography variant="body2" fontWeight={700}>
                    €{item.amount.toLocaleString()}
                  </Typography>
                </Box>
              </Box>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mt: 0.5, pl: 3.5 }}>
                <Box sx={{ flex: 1, height: 4, borderRadius: 2, bgcolor: 'grey.200', overflow: 'hidden' }}>
                  <Box sx={{ width: `${Math.min(100, share)}%`, height: '100%', bgcolor: item.color, opacity: 0.6 }} />
                </Box>
                <Typography variant="caption" color="text.secondary" sx={{ flexShrink: 0, minWidth: 70, textAlign: 'right' }}>
                  {share.toFixed(1)}% of total
                </Typography>
              </Box>
            </Box>
          );
        })}
      </Box>
      {data.length > BREAKDOWN_LIST_LIMIT && (
        <Box sx={{ display: 'flex', justifyContent: 'center', mt: 2 }}>
          <Chip
            clickable
            onClick={onToggleShowAll}
            label={showAll ? 'Show less' : `Show all ${data.length}`}
            size="small"
            sx={{ fontWeight: 600 }}
          />
        </Box>
      )}
    </>
  );

  return (
    <Layout>
      <Box sx={{ bgcolor: '#f5f6fa', minHeight: '100vh', p: { xs: 1, md: 4 } }}>
        {/* Year Selector */}
        <Box sx={{ mb: 3, display: 'flex', justifyContent: 'flex-end', gap: 2 }}>
          <FormControl sx={{ minWidth: 120 }}>
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
        </Box>

        {/* NEW: Annual Summary Section */}
        <Paper
          elevation={6}
          sx={{
            p: 4,
            mb: 4,
            borderRadius: 4,
            background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
            color: 'white'
          }}
        >
          <Typography variant="h5" fontWeight={700} mb={3} textAlign="center">
            ANNUAL SUMMARY {selectedYear}
          </Typography>
          {isLoadingAnnualSummary ? (
            <Skeleton variant="rounded" width="100%" height={220} sx={{ bgcolor: 'rgba(255,255,255,0.15)' }} />
          ) : (
          <Grid container spacing={3}>
            {/* Budget Breakdown */}
            <Grid size={{ xs: 12, md: 4 }}>
              <Box sx={{ bgcolor: 'rgba(255,255,255,0.15)', p: 3, borderRadius: 3, height: '100%' }}>
                <Typography variant="subtitle1" fontWeight={600} mb={2} textAlign="center">
                  BUDGET
                </Typography>
                <Box sx={{ mb: 2 }}>
                  <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 1 }}>
                    <Typography variant="body2">Income</Typography>
                    <Typography variant="body2" fontWeight={700}>€{incomeBudget.toLocaleString()}</Typography>
                  </Box>
                  <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 1 }}>
                    <Typography variant="body2">Expenses</Typography>
                    <Typography variant="body2" fontWeight={700}>€{Math.abs(expenseBudget).toLocaleString()}</Typography>
                  </Box>
                  <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                    <Typography variant="body2">Savings & Investments</Typography>
                    <Typography variant="body2" fontWeight={700}>€{Math.abs(savingsBudget + investmentsBudget).toLocaleString()}</Typography>
                  </Box>
                </Box>
              </Box>
            </Grid>

            {/* Actual Breakdown */}
            <Grid size={{ xs: 12, md: 4 }}>
              <Box sx={{ bgcolor: 'rgba(255,255,255,0.15)', p: 3, borderRadius: 3, height: '100%' }}>
                <Typography variant="subtitle1" fontWeight={600} mb={2} textAlign="center">
                  ACTUAL
                </Typography>
                <Box sx={{ mb: 2 }}>
                  <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 1 }}>
                    <Typography variant="body2">Income</Typography>
                    <Typography variant="body2" fontWeight={700}>€{totalIncome.toLocaleString()}</Typography>
                  </Box>
                  <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 1 }}>
                    <Typography variant="body2">Expenses</Typography>
                    <Typography variant="body2" fontWeight={700}>€{Math.abs(totalExpenses).toLocaleString()}</Typography>
                  </Box>
                  <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                    <Typography variant="body2">Savings & Investments</Typography>
                    <Typography variant="body2" fontWeight={700}>€{Math.abs(totalSavings + totalInvestments).toLocaleString()}</Typography>
                  </Box>
                </Box>
              </Box>
            </Grid>

            {/* Left to Spend */}
            <Grid size={{ xs: 12, md: 4 }}>
              <Box sx={{ bgcolor: 'rgba(255,255,255,0.25)', p: 3, borderRadius: 3, height: '100%', display: 'flex', flexDirection: 'column', justifyContent: 'center', alignItems: 'center' }}>
                <Typography variant="subtitle1" fontWeight={600} mb={1} textAlign="center">
                  LEFT TO SPEND
                </Typography>
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                  <Chip
                    icon={leftToSpend > 0 ? <CheckCircle /> : <Warning />}
                    label={`€${Math.abs(leftToSpend).toLocaleString()}`}
                    sx={{
                      bgcolor: leftToSpend > 0 ? '#4caf50' : '#f44336',
                      color: 'white',
                      fontSize: '2rem',
                      fontWeight: 700,
                      height: 'auto',
                      py: 2,
                      px: 3,
                      '& .MuiChip-icon': {
                        color: 'white',
                        fontSize: '2.5rem'
                      },
                      '& .MuiChip-label': {
                        fontSize: '2rem',
                        fontWeight: 700
                      }
                    }}
                  />
                </Box>
              </Box>
            </Grid>
          </Grid>
          )}
        </Paper>

        {/* Top Cards */}
        <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 3, mb: 3 }}>
          {/* My Income Card */}
          <Paper elevation={4} sx={{ flex: 1, minWidth: 260, p: 3, borderRadius: 4, boxShadow: '0 4px 24px #b2dfdb33', position: 'relative', overflow: 'hidden', background: 'linear-gradient(135deg, #e8f5e9 0%, #ffffff 100%)' }}>
            <Box sx={{ position: 'absolute', top: 16, right: 16, opacity: 0.15, fontSize: 80 }}>
              <TrendingUp fontSize="inherit" color="success" />
            </Box>
            <Typography color="success.main" fontWeight={700} mb={1}>
              <TrendingUp sx={{ verticalAlign: 'middle', mr: 1 }} /> My Income
            </Typography>
            {isLoadingIncomeCard ? (
              <>
                <Skeleton variant="text" width="60%" height={32} sx={{ mb: 1 }} />
                <Skeleton variant="text" width="60%" height={32} sx={{ mb: 2 }} />
                <Skeleton variant="rounded" height={8} sx={{ borderRadius: 5, mb: 1 }} />
                <Skeleton variant="text" width="80%" />
              </>
            ) : (
              <>
                <Box sx={{ display: 'flex', alignItems: 'center', mb: 1 }}>
                  <Chip label="Budget" size="small" sx={{ mr: 1, bgcolor: 'success.light', color: 'success.dark' }} />
                  <Typography variant="h6" fontWeight={700} color="success.main">
                    €{incomeBudget.toLocaleString()}
                  </Typography>
                </Box>
                <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                  <Chip label="Actual" size="small" sx={{ mr: 1, bgcolor: 'success.100', color: 'success.dark' }} />
                  <Typography variant="h6" fontWeight={700} color="success.dark">
                    €{totalIncome.toLocaleString()}
                  </Typography>
                </Box>
                <LinearProgress variant="determinate" value={Math.min(100, (totalIncome / incomeBudget) * 100)} sx={{ height: 8, borderRadius: 5, bgcolor: 'success.light' }} color="success" />
                <Typography variant="caption" color="text.secondary" mt={1}>
                  {Math.round((totalIncome / (incomeBudget == 0 ? 1 : incomeBudget) * 100))}% of income target reached
                </Typography>
              </>
            )}
          </Paper>

          {/* My Expenses Card */}
          <Paper elevation={4} sx={{ flex: 1, minWidth: 260, p: 3, borderRadius: 4, boxShadow: '0 4px 24px #ffcdd233', position: 'relative', overflow: 'hidden', background: 'linear-gradient(135deg, #ffebee 0%, #ffffff 100%)' }}>
            <Box sx={{ position: 'absolute', top: 16, right: 16, opacity: 0.15, fontSize: 80 }}>
              <TrendingDown fontSize="inherit" color="error" />
            </Box>
            <Typography color="error.main" fontWeight={700} mb={1}>
              <TrendingDown sx={{ verticalAlign: 'middle', mr: 1 }} /> My Expenses
            </Typography>
            {isLoadingExpensesCard ? (
              <>
                <Skeleton variant="text" width="60%" height={32} sx={{ mb: 1 }} />
                <Skeleton variant="text" width="60%" height={32} sx={{ mb: 2 }} />
                <Skeleton variant="rounded" height={8} sx={{ borderRadius: 5, mb: 1 }} />
                <Skeleton variant="text" width="80%" />
              </>
            ) : (
              <>
                <Box sx={{ display: 'flex', alignItems: 'center', mb: 1 }}>
                  <Chip label="Budget" size="small" sx={{ mr: 1, bgcolor: 'error.light', color: 'error.dark' }} />
                  <Typography variant="h6" fontWeight={700} color="error.main">
                    €{expenseBudget.toLocaleString()}
                  </Typography>
                </Box>
                <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                  <Chip label="Actual" size="small" sx={{ mr: 1, bgcolor: 'error.100', color: 'error.dark' }} />
                  <Typography variant="h6" fontWeight={700} color="error.dark">
                    €{totalExpenses.toLocaleString()}
                  </Typography>
                </Box>
                <LinearProgress variant="determinate" value={Math.min(100, (totalExpenses / totalBudget) * 100)} sx={{ height: 8, borderRadius: 5, bgcolor: 'error.light' }} color="error" />
                <Typography variant="caption" color="text.secondary" mt={1}>
                  {Math.round((totalExpenses / (totalBudget == 0 ? 1 : totalBudget) * 100))}% of budget spent
                </Typography>
              </>
            )}
          </Paper>

          {/* Savings Card */}
          <Paper elevation={4} sx={{ flex: 1, minWidth: 260, p: 3, borderRadius: 4, boxShadow: '0 4px 24px #bbdefb33', position: 'relative', overflow: 'hidden', background: 'linear-gradient(135deg, #e3f2fd 0%, #ffffff 100%)' }}>
            <Box sx={{ position: 'absolute', top: 16, right: 16, opacity: 0.15, fontSize: 80 }}>
              <Savings fontSize="inherit" color="info" />
            </Box>
            <Typography color="info.main" fontWeight={700} mb={1}>
              <Savings sx={{ verticalAlign: 'middle', mr: 1 }} /> Savings
            </Typography>
            {isLoadingSavingsCard ? (
              <>
                <Skeleton variant="text" width="60%" height={32} sx={{ mb: 1 }} />
                <Skeleton variant="text" width="60%" height={32} sx={{ mb: 2 }} />
                <Skeleton variant="rounded" height={8} sx={{ borderRadius: 5, mb: 1 }} />
                <Skeleton variant="text" width="80%" />
              </>
            ) : (
              <>
                <Box sx={{ display: 'flex', alignItems: 'center', mb: 1 }}>
                  <Chip label="Budget" size="small" sx={{ mr: 1, bgcolor: 'info.light', color: 'info.dark' }} />
                  <Typography variant="h6" fontWeight={700} color="info.main">
                    €{savingsBudget.toLocaleString()}
                  </Typography>
                </Box>
                <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                  <Chip label="Actual" size="small" sx={{ mr: 1, bgcolor: 'info.100', color: 'info.dark' }} />
                  <Typography variant="h6" fontWeight={700} color="info.dark">
                    €{totalSavings.toLocaleString()}
                  </Typography>
                </Box>
                <LinearProgress variant="determinate" value={Math.min(100, (totalSavings / savingsBudget) * 100)} sx={{ height: 8, borderRadius: 5, bgcolor: 'info.light' }} color="info" />
                <Typography variant="caption" color="text.secondary" mt={1}>
                  {Math.round((totalSavings / (savingsBudget == 0 ? 1 : savingsBudget) * 100))}% of savings target reached
                </Typography>
              </>
            )}
          </Paper>

          {/* Investments Card */}
          <Paper elevation={4} sx={{ flex: 1, minWidth: 260, p: 3, borderRadius: 4, boxShadow: '0 4px 24px rgba(0,137,123,0.15)', position: 'relative', overflow: 'hidden', background: 'linear-gradient(135deg, #e0f2f1 0%, #ffffff 100%)' }}>
            <Box sx={{ position: 'absolute', top: 16, right: 16, opacity: 0.15, fontSize: 80 }}>
              <TrendingUp fontSize="inherit" sx={{ color: '#00897b' }} />
            </Box>
            <Typography fontWeight={700} mb={1} sx={{ color: '#00897b' }}>
              <TrendingUp sx={{ verticalAlign: 'middle', mr: 1, color: '#00897b' }} /> Investments
            </Typography>
            {isLoadingInvestmentsCard ? (
              <>
                <Skeleton variant="text" width="60%" height={32} sx={{ mb: 1 }} />
                <Skeleton variant="text" width="60%" height={32} sx={{ mb: 2 }} />
                <Skeleton variant="rounded" height={8} sx={{ borderRadius: 5, mb: 1 }} />
                <Skeleton variant="text" width="80%" />
              </>
            ) : (
              <>
                <Box sx={{ display: 'flex', alignItems: 'center', mb: 1 }}>
                  <Chip label="Budget" size="small" sx={{ mr: 1, bgcolor: '#b2dfdb', color: '#00695c' }} />
                  <Typography variant="h6" fontWeight={700} sx={{ color: '#00897b' }}>
                    €{investmentsBudget.toLocaleString()}
                  </Typography>
                </Box>
                <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                  <Chip label="Actual" size="small" sx={{ mr: 1, bgcolor: '#e0f2f1', color: '#00695c' }} />
                  <Typography variant="h6" fontWeight={700} sx={{ color: '#00695c' }}>
                    €{totalInvestments.toLocaleString()}
                  </Typography>
                </Box>
                <LinearProgress variant="determinate" value={Math.min(100, (totalInvestments / (investmentsBudget === 0 ? 1 : investmentsBudget)) * 100)} sx={{ height: 8, borderRadius: 5, bgcolor: '#b2dfdb', '& .MuiLinearProgress-bar': { bgcolor: '#00897b' } }} />
                <Typography variant="caption" color="text.secondary" mt={1}>
                  {Math.round((totalInvestments / (investmentsBudget == 0 ? 1 : investmentsBudget) * 100))}% of investments target reached
                </Typography>
              </>
            )}
          </Paper>
        </Box>


        {/* Account Flow Chart */}
        <Box sx={{ display: 'flex', flexDirection: 'column', gap: 3, mb: 3 }}>
          {/* Account Total Flow */}
          <Paper elevation={4} sx={{ p: 3, borderRadius: 4, boxShadow: '0 4px 24px #b2dfdb33', background: 'linear-gradient(135deg, #f3e5f5 0%, #ffffff 100%)' }}>
            <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
              <MuiBarChart color="secondary" sx={{ mr: 1 }} />
              <Typography color="secondary.dark" fontWeight={700} variant="h6">
                Account Total Balance - {selectedYear}
              </Typography>
            </Box>
            <Box sx={{ width: '100%', height: 400, mb: 2 }}>
              {isLoadingAccountFlow ? (
                <ChartSkeleton height={400} />
              ) : (
              <ResponsiveContainer width="100%" height="100%">
                <ComposedChart data={transformedAccountData} margin={{ top: 40, right: 30, left: 20, bottom: 100 }}>
                  <CartesianGrid strokeDasharray="3 3" stroke={isDark ? '#444' : '#ccc'} />
                  <XAxis
                    dataKey="period"
                    tick={{ fontSize: 11, fill: theme.palette.text.primary }}
                    angle={-45}
                    textAnchor="end"
                    height={100}
                    interval={0}
                    stroke={theme.palette.text.secondary}
                  />
                  <YAxis
                    tick={{ fontSize: 12, fill: theme.palette.text.secondary }}
                    tickFormatter={(value) => `$${value.toLocaleString()}`}
                    stroke={theme.palette.text.secondary}
                  />
                  <Tooltip
                    formatter={(value) => `$${value?.toLocaleString()}`}
                    contentStyle={{
                      backgroundColor: isDark ? '#2c2c2c' : '#ffffff',
                      border: `1px solid ${isDark ? '#444' : '#ccc'}`,
                      borderRadius: '8px',
                      color: isDark ? '#ffffff' : '#000000'
                    }}
                  />
                  <Legend verticalAlign="top" height={40} wrapperStyle={{ color: theme.palette.text.primary }} />
                  {uniqueAccounts.map((account) => (
                    <Bar
                      key={`${account}_Total`}
                      dataKey={`${account}_Total`}
                      fill={accountColors[account] || '#666666'}
                      radius={[4, 4, 0, 0]}
                      name={`${account} Total`}
                    />
                  ))}
                  <Line
                    type="monotone"
                    dataKey="Total_All_Accounts"
                    stroke={isDark ? '#ffffff' : '#000000'}
                    strokeWidth={2}
                    dot={{ r: 2, fill: isDark ? '#ffffff' : '#000000', stroke: isDark ? '#ffffff' : '#000000', strokeWidth: 1 }}
                    name="Total All Accounts"
                  >
                    <LabelList
                      dataKey="Total_All_Accounts"
                      position="top"
                      content={(props: any) => {
                        const { x, y, value } = props;
                        const isPositive = value >= 0;
                        return (
                          <text
                            x={x}
                            y={y - 10}
                            textAnchor="middle"
                            dominantBaseline="middle"
                            style={{
                              fontSize: '12px',
                              fontWeight: 'bold',
                              fill: isPositive ? '#4caf50' : '#f44336'
                            }}
                          >
                            ${value?.toLocaleString()}
                          </text>
                        );
                      }}
                    />
                  </Line>
                </ComposedChart>
              </ResponsiveContainer>
              )}
            </Box>
            <Typography variant="caption" color="text.secondary" sx={{ mt: 1 }}>
              Total balance (income + savings - expenses) per account with overall total trend line
            </Typography>
          </Paper>
        </Box>


        {/* Macro Category Budget vs Actual Trend Chart */}
        <Box sx={{ mb: 3 }}>
          <Paper elevation={4} sx={{ p: 3, borderRadius: 4, boxShadow: '0 4px 24px #b2dfdb33', background: 'linear-gradient(135deg, #fff3e0 0%, #ffffff 100%)' }}>
            <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
              <Timeline color="warning" sx={{ mr: 1 }} />
              <Typography color="warning.dark" fontWeight={700} variant="subtitle1">
                Budget vs Actual Trend
              </Typography>
            </Box>
            {isLoadingBudgetTrend ? (
              <ChartSkeleton height={320} />
            ) : (
            <MacroCategoryBudgetTrend
              data={macroCategoryBudgetTrend}
              categorySpending={categorySpending}
              months={months}
              colors={macroCategoryColors}
            />
            )}
          </Paper>
        </Box>

        {/* Income vs Savings Bar Chart */}
        <Box sx={{ mb: 4 }}>
          <Paper elevation={4} sx={{ p: 3, borderRadius: 4, boxShadow: '0 4px 24px #b2dfdb33', background: 'linear-gradient(135deg, #e8f5e9 0%, #ffffff 100%)' }}>
            <Typography variant="h6" fontWeight={700} color="text.primary" mb={2} textAlign="center">
              INCOME vs SAVINGS (Monthly)
            </Typography>
            <Box sx={{ width: '100%', height: 300 }}>
              {isLoadingMonthlyData ? (
                <ChartSkeleton height={300} />
              ) : (
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
              )}
            </Box>
          </Paper>
        </Box>

        {/* Income vs Expense Bar Chart */}
        <Box sx={{ mb: 4 }}>
          <Paper elevation={4} sx={{ p: 3, borderRadius: 4, boxShadow: '0 4px 24px #b2dfdb33', background: 'linear-gradient(135deg, #ffebee 0%, #ffffff 100%)' }}>
            <Typography variant="h6" fontWeight={700} color="text.primary" mb={2} textAlign="center">
              INCOME vs EXPENSE (Monthly)
            </Typography>
            <Box sx={{ width: '100%', height: 300 }}>
              {isLoadingMonthlyData ? (
                <ChartSkeleton height={300} />
              ) : (
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
              )}
            </Box>
          </Paper>
        </Box>

        {/* Category Breakdown - Full Width */}
        <Paper elevation={4} sx={{ p: 3, borderRadius: 4, boxShadow: '0 4px 24px #b2dfdb33', mb: 3, background: 'linear-gradient(135deg, #e3f2fd 0%, #ffffff 100%)' }}>
          <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3, flexWrap: 'wrap', gap: 2 }}>
            <Box sx={{ display: 'flex', alignItems: 'center' }}>
              <MuiBarChart color="info" sx={{ mr: 1 }} />
              <Typography color="info.dark" fontWeight={700} variant="h6">
                Category Breakdown
              </Typography>
            </Box>
            <ToggleButtonGroup
              value={categoryBreakdownView}
              exclusive
              size="small"
              onChange={(_, value) => { if (value) { setCategoryBreakdownView(value); setShowAllCategoryBreakdown(false); } }}
            >
              <ToggleButton value="categories">Categories</ToggleButton>
              <ToggleButton value="groups">Groups</ToggleButton>
            </ToggleButtonGroup>
          </Box>

          {isLoadingCategorySpending ? (
            <>
              <Skeleton variant="text" width={180} height={48} sx={{ mb: 2 }} />
              <ListRowsSkeleton rows={8} />
            </>
          ) : (
          <>
          {/* Total + Stacked Bar (Groups view only - too many categories to read as slivers) */}
          <Box sx={{ mb: 3 }}>
            <Typography variant="overline" color="text.secondary">
              Total Spending
            </Typography>
            <Typography variant="h4" fontWeight={800} color="text.primary" mb={categoryBreakdownView === 'groups' ? 3 : 0}>
              €{totalCategoryBreakdownSpend.toLocaleString()}
            </Typography>
            {categoryBreakdownView === 'groups' && (
              <>
                <Box sx={{ position: 'relative', width: '100%', pb: totalCategoryBreakdownBudget > 0 ? 1 : 0 }}>
                  <Box sx={{ display: 'flex', width: '100%', height: 40, borderRadius: 3, overflow: 'hidden', bgcolor: 'grey.200' }}>
                    {categoryBreakdownData.map((item) => {
                      const widthPct = (item.totalSpent / categoryBreakdownBarMax) * 100;
                      if (widthPct <= 0) return null;
                      const share = totalCategoryBreakdownSpend > 0 ? (item.totalSpent / totalCategoryBreakdownSpend) * 100 : 0;
                      return (
                        <MuiTooltip key={item.name} title={`${item.name}: €${item.totalSpent.toLocaleString()} (${share.toFixed(1)}%)`} arrow>
                          <Box sx={{ width: `${widthPct}%`, bgcolor: item.color, cursor: 'pointer', transition: 'opacity 0.15s', '&:hover': { opacity: 0.85 } }} />
                        </MuiTooltip>
                      );
                    })}
                  </Box>
                  {totalCategoryBreakdownBudget > 0 && (
                    <Box sx={{
                      position: 'absolute',
                      top: -6,
                      bottom: 4,
                      left: `${Math.min(100, (totalCategoryBreakdownBudget / categoryBreakdownBarMax) * 100)}%`,
                      borderLeft: `2px dashed ${isDark ? '#fff' : '#222'}`,
                    }} />
                  )}
                </Box>
                <Typography variant="caption" color="text.secondary">
                  {totalCategoryBreakdownBudget > 0
                    ? `Dashed line marks total budget of €${totalCategoryBreakdownBudget.toLocaleString()}`
                    : 'No budget set for these groups'}
                </Typography>
              </>
            )}
          </Box>

          {/* Detail List - ranked rows, largest spend first */}
          <Box sx={{ display: 'flex', flexDirection: 'column' }}>
            {(showAllCategoryBreakdown ? categoryBreakdownData : categoryBreakdownData.slice(0, BREAKDOWN_LIST_LIMIT)).map((item, index) => {
              const share = totalCategoryBreakdownSpend > 0 ? (item.totalSpent / totalCategoryBreakdownSpend) * 100 : 0;
              const difference = item.budgetedAmount - item.totalSpent;
              const isUnderBudget = difference > 0;

              return (
                <Box key={item.name} sx={{
                  py: 1.25,
                  borderBottom: '1px solid',
                  borderColor: 'grey.200',
                }}>
                  {/* Row 1: identity + amount + budget standing */}
                  <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', gap: 1 }}>
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, minWidth: 0 }}>
                      <Typography variant="caption" color="text.secondary" sx={{ width: 20, flexShrink: 0 }}>
                        {index + 1}
                      </Typography>
                      <Box sx={{ width: 10, height: 10, borderRadius: '50%', bgcolor: item.color, flexShrink: 0 }} />
                      <Typography variant="body2" fontWeight={700} noWrap>
                        {item.name}
                      </Typography>
                    </Box>
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, flexShrink: 0 }}>
                      {item.budgetedAmount > 0 && (
                        <Chip
                          icon={isUnderBudget ? <CheckCircle /> : <Warning />}
                          label={`€${Math.abs(difference).toLocaleString()} ${isUnderBudget ? 'left' : 'over'}`}
                          size="small"
                          sx={{
                            bgcolor: isUnderBudget ? '#4caf50' : '#f44336',
                            color: 'white',
                            fontWeight: 600,
                            height: 20,
                            '& .MuiChip-icon': { color: 'white', fontSize: '0.9rem' },
                            '& .MuiChip-label': { fontSize: '0.7rem', px: 1 }
                          }}
                        />
                      )}
                      <Typography variant="body2" fontWeight={700}>
                        €{item.totalSpent.toLocaleString()}
                      </Typography>
                    </Box>
                  </Box>
                  {/* Row 2: de-emphasized share-of-total */}
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mt: 0.5, pl: 3.5 }}>
                    <Box sx={{ flex: 1, height: 4, borderRadius: 2, bgcolor: 'grey.200', overflow: 'hidden' }}>
                      <Box sx={{ width: `${Math.min(100, share)}%`, height: '100%', bgcolor: item.color, opacity: 0.6 }} />
                    </Box>
                    <Typography variant="caption" color="text.secondary" sx={{ flexShrink: 0, minWidth: 70, textAlign: 'right' }}>
                      {share.toFixed(1)}% of total
                    </Typography>
                  </Box>
                </Box>
              );
            })}
          </Box>

          {categoryBreakdownData.length > BREAKDOWN_LIST_LIMIT && (
            <Box sx={{ display: 'flex', justifyContent: 'center', mt: 2 }}>
              <Chip
                clickable
                onClick={() => setShowAllCategoryBreakdown((prev) => !prev)}
                label={showAllCategoryBreakdown ? 'Show less' : `Show all ${categoryBreakdownData.length}`}
                size="small"
                sx={{ fontWeight: 600 }}
              />
            </Box>
          )}
          </>
          )}
        </Paper>

        {/* Savings Breakdown Section */}
        <Paper elevation={4} sx={{ p: 3, borderRadius: 4, boxShadow: '0 4px 24px #b2dfdb33', mb: 3, background: 'linear-gradient(135deg, #e3f2fd 0%, #ffffff 100%)' }}>
          <Box sx={{ display: 'flex', alignItems: 'center', mb: 3 }}>
            <Savings color="info" sx={{ mr: 1 }} />
            <Typography color="info.dark" fontWeight={700} variant="h6">
              Savings Breakdown
            </Typography>
          </Box>

          {isLoadingCategorySavings ? (
            <ListRowsSkeleton rows={5} />
          ) : savingsBreakdownData.length > 0
            ? renderFundBreakdown(
              savingsBreakdownData,
              totalSavingsBreakdown,
              'Saved',
              showAllSavingsBreakdown,
              () => setShowAllSavingsBreakdown((prev) => !prev)
            )
            : (
              <Box sx={{ textAlign: 'center', py: 4 }}>
                <Typography variant="body1" color="text.secondary">
                  No savings data available for {selectedYear}
                </Typography>
              </Box>
            )}
        </Paper>

        {/* Investments Breakdown Section */}
        <Paper elevation={4} sx={{ p: 3, borderRadius: 4, boxShadow: '0 4px 24px rgba(0,137,123,0.15)', mb: 3, background: 'linear-gradient(135deg, #e0f2f1 0%, #ffffff 100%)' }}>
          <Box sx={{ display: 'flex', alignItems: 'center', mb: 3 }}>
            <TrendingUp sx={{ mr: 1, color: '#00897b' }} />
            <Typography fontWeight={700} variant="h6" sx={{ color: '#00695c' }}>
              Investments Breakdown
            </Typography>
          </Box>

          {isLoadingCategoryInvestments ? (
            <ListRowsSkeleton rows={5} />
          ) : investmentsBreakdownData.length > 0
            ? renderFundBreakdown(
              investmentsBreakdownData,
              totalInvestmentsBreakdown,
              'Invested',
              showAllInvestmentsBreakdown,
              () => setShowAllInvestmentsBreakdown((prev) => !prev)
            )
            : (
              <Box sx={{ textAlign: 'center', py: 4 }}>
                <Typography variant="body1" color="text.secondary">
                  No investments data available for {selectedYear}
                </Typography>
              </Box>
            )}
        </Paper>

        {/* 50-30-20 Budget Section */}
        <Paper elevation={4} sx={{ p: 3, borderRadius: 4, boxShadow: '0 4px 24px #b2dfdb33', mb: 3, background: 'linear-gradient(135deg, #f5f6fa 0%, #ffffff 100%)' }}>
          <Typography variant="h6" fontWeight={700} color="#222" mb={2}>
            50-30-20 Budget Breakdown
          </Typography>
          {isLoadingCategorySpending ? (
            <ChartSkeleton height={220} />
          ) : (
          <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 4 }}>
            {/* Percentage Breakdown */}
            <Box sx={{ flex: 1, minWidth: 220 }}>
              <Typography variant="subtitle2" fontWeight={600} mb={1}>Percentage Breakdown</Typography>
              {(Object.keys(budget502010.goal) as BudgetKey[]).map((key) => (
                <Box key={key} sx={{ mb: 2 }}>
                  <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                    <Typography variant="body2" fontWeight={500} textTransform="capitalize">{key.replace('Debts', '+Debts')}</Typography>
                    <Typography variant="body2" color="text.secondary">Goal: {budget502010.goal[key]}% | Actual: {budget502010.actual[key]}%</Typography>
                  </Box>
                  <LinearProgress
                    variant="determinate"
                    value={Math.min(100, (budget502010.actual[key] / budget502010.goal[key]) * 100)}
                    sx={{
                      height: 8, borderRadius: 5, bgcolor: 'grey.200',
                      '& .MuiLinearProgress-bar': {
                        bgcolor: key === 'needs' ? 'success.main' : key === 'wants' ? 'warning.main' : 'info.main',
                      },
                    }}
                  />
                </Box>
              ))}
            </Box>

            {/* Amount Breakdown Table */}
            <Box sx={{ flex: 1, minWidth: 220 }}>
              <Typography variant="subtitle2" fontWeight={600} mb={1}>Amount Breakdown</Typography>
              <TableContainer>
                <Table size="small">
                  <TableHead>
                    <TableRow>
                      <TableCell></TableCell>
                      <TableCell>Budget</TableCell>
                      <TableCell>Actual</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {(Object.keys(budget502010.amount) as BudgetKeyWithTotal[]).map((key) => (
                      <TableRow key={key}>
                        <TableCell sx={{ fontWeight: 600, textTransform: 'capitalize' }}>{key.replace('Debts', '+Debts')}</TableCell>
                        <TableCell>€{budget502010.amount[key].budget.toLocaleString()}</TableCell>
                        <TableCell>€{budget502010.amount[key].actual.toLocaleString()}</TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </TableContainer>
            </Box>
          </Box>
          )}
        </Paper>
      </Box>
    </Layout>
  );
}
