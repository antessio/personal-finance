'use client';

import { Paper, Typography, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Box } from '@mui/material';
import { TrendingUp, TrendingDown } from '@mui/icons-material';

interface MonthlyDataRow {
  month: string;
  budget: number;
  actual: number;
}

interface MonthlyDataTableProps {
  title: string;
  data: MonthlyDataRow[];
  totalBudget: number;
  totalActual: number;
  headerColor?: string;
  type?: 'income' | 'expense' | 'savings'; // To determine if higher actual is good or bad
}

export default function MonthlyDataTable({ title, data, totalBudget, totalActual, headerColor = '#4caf50', type = 'income' }: MonthlyDataTableProps) {
  // Determine if result is positive based on type
  const isPositiveResult = (actual: number, budget: number) => {
    if (type === 'expense') {
      // For expenses, actual < budget is good
      return actual < budget;
    } else {
      // For income and savings, actual > budget is good
      return actual > budget;
    }
  };

  const getResultColor = (actual: number, budget: number) => {
    return isPositiveResult(actual, budget) ? 'success.main' : 'error.main';
  };

  const totalIsPositive = isPositiveResult(totalActual, totalBudget);
  const totalDifference = totalActual - totalBudget;
  const totalPercentage = totalBudget > 0 ? ((totalActual / totalBudget) * 100 - 100).toFixed(1) : 0;
  return (
    <Paper elevation={4} sx={{ p: 3, borderRadius: 4, boxShadow: '0 4px 24px rgba(0,0,0,0.08)' }}>
      <Box sx={{ bgcolor: headerColor, p: 2, borderRadius: 3, mb: 2, boxShadow: '0 2px 8px rgba(0,0,0,0.1)' }}>
        <Typography variant="h6" fontWeight={700} color="white" textAlign="center">
          {title}
        </Typography>
        <Box sx={{ display: 'flex', justifyContent: 'space-around', alignItems: 'center', mt: 1 }}>
          <Box>
            <Typography variant="caption" color="white" sx={{ opacity: 0.9 }}>BUDGET</Typography>
            <Typography variant="h6" fontWeight={700} color="white">€ {totalBudget.toLocaleString()}</Typography>
          </Box>
          <Box sx={{ display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
            {totalIsPositive ? (
              <TrendingUp sx={{ fontSize: 40, color: 'white' }} />
            ) : (
              <TrendingDown sx={{ fontSize: 40, color: 'white' }} />
            )}
            <Typography variant="caption" color="white" fontWeight={700}>
              {totalPercentage}%
            </Typography>
          </Box>
          <Box>
            <Typography variant="caption" color="white" sx={{ opacity: 0.9 }}>ACTUAL</Typography>
            <Typography variant="h6" fontWeight={700} color="white">€ {totalActual.toLocaleString()}</Typography>
          </Box>
        </Box>
      </Box>
      <TableContainer>
        <Table size="small">
          <TableHead>
            <TableRow>
              <TableCell sx={{ fontWeight: 700 }}>MONTH</TableCell>
              <TableCell align="right" sx={{ fontWeight: 700 }}>BUDGET</TableCell>
              <TableCell align="right" sx={{ fontWeight: 700 }}>ACTUAL</TableCell>
              <TableCell align="center" sx={{ fontWeight: 700 }}>STATUS</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {data.map((row) => {
              const rowIsPositive = isPositiveResult(row.actual, row.budget);
              const rowPercentage = row.budget > 0 ? ((row.actual / row.budget) * 100 - 100).toFixed(0) : 0;

              return (
                <TableRow key={row.month} hover>
                  <TableCell sx={{ fontWeight: 600 }}>{row.month}</TableCell>
                  <TableCell align="right" sx={{ fontWeight: 500 }}>€{row.budget.toLocaleString()}</TableCell>
                  <TableCell align="right" sx={{ fontWeight: 700, color: getResultColor(row.actual, row.budget) }}>
                    €{row.actual.toLocaleString()}
                  </TableCell>
                  <TableCell align="center">
                    <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 0.5 }}>
                      {rowIsPositive ? (
                        <TrendingUp sx={{ fontSize: 18, color: 'success.main' }} />
                      ) : (
                        <TrendingDown sx={{ fontSize: 18, color: 'error.main' }} />
                      )}
                      <Typography variant="caption" fontWeight={700} color={rowIsPositive ? 'success.main' : 'error.main'}>
                        {rowPercentage}%
                      </Typography>
                    </Box>
                  </TableCell>
                </TableRow>
              );
            })}
          </TableBody>
        </Table>
      </TableContainer>
    </Paper>
  );
}
