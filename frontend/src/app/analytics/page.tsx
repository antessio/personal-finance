'use client';

import { Box, Paper, Typography, FormControl, InputLabel, Select, MenuItem, Grid, Chip } from '@mui/material';
import Layout from '../../components/Layout';
import { useQuery } from '@tanstack/react-query';
import { service } from '../../services/api';
import { useState } from 'react';
import MonthlyDataTable from '../../components/charts/MonthlyDataTable';
import ChartSkeleton from '../../components/skeletons/ChartSkeleton';
import ListRowsSkeleton from '../../components/skeletons/ListRowsSkeleton';
import { Tooltip, ResponsiveContainer, PieChart, Pie, Cell } from 'recharts';
import { CheckCircle, Warning } from '@mui/icons-material';

export default function AnalyticsPage() {
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
  const [expandedGroups, setExpandedGroups] = useState<{ [key: string]: boolean }>({});
  const [selectedCategory, setSelectedCategory] = useState<{ categoryId: string; categoryName: string } | null>(null);
  const BREAKDOWN_LIST_LIMIT = 8;
  const toggleGroupExpand = (key: string) => setExpandedGroups((prev) => ({ ...prev, [key]: !prev[key] }));
  const handleCategoryClick = (categoryId: string, categoryName: string) =>
    setSelectedCategory((prev) => (prev?.categoryId === categoryId ? null : { categoryId, categoryName }));

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
  const { data: monthlyData = [], isLoading: isLoadingMonthlyData } = useQuery({
    queryKey: ['monthlyData', selectedYear, selectedMonth],
    queryFn: async () => {
      const data = await service.getMonthlyData(selectedYear, selectedMonth);
      return data;
    }
  });

  const { data: categorySpending = [], isLoading: isLoadingCategorySpending } = useQuery({
    queryKey: ['categorySpending', selectedYear, selectedMonth],
    queryFn: async () => {
      const data = await service.getCategorySpending(selectedYear, selectedMonth);
      return data;
    },
  });
  const { data: categoryIncome = [], isLoading: isLoadingCategoryIncome } = useQuery({
    queryKey: ['categoryIncome', selectedYear, selectedMonth],
    queryFn: async () => {
      const data = await service.getCategoryIncome(selectedYear, selectedMonth);
      return data;
    },
  });
  const { data: categorySavings = [], isLoading: isLoadingCategorySavings } = useQuery({
    queryKey: ['categorySavings', selectedYear, selectedMonth],
    queryFn: async () => {
      const data = await service.getCategorySavings(selectedYear, selectedMonth);
      return data;
    },
  });
  const { data: categoryInvestments = [], isLoading: isLoadingCategoryInvestments } = useQuery({
    queryKey: ['categoryInvestments', selectedYear, selectedMonth],
    queryFn: async () => {
      const data = await service.getCategoryInvestments(selectedYear, selectedMonth);
      return data;
    },
  });

  const isLoadingCategoryComparison = isLoadingCategorySpending || isLoadingCategoryIncome || isLoadingCategorySavings || isLoadingCategoryInvestments;

  const { data: categoryTransactions, isLoading: isLoadingCategoryTransactions } = useQuery({
    queryKey: ['categoryTransactions', selectedCategory?.categoryId, selectedYear, selectedMonth],
    queryFn: () => service.getTransactions({ categoryId: selectedCategory!.categoryId, year: selectedYear, month: selectedMonth, limit: 100 }),
    enabled: !!selectedCategory,
  });


  // Prepare pie chart data for Income
  const incomeData = categoryIncome

    .map(cat => ({
      name: cat.categoryName,
      value: cat.totalSpent,
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

  // Prepare Bills, Debts, Subscriptions data
  const billsData = categorySpending
    .filter(cat => cat.macroCategory === 'BILLS');

  const debtsData = categorySpending
    .filter(cat => cat.macroCategory === 'DEBTS');

  const subscriptionsData = categorySpending
    .filter(cat => cat.macroCategory === 'SUBSCRIPTIONS');

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
          {isLoadingCategoryComparison ? (
            <ListRowsSkeleton rows={6} />
          ) : (() => {
            // Income/Savings/Investments keep their own macro-group, ranked-list treatment.
            // Expense/Bills/Subscriptions/Debts are all "spending" and are unified below into one
            // pie + detailed list, instead of 4 separate near-identical ranked lists.
            const nonSpendingCategories = [...categoryIncome, ...categorySavings, ...categoryInvestments];

            const groupedByMacro = nonSpendingCategories.reduce((acc, cat) => {
              const macro = cat.macroCategory || 'OTHER';
              if (!acc[macro]) acc[macro] = [];
              acc[macro].push(cat);
              return acc;
            }, {} as { [key: string]: typeof nonSpendingCategories });

            const macroCategoryConfig: { [key: string]: { color: string; label: string; bgcolor: string } } = {
              'INCOME': { color: '#4caf50', label: 'Income', bgcolor: 'rgba(76, 175, 80, 0.08)' },
              'EXPENSE': { color: '#f44336', label: 'Expenses', bgcolor: 'rgba(244, 67, 54, 0.08)' },
              'BILLS': { color: '#ff9800', label: 'Bills', bgcolor: 'rgba(255, 152, 0, 0.08)' },
              'SUBSCRIPTIONS': { color: '#9c27b0', label: 'Subscriptions', bgcolor: 'rgba(156, 39, 176, 0.08)' },
              'DEBTS': { color: '#795548', label: 'Debts', bgcolor: 'rgba(121, 85, 72, 0.08)' },
              'SAVINGS': { color: '#2196f3', label: 'Savings', bgcolor: 'rgba(33, 150, 243, 0.08)' },
              'INVESTMENTS': { color: '#00897b', label: 'Investments', bgcolor: 'rgba(0, 137, 123, 0.08)' },
              'OTHER': { color: '#607d8b', label: 'Other', bgcolor: 'rgba(96, 125, 139, 0.08)' }
            };

            const categoryPalette = ['#4c93af', '#2196f3', '#ff9800', '#9c27b0', '#f44336', '#795548', '#607d8b', '#4caf50', '#00897b', '#e91e63', '#3f51b5', '#8bc34a'];

            const renderGroup = (macroCategory: string, categories: typeof nonSpendingCategories) => {
              const config = macroCategoryConfig[macroCategory] || macroCategoryConfig['OTHER'];
              // Expense-like macro categories: over budget is bad. Income: over budget is good.
              const isIncomeLike = macroCategory === 'INCOME';

              const sorted = [...categories].sort((a, b) => Math.abs(b.totalSpent) - Math.abs(a.totalSpent));
              const groupTotal = sorted.reduce((sum, c) => sum + Math.abs(c.totalSpent), 0);
              const expanded = !!expandedGroups[macroCategory];
              const visible = expanded ? sorted : sorted.slice(0, BREAKDOWN_LIST_LIMIT);

              return (
                <Box key={macroCategory} sx={{ mb: 3 }}>
                  <Box sx={{ bgcolor: config.color, p: 1.5, mb: 1, borderRadius: 2 }}>
                    <Typography variant="subtitle1" fontWeight={700} color="white">
                      {config.label}
                    </Typography>
                  </Box>
                  {visible.map((category) => {
                    const actualAmount = Math.abs(category.totalSpent);
                    const budgetAmount = category.budgetedAmount || 0;
                    const hasBudget = budgetAmount > 0;
                    const share = groupTotal > 0 ? (actualAmount / groupTotal) * 100 : 0;
                    const difference = isIncomeLike ? (actualAmount - budgetAmount) : (budgetAmount - actualAmount);
                    const isGood = hasBudget && difference > 0;
                    const chipLabel = isIncomeLike
                      ? `€${Math.abs(difference).toLocaleString()} ${isGood ? 'above target' : 'below target'}`
                      : `€${Math.abs(difference).toLocaleString()} ${isGood ? 'left' : 'over'}`;

                    const isSelected = selectedCategory?.categoryId === category.categoryId;

                    return (
                      <Box
                        key={category.categoryName}
                        onClick={() => handleCategoryClick(category.categoryId, category.categoryName)}
                        sx={{
                          py: 1.25,
                          px: 1,
                          mx: -1,
                          borderBottom: '1px solid',
                          borderColor: 'grey.200',
                          borderRadius: 1,
                          cursor: 'pointer',
                          bgcolor: isSelected ? config.bgcolor : 'transparent',
                          '&:hover': { bgcolor: config.bgcolor },
                        }}
                      >
                        <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', gap: 1 }}>
                          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, minWidth: 0 }}>
                            <Box sx={{ width: 10, height: 10, borderRadius: '50%', bgcolor: config.color, flexShrink: 0 }} />
                            <Typography variant="body2" fontWeight={700} noWrap>
                              {category.categoryName}
                            </Typography>
                          </Box>
                          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, flexShrink: 0 }}>
                            {hasBudget && (
                              <Chip
                                icon={isGood ? <CheckCircle /> : <Warning />}
                                label={chipLabel}
                                size="small"
                                sx={{
                                  bgcolor: isGood ? '#4caf50' : '#f44336',
                                  color: 'white',
                                  fontWeight: 600,
                                  height: 20,
                                  '& .MuiChip-icon': { color: 'white', fontSize: '0.9rem' },
                                  '& .MuiChip-label': { fontSize: '0.7rem', px: 1 }
                                }}
                              />
                            )}
                            <Typography variant="body2" fontWeight={700}>
                              €{actualAmount.toLocaleString()}
                            </Typography>
                          </Box>
                        </Box>
                        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mt: 0.5, pl: 2.5 }}>
                          <Box sx={{ flex: 1, height: 4, borderRadius: 2, bgcolor: 'grey.200', overflow: 'hidden' }}>
                            <Box sx={{ width: `${Math.min(100, share)}%`, height: '100%', bgcolor: config.color, opacity: 0.6 }} />
                          </Box>
                          <Typography variant="caption" color="text.secondary" sx={{ flexShrink: 0, minWidth: 100, textAlign: 'right' }}>
                            {share.toFixed(1)}% of {config.label.toLowerCase()}
                          </Typography>
                        </Box>
                      </Box>
                    );
                  })}
                  {sorted.length > BREAKDOWN_LIST_LIMIT && (
                    <Box sx={{ display: 'flex', justifyContent: 'center', mt: 1.5 }}>
                      <Chip
                        clickable
                        onClick={() => toggleGroupExpand(macroCategory)}
                        label={expanded ? 'Show less' : `Show all ${sorted.length}`}
                        size="small"
                        sx={{ fontWeight: 600 }}
                      />
                    </Box>
                  )}
                </Box>
              );
            };

            const PIE_SLICE_LIMIT = 8;

            // Shared pie + detailed list renderer, used for Spending, Savings, and Investments.
            // judgmentMode 'expense' shows a green/red over-under-budget chip; 'neutral' shows a
            // plain "Goal €X" reference chip instead - see home dashboard for why funds are neutral.
            const renderPieAndList = (
              key: string,
              label: string,
              headerColor: string,
              items: { categoryId: string; name: string; amount: number; budget: number; color: string }[],
              judgmentMode: 'expense' | 'neutral'
            ) => {
              const total = items.reduce((sum, c) => sum + c.amount, 0);
              const expanded = !!expandedGroups[`${key}_LIST`];
              const visibleItems = expanded ? items : items.slice(0, BREAKDOWN_LIST_LIMIT);
              const pieData = items.length > PIE_SLICE_LIMIT
                ? [
                  ...items.slice(0, PIE_SLICE_LIMIT),
                  {
                    categoryId: '',
                    name: 'Other',
                    amount: items.slice(PIE_SLICE_LIMIT).reduce((sum, c) => sum + c.amount, 0),
                    budget: 0,
                    color: '#9e9e9e',
                  },
                ]
                : items;

              return (
                <Box key={key} sx={{ mb: 3 }}>
                  <Box sx={{ bgcolor: headerColor, p: 1.5, mb: 1, borderRadius: 2 }}>
                    <Typography variant="subtitle1" fontWeight={700} color="white">
                      {label}
                    </Typography>
                  </Box>
                  {items.length > 0 ? (
                    <>
                      <Typography variant="body2" color="text.secondary" sx={{ my: 1.5 }}>
                        Total {label}: <Typography component="span" variant="subtitle1" fontWeight={700} color="text.primary">€{total.toLocaleString()}</Typography>
                      </Typography>
                      <Grid container spacing={3} alignItems="flex-start">
                        <Grid size={{ xs: 12, md: 5 }}>
                          <Box sx={{ width: '100%', height: 280 }}>
                            <ResponsiveContainer width="100%" height="100%">
                              <PieChart>
                                <Pie
                                  data={pieData}
                                  dataKey="amount"
                                  nameKey="name"
                                  cx="50%"
                                  cy="50%"
                                  outerRadius={100}
                                  label={(entry) => `${((entry.amount / total) * 100).toFixed(0)}%`}
                                >
                                  {pieData.map((entry) => (
                                    <Cell key={entry.name} fill={entry.color} />
                                  ))}
                                </Pie>
                                <Tooltip formatter={(value: number) => `€${value.toLocaleString()}`} />
                              </PieChart>
                            </ResponsiveContainer>
                          </Box>
                        </Grid>
                        <Grid size={{ xs: 12, md: 7 }}>
                          {visibleItems.map((item) => {
                            const hasBudget = item.budget > 0;
                            const share = total > 0 ? (item.amount / total) * 100 : 0;
                            const difference = item.budget - item.amount;
                            const isGood = hasBudget && difference > 0;
                            const isSelected = !!item.categoryId && selectedCategory?.categoryId === item.categoryId;

                            return (
                              <Box
                                key={item.name}
                                onClick={() => item.categoryId && handleCategoryClick(item.categoryId, item.name)}
                                sx={{
                                  py: 1.25,
                                  px: 1,
                                  mx: -1,
                                  borderBottom: '1px solid',
                                  borderColor: 'grey.200',
                                  borderRadius: 1,
                                  cursor: item.categoryId ? 'pointer' : 'default',
                                  bgcolor: isSelected ? 'rgba(0,0,0,0.04)' : 'transparent',
                                  '&:hover': item.categoryId ? { bgcolor: 'rgba(0,0,0,0.04)' } : undefined,
                                }}
                              >
                                <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', gap: 1 }}>
                                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, minWidth: 0 }}>
                                    <Box sx={{ width: 10, height: 10, borderRadius: '50%', bgcolor: item.color, flexShrink: 0 }} />
                                    <Typography variant="body2" fontWeight={700} noWrap>
                                      {item.name}
                                    </Typography>
                                  </Box>
                                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, flexShrink: 0 }}>
                                    {hasBudget && (
                                      judgmentMode === 'neutral' ? (
                                        <Chip
                                          variant="outlined"
                                          size="small"
                                          label={`Goal €${item.budget.toLocaleString()}`}
                                          sx={{ height: 20, fontSize: '0.7rem', '& .MuiChip-label': { px: 1 } }}
                                        />
                                      ) : (
                                        <Chip
                                          icon={isGood ? <CheckCircle /> : <Warning />}
                                          label={`€${Math.abs(difference).toLocaleString()} ${isGood ? 'left' : 'over'}`}
                                          size="small"
                                          sx={{
                                            bgcolor: isGood ? '#4caf50' : '#f44336',
                                            color: 'white',
                                            fontWeight: 600,
                                            height: 20,
                                            '& .MuiChip-icon': { color: 'white', fontSize: '0.9rem' },
                                            '& .MuiChip-label': { fontSize: '0.7rem', px: 1 }
                                          }}
                                        />
                                      )
                                    )}
                                    <Typography variant="body2" fontWeight={700}>
                                      €{item.amount.toLocaleString()}
                                    </Typography>
                                  </Box>
                                </Box>
                                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mt: 0.5, pl: 2.5 }}>
                                  <Box sx={{ flex: 1, height: 4, borderRadius: 2, bgcolor: 'grey.200', overflow: 'hidden' }}>
                                    <Box sx={{ width: `${Math.min(100, share)}%`, height: '100%', bgcolor: item.color, opacity: 0.6 }} />
                                  </Box>
                                  <Typography variant="caption" color="text.secondary" sx={{ flexShrink: 0, minWidth: 100, textAlign: 'right' }}>
                                    {share.toFixed(1)}% of {label.toLowerCase()}
                                  </Typography>
                                </Box>
                              </Box>
                            );
                          })}
                          {items.length > BREAKDOWN_LIST_LIMIT && (
                            <Box sx={{ display: 'flex', justifyContent: 'center', mt: 1.5 }}>
                              <Chip
                                clickable
                                onClick={() => toggleGroupExpand(`${key}_LIST`)}
                                label={expanded ? 'Show less' : `Show all ${items.length}`}
                                size="small"
                                sx={{ fontWeight: 600 }}
                              />
                            </Box>
                          )}
                        </Grid>
                      </Grid>
                    </>
                  ) : (
                    <Typography variant="body2" color="text.secondary" textAlign="center" sx={{ py: 2 }}>
                      No {label.toLowerCase()} data available for this period
                    </Typography>
                  )}
                </Box>
              );
            };

            // Unified "Spending" pie + detailed list, replacing separate Expense/Bills/Subscriptions/Debts lists.
            // Colored by macro-group (red=Expense, orange=Bills, etc.) since it merges those 4 groups.
            const spendingItems = [...categorySpending]
              .map((c) => ({
                categoryId: c.categoryId,
                name: c.categoryName,
                amount: Math.abs(c.totalSpent),
                budget: c.budgetedAmount || 0,
                color: macroCategoryConfig[c.macroCategory || 'OTHER']?.color || macroCategoryConfig['OTHER'].color,
              }))
              .sort((a, b) => b.amount - a.amount);

            // Savings/Investments are each already a single macro-category, so slices are colored
            // per-category instead (a single macro-group color would make every slice identical).
            const buildFundItems = (categories: typeof nonSpendingCategories) => [...categories]
              .map((c, index) => ({
                categoryId: c.categoryId,
                name: c.categoryName,
                amount: Math.abs(c.totalSpent),
                budget: c.budgetedAmount || 0,
                color: categoryPalette[index % categoryPalette.length],
              }))
              .sort((a, b) => b.amount - a.amount);

            const savingsItems = buildFundItems(groupedByMacro['SAVINGS'] || []);
            const investmentsItems = buildFundItems(groupedByMacro['INVESTMENTS'] || []);

            const otherGroupKeys = Object.keys(groupedByMacro).filter((k) => !['INCOME', 'SAVINGS', 'INVESTMENTS'].includes(k));

            return (
              <>
                {groupedByMacro['INCOME'] && renderGroup('INCOME', groupedByMacro['INCOME'])}
                {renderPieAndList('SPENDING', 'Spending', '#455a64', spendingItems, 'expense')}
                {renderPieAndList('SAVINGS', 'Savings', macroCategoryConfig['SAVINGS'].color, savingsItems, 'neutral')}
                {renderPieAndList('INVESTMENTS', 'Investments', macroCategoryConfig['INVESTMENTS'].color, investmentsItems, 'neutral')}
                {otherGroupKeys.map((key) => renderGroup(key, groupedByMacro[key]))}
              </>
            );
          })()}
        </Paper>

        {/* Category Transactions Drill-down - only shown when a category is selected above */}
        {selectedCategory && (
          <Paper elevation={4} sx={{ p: 3, borderRadius: 4, boxShadow: '0 4px 24px rgba(0,0,0,0.08)', mb: 4 }}>
            <Typography variant="h6" fontWeight={700} color="text.primary" mb={2}>
              Transactions — {selectedCategory.categoryName} ({selectedMonth ? `${monthOptions.find(m => m.value === selectedMonth)?.label} ` : ''}{selectedYear})
            </Typography>
            {isLoadingCategoryTransactions ? (
              <ListRowsSkeleton rows={5} />
            ) : categoryTransactions && categoryTransactions.data.length > 0 ? (
              <Box sx={{ display: 'flex', flexDirection: 'column' }}>
                {categoryTransactions.data.map((tx) => (
                  <Box key={tx.id} sx={{ py: 1.25, borderBottom: '1px solid', borderColor: 'grey.200', display: 'flex', alignItems: 'center', justifyContent: 'space-between', gap: 2 }}>
                    <Box sx={{ minWidth: 0 }}>
                      <Typography variant="body2" fontWeight={700} noWrap>
                        {tx.description}
                      </Typography>
                      <Typography variant="caption" color="text.secondary">
                        {tx.date} · {tx.account}
                      </Typography>
                    </Box>
                    <Box
                      sx={{
                        bgcolor: tx.amount < 0 ? 'error.lighter' : 'success.lighter',
                        color: tx.amount < 0 ? 'error.dark' : 'success.dark',
                        px: 1.5,
                        py: 0.5,
                        borderRadius: 2,
                        fontWeight: 700,
                        flexShrink: 0,
                      }}
                    >
                      {tx.amount < 0 ? '-' : '+'}€{Math.abs(tx.amount).toLocaleString()}
                    </Box>
                  </Box>
                ))}
              </Box>
            ) : (
              <Typography variant="body2" color="text.secondary" textAlign="center" sx={{ py: 3 }}>
                No transactions found for this category in the selected period
              </Typography>
            )}
          </Paper>
        )}

        {/* Monthly Data Tables Row - Only show when viewing full year */}
        {!selectedMonth && (
          <Grid container spacing={3} sx={{ mb: 4 }}>
            {isLoadingMonthlyData ? (
              [0, 1, 2].map((i) => (
                <Grid key={i} size={{ xs: 12, md: 4 }}>
                  <ChartSkeleton height={300} />
                </Grid>
              ))
            ) : (
            <>
            <Grid size={{ xs: 12, md: 4 }}>
              <MonthlyDataTable
                title="INCOME"
                data={monthlyIncomeData}
                totalBudget={totalIncomeBudget}
                totalActual={totalIncomeActual}
                headerColor="#4caf50"
                type="income"
              />
            </Grid>
            <Grid size={{ xs: 12, md: 4 }}>
              <MonthlyDataTable
                title="TOTAL EXPENSES"
                data={monthlyExpensesData}
                totalBudget={totalExpensesBudget}
                totalActual={totalExpensesActual}
                headerColor="#f44336"
                type="expense"
              />
            </Grid>
            <Grid size={{ xs: 12, md: 4 }}>
              <MonthlyDataTable
                title="SAVINGS"
                data={monthlySavingsData}
                totalBudget={totalSavingsBudget}
                totalActual={totalSavingsActual}
                headerColor="#2196f3"
                type="savings"
              />
            </Grid>
            </>
            )}
          </Grid>
        )}
      </Box>
    </Layout>
  );
}
