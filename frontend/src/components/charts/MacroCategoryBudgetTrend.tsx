'use client';

import { useState } from 'react';
import { Box, Chip, Typography, useTheme } from '@mui/material';
import { CheckCircle, Warning } from '@mui/icons-material';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts';
import { MacroCategoryMonthlyBudget, CategorySpending } from '../../types';

interface MacroCategoryBudgetTrendProps {
  data: MacroCategoryMonthlyBudget[];
  categorySpending: CategorySpending[];
  months: string[];
  colors: { [key: string]: string };
}

const CATEGORY_LIST_COLORS = ['#4c93af', '#2196f3', '#ff9800', '#9c27b0', '#f44336', '#795548', '#607d8b', '#00897b'];

export default function MacroCategoryBudgetTrend({ data, categorySpending, months, colors }: MacroCategoryBudgetTrendProps) {
  const theme = useTheme();
  const isDark = theme.palette.mode === 'dark';

  const macroCategories = [...new Set(data.map(item => item.macroCategory))];
  const [selectedMacroCategory, setSelectedMacroCategory] = useState<string | null>(null);

  const handleChipClick = (category: string) => {
    setSelectedMacroCategory(prev => (prev === category ? null : category));
  };

  const singleCategoryChartData = selectedMacroCategory
    ? months.map((month, index) => {
      const monthNumber = index + 1;
      const entry = data.find(item => item.macroCategory === selectedMacroCategory && item.month === monthNumber);
      return {
        month,
        actual: entry?.actual || 0,
        budget: entry?.budget || 0,
      };
    })
    : [];

  const totalChartData = months.map((month, index) => {
    const monthNumber = index + 1;
    const monthEntries = data.filter(item => item.month === monthNumber);
    return {
      month,
      actual: monthEntries.reduce((sum, item) => sum + item.actual, 0),
      budget: monthEntries.reduce((sum, item) => sum + item.budget, 0),
    };
  });

  const activeChartData = selectedMacroCategory ? singleCategoryChartData : totalChartData;
  const activeColor = selectedMacroCategory ? (colors[selectedMacroCategory] || '#666666') : '#1976d2';
  const activeLabel = selectedMacroCategory ? 'Actual' : 'Total Actual';

  const categoriesInGroup = selectedMacroCategory
    ? categorySpending
      .filter(item => item.macroCategory === selectedMacroCategory)
      .sort((a, b) => b.totalSpent - a.totalSpent)
    : [];

  const totalSpentInGroup = categoriesInGroup.reduce((sum, item) => sum + item.totalSpent, 0);

  return (
    <Box>
      <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1, mb: 2 }}>
        {macroCategories.map(category => (
          <Chip
            key={category}
            clickable
            label={category.charAt(0) + category.slice(1).toLowerCase()}
            onClick={() => handleChipClick(category)}
            sx={{
              fontWeight: 600,
              bgcolor: category === selectedMacroCategory ? (colors[category] || '#607d8b') : 'transparent',
              color: category === selectedMacroCategory ? '#fff' : 'text.primary',
              border: `1px solid ${colors[category] || '#607d8b'}`,
            }}
          />
        ))}
      </Box>

      <Box sx={{ width: '100%', height: 280, mb: 3 }}>
        <ResponsiveContainer width="100%" height="100%">
          <LineChart data={activeChartData} margin={{ top: 10, right: 30, left: 0, bottom: 10 }}>
            <CartesianGrid strokeDasharray="3 3" stroke={isDark ? '#444' : '#ccc'} />
            <XAxis
              dataKey="month"
              tick={{ fontSize: 12, fill: theme.palette.text.primary }}
              stroke={theme.palette.text.secondary}
            />
            <YAxis
              tick={{ fontSize: 12, fill: theme.palette.text.secondary }}
              tickFormatter={(value) => `€${value.toLocaleString()}`}
              stroke={theme.palette.text.secondary}
            />
            <Tooltip
              formatter={(value) => `€${Number(value).toLocaleString()}`}
              contentStyle={{
                backgroundColor: isDark ? '#2c2c2c' : '#ffffff',
                border: `1px solid ${isDark ? '#444' : '#ccc'}`,
                borderRadius: '8px',
                color: isDark ? '#ffffff' : '#000000'
              }}
            />
            <Legend verticalAlign="top" height={36} wrapperStyle={{ color: theme.palette.text.primary }} />
            <Line
              type="monotone"
              dataKey="actual"
              stroke={activeColor}
              strokeWidth={3}
              dot={{ r: 4 }}
              name={activeLabel}
            />
            <Line
              type="monotone"
              dataKey="budget"
              stroke={theme.palette.text.secondary}
              strokeWidth={2}
              strokeDasharray="5 5"
              dot={false}
              name={selectedMacroCategory ? 'Budget' : 'Total Budget'}
            />
          </LineChart>
        </ResponsiveContainer>
      </Box>

      {selectedMacroCategory && (
        <>
          <Typography variant="subtitle2" fontWeight={700} color="text.secondary" mb={1}>
            By Category — {selectedMacroCategory.charAt(0) + selectedMacroCategory.slice(1).toLowerCase()}
          </Typography>
          {categoriesInGroup.length > 0 ? (
            <Box sx={{ display: 'flex', flexDirection: 'column' }}>
              {categoriesInGroup.map((item, index) => {
                const share = totalSpentInGroup > 0 ? (item.totalSpent / totalSpentInGroup) * 100 : 0;
                const difference = (item.budgetedAmount || 0) - item.totalSpent;
                const isUnderBudget = difference > 0;
                const color = CATEGORY_LIST_COLORS[index % CATEGORY_LIST_COLORS.length];

                return (
                  <Box key={item.categoryName} sx={{ py: 1.25, borderBottom: '1px solid', borderColor: 'grey.200' }}>
                    <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', gap: 1 }}>
                      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, minWidth: 0 }}>
                        <Typography variant="caption" color="text.secondary" sx={{ width: 20, flexShrink: 0 }}>
                          {index + 1}
                        </Typography>
                        <Box sx={{ width: 10, height: 10, borderRadius: '50%', bgcolor: color, flexShrink: 0 }} />
                        <Typography variant="body2" fontWeight={700} noWrap>
                          {item.categoryName}
                        </Typography>
                      </Box>
                      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, flexShrink: 0 }}>
                        {!!item.budgetedAmount && (
                          <Chip
                            icon={isUnderBudget ? <CheckCircle /> : <Warning />}
                            label={`€${Math.abs(difference).toLocaleString()} ${isUnderBudget ? 'left' : 'over'}`}
                            size="small"
                            sx={{
                              bgcolor: isUnderBudget ? '#4caf50' : '#f44336',
                              color: 'white',
                              fontWeight: 600,
                              height: 20,
                              '& .MuiChip-icon': { color: 'white', fontSize: '0.9rem' },
                              '& .MuiChip-label': { fontSize: '0.7rem', px: 1 }
                            }}
                          />
                        )}
                        <Typography variant="body2" fontWeight={700}>
                          €{item.totalSpent.toLocaleString()}
                        </Typography>
                      </Box>
                    </Box>
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mt: 0.5, pl: 3.5 }}>
                      <Box sx={{ flex: 1, height: 4, borderRadius: 2, bgcolor: 'grey.200', overflow: 'hidden' }}>
                        <Box sx={{ width: `${Math.min(100, share)}%`, height: '100%', bgcolor: color, opacity: 0.6 }} />
                      </Box>
                      <Typography variant="caption" color="text.secondary" sx={{ flexShrink: 0, minWidth: 70, textAlign: 'right' }}>
                        {share.toFixed(1)}% of group
                      </Typography>
                    </Box>
                  </Box>
                );
              })}
            </Box>
          ) : (
            <Typography variant="body2" color="text.secondary" sx={{ py: 2, textAlign: 'center' }}>
              No categories with spending in this group
            </Typography>
          )}
        </>
      )}
    </Box>
  );
}
