'use client';

import { Box, Paper, Typography, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, LinearProgress, Chip, FormControl, InputLabel, Select, MenuItem } from '@mui/material';
import Layout from '../components/Layout';
import { TrendingUp, TrendingDown, Savings, BarChart as MuiBarChart, PieChart, Timeline } from '@mui/icons-material';
import { Bar, XAxis, YAxis, Tooltip, Legend, ResponsiveContainer, CartesianGrid, LineChart, Line, ComposedChart, LabelList } from 'recharts';
import { useQuery } from '@tanstack/react-query';
import { service } from '../services/api';
import { MonthlyData } from '../types';
import { useState } from 'react';

export default function HomePage() {
  // const { user } = useAuth();
  const currentYear = new Date().getFullYear();
  const [selectedYear, setSelectedYear] = useState<number>(currentYear);
  // Year options for the selector (current year and 4 years back)
  const yearOptions = [];
  for (let year = currentYear; year >= currentYear - 4; year--) {
    yearOptions.push({ value: year, label: year.toString() });
  }

  // Month options for the selector
  // const monthOptions = [
  //   { value: undefined, label: 'All Year' },
  //   { value: 1, label: 'January' },
  //   { value: 2, label: 'February' },
  //   { value: 3, label: 'March' },
  //   { value: 4, label: 'April' },
  //   { value: 5, label: 'May' },
  //   { value: 6, label: 'June' },
  //   { value: 7, label: 'July' },
  //   { value: 8, label: 'August' },
  //   { value: 9, label: 'September' },
  //   { value: 10, label: 'October' },
  //   { value: 11, label: 'November' },
  //   { value: 12, label: 'December' },
  // ];

  // Fetch data with month filtering
  const { data: totalIncome = 0 } = useQuery({
    queryKey: ['totalIncome', selectedYear],
    queryFn: () => service.getTotalIncome(selectedYear),
  });

  const { data: totalExpenses = 0 } = useQuery({
    queryKey: ['totalExpenses', selectedYear],
    queryFn: () => service.getTotalExpenses(selectedYear),
  });

  const { data: totalSavings = 0 } = useQuery({
    queryKey: ['totalSavings', selectedYear],
    queryFn: () => service.getTotalSavings(selectedYear),
  });

  const { data: incomeBudget = 0 } = useQuery({
    queryKey: ['incomeBudget', selectedYear],
    queryFn: () => service.getIncomeBudget(selectedYear),
  });

  const { data: expenseBudget = 0 } = useQuery({
    queryKey: ['expenseBudget', selectedYear],
    queryFn: () => service.getExpenseBudget(selectedYear),
  });

  const { data: savingsBudget = 0 } = useQuery({
    queryKey: ['savingsBudget', selectedYear],
    queryFn: () => service.getSavingsBudget(selectedYear),
  });


  // Calculate budget data
  const totalBudget = incomeBudget + expenseBudget + savingsBudget;
  const { data: categorySpending = [] } = useQuery({
    queryKey: ['categorySpending', selectedYear],
    queryFn: () => service.getCategorySpending(selectedYear),
  });

  // Money Flow data - show different data based on month selection
  const months = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];
  const { data: monthlyData = [] } = useQuery({
    queryKey: ['monthlyData', selectedYear],
    queryFn: () => service.getMonthlyData(selectedYear)
  });

  // Account Flow data
  const { data: accountFlowData = [] } = useQuery({
    queryKey: ['accountFlowData', selectedYear],
    queryFn: () => service.getAccountFlowData(selectedYear)
  });

  // Fetch accounts from API
  const { data: accounts = [] } = useQuery({
    queryKey: ['accounts'],
    queryFn: () => service.getAccounts()
  });

  // Generate chart data based on selection
  // const monthTransactions = months.map((month, index) => {
  //   const monthData = monthlyData.find((tx: MonthlyData) => {
  //
  //     return Number(tx.year) === selectedYear && Number(tx.month) === index + 1;
  //   });
  //   return {
  //     month,
  //     Income: monthData ? monthData.totalIncome : 0,
  //     Expense: monthData ? monthData.totalExpenses : 0,
  //     Savings: monthData ? monthData.totalSavings : 0,
  //   };
  // });


  const { data: macroCategoryTrends = [] } = useQuery({
    queryKey: ['macroCategoryTrends', selectedYear],
    queryFn: () => service.getMacroCategoriesMontlyData(selectedYear)
  });

  // Transform macro category data for line chart
  const transformedMacroData = months.map((month, index) => {
    const monthNumber = index + 1; // 1, 2, 3, etc.
    const monthData = macroCategoryTrends.filter(data => data.month === monthNumber);

    const result: { [key: string]: string | number } = { month };
    monthData.forEach(item => {
      result[item.macroCategory] = item.total;
    });

    return result;
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

  // Determine line color based on overall trend
  

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

  return (
    <Layout>
      <Box sx={{ bgcolor: '#f5f6fa', minHeight: '100vh', p: { xs: 1, md: 4 } }}>
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
        </Box>
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
          </Paper>

          {/* My Expenses Card */}
          <Paper elevation={4} sx={{ flex: 1, minWidth: 260, p: 3, borderRadius: 4, boxShadow: '0 4px 24px #ffcdd233', position: 'relative', overflow: 'hidden', background: 'linear-gradient(135deg, #ffebee 0%, #ffffff 100%)' }}>
            <Box sx={{ position: 'absolute', top: 16, right: 16, opacity: 0.15, fontSize: 80 }}>
              <TrendingDown fontSize="inherit" color="error" />
            </Box>
            <Typography color="error.main" fontWeight={700} mb={1}>
              <TrendingDown sx={{ verticalAlign: 'middle', mr: 1 }} /> My Expenses
            </Typography>
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
          </Paper>

          {/* Savings Card */}
          <Paper elevation={4} sx={{ flex: 1, minWidth: 260, p: 3, borderRadius: 4, boxShadow: '0 4px 24px #bbdefb33', position: 'relative', overflow: 'hidden', background: 'linear-gradient(135deg, #e3f2fd 0%, #ffffff 100%)' }}>
            <Box sx={{ position: 'absolute', top: 16, right: 16, opacity: 0.15, fontSize: 80 }}>
              <Savings fontSize="inherit" color="info" />
            </Box>
            <Typography color="info.main" fontWeight={700} mb={1}>
              <Savings sx={{ verticalAlign: 'middle', mr: 1 }} /> Savings
            </Typography>
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
              <ResponsiveContainer width="100%" height="100%">
                <ComposedChart data={transformedAccountData} margin={{ top: 40, right: 30, left: 20, bottom: 100 }}>
                  <CartesianGrid strokeDasharray="3 3" />
                  <XAxis
                    dataKey="period"
                    tick={{ fontSize: 11 }}
                    angle={-45}
                    textAnchor="end"
                    height={100}
                    interval={0}
                  />
                  <YAxis tick={{ fontSize: 12 }} tickFormatter={(value) => `$${value.toLocaleString()}`} />
                  <Tooltip formatter={(value) => `$${value?.toLocaleString()}`} />
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
                  <Line
                    type="monotone"
                    dataKey="Total_All_Accounts"
                    stroke={'#000000ff'}
                    strokeWidth={2}
                    dot={{ r: 2, fill: '#000000ff', stroke: '#000000ff', strokeWidth: 1 }}
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
            </Box>
            <Typography variant="caption" color="text.secondary" sx={{ mt: 1 }}>
              Total balance (income + savings - expenses) per account with overall total trend line
            </Typography>
          </Paper>
        </Box>


        {/* Macro Category Trends Chart */}
        <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 3, mb: 3 }}>
          <Paper elevation={4} sx={{ flex: 1, minWidth: 400, p: 3, borderRadius: 4, boxShadow: '0 4px 24px #b2dfdb33', display: 'flex', flexDirection: 'column', justifyContent: 'center', background: 'linear-gradient(135deg, #fffbf5ff 0%, #ffffff 100%)' }}>
            <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
              <Timeline color="warning" sx={{ mr: 1 }} />
              <Typography color="warning.dark" fontWeight={700} variant="subtitle1">
                Macro Category Trends
              </Typography>
            </Box>
            <Box sx={{ width: '100%', height: 280, mb: 2 }}>
              <ResponsiveContainer width="100%" height="100%">
                <LineChart data={transformedMacroData} margin={{ top: 10, right: 30, left: 0, bottom: 10 }}>
                  <CartesianGrid strokeDasharray="3 3" />
                  <XAxis dataKey="month" tick={{ fontSize: 12 }} />
                  <YAxis tick={{ fontSize: 12 }} />
                  <Tooltip formatter={(value) => `€${value?.toLocaleString()}`} />
                  <Legend verticalAlign="top" height={36} />
                  {uniqueMacroCategories.map((category) => (
                    <Line
                      key={category}
                      type="monotone"
                      dataKey={category}
                      stroke={macroCategoryColors[category as keyof typeof macroCategoryColors] || '#666666'}
                      strokeWidth={3}
                      dot={{ r: 4 }}
                      name={category.charAt(0) + category.slice(1).toLowerCase()}
                    />
                  ))}
                </LineChart>
              </ResponsiveContainer>
            </Box>
          </Paper>
        </Box>

        {/* Category Breakdown - Full Width */}
        <Paper elevation={4} sx={{ p: 3, borderRadius: 4, boxShadow: '0 4px 24px #b2dfdb33', mb: 3, background: 'linear-gradient(135deg, #e3f2fd 0%, #ffffff 100%)' }}>
          <Box sx={{ display: 'flex', alignItems: 'center', mb: 3 }}>
            <PieChart color="info" sx={{ mr: 1 }} />
            <Typography color="info.dark" fontWeight={700} variant="h6">
              Category Breakdown
            </Typography>
          </Box>
          <Box sx={{
            display: 'grid',
            gridTemplateColumns: {
              xs: '1fr',
              sm: 'repeat(2, 1fr)',
              md: 'repeat(3, 1fr)',
              lg: 'repeat(4, 1fr)'
            },
            gap: 3
          }}>
            {categorySpending.map((category) => (
              <Box key={category.categoryName} sx={{
                p: 2,
                borderRadius: 2,
                bgcolor: 'rgba(255, 255, 255, 0.7)',
                border: '1px solid',
                borderColor: 'grey.200'
              }}>
                <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 1 }}>
                  <Typography variant="subtitle2" fontWeight={700} noWrap>
                    {category.categoryName}
                  </Typography>
                  <Chip
                    label={`${Math.round(category.percentage)}%`}
                    size="small"
                    sx={{
                      bgcolor: category.percentage > 100 ? 'error.light' : 'success.light',
                      color: category.percentage > 100 ? 'error.dark' : 'success.dark',
                      fontWeight: 700,
                      minWidth: 45
                    }}
                  />
                </Box>
                <LinearProgress
                  variant="determinate"
                  value={Math.min(100, category.percentage)}
                  sx={{
                    height: 8,
                    borderRadius: 4,
                    bgcolor: 'grey.200',
                    mb: 1,
                    '& .MuiLinearProgress-bar': {
                      bgcolor: category.percentage > 100 ? 'error.main' : 'success.main',
                    },
                  }}
                />
                <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                  <Box>
                    <Typography variant="body2" fontWeight={600} color="text.primary">
                      €{category.totalSpent.toLocaleString()}
                    </Typography>
                    <Typography variant="caption" color="text.secondary">
                      Spent
                    </Typography>
                  </Box>
                  <Box sx={{ textAlign: 'right' }}>
                    <Typography variant="body2" fontWeight={600} color="text.secondary">
                      €{(category.budgetedAmount || 0).toLocaleString()}
                    </Typography>
                    <Typography variant="caption" color="text.secondary">
                      Budget
                    </Typography>
                  </Box>
                </Box>
              </Box>
            ))}
          </Box>
        </Paper>


        {/* 50-30-20 Budget Section */}
        <Paper elevation={4} sx={{ p: 3, borderRadius: 4, boxShadow: '0 4px 24px #b2dfdb33', mb: 3, background: 'linear-gradient(135deg, #f5f6fa 0%, #ffffff 100%)' }}>
          <Typography variant="h6" fontWeight={700} color="#222" mb={2}>
            50-30-20 Budget Breakdown
          </Typography>
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
        </Paper>
      </Box>
    </Layout>
  );
}
