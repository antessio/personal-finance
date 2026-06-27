'use client';

import { useState, useEffect } from 'react';
import {
  Box,
  Paper,
  Typography,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Chip,
  LinearProgress,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Checkbox,
  Button,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  CircularProgress,
} from '@mui/material';
import { TrendingUp } from '@mui/icons-material';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { service } from '../../services/api';
import { Transaction, BulkUpdatePayload, Category } from '../../types';
import Layout from '../../components/Layout';

export default function InvestmentsPage() {
  const currentYear = new Date().getFullYear();
  const currentMonth = new Date().getMonth() + 1;
  const previousMonth = currentMonth === 1 ? 12 : currentMonth - 1;
  const previousMonthYear = currentMonth === 1 ? currentYear - 1 : currentYear;

  const [selectedYear, setSelectedYear] = useState<number>(previousMonthYear);
  const [selectedMonth, setSelectedMonth] = useState<number | undefined>(previousMonth);

  const [selected, setSelected] = useState<string[]>([]);
  const [allTransactions, setAllTransactions] = useState<Transaction[]>([]);
  const [openBulkUpdateModal, setOpenBulkUpdateModal] = useState(false);
  const [selectedCategoryId, setSelectedCategoryId] = useState<string>('');
  const [transactionCursor, setTransactionCursor] = useState<string | undefined>(undefined);
  const [isLoadingMore, setIsLoadingMore] = useState(false);

  const queryClient = useQueryClient();

  const yearOptions: number[] = [];
  for (let y = currentYear; y >= currentYear - 4; y--) {
    yearOptions.push(y);
  }

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

  const { data: totalInvestments = 0 } = useQuery({
    queryKey: ['totalInvestments', selectedYear, selectedMonth],
    queryFn: () => service.getTotalInvestments(selectedYear, selectedMonth),
  });

  const { data: investmentsBudget = 0 } = useQuery({
    queryKey: ['investmentsBudget', selectedYear, selectedMonth],
    queryFn: () => service.getInvestmentsBudget(selectedYear, selectedMonth),
  });

  const { data: categoryInvestments = [] } = useQuery({
    queryKey: ['categoryInvestments', selectedYear, selectedMonth],
    queryFn: () => service.getCategoryInvestments(selectedYear, selectedMonth),
  });

  const transactionFilters = {
    macroCategory: 'INVESTMENTS',
    limit: 20,
    month: selectedMonth !== undefined
      ? `${selectedYear}-${selectedMonth.toString().padStart(2, '0')}-01`
      : `${selectedYear}-01-01`,
    cursor: transactionCursor,
  };

  const { data: paginatedTransactions } = useQuery({
    queryKey: ['transactions', transactionFilters],
    queryFn: () => service.getTransactions(transactionFilters),
  });

  useEffect(() => {
    if (paginatedTransactions) {
      if (transactionCursor) {
        setAllTransactions(prev => [...prev, ...paginatedTransactions.data]);
      } else {
        setAllTransactions(paginatedTransactions.data);
      }
    }
  }, [paginatedTransactions]);

  const { data: categories = [] } = useQuery<Category[]>({
    queryKey: ['categories'],
    queryFn: () => service.getAllCategories(),
  });

  const bulkUpdateMutation = useMutation({
    mutationFn: (payload: BulkUpdatePayload) => service.bulkUpdateTransactions(payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['transactions'] });
      queryClient.invalidateQueries({ queryKey: ['totalInvestments'] });
      queryClient.invalidateQueries({ queryKey: ['categoryInvestments'] });
      setSelected([]);
      setOpenBulkUpdateModal(false);
    },
  });

  const handlePeriodChange = (year: number, month: number | undefined) => {
    setSelectedYear(year);
    setSelectedMonth(month);
    setTransactionCursor(undefined);
    setAllTransactions([]);
    setSelected([]);
  };

  const handleLoadMore = () => {
    if (paginatedTransactions?.nextCursor && !isLoadingMore) {
      setIsLoadingMore(true);
      setTransactionCursor(paginatedTransactions.nextCursor);
      setIsLoadingMore(false);
    }
  };

  const handleSelectAll = (event: React.ChangeEvent<HTMLInputElement>) => {
    setSelected(event.target.checked ? allTransactions.map(t => t.id) : []);
  };

  const handleToggle = (id: string) => {
    setSelected(prev =>
      prev.includes(id) ? prev.filter(x => x !== id) : [...prev, id]
    );
  };

  const handleBulkUpdate = (updates: Partial<Transaction>) => {
    bulkUpdateMutation.mutate({ transactionIds: selected, updates });
  };

  const budgetProgress = investmentsBudget > 0 ? Math.min(100, (totalInvestments / investmentsBudget) * 100) : 0;
  const activeCategories = categoryInvestments.filter(c => Math.abs(c.totalSpent) > 0);

  return (
    <Layout>
      {/* Header + period selectors */}
      <Box sx={{ mb: 3, display: 'flex', alignItems: 'center', gap: 2, flexWrap: 'wrap' }}>
        <TrendingUp sx={{ color: '#00897b', fontSize: 32 }} />
        <Typography variant="h4" fontWeight={700} sx={{ color: '#00897b', flexGrow: 1 }}>
          Investments
        </Typography>
        <FormControl sx={{ minWidth: 110 }}>
          <InputLabel>Year</InputLabel>
          <Select
            value={selectedYear}
            label="Year"
            onChange={e => handlePeriodChange(Number(e.target.value), selectedMonth)}
          >
            {yearOptions.map(y => (
              <MenuItem key={y} value={y}>{y}</MenuItem>
            ))}
          </Select>
        </FormControl>
        <FormControl sx={{ minWidth: 130 }}>
          <InputLabel>Month</InputLabel>
          <Select
            value={selectedMonth ?? ''}
            label="Month"
            onChange={e => handlePeriodChange(selectedYear, e.target.value === '' ? undefined : Number(e.target.value))}
          >
            {monthOptions.map(opt => (
              <MenuItem key={opt.label} value={opt.value ?? ''}>{opt.label}</MenuItem>
            ))}
          </Select>
        </FormControl>
      </Box>

      {/* Summary card */}
      <Paper elevation={4} sx={{ p: 3, mb: 3, borderRadius: 4, background: 'linear-gradient(135deg, #e0f2f1 0%, #ffffff 100%)', boxShadow: '0 4px 24px rgba(0,137,123,0.15)' }}>
        <Box sx={{ display: 'flex', gap: 4, flexWrap: 'wrap', mb: 2 }}>
          <Box>
            <Typography variant="body2" color="text.secondary">Invested</Typography>
            <Typography variant="h4" fontWeight={700} sx={{ color: '#00897b' }}>
              €{Math.abs(totalInvestments).toLocaleString()}
            </Typography>
          </Box>
          {investmentsBudget > 0 && (
            <Box>
              <Typography variant="body2" color="text.secondary">Budget</Typography>
              <Typography variant="h4" fontWeight={700} sx={{ color: '#00695c' }}>
                €{investmentsBudget.toLocaleString()}
              </Typography>
            </Box>
          )}
        </Box>
        {investmentsBudget > 0 && (
          <>
            <LinearProgress
              variant="determinate"
              value={budgetProgress}
              sx={{ height: 8, borderRadius: 5, bgcolor: '#b2dfdb', '& .MuiLinearProgress-bar': { bgcolor: '#00897b' } }}
            />
            <Typography variant="caption" color="text.secondary" mt={0.5} display="block">
              {Math.round(budgetProgress)}% of investment target reached
            </Typography>
          </>
        )}
      </Paper>

      {/* Category breakdown */}
      {activeCategories.length > 0 && (
        <Paper elevation={4} sx={{ p: 3, mb: 3, borderRadius: 4, background: 'linear-gradient(135deg, #e0f2f1 0%, #ffffff 100%)', boxShadow: '0 4px 24px rgba(0,137,123,0.12)' }}>
          <Typography variant="h6" fontWeight={700} mb={2} sx={{ color: '#00695c' }}>
            By Category
          </Typography>
          <Box sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', sm: 'repeat(2, 1fr)', md: 'repeat(3, 1fr)' }, gap: 2 }}>
            {activeCategories.sort((a, b) => Math.abs(b.totalSpent) - Math.abs(a.totalSpent)).map(cat => {
              const amount = Math.abs(cat.totalSpent);
              const budget = cat.budgetedAmount || 0;
              const pct = budget > 0 ? Math.min(100, (amount / budget) * 100) : 0;
              const overBudget = budget > 0 && amount > budget;
              return (
                <Box key={cat.categoryName} sx={{ p: 2, borderRadius: 2, border: '1px solid #b2dfdb', bgcolor: 'white' }}>
                  <Typography fontWeight={700} sx={{ color: '#00695c', mb: 0.5 }}>{cat.categoryName}</Typography>
                  <Typography variant="h6" fontWeight={700} sx={{ color: '#00897b' }}>
                    €{amount.toLocaleString()}
                  </Typography>
                  {budget > 0 && (
                    <>
                      <Box sx={{ display: 'flex', justifyContent: 'space-between', mt: 1 }}>
                        <Typography variant="caption" color="text.secondary">Budget: €{budget.toLocaleString()}</Typography>
                        <Chip
                          label={`${Math.round(pct)}%`}
                          size="small"
                          sx={{ bgcolor: overBudget ? 'error.lighter' : 'success.lighter', color: overBudget ? 'error.main' : 'success.main', fontWeight: 700 }}
                        />
                      </Box>
                      <LinearProgress
                        variant="determinate"
                        value={pct}
                        sx={{ mt: 0.5, height: 6, borderRadius: 3, bgcolor: '#b2dfdb', '& .MuiLinearProgress-bar': { bgcolor: overBudget ? '#e53935' : '#00897b' } }}
                      />
                    </>
                  )}
                </Box>
              );
            })}
          </Box>
        </Paper>
      )}

      {/* Transaction list */}
      <Paper elevation={4} sx={{ borderRadius: 4, p: 2, background: 'linear-gradient(135deg, #e0f2f1 0%, #ffffff 100%)', boxShadow: '0 4px 24px rgba(0,137,123,0.10)' }}>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
          <Typography variant="subtitle1" fontWeight={700}>
            {allTransactions.length} transactions
          </Typography>
          {selected.length > 0 && (
            <Box sx={{ display: 'flex', gap: 1 }}>
              <Button variant="contained" color="primary" size="small" onClick={() => handleBulkUpdate({ included: true })}>
                Include
              </Button>
              <Button variant="contained" color="secondary" size="small" onClick={() => handleBulkUpdate({ included: false })}>
                Exclude
              </Button>
              <Button variant="contained" color="info" size="small" onClick={() => setOpenBulkUpdateModal(true)}>
                Recategorize
              </Button>
            </Box>
          )}
        </Box>

        <TableContainer>
          <Table size="small">
            <TableHead>
              <TableRow sx={{ bgcolor: 'rgba(0,0,0,0.04)' }}>
                <TableCell padding="checkbox">
                  <Checkbox
                    indeterminate={selected.length > 0 && selected.length < allTransactions.length}
                    checked={allTransactions.length > 0 && selected.length === allTransactions.length}
                    onChange={handleSelectAll}
                  />
                </TableCell>
                <TableCell sx={{ fontWeight: 700 }}>Date</TableCell>
                <TableCell sx={{ fontWeight: 700 }}>Description</TableCell>
                <TableCell sx={{ fontWeight: 700 }}>Amount</TableCell>
                <TableCell sx={{ fontWeight: 700 }}>Account</TableCell>
                <TableCell sx={{ fontWeight: 700 }}>Category</TableCell>
                <TableCell sx={{ fontWeight: 700 }}>Status</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {allTransactions.map((tx, idx) => {
                const isItemSelected = selected.includes(tx.id);
                return (
                  <TableRow
                    key={tx.id}
                    hover
                    onClick={() => handleToggle(tx.id)}
                    selected={isItemSelected}
                    sx={{ cursor: 'pointer', bgcolor: idx % 2 === 0 ? 'white' : 'grey.50' }}
                  >
                    <TableCell padding="checkbox">
                      <Checkbox checked={isItemSelected} onClick={e => e.stopPropagation()} onChange={() => handleToggle(tx.id)} />
                    </TableCell>
                    <TableCell sx={{ color: 'grey.600', fontSize: 13 }}>{tx.date}</TableCell>
                    <TableCell sx={{ fontWeight: 700 }}>{tx.description}</TableCell>
                    <TableCell sx={{ p: 0 }}>
                      <Box sx={{
                        bgcolor: tx.amount < 0 ? 'error.lighter' : 'success.lighter',
                        color: tx.amount < 0 ? 'error.main' : 'success.main',
                        fontWeight: 700, borderRadius: 2, px: 2, py: 0.5,
                        display: 'inline-block', fontSize: 14, minWidth: 70, textAlign: 'center',
                      }}>
                        {tx.amount < 0 ? '-' : '+'}€{Math.abs(tx.amount).toLocaleString()}
                      </Box>
                    </TableCell>
                    <TableCell>
                      <Chip label={tx.account} size="small" sx={{ bgcolor: '#e8f5e9', color: '#388e3c', border: '1px solid #43a047', fontWeight: 700 }} />
                    </TableCell>
                    <TableCell>
                      {tx.category ? (
                        <Chip label={tx.category.name} color="primary" size="small" sx={{ fontWeight: 700 }} />
                      ) : (
                        <Chip label="Uncategorized" size="small" color="warning" />
                      )}
                    </TableCell>
                    <TableCell>
                      <Chip
                        label={tx.included ? 'Included' : 'Excluded'}
                        size="small"
                        sx={{
                          fontWeight: 700,
                          bgcolor: tx.included ? 'success.lighter' : 'error.lighter',
                          color: tx.included ? 'success.main' : 'error.main',
                          border: `1px solid ${tx.included ? '#43a047' : '#d32f2f'}`,
                        }}
                      />
                    </TableCell>
                  </TableRow>
                );
              })}
            </TableBody>
          </Table>
        </TableContainer>

        {paginatedTransactions?.hasMore && (
          <Box sx={{ display: 'flex', justifyContent: 'center', mt: 2 }}>
            <Button variant="outlined" onClick={handleLoadMore} disabled={isLoadingMore} startIcon={isLoadingMore ? <CircularProgress size={20} /> : null}>
              {isLoadingMore ? 'Loading...' : 'Load More'}
            </Button>
          </Box>
        )}
      </Paper>

      {/* Recategorize dialog */}
      <Dialog open={openBulkUpdateModal} onClose={() => setOpenBulkUpdateModal(false)} maxWidth="sm" fullWidth>
        <DialogTitle>Recategorize {selected.length} transaction{selected.length !== 1 ? 's' : ''}</DialogTitle>
        <DialogContent>
          <Box sx={{ pt: 2 }}>
            <FormControl fullWidth>
              <InputLabel>Category</InputLabel>
              <Select
                value={selectedCategoryId}
                label="Category"
                onChange={e => setSelectedCategoryId(e.target.value)}
              >
                <MenuItem value=""><em>None</em></MenuItem>
                {categories.map(c => (
                  <MenuItem key={c.id} value={c.id}>{c.name} ({c.macroCategory})</MenuItem>
                ))}
              </Select>
            </FormControl>
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setOpenBulkUpdateModal(false)}>Cancel</Button>
          <Button
            onClick={() => { if (selectedCategoryId) handleBulkUpdate({ categoryId: selectedCategoryId }); }}
            variant="contained"
            disabled={!selectedCategoryId}
          >
            Update
          </Button>
        </DialogActions>
      </Dialog>
    </Layout>
  );
}