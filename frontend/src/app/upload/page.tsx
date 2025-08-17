'use client';

import { useState } from 'react';
import {
  Box,
  Paper,
  Typography,
  Button,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  IconButton,
  Chip,
  LinearProgress,
  Stack,
  Alert,
  Tooltip,
  CircularProgress,
} from '@mui/material';
import { 
  Upload as UploadIcon, 
  Delete as DeleteIcon, 
  PlayArrow as PlayArrowIcon,
  CloudUpload as CloudUploadIcon,
  History as HistoryIcon,
  Info as InfoIcon,
} from '@mui/icons-material';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { service } from '../../services/api';
import { UploadFile, PaginatedResponse } from '../../types';
import Layout from '../../components/Layout';
import { useEffect } from 'react';

export default function UploadPage() {
  const [selectedAccount, setSelectedAccount] = useState<string>('');
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [allUploads, setAllUploads] = useState<UploadFile[]>([]);
  const [isLoadingMore, setIsLoadingMore] = useState(false);
  const [filters, setFilters] = useState<{ limit: number; cursor?: string }>({
    limit: 20,
  });
  const queryClient = useQueryClient();

  // Get unique accounts from transactions
  const { data: accounts = [] } = useQuery({
    queryKey: ['accounts'],
    queryFn: () => service.getAccounts(),
  });

  // Get uploaded files with pagination
  const { data: paginatedData, isLoading } = useQuery<PaginatedResponse<UploadFile>>({
    queryKey: ['uploads', filters],
    queryFn: () => service.getUploads(filters),
  });

  // Update allUploads when new data is fetched
  useEffect(() => {
    if (paginatedData) {
      if (filters.cursor) {
        // Append new uploads when loading more (cursor-based pagination)
        setAllUploads(prev => [...prev, ...paginatedData.data]);
        setIsLoadingMore(false); // Reset loading state after data is loaded
      } else {
        // Replace uploads when filters change (new query)
        setAllUploads(paginatedData.data);
      }
    }
  }, [paginatedData, filters.cursor]);

  const uploadMutation = useMutation({
    mutationFn: (file: { file: File; account: string }) => service.uploadFile(file),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['uploads'] });
      setSelectedFile(null);
      setSelectedAccount('');
    },
  });

  const processMutation = useMutation({
    mutationFn: (id: string) => service.processUpload(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['uploads'] });
    },
  });

  const deleteMutation = useMutation({
    mutationFn: (id: string) => service.deleteUpload(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['uploads'] });
    },
  });

  const handleFileChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    if (event.target.files && event.target.files[0]) {
      setSelectedFile(event.target.files[0]);
    }
  };

  const handleUpload = () => {
    if (selectedFile && selectedAccount) {
      uploadMutation.mutate({ file: selectedFile, account: selectedAccount });
    }
  };

  const handleProcess = (id: string) => {
    processMutation.mutate(id);
  };

  const handleDelete = (id: string) => {
    if (window.confirm('Are you sure you want to delete this upload?')) {
      deleteMutation.mutate(id);
    }
  };

  const handleLoadMore = () => {
    if (paginatedData && paginatedData.hasMore && !isLoadingMore) {
      setIsLoadingMore(true);
      setFilters(prev => ({
        ...prev,
        cursor: paginatedData.nextCursor,
      }));
    }
  };

  const getStatusColor = (status: UploadFile['status']) => {
    switch (status) {
      case 'pending': return 'warning';
      case 'processing': return 'info';
      case 'completed': return 'success';
      case 'error': return 'error';
      default: return 'default';
    }
  };

  const getStatusIcon = (status: UploadFile['status']) => {
    switch (status) {
      case 'pending': return <InfoIcon />;
      case 'processing': return <LinearProgress value={20} />;
      case 'completed': return <CloudUploadIcon />;
      case 'error': return <InfoIcon />;
      default: return null;
    }
  };

  return (
    <Layout>
      <Box sx={{ mb: 3 }}>
        <Stack direction="row" alignItems="center" spacing={2} mb={3}>
          <CloudUploadIcon sx={{ fontSize: 40, color: 'primary.main' }} />
          <Typography variant="h4" fontWeight={800} color="primary.main" letterSpacing={1}>
            Upload Transactions
          </Typography>
        </Stack>

        <Paper elevation={4} sx={{ p: 3, borderRadius: 4, boxShadow: '0 4px 24px #b2dfdb33', mb: 3 }}>
          <Stack spacing={3}>
            <Alert severity="info" icon={<InfoIcon />}>
              Upload your bank statements in CSV or Excel format. Make sure the file includes date, description, and amount columns.
            </Alert>
            <Box sx={{ display: 'flex', gap: 2, alignItems: 'center' }}>
              <FormControl sx={{ minWidth: 200 }}>
                <InputLabel>Account</InputLabel>
                <Select
                  value={selectedAccount}
                  label="Account"
                  onChange={(e) => setSelectedAccount(e.target.value)}
                >
                  {accounts.map((account) => (
                    <MenuItem key={account.id} value={account.name}>
                      {account.name}
                    </MenuItem>
                  ))}
                </Select>
              </FormControl>
              <Button
                variant="contained"
                component="label"
                startIcon={<UploadIcon />}
                disabled={!selectedAccount}
                sx={{ minWidth: 150 }}
              >
                Select File
                <input
                  type="file"
                  hidden
                  accept=".csv,.xlsx"
                  onChange={handleFileChange}
                />
              </Button>
              <Button
                variant="contained"
                color="success"
                onClick={handleUpload}
                disabled={!selectedFile || !selectedAccount}
                sx={{ minWidth: 150 }}
              >
                Upload
              </Button>
              {selectedFile && (
                <Typography variant="body2" color="text.secondary">
                  Selected: {selectedFile.name}
                </Typography>
              )}
            </Box>
          </Stack>
        </Paper>

        <Stack direction="row" alignItems="center" spacing={2} mb={2}>
          <HistoryIcon sx={{ fontSize: 32, color: 'primary.main' }} />
          <Typography variant="h5" fontWeight={700} color="primary.main">
            Upload History
          </Typography>
        </Stack>

        <Paper elevation={4} sx={{ borderRadius: 4, boxShadow: '0 4px 24px #b2dfdb33' }}>
          <TableContainer>
            <Table>
              <TableHead>
                <TableRow sx={{ bgcolor: 'grey.100' }}>
                  <TableCell sx={{ fontWeight: 700 }}>Filename</TableCell>
                  <TableCell sx={{ fontWeight: 700 }}>Account</TableCell>
                  <TableCell sx={{ fontWeight: 700 }}>Status</TableCell>
                  <TableCell sx={{ fontWeight: 700 }}>Uploaded At</TableCell>
                  <TableCell sx={{ fontWeight: 700 }}>Processed At</TableCell>
                  <TableCell sx={{ fontWeight: 700 }}>Actions</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {allUploads.map((upload: UploadFile) => (
                  <TableRow 
                    key={upload.id}
                    hover
                    sx={{
                      '&:nth-of-type(odd)': { bgcolor: 'grey.50' },
                      transition: 'background 0.2s',
                    }}
                  >
                    <TableCell>{upload.filename}</TableCell>
                    <TableCell>{upload.account}</TableCell>
                    <TableCell>
                      <Stack direction="row" alignItems="center" spacing={1}>
                        {getStatusIcon(upload.status)}
                        <Chip
                          label={upload.status}
                          color={getStatusColor(upload.status)}
                          size="small"
                          sx={{ fontWeight: 600 }}
                        />
                      </Stack>
                      {upload.errorMessage && (
                        <Typography variant="caption" color="error" display="block" mt={0.5}>
                          {upload.errorMessage}
                        </Typography>
                      )}
                    </TableCell>
                    <TableCell>{new Date(upload.uploadedAt).toLocaleString()}</TableCell>
                    <TableCell>
                      {upload.processedAt ? new Date(upload.processedAt).toLocaleString() : '-'}
                    </TableCell>
                    <TableCell>
                      <Tooltip title="Process file">
                        <IconButton
                          onClick={() => handleProcess(upload.id)}
                          disabled={!['pending', 'error'].includes(upload.status)}
                          color="primary"
                        >
                          <PlayArrowIcon />
                        </IconButton>
                      </Tooltip>
                      <Tooltip title="Delete file">
                        <IconButton
                          onClick={() => handleDelete(upload.id)}
                          color="error"
                        >
                          <DeleteIcon />
                        </IconButton>
                      </Tooltip>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
          
          {paginatedData && paginatedData.hasMore && (
            <Box sx={{ display: 'flex', justifyContent: 'center', mt: 2 }}>
              <Button
                variant="outlined"
                onClick={handleLoadMore}
                disabled={isLoadingMore}
                startIcon={isLoadingMore ? <CircularProgress size={20} /> : undefined}
              >
                {isLoadingMore ? 'Loading...' : 'Load More'}
              </Button>
            </Box>
          )}
        </Paper>
      </Box>
    </Layout>
  );
} 