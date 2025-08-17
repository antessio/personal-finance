'use client';

import { Box, Paper, Typography, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Divider, LinearProgress, Avatar, Stack, Chip, CircularProgress } from '@mui/material';
import { useAuth } from '../contexts/AuthContext';
import Layout from '../components/Layout';
import { TrendingUp, TrendingDown, Savings, BarChart as MuiBarChart, PieChart, ArrowUpward, ArrowDownward } from '@mui/icons-material';
import { BarChart, Bar, XAxis, YAxis, Tooltip, Legend, ResponsiveContainer, CartesianGrid, Pie, Cell } from 'recharts';
import { useQuery } from '@tanstack/react-query';
import { service } from '../services/api';
import { MonthlyData } from '../types';

export default function HomePage() {
  const { user } = useAuth();
  const currentYear = new Date().getFullYear();

  // Fetch data
  const { data: transactionsResponse } = useQuery({
    queryKey: ['transactions'],
    queryFn: () => service.getTransactions({}),
  });

  const { data: totalIncome = 0 } = useQuery({
    queryKey: ['totalIncome'],
    queryFn: () => service.getTotalIncome(currentYear),
  });


  const { data: totalExpenses = 0 } = useQuery({
    queryKey: ['totalExpenses'],
    queryFn: () => service.getTotalExpenses(currentYear),
  });

  const { data: totalSavings = 0 } = useQuery({
    queryKey: ['totalSavings'],
    queryFn: () => service.getTotalSavings(currentYear),
  });

  const { data: categories = [] } = useQuery({
    queryKey: ['categories'],
    queryFn: () => service.getAllCategories(),
  });

  const { data: budgets = [] } = useQuery({
    queryKey: ['budgets', currentYear],
    queryFn: () => service.getBudgets(currentYear.toString()),
  });
  const incomeBudget = 0;
  const expenseBudget = 0;
  const savingsBudget = 0;
  // const { data: incomeBudget = 0 } = useQuery({
  //   queryKey: ['incomeBudget', currentYear],
  //   queryFn: () => service.getIncomeBudget(currentYear),
  // });

  // const { data: expenseBudget = 0 } = useQuery({
  //   queryKey: ['expenseBudget', currentYear],
  //   queryFn: () => service.getExpenseBudget(currentYear),
  // });
  // const { data: savingsBudget = 0 } = useQuery({
  //   queryKey: ['savingsBudget', currentYear],
  //   queryFn: () => service.getSavingsBudget(currentYear),
  // });

  const transactions = transactionsResponse?.data || [];


  // Calculate budget data
  const totalBudget = incomeBudget + expenseBudget + savingsBudget;
  const { data: categorySpending = [] } = useQuery({
    queryKey: ['categorySpending', currentYear],
    queryFn: () => service.getCategorySpending(currentYear.toString()),
  });

  // Money Flow data
  const months = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];
  const { data: monthlyData = [] } = useQuery({
    queryKey: ['monthlyData', currentYear],
    queryFn: () => service.getMonthlyData(currentYear.toString()),
  });


  const monthTransactions = months.map((month, index) => {
    const monthData = monthlyData.find((tx: MonthlyData) => Number(tx.month) === index);
    return {
      month,
      Income: monthData ? monthData.totalIncome : 0,
      Expense: monthData ? monthData.totalExpenses : 0,
    };
  });

  // --- 50-30-20 Budget Fake Data ---
  type BudgetKey = 'needs' | 'wants' | 'savingsDebts';
  type BudgetKeyWithTotal = BudgetKey | 'total';
  const budget502010 = {
    goal: {
      needs: 50,
      wants: 30,
      savingsDebts: 20,
    },
    actual: {
      needs: 22.08,
      wants: 75.91,
      savingsDebts: 24.23,
    },
    amount: {
      needs: { budget: 1488, actual: 656.99 },
      wants: { budget: 892.8, actual: 2259.06 },
      savingsDebts: { budget: 595.2, actual: 721.07 },
      total: { budget: 2976, actual: 3637.12 },
    },
  };

  return (
    <Layout>
      <Box sx={{ bgcolor: '#f5f6fa', minHeight: '100vh', p: { xs: 1, md: 4 } }}>
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
              {Math.round((totalIncome / incomeBudget == 0 ? 1 : incomeBudget) * 100)}% of income target reached
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
                €{totalBudget.toLocaleString()}
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
              {Math.round((totalExpenses / totalBudget == 0 ? 1 : totalBudget) * 100)}% of budget spent
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
              {Math.round((totalSavings / savingsBudget == 0 ? 1 : savingsBudget) * 100)}% of savings target reached
            </Typography>
          </Paper>
        </Box>

        {/* Middle Row: Money Flow Chart */}
        <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 3, mb: 3 }}>
          {/* Money Flow Card */}
          <Paper elevation={4} sx={{ flex: 1, minWidth: 400, p: 3, borderRadius: 4, boxShadow: '0 4px 24px #b2dfdb33', display: 'flex', flexDirection: 'column', justifyContent: 'center', background: 'linear-gradient(135deg, #e8f5e9 0%, #ffffff 100%)' }}>
            <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
              <MuiBarChart color="success" sx={{ mr: 1 }} />
              <Typography color="success.dark" fontWeight={700} variant="subtitle1">
                Money Flow
              </Typography>
            </Box>
            <Box sx={{ width: '100%', height: 220, mb: 2 }}>
              <ResponsiveContainer width="100%" height="100%">
                <BarChart data={monthTransactions} margin={{ top: 10, right: 20, left: 0, bottom: 10 }}>
                  <CartesianGrid strokeDasharray="3 3" />
                  <XAxis dataKey="month" tick={{ fontSize: 12 }} />
                  <YAxis tick={{ fontSize: 12 }} />
                  <Tooltip formatter={(value) => `€${value.toLocaleString()}`} />
                  <Legend verticalAlign="top" height={36} />
                  <Bar dataKey="Income" fill="#43a047" radius={[6, 6, 0, 0]} barSize={18} name="Income" />
                  <Bar dataKey="Expense" fill="#e53935" radius={[6, 6, 0, 0]} barSize={18} name="Expense" />
                </BarChart>
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

        {/* Remaining Monthly Card */}
        <Paper elevation={4} sx={{ p: 3, borderRadius: 4, boxShadow: '0 4px 24px #b2dfdb33', mb: 3, background: 'linear-gradient(135deg, #e8f5e9 0%, #ffffff 100%)' }}>
          <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
            <PieChart color="success" sx={{ mr: 1 }} />
            <Typography color="success.dark" fontWeight={700} variant="subtitle1">
              Remaining Monthly
            </Typography>
          </Box>
          <Box sx={{ position: 'relative', width: 120, height: 120, mb: 2 }}>
            <ResponsiveContainer width="100%" height="100%">
              <PieChart>
                <Pie
                  data={[{ name: 'Remaining', value: 69 }, { name: 'Used', value: 31 }]}
                  dataKey="value"
                  cx="50%"
                  cy="50%"
                  innerRadius={40}
                  outerRadius={55}
                  startAngle={90}
                  endAngle={-270}
                  paddingAngle={0}
                >
                  <Cell key="remaining" fill="#43a047" />
                  <Cell key="used" fill="#e0e0e0" />
                </Pie>
              </PieChart>
            </ResponsiveContainer>
            <Box sx={{
              position: 'absolute',
              top: 0,
              left: 0,
              width: '100%',
              height: '100%',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              flexDirection: 'column',
              pointerEvents: 'none',
            }}>
              <Typography variant="h3" fontWeight={800} color="success.main" sx={{ lineHeight: 1 }}>
                69%
              </Typography>
              <Chip icon={<ArrowUpward sx={{ color: 'success.main' }} />} label={'+2.4%'} size="small" sx={{ bgcolor: 'success.light', color: 'success.dark', mt: 1, fontWeight: 700 }} />
            </Box>
          </Box>
          <Typography variant="subtitle2" color="success.dark" align="center" mb={1} fontWeight={600}>
            You're in great shape
          </Typography>
          <Typography variant="body2" color="text.secondary" align="center" mb={2}>
            Your monthly usage is still very safe
          </Typography>
          <Box sx={{ width: '100%', display: 'flex', gap: 1, justifyContent: 'center', flexWrap: 'wrap' }}>
            <Chip icon={<TrendingUp sx={{ color: 'success.main' }} />} label={<Box component="span" sx={{ fontWeight: 700 }}>89% Needs</Box>} sx={{ bgcolor: 'success.light', color: 'success.dark', fontWeight: 700, borderRadius: 2, px: 1.5, py: 0.5, fontSize: 16 }} />
            <Chip icon={<TrendingUp sx={{ color: 'warning.main' }} />} label={<Box component="span" sx={{ fontWeight: 700 }}>78% Food</Box>} sx={{ bgcolor: 'warning.light', color: 'warning.dark', fontWeight: 700, borderRadius: 2, px: 1.5, py: 0.5, fontSize: 16 }} />
            <Chip icon={<TrendingUp sx={{ color: 'info.main' }} />} label={<Box component="span" sx={{ fontWeight: 700 }}>42% Education</Box>} sx={{ bgcolor: 'info.light', color: 'info.dark', fontWeight: 700, borderRadius: 2, px: 1.5, py: 0.5, fontSize: 16 }} />
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

            {/* Pie Charts */}
            <Box sx={{ flex: 1, minWidth: 220, display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 2 }}>
              <Typography variant="subtitle2" fontWeight={600}>Goal Breakdown</Typography>
              <ResponsiveContainer width={160} height={160}>
                <PieChart>
                  <Pie
                    data={[
                      { name: 'Needs', value: budget502010.goal.needs },
                      { name: 'Wants', value: budget502010.goal.wants },
                      { name: 'Savings+Debts', value: budget502010.goal.savingsDebts },
                    ]}
                    dataKey="value"
                    cx="50%"
                    cy="50%"
                    outerRadius={60}
                    label
                  >
                    <Cell fill="#43a047" />
                    <Cell fill="#ffa726" />
                    <Cell fill="#29b6f6" />
                  </Pie>
                </PieChart>
              </ResponsiveContainer>
              <Typography variant="subtitle2" fontWeight={600}>Actual Breakdown</Typography>
              <ResponsiveContainer width={160} height={160}>
                <PieChart>
                  <Pie
                    data={[
                      { name: 'Needs', value: budget502010.actual.needs },
                      { name: 'Wants', value: budget502010.actual.wants },
                      { name: 'Savings+Debts', value: budget502010.actual.savingsDebts },
                    ]}
                    dataKey="value"
                    cx="50%"
                    cy="50%"
                    outerRadius={60}
                    label
                  >
                    <Cell fill="#43a047" />
                    <Cell fill="#ffa726" />
                    <Cell fill="#29b6f6" />
                  </Pie>
                </PieChart>
              </ResponsiveContainer>
            </Box>
          </Box>
        </Paper>

        {/* Recent Transactions */}
        <Paper elevation={3} sx={{ p: 3, borderRadius: 4, boxShadow: '0 2px 12px #b2dfdb33', mt: 3 }}>
          <Typography variant="h6" fontWeight={700} color="#222" mb={2}>
            Recent Transactions
          </Typography>
          <TableContainer>
            <Table size="small">
              <TableHead>
                <TableRow>
                  <TableCell>Date</TableCell>
                  <TableCell>Description</TableCell>
                  <TableCell>Amount</TableCell>
                  <TableCell>Account</TableCell>
                  <TableCell>Category</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {transactions.slice(0, 10).map((tx) => {
                  const category = categories.find(cat => cat.id === tx.categoryId);
                  return (
                    <TableRow key={tx.id} hover>
                      <TableCell>{tx.date}</TableCell>
                      <TableCell>{tx.description}</TableCell>
                      <TableCell>
                        <Typography color={tx.amount < 0 ? 'error.main' : 'success.main'} fontWeight={700}>
                          {tx.amount < 0 ? '-' : '+'}€{Math.abs(tx.amount).toLocaleString()}
                        </Typography>
                      </TableCell>
                      <TableCell>{tx.account}</TableCell>
                      <TableCell>{category ? category.name : tx.categoryId}</TableCell>
                    </TableRow>
                  );
                })}
              </TableBody>
            </Table>
          </TableContainer>
        </Paper>
      </Box>
    </Layout>
  );
}
