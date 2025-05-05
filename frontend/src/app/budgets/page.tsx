'use client';

import { useState } from 'react';
import {
  Box,
  Paper,
  Typography,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Chip,
  Button,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
} from '@mui/material';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { service } from '../../services/api';
import { Budget, Category } from '../../types';
import Layout from '../../components/Layout';
import { mockCategories } from '../../services/mockData';
import { Add as AddIcon } from '@mui/icons-material';

export default function BudgetsPage() {
  const [selectedYear, setSelectedYear] = useState('2024');
  const [open, setOpen] = useState(false);
  const [editingBudget, setEditingBudget] = useState<Budget | null>(null);
  const [formData, setFormData] = useState({
    categoryId: '',
    amount: '',
    period: 'monthly',
    month: '',
  });

  const queryClient = useQueryClient();

  const { data: budgets = [], isLoading: isLoadingBudgets } = useQuery({
    queryKey: ['budgets', selectedYear],
    queryFn: () => service.getBudgets(selectedYear),
  });

  const { data: categories = [] } = useQuery({
    queryKey: ['categories'],
    queryFn: () => service.getCategories(),
  });

  const createMutation = useMutation({
    mutationFn: (budget: Omit<Budget, 'id'>) => service.createBudget(budget),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['budgets'] });
      handleClose();
    },
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, budget }: { id: string; budget: Partial<Budget> }) =>
      service.updateBudget(id, budget),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['budgets'] });
      handleClose();
    },
  });

  const deleteMutation = useMutation({
    mutationFn: (id: string) => service.deleteBudget(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['budgets'] });
    },
  });

  const handleOpen = (budget?: Budget) => {
    if (budget) {
      setEditingBudget(budget);
      setFormData({
        categoryId: budget.categoryId,
        amount: budget.amount.toString(),
        period: budget.period,
        month: budget.month || '',
      });
    } else {
      setEditingBudget(null);
      setFormData({
        categoryId: '',
        amount: '',
        period: 'monthly',
        month: '',
      });
    }
    setOpen(true);
  };

  const handleClose = () => {
    setOpen(false);
    setEditingBudget(null);
    setFormData({
      categoryId: '',
      amount: '',
      period: 'monthly',
      month: '',
    });
  };

  const handleSubmit = () => {
    const budgetData = {
      categoryId: formData.categoryId,
      amount: parseFloat(formData.amount),
      period: formData.period as 'monthly' | 'annual',
      month: formData.period === 'monthly' ? formData.month : undefined,
      year: selectedYear,
    };

    if (editingBudget) {
      updateMutation.mutate({ id: editingBudget.id, budget: budgetData });
    } else {
      createMutation.mutate(budgetData);
    }
  };

  const handleDelete = (id: string) => {
    if (window.confirm('Are you sure you want to delete this budget?')) {
      deleteMutation.mutate(id);
    }
  };

  const getCategoryName = (categoryId: string) => {
    const category = categories.find(c => c.id === categoryId);
    return category ? category.name : categoryId;
  };

  return (
    <Layout>
      <Box sx={{ mb: 3 }}>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
          <Typography variant="h4" fontWeight={800} color="primary.main" letterSpacing={1}>
            Budgets
          </Typography>
          <Button
            variant="contained"
            color="primary"
            startIcon={<AddIcon />}
            onClick={() => handleOpen()}
            sx={{ borderRadius: 2, fontWeight: 700, px: 3, py: 1 }}
          >
            Add Budget
          </Button>
        </Box>
      </Box>

      <Paper elevation={4} sx={{ p: 3, borderRadius: 4, boxShadow: '0 4px 24px #b2dfdb33' }}>
        <TableContainer>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell>Category</TableCell>
                <TableCell>Amount</TableCell>
                <TableCell>Period</TableCell>
                <TableCell>Month</TableCell>
                <TableCell>Year</TableCell>
                <TableCell>Actions</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {budgets.map((budget) => (
                <TableRow key={budget.id}>
                  <TableCell>{getCategoryName(budget.categoryId)}</TableCell>
                  <TableCell>€{budget.amount.toLocaleString()}</TableCell>
                  <TableCell>
                    <Chip
                      label={budget.period}
                      color={budget.period === 'annual' ? 'primary' : 'secondary'}
                      size="small"
                    />
                  </TableCell>
                  <TableCell>{budget.month || '-'}</TableCell>
                  <TableCell>{budget.year}</TableCell>
                  <TableCell>
                    <Box sx={{ display: 'flex', gap: 1 }}>
                      <Button
                        size="small"
                        variant="outlined"
                        color="primary"
                        onClick={() => handleOpen(budget)}
                      >
                        Edit
                      </Button>
                      <Button
                        size="small"
                        variant="outlined"
                        color="error"
                        onClick={() => handleDelete(budget.id)}
                      >
                        Delete
                      </Button>
                    </Box>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </TableContainer>
      </Paper>

      <Dialog open={open} onClose={handleClose} maxWidth="sm" fullWidth>
        <DialogTitle>
          {editingBudget ? 'Edit Budget' : 'Add Budget'}
        </DialogTitle>
        <DialogContent>
          <Box sx={{ pt: 2, display: 'flex', flexDirection: 'column', gap: 2 }}>
            <FormControl fullWidth>
              <InputLabel>Category</InputLabel>
              <Select
                value={formData.categoryId}
                label="Category"
                onChange={(e) => setFormData({ ...formData, categoryId: e.target.value })}
              >
                {categories.map((category) => (
                  <MenuItem key={category.id} value={category.id}>
                    {category.name}
                  </MenuItem>
                ))}
              </Select>
            </FormControl>

            <TextField
              label="Amount"
              type="number"
              value={formData.amount}
              onChange={(e) => setFormData({ ...formData, amount: e.target.value })}
              fullWidth
            />

            <FormControl fullWidth>
              <InputLabel>Period</InputLabel>
              <Select
                value={formData.period}
                label="Period"
                onChange={(e) => setFormData({ ...formData, period: e.target.value })}
              >
                <MenuItem value="monthly">Monthly</MenuItem>
                <MenuItem value="annual">Annual</MenuItem>
              </Select>
            </FormControl>

            {formData.period === 'monthly' && (
              <TextField
                label="Month"
                type="month"
                value={formData.month}
                onChange={(e) => setFormData({ ...formData, month: e.target.value })}
                fullWidth
                InputLabelProps={{ shrink: true }}
              />
            )}
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleClose}>Cancel</Button>
          <Button
            onClick={handleSubmit}
            variant="contained"
            color="primary"
            disabled={!formData.categoryId || !formData.amount || (formData.period === 'monthly' && !formData.month)}
          >
            {editingBudget ? 'Update' : 'Add'}
          </Button>
        </DialogActions>
      </Dialog>
    </Layout>
  );
} 