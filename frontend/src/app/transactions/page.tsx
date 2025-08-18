'use client';

import { useState, useEffect } from 'react';
import {
  Box,
  Paper,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
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
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  CircularProgress,
  Snackbar,
  Alert,
} from '@mui/material';
import { DatePicker } from '@mui/x-date-pickers/DatePicker';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { service } from '../../services/api';
import { Transaction, TransactionFilters, Category, BulkUpdatePayload, PaginatedResponse } from '../../types';
import Layout from '../../components/Layout';
import WarningIcon from '@mui/icons-material/Warning';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';

export default function TransactionsPage() {
  const [selected, setSelected] = useState<string[]>([]);
  const [filters, setFilters] = useState<TransactionFilters>({
    limit: 20,
  });
  const [openBulkUpdateModal, setOpenBulkUpdateModal] = useState(false);
  const [selectedCategoryId, setSelectedCategoryId] = useState<string>('');
  const [allTransactions, setAllTransactions] = useState<Transaction[]>([]);
  const [isLoadingMore, setIsLoadingMore] = useState(false);
  const [snackbarOpen, setSnackbarOpen] = useState(false);
  const [copiedId, setCopiedId] = useState<string>('');
  const queryClient = useQueryClient();

  const { data: paginatedData, isLoading: isLoadingTransactions } = useQuery({
    queryKey: ['transactions', filters],
    queryFn: () => service.getTransactions(filters),
  });

  const { data: accounts = [] } = useQuery({
    queryKey: ['accounts'],
    queryFn: () => service.getAccounts(),
  });
  const { data: categories = [], isLoading: isLoadingCategories } = useQuery<Category[]>({
    queryKey: ['categories'],
    queryFn: () => service.getAllCategories(),
  });

  // Update allTransactions when new data is fetched
  useEffect(() => {
    if (paginatedData) {
      if (filters.cursor) {
        // Append new transactions when loading more
        setAllTransactions(prev => [...prev, ...paginatedData.data]);
      } else {
        // Replace transactions when filters change
        setAllTransactions(paginatedData.data);
      }
    }
  }, [paginatedData]);


  // Add special categories
  const allCategories = [
    { id: 'uncategorized', name: 'Uncategorized', macroCategory: 'Special' },
    { id: 'categorized', name: 'Categorized', macroCategory: 'Special' },
    ...categories
  ];



  // Calculate the sum of displayed transactions
  const totalAmount = allTransactions.reduce(
    (sum, transaction) => sum + transaction.amount, 
    0
  );

  const bulkUpdateMutation = useMutation({
    mutationFn: (payload: BulkUpdatePayload) => service.bulkUpdateTransactions(payload) ,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['transactions'] });
      setSelected([]);
      setOpenBulkUpdateModal(false);
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
      setSelected(allTransactions.map((t: Transaction) => t.id));
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

  const handleLoadMore = () => {
    if (paginatedData?.nextCursor && !isLoadingMore) {
      setIsLoadingMore(true);
      setFilters(prev => ({
        ...prev,
        cursor: paginatedData.nextCursor
      }));
      setIsLoadingMore(false);
    }
  };

  const handleBulkUpdate = (updates: Partial<Transaction>) => {
    bulkUpdateMutation.mutate({ transactionIds: selected, updates });
  };

  const handleCategorize = () => {
    categorizeMutation.mutate(selected);
  };

  const handleOpenBulkUpdateModal = () => {
    setOpenBulkUpdateModal(true);
  };

  const handleCloseBulkUpdateModal = () => {
    setOpenBulkUpdateModal(false);
    setSelectedCategoryId('');
  };

  const handleSubmitBulkUpdate = () => {
    if (selectedCategoryId) {
      handleBulkUpdate({ categoryId: selectedCategoryId });
    }
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
              setFilters({ ...filters, month: date?.toISOString().split('T')[0], cursor: undefined })
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
                  cursor: undefined
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
                setFilters({ ...filters, account: e.target.value || undefined, cursor: undefined })
              }
            >
              <MenuItem value="">All</MenuItem>
              {accounts.map((account) => (
                <MenuItem key={account.id} value={account.id}>
                  {account.name}
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
                setFilters({ ...filters, categoryId: e.target.value || undefined, cursor: undefined })
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
              sx={{ mr: 1 }}
            >
              Categorize Selected
            </Button>
            <Button
              variant="contained"
              color="info"
              onClick={handleOpenBulkUpdateModal}
            >
              Bulk Update Selected
            </Button>
          </Box>
        )}
      </Box>
      <Paper elevation={4} sx={{ borderRadius: 4, boxShadow: '0 4px 24px #b2dfdb33', p: 2 }}>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
          <Typography variant="subtitle1" sx={{ fontWeight: 'bold' }}>
            {allTransactions.length} transactions showing
          </Typography>
          <Typography variant="subtitle1" sx={{ fontWeight: 'bold' }}>
            Total: <span style={{ color: totalAmount < 0 ? 'red' : 'green' }}>€{totalAmount.toLocaleString()}</span>
          </Typography>
        </Box>
        <TableContainer>
          <Table>
            <TableHead>
              <TableRow sx={{ bgcolor: 'grey.100' }}>
                <TableCell padding="checkbox">
                  <Checkbox
                    indeterminate={selected.length > 0 && selected.length < allTransactions.length}
                    checked={allTransactions.length > 0 && selected.length === allTransactions.length}
                    onChange={handleSelectAllClick}
                  />
                </TableCell>
                <TableCell sx={{ fontWeight: 700, fontSize: 15 }}>ID</TableCell>
                <TableCell sx={{ fontWeight: 700, fontSize: 15 }}>Date</TableCell>
                <TableCell sx={{ fontWeight: 700, fontSize: 15 }}>Description</TableCell>
                <TableCell sx={{ fontWeight: 700, fontSize: 15 }}>Amount</TableCell>
                <TableCell sx={{ fontWeight: 700, fontSize: 15 }}>Account</TableCell>
                <TableCell sx={{ fontWeight: 700, fontSize: 15 }}>Category</TableCell>
                <TableCell sx={{ fontWeight: 700, fontSize: 15 }}>Status</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {allTransactions.map((transaction, idx) => {
                const category = transaction.category
                const isItemSelected = isSelected(transaction.id);
                
                return (
                  <TableRow
                    key={transaction.id}
                    hover
                    onClick={() => handleClick(transaction.id)}
                    role="checkbox"
                    aria-checked={isItemSelected}
                    selected={isItemSelected}
                    sx={{
                      transition: 'background 0.2s',
                      bgcolor: idx % 2 === 0 ? 'white' : 'grey.50',
                      borderBottom: '1px solid #f0f0f0',
                      cursor: 'pointer',
                    }}
                  >
                    <TableCell padding="checkbox">
                      <Checkbox
                        checked={isItemSelected}
                        onClick={(event) => event.stopPropagation()}
                        onChange={() => handleClick(transaction.id)}
                      />
                    </TableCell>
                    <TableCell 
                      sx={{ 
                        color: 'grey.600', 
                        fontSize: 13, 
                        fontWeight: 500,
                        cursor: 'pointer',
                        '&:hover': {
                          color: 'primary.main',
                          bgcolor: 'primary.lighter'
                        }
                      }}
                      onClick={(event) => {
                        event.stopPropagation();
                        navigator.clipboard.writeText(transaction.id);
                        setCopiedId(transaction.id);
                        setSnackbarOpen(true);
                      }}
                      title="Click to copy ID"
                    >
                      {transaction.id}
                    </TableCell>
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
                    <TableCell>
                      <Chip
                        label={transaction.included ? "Included" : "Excluded"}
                        size="small"
                        sx={{
                          fontWeight: 700,
                          bgcolor: transaction.included ? 'success.lighter' : 'error.lighter',
                          color: transaction.included ? 'success.main' : 'error.main',
                          border: `1px solid ${transaction.included ? '#43a047' : '#d32f2f'}`,
                        }}
                      />
                    </TableCell>
                  </TableRow>
                );
              })}
            </TableBody>
          </Table>
        </TableContainer>
        
        {paginatedData?.hasMore && (
          <Box sx={{ display: 'flex', justifyContent: 'center', mt: 2 }}>
            <Button
              variant="outlined"
              onClick={handleLoadMore}
              disabled={isLoadingMore}
              startIcon={isLoadingMore ? <CircularProgress size={20} /> : null}
            >
              {isLoadingMore ? 'Loading...' : 'Load More'}
            </Button>
          </Box>
        )}
      </Paper>

      {/* Bulk Update Modal */}
      <Dialog open={openBulkUpdateModal} onClose={handleCloseBulkUpdateModal} maxWidth="sm" fullWidth>
        <DialogTitle>Update {selected.length} Transaction{selected.length !== 1 ? 's' : ''}</DialogTitle>
        <DialogContent>
          <Box sx={{ pt: 2 }}>
            <FormControl fullWidth sx={{ mb: 2 }}>
              <InputLabel>Category</InputLabel>
              <Select
                value={selectedCategoryId}
                label="Category"
                onChange={(e) => setSelectedCategoryId(e.target.value)}
              >
                <MenuItem value="">
                  <em>None</em>
                </MenuItem>
                {categories.map((category) => (
                  <MenuItem key={category.id} value={category.id}>
                    {category.name} ({category.macroCategory})
                  </MenuItem>
                ))}
              </Select>
            </FormControl>
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseBulkUpdateModal}>Cancel</Button>
          <Button 
            onClick={handleSubmitBulkUpdate} 
            variant="contained" 
            color="primary"
            disabled={!selectedCategoryId}
          >
            Update
          </Button>
        </DialogActions>
      </Dialog>

      {/* Copy ID Snackbar */}
      <Snackbar
        open={snackbarOpen}
        autoHideDuration={3000}
        onClose={() => setSnackbarOpen(false)}
        anchorOrigin={{ vertical: 'bottom', horizontal: 'center' }}
      >
        <Alert 
          onClose={() => setSnackbarOpen(false)} 
          severity="success"
          variant="filled"
          sx={{ width: '100%' }}
        >
          ID {copiedId} copied to clipboard!
        </Alert>
      </Snackbar>
    </Layout>
  );
}
