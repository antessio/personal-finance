'use client';

import { useState } from 'react';
import {
  Box,
  Paper,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  TablePagination,
  Checkbox,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  TextField,
  Button,
  Typography,
  Chip,
  Divider,
} from '@mui/material';
import { DatePicker } from '@mui/x-date-pickers/DatePicker';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { service } from '../../services/api';
import { Transaction, TransactionFilters, Category } from '../../types';
import Layout from '../../components/Layout';
import { mockCategories } from '../../services/mockData';
import WarningIcon from '@mui/icons-material/Warning';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';

export default function TransactionsPage() {
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(10);
  const [selected, setSelected] = useState<string[]>([]);
  const [filters, setFilters] = useState<TransactionFilters>({});
  const queryClient = useQueryClient();

  const { data: transactions = [], isLoading: isLoadingTransactions } = useQuery<Transaction[]>({
    queryKey: ['transactions', filters],
    queryFn: () => service.getTransactions(filters),
  });

  const { data: categories = [], isLoading: isLoadingCategories } = useQuery<Category[]>({
    queryKey: ['categories'],
    queryFn: () => service.getCategories(),
  });

  // Get unique accounts from transactions
  const accounts = Array.from(new Set(transactions.map(t => t.account))).sort();

  // Add special categories
  const allCategories = [
    { id: 'uncategorized', name: 'Uncategorized', macroCategory: 'Special' },
    { id: 'categorized', name: 'Categorized', macroCategory: 'Special' },
    ...categories
  ];

  const bulkUpdateMutation = useMutation({
    mutationFn: service.bulkUpdateTransactions,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['transactions'] });
      setSelected([]);
    },
  });

  const categorizeMutation = useMutation({
    mutationFn: service.categorizeTransactions,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['transactions'] });
      setSelected([]);
    },
  });

  const handleSelectAllClick = (event: React.ChangeEvent<HTMLInputElement>) => {
    if (event.target.checked) {
      setSelected(transactions.map((t: Transaction) => t.id));
    } else {
      setSelected([]);
    }
  };

  const handleClick = (id: string) => {
    const selectedIndex = selected.indexOf(id);
    let newSelected: string[] = [];

    if (selectedIndex === -1) {
      newSelected = newSelected.concat(selected, id);
    } else if (selectedIndex === 0) {
      newSelected = newSelected.concat(selected.slice(1));
    } else if (selectedIndex === selected.length - 1) {
      newSelected = newSelected.concat(selected.slice(0, -1));
    } else if (selectedIndex > 0) {
      newSelected = newSelected.concat(
        selected.slice(0, selectedIndex),
        selected.slice(selectedIndex + 1)
      );
    }

    setSelected(newSelected);
  };

  const handleChangePage = (event: unknown, newPage: number) => {
    setPage(newPage);
  };

  const handleChangeRowsPerPage = (event: React.ChangeEvent<HTMLInputElement>) => {
    setRowsPerPage(parseInt(event.target.value, 10));
    setPage(0);
  };

  const handleBulkUpdate = (updates: Partial<Transaction>) => {
    bulkUpdateMutation.mutate({ transactionIds: selected, updates });
  };

  const handleCategorize = () => {
    categorizeMutation.mutate(selected);
  };

  const isSelected = (id: string) => selected.indexOf(id) !== -1;

  return (
    <Layout>
      <Box sx={{ mb: 3 }}>
        <Typography variant="h4" gutterBottom>
          Transactions
        </Typography>
        <Box sx={{ display: 'flex', gap: 2, mb: 2 }}>
          <DatePicker
            label="Month"
            views={['year', 'month']}
            value={filters.month ? new Date(filters.month) : null}
            onChange={(date: Date | null) =>
              setFilters({ ...filters, month: date?.toISOString().split('T')[0] })
            }
          />
          <FormControl sx={{ minWidth: 120 }}>
            <InputLabel>Included</InputLabel>
            <Select
              value={filters.included ?? ''}
              label="Included"
              onChange={(e) =>
                setFilters({
                  ...filters,
                  included: e.target.value === '' ? undefined : e.target.value === 'true',
                })
              }
            >
              <MenuItem value="">All</MenuItem>
              <MenuItem value="true">Included</MenuItem>
              <MenuItem value="false">Excluded</MenuItem>
            </Select>
          </FormControl>
          <FormControl sx={{ minWidth: 120 }}>
            <InputLabel>Account</InputLabel>
            <Select
              value={filters.account ?? ''}
              label="Account"
              onChange={(e) =>
                setFilters({ ...filters, account: e.target.value || undefined })
              }
            >
              <MenuItem value="">All</MenuItem>
              {accounts.map((account) => (
                <MenuItem key={account} value={account}>
                  {account}
                </MenuItem>
              ))}
            </Select>
          </FormControl>
          <FormControl sx={{ minWidth: 120 }}>
            <InputLabel>Category</InputLabel>
            <Select
              value={filters.categoryId ?? ''}
              label="Category"
              onChange={(e) =>
                setFilters({ ...filters, categoryId: e.target.value || undefined })
              }
            >
              <MenuItem value="">All</MenuItem>
              <Divider />
              <MenuItem value="uncategorized" sx={{ color: 'warning.main', fontWeight: 600 }}>
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                  <WarningIcon fontSize="small" />
                  Uncategorized
                </Box>
              </MenuItem>
              <MenuItem value="categorized" sx={{ color: 'success.main', fontWeight: 600 }}>
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                  <CheckCircleIcon fontSize="small" />
                  Categorized
                </Box>
              </MenuItem>
              <Divider />
              {categories.map((category) => (
                <MenuItem key={category.id} value={category.id}>
                  {category.name}
                </MenuItem>
              ))}
            </Select>
          </FormControl>
        </Box>
        {selected.length > 0 && (
          <Box sx={{ mb: 2 }}>
            <Button
              variant="contained"
              color="primary"
              onClick={() => handleBulkUpdate({ included: true })}
              sx={{ mr: 1 }}
            >
              Include Selected
            </Button>
            <Button
              variant="contained"
              color="secondary"
              onClick={() => handleBulkUpdate({ included: false })}
              sx={{ mr: 1 }}
            >
              Exclude Selected
            </Button>
            <Button
              variant="contained"
              color="success"
              onClick={handleCategorize}
            >
              Categorize Selected
            </Button>
          </Box>
        )}
      </Box>
      <Paper elevation={4} sx={{ borderRadius: 4, boxShadow: '0 4px 24px #b2dfdb33', p: 2 }}>
        <TableContainer>
          <Table>
            <TableHead>
              <TableRow sx={{ bgcolor: 'grey.100' }}>
                <TableCell sx={{ fontWeight: 700, fontSize: 15 }}>Date</TableCell>
                <TableCell sx={{ fontWeight: 700, fontSize: 15 }}>Description</TableCell>
                <TableCell sx={{ fontWeight: 700, fontSize: 15 }}>Amount</TableCell>
                <TableCell sx={{ fontWeight: 700, fontSize: 15 }}>Account</TableCell>
                <TableCell sx={{ fontWeight: 700, fontSize: 15 }}>Category</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {transactions.slice(page * rowsPerPage, page * rowsPerPage + rowsPerPage).map((transaction, idx) => {
                const category = mockCategories.find((cat: Category) => cat.id === transaction.categoryId);
                return (
                  <TableRow
                    key={transaction.id}
                    hover
                    sx={{
                      transition: 'background 0.2s',
                      bgcolor: idx % 2 === 0 ? 'white' : 'grey.50',
                      borderBottom: '1px solid #f0f0f0',
                    }}
                  >
                    <TableCell sx={{ color: 'grey.600', fontSize: 13, fontWeight: 500 }}>{transaction.date}</TableCell>
                    <TableCell sx={{ fontWeight: 700, fontSize: 15 }}>{transaction.description}</TableCell>
                    <TableCell sx={{ p: 0 }}>
                      <Box sx={{
                        bgcolor: transaction.amount < 0 ? 'error.lighter' : 'success.lighter',
                        color: transaction.amount < 0 ? 'error.main' : 'success.main',
                        fontWeight: 700,
                        borderRadius: 2,
                        px: 2,
                        py: 0.5,
                        display: 'inline-block',
                        fontSize: 15,
                        minWidth: 80,
                        textAlign: 'center',
                      }}>
                        {transaction.amount < 0 ? '-' : '+'}€{Math.abs(transaction.amount).toLocaleString()}
                      </Box>
                    </TableCell>
                    <TableCell>
                      <Chip 
                        label={transaction.account}
                        size="small"
                        sx={{
                          fontWeight: 700,
                          bgcolor: '#e8f5e9',
                          color: '#388e3c',
                          border: '1px solid #43a047',
                          letterSpacing: 0.2,
                        }}
                      />
                    </TableCell>
                    <TableCell>
                      {category ? (
                        <Box>
                          <Chip label={category.name} color="primary" size="small" sx={{ fontWeight: 700, mb: 0.5 }} />
                          <Chip label={category.macroCategory} variant="outlined" color="primary" size="small" sx={{ fontWeight: 500, ml: 0.5 }} />
                        </Box>
                      ) : (
                        <Chip label={transaction.categoryId} size="small" />
                      )}
                    </TableCell>
                  </TableRow>
                );
              })}
            </TableBody>
          </Table>
        </TableContainer>
        <TablePagination
          rowsPerPageOptions={[5, 10, 25]}
          component="div"
          count={transactions.length}
          rowsPerPage={rowsPerPage}
          page={page}
          onPageChange={handleChangePage}
          onRowsPerPageChange={handleChangeRowsPerPage}
        />
      </Paper>
    </Layout>
  );
} 