'use client';

import { Box, Paper, Typography, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Divider, LinearProgress, Avatar, Stack, Chip, CircularProgress, FormControl, InputLabel, Select, MenuItem } from '@mui/material';
import { useAuth } from '../../contexts/AuthContext';
import Layout from '../../components/Layout';
import { TrendingUp, TrendingDown, Savings, BarChart as MuiBarChart, PieChart, ArrowUpward, ArrowDownward, Timeline, Analytics } from '@mui/icons-material';
import { BarChart, Bar, XAxis, YAxis, Tooltip, Legend, ResponsiveContainer, CartesianGrid, Pie, Cell, LineChart, Line } from 'recharts';
import { useQuery } from '@tanstack/react-query';
import { service } from '../../services/api';
import { MonthlyData } from '../../types';
import { useState } from 'react';

export default function AnalyticsPage() {
  const { user } = useAuth();
  const currentYear = new Date().getFullYear();
  const [selectedYear, setSelectedYear] = useState<number>(currentYear);
  const [selectedMonth, setSelectedMonth] = useState<number | undefined>(undefined);

  // Year options for the selector (current year and 4 years back)
  const yearOptions = [];
  for (let year = currentYear; year >= currentYear - 4; year--) {
    yearOptions.push({ value: year, label: year.toString() });
  }

  // Month options for the selector
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

  // Fetch all the detailed analytics data
  const { data: categorySpending = [] } = useQuery({
    queryKey: ['categorySpending', selectedYear, selectedMonth],
    queryFn: () => selectedMonth
      ? service.getCategorySpending(selectedYear, selectedMonth)
      : service.getCategorySpending(selectedYear),
  });

  const { data: monthlyData = [] } = useQuery({
    queryKey: ['monthlyData', selectedYear, selectedMonth],
    queryFn: () => service.getMonthlyData(selectedYear, selectedMonth)
  });

  const { data: macroCategoryTrends = [] } = useQuery({
    queryKey: ['macroCategoryTrends', selectedYear, selectedMonth],
    queryFn: () => service.getMacroCategoriesMontlyData(selectedYear, selectedMonth)
  });

  const { data: accountFlowData = [] } = useQuery({
    queryKey: ['accountFlowData', selectedYear, selectedMonth],
    queryFn: () => service.getAccountFlowData(selectedYear, selectedMonth)
  });

  const { data: accounts = [] } = useQuery({
    queryKey: ['accounts'],
    queryFn: () => service.getAccounts()
  });

  const { data: categoryTrendsData = [] } = useQuery({
    queryKey: ['categoryTrendsData', selectedYear, selectedMonth],
    queryFn: () => service.getCategoryTrendsData(selectedYear, selectedMonth)
  });

  const { data: cumulativeSpendingData = [] } = useQuery({
    queryKey: ['cumulativeSpendingData', selectedYear, selectedMonth],
    queryFn: () => service.getCumulativeSpendingData(selectedYear, selectedMonth)
  });

  const { data: largestExpenses = [] } = useQuery({
    queryKey: ['largestExpenses', selectedYear, selectedMonth],
    queryFn: () => service.getLargestExpenses(selectedYear, selectedMonth, 10)
  });

  // Generate chart data based on selection
  const monthTransactions = selectedMonth
    ? (() => {
      // For monthly view, show weeks or days of the selected month
      const daysInMonth = new Date(selectedYear, selectedMonth, 0).getDate();
      const monthData = monthlyData.filter((tx: MonthlyData) => {
        const [year, month] = tx.month.split('-');
        return Number(year) === selectedYear && Number(month) === selectedMonth;
      });
      const weekData = [];
      for (let week = 1; week <= Math.ceil(daysInMonth / 7); week++) {
        const wd = monthData.find(m => m.week === week);
        weekData.push({
          month: `Week ${week}`,
          Income: wd ? wd.totalIncome / 4 : 0,
          Expense: wd ? wd.totalExpenses / 4 : 0,
          Savings: wd ? wd.totalSavings / 4 : 0,
        });
      }
      return weekData;
    })()
    : months.map((month, index) => {
      const monthData = monthlyData.find((tx: MonthlyData) => {
        const [year, monthStr] = tx.month.split('-');
        return Number(year) === selectedYear && Number(monthStr) === index + 1;
      });
      
      return {
        month,
        Income: monthData ? monthData.totalIncome : 0,
        Expense: monthData ? monthData.totalExpenses : 0,
        Savings: monthData ? monthData.totalSavings : 0,
      };
    });

  // Define colors for each macro category
  const macroCategoryColors: { [key: string]: string } = {
    'EXPENSE': '#f44336',
    'BILLS': '#ff9800',
    'SAVINGS': '#2196f3',
    'SUBSCRIPTIONS': '#9c27b0',
    'DEBTS': '#795548',
    'INCOME': '#4caf50'
  };

  // Get unique macro categories from the data
  const uniqueMacroCategories = [...new Set(macroCategoryTrends.map(item => item.macroCategory))];

  // Transform account flow data for chart
  const transformedAccountData = (() => {
    if (!accountFlowData || accountFlowData.length === 0) return [];
    
    // Group data by period and aggregate by account
    const periodGroups: { [period: string]: { [account: string]: { expenses: number; savings: number; income: number; total: number } } } = {};
    
    accountFlowData.forEach(item => {
      if (!periodGroups[item.period]) {
        periodGroups[item.period] = {};
      }
      periodGroups[item.period][item.accountName] = {
        expenses: item.expenses,
        savings: item.savings,
        income: item.income,
        total: item.total
      };
    });
    
    // Convert to chart format
    return Object.keys(periodGroups).map(period => {
      const result: any = { period };
      Object.keys(periodGroups[period]).forEach(account => {
        result[`${account}_Expenses`] = periodGroups[period][account].expenses;
        result[`${account}_Savings`] = periodGroups[period][account].savings;
        result[`${account}_Income`] = periodGroups[period][account].income;
        result[`${account}_Total`] = periodGroups[period][account].total;
      });
      return result;
    });
  })();

  // Get unique accounts for the account chart
  const uniqueAccounts = [...new Set(accountFlowData.map(item => item.accountName))];
  
  // Generate dynamic colors for accounts based on API data
  const generateAccountColors = (accounts: any[]) => {
    const colors = ['#4caf50', '#2196f3', '#ff9800', '#9c27b0', '#f44336', '#795548', '#607d8b'];
    const accountColors: { [key: string]: string } = {};
    accounts.forEach((account, index) => {
      accountColors[account.name] = colors[index % colors.length];
    });
    return accountColors;
  };
  
  const accountColors = generateAccountColors(accounts);

  // Transform trends data for line chart
  const trendsChartData = (() => {
    if (!categoryTrendsData || categoryTrendsData.length === 0) return [];
    
    if (selectedMonth !== undefined) {
      // Monthly view - show weekly trends
      const weeks = [...new Set(categoryTrendsData.map(item => item.week))].sort();
      return weeks.map(week => {
        const weekData: any = { period: `Week ${week}` };
        const weekItems = categoryTrendsData.filter(item => item.week === week);
        
        weekItems.forEach(item => {
          weekData[item.categoryName] = item.total;
        });
        
        return weekData;
      });
    } else {
      // Yearly view - show monthly trends
      return months.map((monthName, index) => {
        const monthData: any = { period: monthName };
        const monthItems = categoryTrendsData.filter(item => item.month === index + 1);
        
        monthItems.forEach(item => {
          monthData[item.categoryName] = item.total;
        });
        
        return monthData;
      });
    }
  })();

  // Get unique categories for trends chart
  const uniqueCategories = [...new Set(categoryTrendsData.map(item => item.categoryName))];

  // Generate colors for categories
  const generateCategoryColors = (categories: string[]) => {
    const colors = ['#f44336', '#e91e63', '#9c27b0', '#673ab7', '#3f51b5', '#2196f3', '#03a9f4', '#00bcd4', '#009688', '#4caf50', '#8bc34a', '#cddc39', '#ffeb3b', '#ffc107', '#ff9800', '#ff5722'];
    const categoryColors: { [key: string]: string } = {};
    categories.forEach((category, index) => {
      categoryColors[category] = colors[index % colors.length];
    });
    return categoryColors;
  };

  const categoryColors = generateCategoryColors(uniqueCategories);

  // Transform cumulative spending data for chart
  const cumulativeChartData = (() => {
    if (!cumulativeSpendingData || cumulativeSpendingData.length === 0) return [];
    
    // Group data by period
    const periodGroups: { [period: string]: any } = {};
    
    cumulativeSpendingData.forEach(item => {
      if (!periodGroups[item.period]) {
        periodGroups[item.period] = { period: item.period };
      }
      periodGroups[item.period][`${item.categoryName}_Cumulative`] = item.cumulativeAmount;
      periodGroups[item.period][`${item.categoryName}_Budget`] = item.budgetCumulative;
    });
    
    return Object.values(periodGroups);
  })();

  // Get unique categories for cumulative chart
  const uniqueCumulativeCategories = [...new Set(cumulativeSpendingData.map(item => item.categoryName))];

  return (
    <Layout>
      <Box sx={{ bgcolor: '#f5f6fa', minHeight: '100vh', p: { xs: 1, md: 4 } }}>
        {/* Header */}
        <Box sx={{ mb: 4 }}>
          <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
            <Analytics color="primary" sx={{ mr: 2, fontSize: 32 }} />
            <Typography variant="h4" fontWeight={700} color="primary.dark">
              Financial Analytics
            </Typography>
          </Box>
          <Typography variant="body1" color="text.secondary">
            Detailed analysis and insights into your financial data with flexible time period selection
          </Typography>
        </Box>

        {/* Year and Month Selectors */}
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
          <FormControl sx={{ minWidth: 200 }}>
            <InputLabel id="month-selector-label">Filter by Month</InputLabel>
            <Select
              labelId="month-selector-label"
              value={selectedMonth}
              label="Filter by Month"
              onChange={(e) => setSelectedMonth(e.target.value as number | undefined)}
              sx={{ bgcolor: 'white' }}
            >
              {monthOptions.map((option) => (
                <MenuItem key={option.value || 'all'} value={option.value}>
                  {option.label}
                </MenuItem>
              ))}
            </Select>
          </FormControl>
        </Box>

        {/* Money Flow Chart */}
        <Paper elevation={4} sx={{ p: 3, borderRadius: 4, boxShadow: '0 4px 24px #b2dfdb33', mb: 3, background: 'linear-gradient(135deg, #e8f5e9 0%, #ffffff 100%)' }}>
          <Box sx={{ display: 'flex', alignItems: 'center', mb: 3 }}>
            <TrendingUp color="success" sx={{ mr: 1 }} />
            <Typography color="success.dark" fontWeight={700} variant="h6">
              Money Flow Analysis - {selectedMonth ? `${monthOptions.find(m => m.value === selectedMonth)?.label} ${selectedYear}` : `${selectedYear}`}
            </Typography>
          </Box>
          <Box sx={{ width: '100%', height: 400, mb: 2 }}>
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={monthTransactions} margin={{ top: 20, right: 30, left: 20, bottom: 5 }}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="month" />
                <YAxis tickFormatter={(value) => `€${value.toLocaleString()}`} />
                <Tooltip formatter={(value) => `€${value?.toLocaleString()}`} />
                <Legend />
                <Bar dataKey="Income" fill="#4caf50" radius={[4, 4, 0, 0]} />
                <Bar dataKey="Expense" fill="#f44336" radius={[4, 4, 0, 0]} />
                <Bar dataKey="Savings" fill="#2196f3" radius={[4, 4, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </Box>
        </Paper>

        {/* Account Flow Chart */}
        <Paper elevation={4} sx={{ p: 3, borderRadius: 4, boxShadow: '0 4px 24px #b2dfdb33', mb: 3, background: 'linear-gradient(135deg, #f3e5f5 0%, #ffffff 100%)' }}>
          <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
            <MuiBarChart color="secondary" sx={{ mr: 1 }} />
            <Typography color="secondary.dark" fontWeight={700} variant="h6">
              Account Total Balance - {selectedMonth ? `${monthOptions.find(m => m.value === selectedMonth)?.label} ${selectedYear}` : `${selectedYear}`}
            </Typography>
          </Box>
          <Box sx={{ width: '100%', height: 400, mb: 2 }}>
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={transformedAccountData} margin={{ top: 20, right: 30, left: 20, bottom: 100 }}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis 
                  dataKey="period" 
                  tick={{ fontSize: 11 }} 
                  angle={-45}
                  textAnchor="end"
                  height={100}
                  interval={0}
                />
                <YAxis tick={{ fontSize: 12 }} tickFormatter={(value) => `€${value.toLocaleString()}`} />
                <Tooltip formatter={(value) => `€${value?.toLocaleString()}`} />
                <Legend verticalAlign="top" height={40} />
                {uniqueAccounts.map((account) => (
                  <Bar
                    key={`${account}_Total`}
                    dataKey={`${account}_Total`}
                    fill={accountColors[account] || '#666666'}
                    radius={[4, 4, 0, 0]}
                    name={`${account} Total`}
                  />
                ))}
              </BarChart>
            </ResponsiveContainer>
          </Box>
          <Typography variant="caption" color="text.secondary" sx={{ mt: 1 }}>
            Total balance (income + savings - expenses) per account
          </Typography>
        </Paper>

        {/* Category Trends Over Time */}
        <Paper elevation={4} sx={{ p: 3, borderRadius: 4, boxShadow: '0 4px 24px #b2dfdb33', mb: 3, background: 'linear-gradient(135deg, #fff3e0 0%, #ffffff 100%)' }}>
          <Box sx={{ display: 'flex', alignItems: 'center', mb: 3 }}>
            <Timeline color="warning" sx={{ mr: 1 }} />
            <Typography color="warning.dark" fontWeight={700} variant="h6">
              Category Trends Over Time - {selectedMonth ? `${monthOptions.find(m => m.value === selectedMonth)?.label} ${selectedYear}` : `${selectedYear}`}
            </Typography>
          </Box>
          <Box sx={{ width: '100%', height: 400, mb: 2 }}>
            <ResponsiveContainer width="100%" height="100%">
              <LineChart data={trendsChartData} margin={{ top: 20, right: 30, left: 20, bottom: 60 }}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis 
                  dataKey="period" 
                  angle={-45}
                  textAnchor="end"
                  height={80}
                  interval={0}
                  fontSize={12}
                />
                <YAxis 
                  tickFormatter={(value) => `€${value.toLocaleString()}`}
                  width={80}
                />
                <Tooltip 
                  formatter={(value: number, name: string) => [`€${value.toLocaleString()}`, name]}
                  labelFormatter={(label) => `Period: ${label}`}
                />
                <Legend />
                {uniqueCategories.map((category) => (
                  <Line
                    key={category}
                    type="monotone"
                    dataKey={category}
                    stroke={categoryColors[category] || '#666666'}
                    strokeWidth={3}
                    dot={{ r: 6 }}
                    name={category}
                  />
                ))}
              </LineChart>
            </ResponsiveContainer>
          </Box>
          <Typography variant="caption" color="text.secondary" sx={{ mt: 1 }}>
            Track spending trends across different categories over time
          </Typography>
        </Paper>

        {/* Cumulative Spending Chart */}
        <Paper elevation={4} sx={{ p: 3, borderRadius: 4, boxShadow: '0 4px 24px #b2dfdb33', mb: 3, background: 'linear-gradient(135deg, #fce4ec 0%, #ffffff 100%)' }}>
          <Box sx={{ display: 'flex', alignItems: 'center', mb: 3 }}>
            <TrendingUp color="error" sx={{ mr: 1 }} />
            <Typography color="error.dark" fontWeight={700} variant="h6">
              Cumulative Spending vs Budget - {selectedMonth ? `${monthOptions.find(m => m.value === selectedMonth)?.label} ${selectedYear}` : `${selectedYear}`}
            </Typography>
          </Box>
          <Box sx={{ width: '100%', height: 400, mb: 2 }}>
            <ResponsiveContainer width="100%" height="100%">
              <LineChart data={cumulativeChartData} margin={{ top: 20, right: 30, left: 20, bottom: 60 }}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis 
                  dataKey="period" 
                  angle={-45}
                  textAnchor="end"
                  height={80}
                  interval={0}
                  fontSize={12}
                />
                <YAxis 
                  tickFormatter={(value) => `€${value.toLocaleString()}`}
                  width={80}
                />
                <Tooltip 
                  formatter={(value: number, name: string) => [`€${value.toLocaleString()}`, name]}
                  labelFormatter={(label) => `Period: ${label}`}
                />
                <Legend />
                {uniqueCumulativeCategories.map((category) => [
                  <Line
                    key={`${category}_Cumulative`}
                    type="monotone"
                    dataKey={`${category}_Cumulative`}
                    stroke={categoryColors[category] || '#666666'}
                    strokeWidth={3}
                    dot={{ r: 6 }}
                    name={`${category} Spent`}
                  />,
                  <Line
                    key={`${category}_Budget`}
                    type="monotone"
                    dataKey={`${category}_Budget`}
                    stroke={categoryColors[category] || '#666666'}
                    strokeWidth={2}
                    strokeDasharray="5 5"
                    dot={{ r: 4 }}
                    name={`${category} Budget`}
                  />
                ]).flat()}
              </LineChart>
            </ResponsiveContainer>
          </Box>
          <Typography variant="caption" color="text.secondary" sx={{ mt: 1 }}>
            Monitor if you're burning through your budget too fast - solid lines show actual spending, dashed lines show budget pace
          </Typography>
        </Paper>

        {/* Largest Expenses Report */}
        <Paper elevation={4} sx={{ p: 3, borderRadius: 4, boxShadow: '0 4px 24px #b2dfdb33', mb: 3, background: 'linear-gradient(135deg, #fff9c4 0%, #ffffff 100%)' }}>
          <Box sx={{ display: 'flex', alignItems: 'center', mb: 3 }}>
            <ArrowDownward color="warning" sx={{ mr: 1 }} />
            <Typography color="warning.dark" fontWeight={700} variant="h6">
              Top 10 Largest Expenses - {selectedMonth ? `${monthOptions.find(m => m.value === selectedMonth)?.label} ${selectedYear}` : `${selectedYear}`}
            </Typography>
          </Box>
          
          {largestExpenses.length > 0 ? (
            <TableContainer>
              <Table>
                <TableHead>
                  <TableRow>
                    <TableCell sx={{ fontWeight: 700 }}>Rank</TableCell>
                    <TableCell sx={{ fontWeight: 700 }}>Date</TableCell>
                    <TableCell sx={{ fontWeight: 700 }}>Description</TableCell>
                    <TableCell sx={{ fontWeight: 700 }}>Category</TableCell>
                    <TableCell sx={{ fontWeight: 700 }}>Account</TableCell>
                    <TableCell sx={{ fontWeight: 700, textAlign: 'right' }}>Amount</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {largestExpenses.map((expense, index) => (
                    <TableRow key={expense.id} sx={{ '&:hover': { backgroundColor: 'action.hover' } }}>
                      <TableCell>
                        <Chip 
                          label={`#${index + 1}`} 
                          size="small" 
                          color={index < 3 ? 'error' : 'default'}
                          sx={{ fontWeight: 600 }}
                        />
                      </TableCell>
                      <TableCell>
                        <Typography variant="body2" fontWeight={500}>
                          {new Date(expense.date).toLocaleDateString()}
                        </Typography>
                      </TableCell>
                      <TableCell>
                        <Typography variant="body2" sx={{ maxWidth: 200, overflow: 'hidden', textOverflow: 'ellipsis' }}>
                          {expense.description}
                        </Typography>
                      </TableCell>
                      <TableCell>
                        <Chip 
                          label={expense.categoryName || 'Uncategorized'} 
                          size="small" 
                          variant="outlined"
                          sx={{ fontSize: '0.7rem' }}
                        />
                      </TableCell>
                      <TableCell>
                        <Typography variant="body2" color="text.secondary">
                          {expense.account}
                        </Typography>
                      </TableCell>
                      <TableCell sx={{ textAlign: 'right' }}>
                        <Typography variant="body1" fontWeight={700} color="error.main">
                          €{expense.amount.toLocaleString()}
                        </Typography>
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </TableContainer>
          ) : (
            <Box sx={{ textAlign: 'center', py: 4 }}>
              <Typography variant="body1" color="text.secondary">
                No expense data available for this period
              </Typography>
            </Box>
          )}
          
          <Typography variant="caption" color="text.secondary" sx={{ mt: 2, display: 'block' }}>
            Track your biggest spending items to identify opportunities for savings
          </Typography>
        </Paper>
      </Box>
    </Layout>
  );
}
