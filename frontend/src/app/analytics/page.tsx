'use client';

import { Box, Paper, Typography, FormControl, InputLabel, Select, MenuItem, Grid } from '@mui/material';
import Layout from '../../components/Layout';
import { useQuery } from '@tanstack/react-query';
import { service } from '../../services/api';
import { useState } from 'react';
import MonthlyDataTable from '../../components/charts/MonthlyDataTable';
import HorizontalBarChart from '../../components/charts/HorizontalBarChart';
import { BarChart, Bar, XAxis, YAxis, Tooltip, Legend, ResponsiveContainer, CartesianGrid } from 'recharts';
import { se } from 'date-fns/locale';

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
      value: cat.totalSpent
    }));

  // Prepare pie chart data for Expenses
  const expenseData = categorySpending
    .map(cat => ({
      name: cat.categoryName,
      value: Math.abs(cat.totalSpent)
    }));

  // Prepare pie chart data for Savings
  const savingsData = categorySavings
    .map(cat => ({
      name: cat.categoryName,
      value: Math.abs(cat.totalSpent)
    }));

  // Colors for pie charts
  const incomeColors = ['#4caf50', '#66bb6a', '#81c784', '#a5d6a7', '#c8e6c9'];
  const expenseColors = ['#f44336', '#e57373', '#ef5350', '#ff5252', '#ff1744', '#d32f2f', '#c62828'];
  const savingsColors = ['#2196f3', '#42a5f5', '#64b5f6', '#90caf9', '#bbdefb'];

  // Prepare monthly income data for table
  const monthlyIncomeData = months.map((month, index) => {
    const monthNumber = monthNumberMap[month].toString().padStart(2, '0');
    console.log('monthlyData', monthlyData);
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
  console.log('monthlyData', monthlyData);

  console.log('monthlySavingsData', monthlySavingsData);

  // Calculate totals
  const totalIncomeBudget = monthlyIncomeData.reduce((sum, d) => sum + d.budget, 0);
  const totalIncomeActual = monthlyIncomeData.reduce((sum, d) => sum + d.actual, 0);
  const totalExpensesBudget = monthlyExpensesData.reduce((sum, d) => sum + d.budget, 0);
  const totalExpensesActual = monthlyExpensesData.reduce((sum, d) => sum + d.actual, 0);
  const totalSavingsBudget = monthlySavingsData.reduce((sum, d) => sum + d.budget, 0);
  const totalSavingsActual = monthlySavingsData.reduce((sum, d) => sum + d.actual, 0);

  // Prepare expense budget vs actual data for horizontal bar chart
  const expenseBudgetVsActual = categorySpending
    .map(cat => ({
      name: cat.categoryName,
      value: Math.abs(cat.totalSpent),
      budget: cat.budgetedAmount || 0
    }))
    .sort((a, b) => b.value - a.value);

  // Prepare income vs savings bar chart data
  const incomeVsSavingsData = selectedMonth
    ? // Weekly data for selected month
      Array.from({ length: 4 }, (_, weekIndex) => {
        const weekNum = weekIndex + 1;
        const monthNumber = selectedMonth;
        const monthData = monthlyData.find(d => d.month === monthNumber.toString() && d.year === selectedYear.toString() && d.week === weekNum);
        return {
          month: `Week ${weekNum}`,
          Income: monthData ? monthData.totalIncome : 0,
          Savings: monthData ? Math.abs(monthData.totalSavings) : 0
        };
      })
    : // Monthly data for full year
      months.map((month, index) => {
        const monthStr = monthNumberMap[month].toString().padStart(2, '0');
        const monthData = monthlyData.find(d => d.month === monthStr && d.year === selectedYear.toString());
        return {
          month,
          Income: monthData ? monthData.totalIncome : 0,
          Savings: monthData ? Math.abs(monthData.totalSavings) : 0
        };
      });

  // Prepare income vs expense bar chart data
  const incomeVsExpenseData = selectedMonth
    ? // Weekly data for selected month
      Array.from({ length: 4 }, (_, weekIndex) => {
        const weekNum = weekIndex + 1;
        const monthData = monthlyData.find(d => d.month === selectedMonth.toString() && d.year === selectedYear.toString() && d.week === weekNum);
      
        return {
          month: `Week ${weekNum}`,
          Income: monthData ? monthData.totalIncome : 0,
          Expense: monthData ? Math.abs(monthData.totalExpenses) : 0
        };
      })
    : // Monthly data for full year
      months.map((month, index) => {
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

        {/* Category Breakdown Charts */}
        <Grid container spacing={3} sx={{ mb: 4 }}>
          <Grid item xs={12} md={4}>
            <Paper elevation={4} sx={{ p: 3, borderRadius: 4, boxShadow: '0 4px 24px rgba(0,0,0,0.08)', height: '100%' }}>
              <Typography variant="h6" fontWeight={700} color="success.main" mb={2} textAlign="center">
                INCOME BY CATEGORY
              </Typography>
              <Box sx={{ width: '100%', height: 400, overflowY: 'auto' }}>
                {incomeData.sort((a, b) => b.value - a.value).map((item, index) => (
                  <Box key={index} sx={{ mb: 2 }}>
                    <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 0.5 }}>
                      <Typography variant="body2" fontWeight={600} noWrap sx={{ maxWidth: '60%' }}>
                        {item.name}
                      </Typography>
                      <Typography variant="body2" fontWeight={700} color="success.main">
                        €{item.value.toLocaleString()}
                      </Typography>
                    </Box>
                    <Box sx={{
                      width: '100%',
                      height: 8,
                      bgcolor: 'grey.200',
                      borderRadius: 1,
                      overflow: 'hidden'
                    }}>
                      <Box sx={{
                        width: `${(item.value / Math.max(...incomeData.map(d => d.value))) * 100}%`,
                        height: '100%',
                        bgcolor: incomeColors[index % incomeColors.length],
                        borderRadius: 1,
                        transition: 'width 0.3s'
                      }} />
                    </Box>
                  </Box>
                ))}
              </Box>
            </Paper>
          </Grid>
          <Grid item xs={12} md={4}>
            <Paper elevation={4} sx={{ p: 3, borderRadius: 4, boxShadow: '0 4px 24px rgba(0,0,0,0.08)', height: '100%' }}>
              <Typography variant="h6" fontWeight={700} color="error.main" mb={2} textAlign="center">
                EXPENSES BY CATEGORY
              </Typography>
              <Box sx={{ width: '100%', height: 400, overflowY: 'auto' }}>
                {expenseData.sort((a, b) => b.value - a.value).map((item, index) => (
                  <Box key={index} sx={{ mb: 2 }}>
                    <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 0.5 }}>
                      <Typography variant="body2" fontWeight={600} noWrap sx={{ maxWidth: '60%' }}>
                        {item.name}
                      </Typography>
                      <Typography variant="body2" fontWeight={700} color="error.main">
                        €{item.value.toLocaleString()}
                      </Typography>
                    </Box>
                    <Box sx={{
                      width: '100%',
                      height: 8,
                      bgcolor: 'grey.200',
                      borderRadius: 1,
                      overflow: 'hidden'
                    }}>
                      <Box sx={{
                        width: `${(item.value / Math.max(...expenseData.map(d => d.value))) * 100}%`,
                        height: '100%',
                        bgcolor: expenseColors[index % expenseColors.length],
                        borderRadius: 1,
                        transition: 'width 0.3s'
                      }} />
                    </Box>
                  </Box>
                ))}
              </Box>
            </Paper>
          </Grid>
          <Grid item xs={12} md={4}>
            <Paper elevation={4} sx={{ p: 3, borderRadius: 4, boxShadow: '0 4px 24px rgba(0,0,0,0.08)', height: '100%' }}>
              <Typography variant="h6" fontWeight={700} color="info.main" mb={2} textAlign="center">
                SAVINGS BY CATEGORY
              </Typography>
              <Box sx={{ width: '100%', height: 400, overflowY: 'auto' }}>
                {savingsData.sort((a, b) => b.value - a.value).map((item, index) => (
                  <Box key={index} sx={{ mb: 2 }}>
                    <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 0.5 }}>
                      <Typography variant="body2" fontWeight={600} noWrap sx={{ maxWidth: '60%' }}>
                        {item.name}
                      </Typography>
                      <Typography variant="body2" fontWeight={700} color="info.main">
                        €{item.value.toLocaleString()}
                      </Typography>
                    </Box>
                    <Box sx={{
                      width: '100%',
                      height: 8,
                      bgcolor: 'grey.200',
                      borderRadius: 1,
                      overflow: 'hidden'
                    }}>
                      <Box sx={{
                        width: `${(item.value / Math.max(...savingsData.map(d => d.value))) * 100}%`,
                        height: '100%',
                        bgcolor: savingsColors[index % savingsColors.length],
                        borderRadius: 1,
                        transition: 'width 0.3s'
                      }} />
                    </Box>
                  </Box>
                ))}
              </Box>
            </Paper>
          </Grid>
        </Grid>

        {/* Income vs Savings Bar Chart */}
        <Box sx={{ mb: 4 }}>
          <Paper elevation={4} sx={{ p: 3, borderRadius: 4, boxShadow: '0 4px 24px #b2dfdb33', background: 'linear-gradient(135deg, #e8f5e9 0%, #ffffff 100%)' }}>
            <Typography variant="h6" fontWeight={700} color="text.primary" mb={2} textAlign="center">
              INCOME vs SAVINGS {selectedMonth ? '(Weekly)' : '(Monthly)'}
            </Typography>
            <Box sx={{ width: '100%', height: 300 }}>
              <ResponsiveContainer width="100%" height="100%">
                <BarChart data={incomeVsSavingsData} margin={{ top: 20, right: 30, left: 20, bottom: 5 }}>
                  <CartesianGrid strokeDasharray="3 3" />
                  <XAxis dataKey="month" tick={{ fontSize: 12 }} />
                  <YAxis tick={{ fontSize: 12 }} tickFormatter={(value) => `€${value.toLocaleString()}`} />
                  <Tooltip formatter={(value) => `€${Number(value).toLocaleString()}`} />
                  <Legend />
                  <Bar dataKey="Income" fill="#4caf50" radius={[4, 4, 0, 0]} />
                  <Bar dataKey="Savings" fill="#2196f3" radius={[4, 4, 0, 0]} />
                </BarChart>
              </ResponsiveContainer>
            </Box>
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

        {/* Category Tables Row */}
        <Grid container spacing={3} sx={{ mb: 4 }}>
          <Grid item xs={12} md={4}>
            <Paper elevation={4} sx={{ p: 3, borderRadius: 4, boxShadow: '0 4px 24px rgba(255,152,0,0.15)' }}>
              <Box sx={{ bgcolor: '#ff9800', p: 2, borderRadius: 3, mb: 2, boxShadow: '0 2px 8px rgba(0,0,0,0.1)' }}>
                <Typography variant="h6" fontWeight={700} color="white" textAlign="center">
                  BILLS
                </Typography>
                <Typography variant="h5" fontWeight={700} color="white" textAlign="center" mt={1}>
                  € {billsData.reduce((sum, cat) => sum + Math.abs(cat.totalSpent), 0).toLocaleString()}
                </Typography>
              </Box>
              {billsData.map((cat, index) => (
                <Box key={index} sx={{ display: 'flex', justifyContent: 'space-between', mb: 1, p: 1.5, borderRadius: 2, bgcolor: '#fafafa', '&:hover': { bgcolor: '#f5f5f5' } }}>
                  <Typography variant="body2" fontWeight={500}>{cat.categoryName}</Typography>
                  <Typography variant="body2" fontWeight={700}>€ {Math.abs(cat.totalSpent).toLocaleString()}</Typography>
                </Box>
              ))}
            </Paper>
          </Grid>
          <Grid item xs={12} md={4}>
            <Paper elevation={4} sx={{ p: 3, borderRadius: 4, boxShadow: '0 4px 24px rgba(121,85,72,0.15)' }}>
              <Box sx={{ bgcolor: '#795548', p: 2, borderRadius: 3, mb: 2, boxShadow: '0 2px 8px rgba(0,0,0,0.1)' }}>
                <Typography variant="h6" fontWeight={700} color="white" textAlign="center">
                  DEBTS
                </Typography>
                <Typography variant="h5" fontWeight={700} color="white" textAlign="center" mt={1}>
                  € {debtsData.reduce((sum, cat) => sum + Math.abs(cat.totalSpent), 0).toLocaleString()}
                </Typography>
              </Box>
              {debtsData.map((cat, index) => (
                <Box key={index} sx={{ display: 'flex', justifyContent: 'space-between', mb: 1, p: 1.5, borderRadius: 2, bgcolor: '#fafafa', '&:hover': { bgcolor: '#f5f5f5' } }}>
                  <Typography variant="body2" fontWeight={500}>{cat.categoryName}</Typography>
                  <Typography variant="body2" fontWeight={700}>€ {Math.abs(cat.totalSpent).toLocaleString()}</Typography>
                </Box>
              ))}
            </Paper>
          </Grid>
          <Grid item xs={12} md={4}>
            <Paper elevation={4} sx={{ p: 3, borderRadius: 4, boxShadow: '0 4px 24px rgba(156,39,176,0.15)' }}>
              <Box sx={{ bgcolor: '#9c27b0', p: 2, borderRadius: 3, mb: 2, boxShadow: '0 2px 8px rgba(0,0,0,0.1)' }}>
                <Typography variant="h6" fontWeight={700} color="white" textAlign="center">
                  SUBSCRIPTIONS
                </Typography>
                <Typography variant="h5" fontWeight={700} color="white" textAlign="center" mt={1}>
                  € {subscriptionsData.reduce((sum, cat) => sum + Math.abs(cat.totalSpent), 0).toLocaleString()}
                </Typography>
              </Box>
              {subscriptionsData.map((cat, index) => (
                <Box key={index} sx={{ display: 'flex', justifyContent: 'space-between', mb: 1, p: 1.5, borderRadius: 2, bgcolor: '#fafafa', '&:hover': { bgcolor: '#f5f5f5' } }}>
                  <Typography variant="body2" fontWeight={500}>{cat.categoryName}</Typography>
                  <Typography variant="body2" fontWeight={700}>€ {Math.abs(cat.totalSpent).toLocaleString()}</Typography>
                </Box>
              ))}
            </Paper>
          </Grid>
        </Grid>

        {/* Expense Budget vs Actual Chart */}
        <Box sx={{ mb: 4 }}>
          <HorizontalBarChart
            title="EXPENSE BUDGET vs ACTUAL"
            data={expenseBudgetVsActual}
            color="#f44336"
            showBudget={true}
          />
        </Box>

        {/* Income vs Expense Chart */}
        <Box sx={{ mb: 4 }}>
          <Paper elevation={4} sx={{ p: 3, borderRadius: 4, boxShadow: '0 4px 24px #b2dfdb33', background: 'linear-gradient(135deg, #ffebee 0%, #ffffff 100%)' }}>
            <Typography variant="h6" fontWeight={700} color="text.primary" mb={2} textAlign="center">
              INCOME vs EXPENSE {selectedMonth ? '(Weekly)' : '(Monthly)'}
            </Typography>
            <Box sx={{ width: '100%', height: 300 }}>
              <ResponsiveContainer width="100%" height="100%">
                <BarChart data={incomeVsExpenseData} margin={{ top: 20, right: 30, left: 20, bottom: 5 }}>
                  <CartesianGrid strokeDasharray="3 3" />
                  <XAxis dataKey="month" tick={{ fontSize: 12 }} />
                  <YAxis tick={{ fontSize: 12 }} tickFormatter={(value) => `€${value.toLocaleString()}`} />
                  <Tooltip formatter={(value) => `€${Number(value).toLocaleString()}`} />
                  <Legend />
                  <Bar dataKey="Income" fill="#4caf50" radius={[4, 4, 0, 0]} />
                  <Bar dataKey="Expense" fill="#f44336" radius={[4, 4, 0, 0]} />
                </BarChart>
              </ResponsiveContainer>
            </Box>
          </Paper>
        </Box>

        {/* Expenses Table */}
        <Box sx={{ mb: 4 }}>
          <Paper elevation={4} sx={{ p: 3, borderRadius: 4, boxShadow: '0 4px 24px rgba(255,87,34,0.15)' }}>
            <Box sx={{ bgcolor: '#ff5722', p: 2, borderRadius: 3, mb: 2, boxShadow: '0 2px 8px rgba(0,0,0,0.1)' }}>
              <Typography variant="h6" fontWeight={700} color="white" textAlign="center">
                EXPENSES
              </Typography>
              <Typography variant="h5" fontWeight={700} color="white" textAlign="center" mt={1}>
                € {expenseData.reduce((sum, cat) => sum + cat.value, 0).toLocaleString()}
              </Typography>
            </Box>
            <Grid container spacing={2}>
              {expenseData.map((cat, index) => (
                <Grid item xs={12} sm={6} md={3} key={index}>
                  <Box sx={{ p: 2, bgcolor: '#fafafa', borderRadius: 2, boxShadow: '0 1px 4px rgba(0,0,0,0.05)', transition: 'all 0.2s', '&:hover': { transform: 'translateY(-2px)', boxShadow: '0 2px 8px rgba(0,0,0,0.1)' } }}>
                    <Typography variant="body2" color="text.secondary" gutterBottom fontWeight={500}>
                      {cat.name}
                    </Typography>
                    <Typography variant="h6" fontWeight={700}>
                      € {cat.value.toLocaleString()}
                    </Typography>
                  </Box>
                </Grid>
              ))}
            </Grid>
          </Paper>
        </Box>
      </Box>
    </Layout>
  );
}
