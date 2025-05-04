'use client';

import { Box, Paper, Typography, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Divider, LinearProgress, Avatar, Stack, Chip, CircularProgress } from '@mui/material';
import { useAuth } from '../contexts/AuthContext';
import Layout from '../components/Layout';
import { TrendingUp, TrendingDown, Savings, BarChart as MuiBarChart, PieChart, ArrowUpward, ArrowDownward } from '@mui/icons-material';
import { BarChart, Bar, XAxis, YAxis, Tooltip, Legend, ResponsiveContainer, CartesianGrid, Pie, Cell } from 'recharts';
import { mockTransactions, mockCategories } from '../services/mockData';

export default function HomePage() {
  const { user } = useAuth();

  // Mock data for dashboard
  const summary = {
    budget: 40900,
    actualIncome: 23530,
    expenseBudget: 34700,
    actualExpense: 16971.69,
    savingsBudget: 3600,
    actualSavings: 5000,
    leftToSpendBudget: 3030,
    leftToSpendActual: 1558.31,
  };

  // Mock data for Money Flow (monthly income and expenses)
  const months = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];
  const monthlyIncome = [3200, 3200, 3200, 3200, 3700, 3813, 3200, 3200, 3200, 3210, 3200, 3200];
  const monthlyExpense = [2200, 2100, 2500, 2300, 2700, 2800, 2100, 2000, 2250, 2150, 2400, 2300];
  const moneyFlowData = months.map((month, i) => ({
    month,
    Income: monthlyIncome[i],
    Expense: monthlyExpense[i],
  }));

  const tableData = [
    { month: 'Sep-2024', budget: 3200, actual: 3248 },
    { month: 'Oct-2024', budget: 3210, actual: 2947 },
    { month: 'Nov-2024', budget: 3200, actual: 3292 },
    { month: 'Dec-2024', budget: 3200, actual: 3190 },
    { month: 'Jan-2025', budget: 3200, actual: 3060 },
    { month: 'Feb-2025', budget: 3200, actual: 2976 },
    { month: 'Mar-2025', budget: 3700, actual: 3088 },
    { month: 'Apr-2025', budget: 3700, actual: 0 },
    { month: 'May-2025', budget: 3813, actual: 0 },
    { month: 'Jun-2025', budget: 3200, actual: 0 },
    { month: 'Jul-2025', budget: 3200, actual: 0 },
    { month: 'Aug-2025', budget: 3200, actual: 0 },
  ];

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
                €{summary.budget.toLocaleString()}
              </Typography>
            </Box>
            <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
              <Chip label="Actual" size="small" sx={{ mr: 1, bgcolor: 'success.100', color: 'success.dark' }} />
              <Typography variant="h6" fontWeight={700} color="success.dark">
                €{summary.actualIncome.toLocaleString()}
              </Typography>
            </Box>
            <LinearProgress variant="determinate" value={Math.min(100, (summary.actualIncome / summary.budget) * 100)} sx={{ height: 8, borderRadius: 5, bgcolor: 'success.light' }} color="success" />
            <Typography variant="caption" color="text.secondary" mt={1}>
              {Math.round((summary.actualIncome / summary.budget) * 100)}% of budget reached
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
                €{summary.expenseBudget.toLocaleString()}
              </Typography>
            </Box>
            <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
              <Chip label="Actual" size="small" sx={{ mr: 1, bgcolor: 'error.100', color: 'error.dark' }} />
              <Typography variant="h6" fontWeight={700} color="error.dark">
                €{summary.actualExpense.toLocaleString()}
              </Typography>
            </Box>
            <LinearProgress variant="determinate" value={Math.min(100, (summary.actualExpense / summary.expenseBudget) * 100)} sx={{ height: 8, borderRadius: 5, bgcolor: 'error.light' }} color="error" />
            <Typography variant="caption" color="text.secondary" mt={1}>
              {Math.round((summary.actualExpense / summary.expenseBudget) * 100)}% of budget spent
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
                €{summary.savingsBudget.toLocaleString()}
              </Typography>
            </Box>
            <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
              <Chip label="Actual" size="small" sx={{ mr: 1, bgcolor: 'info.100', color: 'info.dark' }} />
              <Typography variant="h6" fontWeight={700} color="info.dark">
                €{summary.actualSavings.toLocaleString()}
              </Typography>
            </Box>
            <LinearProgress variant="determinate" value={Math.min(100, (summary.actualSavings / summary.savingsBudget) * 100)} sx={{ height: 8, borderRadius: 5, bgcolor: 'info.light' }} color="info" />
            <Typography variant="caption" color="text.secondary" mt={1}>
              {Math.round((summary.actualSavings / summary.savingsBudget) * 100)}% of budget saved
            </Typography>
          </Paper>
        </Box>

        {/* Middle Row: Money Flow Chart + Remaining Monthly */}
        <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 3, mb: 3 }}>
          {/* Money Flow Card */}
          <Paper elevation={4} sx={{ flex: 2, minWidth: 400, p: 3, borderRadius: 4, boxShadow: '0 4px 24px #b2dfdb33', display: 'flex', flexDirection: 'column', justifyContent: 'center', background: 'linear-gradient(135deg, #e8f5e9 0%, #ffffff 100%)' }}>
            <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
              <MuiBarChart color="success" sx={{ mr: 1 }} />
              <Typography color="success.dark" fontWeight={700} variant="subtitle1">
                Money Flow
              </Typography>
            </Box>
            {/* Real Bar Chart with Recharts */}
            <Box sx={{ width: '100%', height: 220, mb: 2 }}>
              <ResponsiveContainer width="100%" height="100%">
                <BarChart data={moneyFlowData} margin={{ top: 10, right: 20, left: 0, bottom: 10 }}>
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
          {/* Remaining Monthly Card */}
          <Paper elevation={4} sx={{ flex: 1, minWidth: 260, p: 3, borderRadius: 4, boxShadow: '0 4px 24px #b2dfdb33', display: 'flex', flexDirection: 'column', alignItems: 'center', background: 'linear-gradient(135deg, #e3f2fd 0%, #ffffff 100%)' }}>
            <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
              <PieChart color="info" sx={{ mr: 1 }} />
              <Typography color="info.dark" fontWeight={700} variant="subtitle1">
                Remaining Monthly
              </Typography>
            </Box>
            {/* Donut Chart with Centered Percentage */}
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
            {/* Category breakdowns as pill-shaped cards with icons */}
            <Box sx={{ width: '100%', display: 'flex', gap: 1, justifyContent: 'center', flexWrap: 'wrap' }}>
              <Chip icon={<TrendingUp sx={{ color: 'success.main' }} />} label={<Box component="span" sx={{ fontWeight: 700 }}>89% Needs</Box>} sx={{ bgcolor: 'success.light', color: 'success.dark', fontWeight: 700, borderRadius: 2, px: 1.5, py: 0.5, fontSize: 16 }} />
              <Chip icon={<TrendingUp sx={{ color: 'warning.main' }} />} label={<Box component="span" sx={{ fontWeight: 700 }}>78% Food</Box>} sx={{ bgcolor: 'warning.light', color: 'warning.dark', fontWeight: 700, borderRadius: 2, px: 1.5, py: 0.5, fontSize: 16 }} />
              <Chip icon={<TrendingUp sx={{ color: 'info.main' }} />} label={<Box component="span" sx={{ fontWeight: 700 }}>42% Education</Box>} sx={{ bgcolor: 'info.light', color: 'info.dark', fontWeight: 700, borderRadius: 2, px: 1.5, py: 0.5, fontSize: 16 }} />
            </Box>
          </Paper>
        </Box>

        {/* Transaction History Table */}
        <Paper elevation={3} sx={{ p: 3, borderRadius: 4, boxShadow: '0 2px 12px #b2dfdb33', mt: 3 }}>
          <Typography variant="h6" fontWeight={700} color="#222" mb={2}>
            Transaction History
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
                {mockTransactions.slice(0, 10).map((tx) => {
                  const category = mockCategories.find(cat => cat.id === tx.categoryId);
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
