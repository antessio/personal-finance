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
} from '@mui/material';
import { Add as AddIcon, Edit as EditIcon, Delete as DeleteIcon } from '@mui/icons-material';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { service } from '../../services/api';
import { Category } from '../../types';
import Layout from '../../components/Layout';

export default function CategoriesPage() {
  const [open, setOpen] = useState(false);
  const [editingCategory, setEditingCategory] = useState<Category | null>(null);
  const [formData, setFormData] = useState<Omit<Category, 'id'>>({
    name: '',
    macroCategory: '',
    regexPatterns: [],
  });
  const queryClient = useQueryClient();
  const [needWantMap, setNeedWantMap] = useState<{ [categoryId: string]: 'Need' | 'Want' }>({});

  const { data: categories = [], isLoading } = useQuery<Category[]>({
    queryKey: ['categories'],
    queryFn: () => service.getCategories(),
  });
  // Initialize mapping for expense categories
  useEffect(() => {
    if (categories.length && Object.keys(needWantMap).length === 0) {
      const initial: { [categoryId: string]: 'Need' | 'Want' } = {};
      categories.forEach((cat) => {
        if (cat.macroCategory.toUpperCase() === 'EXPENSE') {
          initial[cat.id] = 'Need';
        }
      });
      setNeedWantMap(initial);
    }
    // eslint-disable-next-line
  }, [categories]);

  const createMutation = useMutation({
    mutationFn: service.createCategory,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['categories'] });
      handleClose();
    },
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, category }: { id: string; category: Partial<Category> }) =>
      service.updateCategory(id, category),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['categories'] });
      handleClose();
    },
  });

  const deleteMutation = useMutation({
    mutationFn: service.deleteCategory,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['categories'] });
    },
  });

  const handleOpen = (category?: Category) => {
    if (category) {
      setEditingCategory(category);
      setFormData({
        name: category.name,
        macroCategory: category.macroCategory,
        regexPatterns: category.regexPatterns,
      });
    } else {
      setEditingCategory(null);
      setFormData({
        name: '',
        macroCategory: '',
        regexPatterns: [],
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
      <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 3 }}>
        {categories.map((category) => (
          <Box key={category.id} sx={{ flex: '1 1 320px', minWidth: 280, maxWidth: 400 }}>
            <Paper elevation={4} sx={{ p: 3, borderRadius: 4, boxShadow: '0 4px 24px #b2dfdb33', position: 'relative', minHeight: 170 }}>
              <Box sx={{ position: 'absolute', top: 12, right: 12, display: 'flex', gap: 1 }}>
                <IconButton onClick={() => handleOpen(category)} size="small" color="info">
                  <EditIcon fontSize="small" />
                </IconButton>
                <IconButton onClick={() => handleDelete(category.id)} size="small" color="error">
                  <DeleteIcon fontSize="small" />
                </IconButton>
              </Box>
              <Typography variant="h6" fontWeight={700} mb={1}>
                {category.name}
              </Typography>
              <Chip label={category.macroCategory} color="primary" sx={{ mb: 2, fontWeight: 700, fontSize: 14 }} />
              {/* Need/Want selector for EXPENSE categories */}
              {category.macroCategory.toUpperCase() === 'EXPENSE' && (
                <RadioGroup
                  row
                  value={needWantMap[category.id] || 'Need'}
                  onChange={(e) => handleNeedWantChange(category.id, e.target.value as 'Need' | 'Want')}
                  sx={{ mb: 1 }}
                >
                  <FormControlLabel value="Need" control={<Radio color="success" />} label="Need" />
                  <FormControlLabel value="Want" control={<Radio color="warning" />} label="Want" />
                </RadioGroup>
              )}
              <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1, mt: 1 }}>
                {category.regexPatterns.map((pattern, idx) => (
                  <Chip key={idx} label={pattern} variant="outlined" color="primary" size="small" />
                ))}
              </Box>
            </Paper>
          </Box>
        ))}
      </Box>
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