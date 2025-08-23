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
  Button,
  Typography,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  IconButton,
  Chip,
  RadioGroup,
  FormControlLabel,
  Radio,
  CircularProgress,
} from '@mui/material';
import { Add as AddIcon, Edit as EditIcon, Delete as DeleteIcon } from '@mui/icons-material';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { service } from '../../services/api';
import { Category, PaginatedResponse } from '../../types';
import Layout from '../../components/Layout';

export default function CategoriesPage() {
  const [open, setOpen] = useState(false);
  const [editingCategory, setEditingCategory] = useState<Category | null>(null);
  const [formData, setFormData] = useState<Omit<Category, 'id'>>({
    name: '',
    macroCategory: '',
    regexPatterns: [],
    type: 'NEEDS',
  });
  const queryClient = useQueryClient();
  const [needWantMap, setNeedWantMap] = useState<{ [categoryId: string]: 'Need' | 'Want' }>({});
  const [allCategories, setAllCategories] = useState<Category[]>([]);
  const [isLoadingMore, setIsLoadingMore] = useState(false);
  const [filters, setFilters] = useState<{ limit: number; cursor?: string }>({
    limit: 20,
  });

  const { data: paginatedData, isLoading } = useQuery<PaginatedResponse<Category>>({
    queryKey: ['categories', filters],
    queryFn: () => service.getCategories(filters),
  });

  // Update allCategories when new data is fetched
  useEffect(() => {
    if (paginatedData) {
      if (filters.cursor) {
        // Append new categories when loading more (cursor-based pagination)
        setAllCategories(prev => [...prev, ...paginatedData.data]);
        setIsLoadingMore(false); // Reset loading state after data is loaded
      } else {
        // Replace categories when filters change (new query)
        setAllCategories(paginatedData.data);
      }
    }
  }, [paginatedData, filters.cursor]);

  // Initialize mapping for expense categories
  useEffect(() => {
    if (allCategories.length && Object.keys(needWantMap).length === 0) {
      const initial: { [categoryId: string]: 'Need' | 'Want' } = {};
      allCategories.forEach((cat: Category) => {
        if (cat.macroCategory.toUpperCase() === 'EXPENSE') {
          initial[cat.id] = 'Need';
        }
      });
      setNeedWantMap(initial);
    }
    // eslint-disable-next-line
  }, [allCategories]);

  const createMutation = useMutation({
    mutationFn: service.createCategory,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['categories'] });
      // Reset pagination to show new category
      setFilters({ limit: 20 });
      setAllCategories([]);
      handleClose();
    },
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, category }: { id: string; category: Partial<Category> }) =>
      service.updateCategory(id, category),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['categories'] });
      // Reset pagination to reload updated data
      setFilters({ limit: 20 });
      setAllCategories([]);
      handleClose();
    },
  });

  const deleteMutation = useMutation({
    mutationFn: service.deleteCategory,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['categories'] });
      // Reset pagination to reload after deletion
      setFilters({ limit: 20 });
      setAllCategories([]);
    },
  });

  const handleOpen = (category?: Category) => {
    if (category) {
      setEditingCategory(category);
      setFormData({
        name: category.name,
        macroCategory: category.macroCategory,
        regexPatterns: category.regexPatterns,
        type: category.type,
      });
    } else {
      setEditingCategory(null);
      setFormData({
        name: '',
        macroCategory: '',
        regexPatterns: [],
        type: 'NEEDS',
      });
    }
    setOpen(true);
  };

  const handleClose = () => {
    setOpen(false);
    setEditingCategory(null);
    setFormData({
      name: '',
      macroCategory: '',
      regexPatterns: [],
      type: 'NEEDS',
    });
  };

  const handleSubmit = () => {
    if (editingCategory) {
      updateMutation.mutate({ id: editingCategory.id, category: formData });
    } else {
      createMutation.mutate(formData);
    }
  };

  const handleDelete = (id: string) => {
    if (window.confirm('Are you sure you want to delete this category?')) {
      deleteMutation.mutate(id);
    }
  };

  const handleNeedWantChange = (categoryId: string, value: 'Need' | 'Want') => {
    setNeedWantMap((prev) => ({ ...prev, [categoryId]: value }));
  };

  const handleLoadMore = () => {
    if (paginatedData?.nextCursor && paginatedData?.hasMore && !isLoadingMore) {
      setIsLoadingMore(true);
      setFilters(prev => ({
        ...prev,
        cursor: paginatedData.nextCursor
      }));
    }
  };
  return (
    <Layout>
      <Box sx={{ mb: 3 }}>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
          <Typography variant="h4" fontWeight={800} color="success.main" letterSpacing={1}>
            Categories
          </Typography>
          <Button
            variant="contained"
            color="success"
            startIcon={<AddIcon />}
            onClick={() => handleOpen()}
            sx={{ borderRadius: 2, fontWeight: 700, px: 3, py: 1 }}
          >
            Add Category
          </Button>
        </Box>
      </Box>

      {/* Categories Table */}
      <Paper elevation={4} sx={{ borderRadius: 4, overflow: 'hidden' }}>
        <TableContainer>
          <Table>
            <TableHead>
              <TableRow sx={{ bgcolor: 'grey.100' }}>
                <TableCell sx={{ fontWeight: 700, color: 'text.primary' }}>Name</TableCell>
                <TableCell sx={{ fontWeight: 700, color: 'text.primary' }}>Macro Category</TableCell>
                <TableCell sx={{ fontWeight: 700, color: 'text.primary' }}>Type</TableCell>
                <TableCell sx={{ fontWeight: 700, color: 'text.primary' }}>Regex Patterns</TableCell>
                <TableCell sx={{ fontWeight: 700, color: 'text.primary', textAlign: 'center' }}>Actions</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {allCategories.map((category: Category, index: number) => (
                <TableRow 
                  key={category.id} 
                  sx={{ 
                    '&:nth-of-type(odd)': { bgcolor: 'grey.50' },
                    '&:hover': { bgcolor: 'grey.100' },
                    transition: 'background-color 0.2s'
                  }}
                >
                  <TableCell>
                    <Typography variant="body1" fontWeight={600}>
                      {category.name}
                    </Typography>
                  </TableCell>
                  <TableCell>
                    <Chip 
                      label={category.macroCategory} 
                      variant="outlined"
                      size="small"
                      sx={{ fontWeight: 600, fontSize: 12, borderColor: 'grey.400', color: 'text.secondary' }} 
                    />
                  </TableCell>
                  <TableCell>
                    <Chip 
                      label={category.type} 
                      variant="outlined"
                      size="small"
                      sx={{ fontWeight: 600, fontSize: 12, borderColor: 'grey.400', color: 'text.secondary' }} 
                    />
                  </TableCell>
                  <TableCell>
                    <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 0.5, maxWidth: 300 }}>
                      {category.regexPatterns.length > 0 ? (
                        category.regexPatterns.map((pattern: string, idx: number) => (
                          <Chip 
                            key={idx} 
                            label={pattern} 
                            variant="outlined" 
                            size="small"
                            sx={{ fontSize: 11, height: 24, borderColor: 'grey.300', color: 'text.secondary' }}
                          />
                        ))
                      ) : (
                        <Typography variant="body2" color="text.secondary" fontStyle="italic">
                          No patterns
                        </Typography>
                      )}
                    </Box>
                  </TableCell>
                  <TableCell>
                    <Box sx={{ display: 'flex', gap: 1, justifyContent: 'center' }}>
                      <IconButton 
                        onClick={() => handleOpen(category)} 
                        size="small" 
                        sx={{ 
                          color: 'grey.600',
                          '&:hover': { bgcolor: 'grey.200', color: 'primary.main' },
                          transition: 'all 0.2s'
                        }}
                      >
                        <EditIcon fontSize="small" />
                      </IconButton>
                      <IconButton 
                        onClick={() => handleDelete(category.id)} 
                        size="small" 
                        sx={{ 
                          color: 'grey.600',
                          '&:hover': { bgcolor: 'error.50', color: 'error.main' },
                          transition: 'all 0.2s'
                        }}
                      >
                        <DeleteIcon fontSize="small" />
                      </IconButton>
                    </Box>
                  </TableCell>
                </TableRow>
              ))}
              {allCategories.length === 0 && !isLoading && (
                <TableRow>
                  <TableCell colSpan={5} sx={{ textAlign: 'center', py: 4 }}>
                    <Typography variant="body1" color="text.secondary">
                      No categories found. Add your first category to get started.
                    </Typography>
                  </TableCell>
                </TableRow>
              )}
              {isLoading && (
                <TableRow>
                  <TableCell colSpan={5} sx={{ textAlign: 'center', py: 4 }}>
                    <CircularProgress />
                  </TableCell>
                </TableRow>
              )}
            </TableBody>
          </Table>
        </TableContainer>
      </Paper>

      {/* Load More Button */}
      {paginatedData?.hasMore && (
        <Box sx={{ display: 'flex', justifyContent: 'center', mt: 4 }}>
          <Button
            variant="outlined"
            onClick={handleLoadMore}
            disabled={isLoadingMore}
            startIcon={isLoadingMore ? <CircularProgress size={20} /> : null}
            sx={{ borderRadius: 2, fontWeight: 700, px: 4, py: 1 }}
          >
            {isLoadingMore ? 'Loading...' : 'Load More Categories'}
          </Button>
        </Box>
      )}

      {/* Category Dialog */}
      <Dialog open={open} onClose={handleClose} fullWidth maxWidth="sm">
        <DialogTitle sx={{ fontWeight: 700 }}>{editingCategory ? 'Edit Category' : 'Add Category'}</DialogTitle>
        <DialogContent>
          <TextField
            autoFocus
            margin="dense"
            label="Name"
            fullWidth
            value={formData.name}
            onChange={(e) => setFormData({ ...formData, name: e.target.value })}
            sx={{ mb: 2 }}
          />
          <TextField
            margin="dense"
            label="Macro Category"
            fullWidth
            value={formData.macroCategory}
            onChange={(e) => setFormData({ ...formData, macroCategory: e.target.value })}
            sx={{ mb: 2 }}
          />
          <TextField
            margin="dense"
            label="Regex Patterns (one per line)"
            fullWidth
            multiline
            rows={4}
            value={formData.regexPatterns.join('\n')}
            onChange={(e) =>
              setFormData({
                ...formData,
                regexPatterns: e.target.value.split('\n').filter((pattern) => pattern.trim()),
              })
            }
          />
        </DialogContent>
        <DialogActions sx={{ px: 3, pb: 2 }}>
          <Button onClick={handleClose} sx={{ borderRadius: 2 }}>Cancel</Button>
          <Button onClick={handleSubmit} variant="contained" color="success" sx={{ borderRadius: 2, fontWeight: 700 }}>
            {editingCategory ? 'Update' : 'Add'}
          </Button>
        </DialogActions>
      </Dialog>
    </Layout>
  );
} 