'use client';

import { Box, Paper, Typography, FormControl, InputLabel, Select, MenuItem } from '@mui/material';
import { useAuth } from '../../contexts/AuthContext';
import Layout from '../../components/Layout';
import { Timeline, BarChart as MuiBarChart } from '@mui/icons-material';
import { XAxis, YAxis, Tooltip, Legend, ResponsiveContainer, CartesianGrid, LineChart, Line, BarChart, Bar } from 'recharts';
import { useQuery } from '@tanstack/react-query';
import { service } from '../../services/api';
import { useState } from 'react';
import { MonthlyData } from '@/types';

export default function AnalyticsPage() {
  const { user } = useAuth();
  const currentYear = new Date().getFullYear();
  const [selectedYear, setSelectedYear] = useState<number>(currentYear);
  const [selectedMonth, setSelectedMonth] = useState<number | undefined>(undefined); // Empty string means show all year data

  // Year options for the selector (current year and 4 years back)
  const yearOptions = [];
  for (let year = currentYear; year >= currentYear - 4; year--) {
    yearOptions.push({ value: year, label: year.toString() });
  }

  // Month options for the selector
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
  const months = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];
  const { data: monthlyData = [] } = useQuery({
    queryKey: ['monthlyData', selectedYear, selectedMonth],
    queryFn: () => service.getMonthlyData(selectedYear, selectedMonth)
  });

  const monthTransactions = selectedMonth
    ? (() => {
      // For monthly view, show weeks or days of the selected month
      const daysInMonth = new Date(selectedYear, selectedMonth, 0).getDate();
      const monthData = monthlyData.filter((tx: MonthlyData) => {

        return Number(tx.year) === selectedYear && Number(tx.month) === selectedMonth;
      });
      const weekData = [];
      for (let week = 1; week <= Math.ceil(daysInMonth / 7); week++) {
        const wd = monthData.find(m => m.week === week);

        weekData.push({
          month: `Week ${week}`,
          Income: wd ? wd.totalIncome / 4 : 0,
          Expense: wd ? wd.totalExpenses / 4 : 0,
          Savings: wd ? wd.totalSavings / 4 : 0,
        });
      }
      return weekData;
    })()
    : months.map((month, index) => {
      const monthData = monthlyData.find((tx: MonthlyData) => {

        return Number(tx.year) === selectedYear && Number(tx.month) === index + 1;
      });
      return {
        month,
        Income: monthData ? monthData.totalIncome : 0,
        Expense: monthData ? monthData.totalExpenses : 0,
        Savings: monthData ? monthData.totalSavings : 0,
      };
    });


  // Fetch individual category trends data
  const { data: categoryTrendsData = [] } = useQuery({
    queryKey: ['categoryTrendsData', selectedYear, selectedMonth],
    queryFn: () => service.getCategoryTrendsData(selectedYear, selectedMonth)
  });


  // Transform trends data for line chart
  const trendsChartData = (() => {
    if (!categoryTrendsData || categoryTrendsData.length === 0) return [];

    if (selectedMonth !== undefined) {
      // Monthly view - show weekly trends
      const weeks = [...new Set(categoryTrendsData.map(item => item.week))].sort();
      return weeks.map(week => {
        const weekData: any = { period: `Week ${week}` };
        const weekItems = categoryTrendsData.filter(item => item.week === week);

        weekItems.forEach(item => {
          weekData[item.categoryName] = item.total;
        });

        return weekData;
      });
    } else {
      // Yearly view - show monthly trends
      const months = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];
      return months.map((monthName, index) => {
        const monthData: any = { period: monthName };
        const monthItems = categoryTrendsData.filter(item => item.month === index + 1);

        monthItems.forEach(item => {
          monthData[item.categoryName] = item.total;
        });

        return monthData;
      });
    }
  })();

  // Get unique categories for trends chart
  const uniqueCategories = [...new Set(categoryTrendsData.map(item => item.categoryName))];

  // Generate colors for categories
  const generateCategoryColors = (categories: string[]) => {
    const colors = [
      '#f44336', '#e91e63', '#9c27b0', '#673ab7', '#3f51b5', '#2196f3', '#03a9f4', '#00bcd4',
      '#009688', '#4caf50', '#8bc34a', '#cddc39', '#ffeb3b', '#ffc107', '#ff9800', '#ff5722',
      '#795548', '#607d8b', '#e57373', '#f06292', '#ba68c8', '#9575cd', '#7986cb', '#64b5f6',
      '#4fc3f7', '#4dd0e1', '#4db6ac', '#81c784', '#aed581', '#dce775', '#fff176', '#ffb74d',
      '#ff8a65', '#a1887f', '#90a4ae', '#ef5350', '#ec407a', '#ab47bc', '#7e57c2', '#5c6bc0',
      '#42a5f5', '#29b6f6', '#26c6da', '#26a69a', '#66bb6a', '#9ccc65', '#d4e157', '#ffee58',
      '#ffca28', '#ffa726', '#ff7043', '#8d6e63', '#78909c', '#b71c1c', '#880e4f', '#4a148c',
      '#311b92', '#1a237e', '#0d47a1', '#01579b', '#006064', '#004d40', '#1b5e20', '#33691e',
      '#827717', '#f57f17', '#ff6f00', '#e65100', '#bf360c', '#3e2723', '#263238', '#c62828',
      '#ad1457', '#6a1b9a', '#4527a0', '#283593', '#1565c0', '#0277bd', '#00838f', '#00695c',
      '#2e7d32', '#558b2f', '#9e9d24', '#f9a825', '#ff8f00', '#ef6c00', '#d84315', '#5d4037'
    ];
    const categoryColors: { [key: string]: string } = {};
    categories.forEach((category, index) => {
      categoryColors[category] = colors[index % colors.length];
    });
    return categoryColors;
  };

  const categoryColors = generateCategoryColors(uniqueCategories);



  return (
    <Layout>
      <Box sx={{ bgcolor: '#f5f6fa', minHeight: '100vh', p: { xs: 1, md: 4 } }}>
        {/* Year and Month Selectors */}
        <Box sx={{ mb: 3, display: 'flex', justifyContent: 'flex-end', gap: 2 }}>
          <FormControl sx={{ minWidth: 120 }}>
            <InputLabel id="year-selector-label">Year</InputLabel>
            <Select
              labelId="year-selector-label"
              value={selectedYear}
              label="Year"
              onChange={(e) => setSelectedYear(e.target.value as number)}
              sx={{ bgcolor: 'white' }}
            >
              {yearOptions.map((option) => (
                <MenuItem key={option.value} value={option.value}>
                  {option.label}
                </MenuItem>
              ))}
            </Select>
          </FormControl>
          <FormControl sx={{ minWidth: 200 }}>
            <InputLabel id="month-selector-label">Filter by Month</InputLabel>
            <Select
              labelId="month-selector-label"
              value={selectedMonth}
              label="Filter by Month"
              onChange={(e) => setSelectedMonth(e.target.value as number | undefined)}
              sx={{ bgcolor: 'white' }}
            >
              {monthOptions.map((option) => (
                <MenuItem key={option.value} value={option.value}>
                  {option.label}
                </MenuItem>
              ))}
            </Select>
          </FormControl>
        </Box>
        {/* Global Money Flow Card */}
        <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 3, mb: 3 }}>
          {/* Money Flow Card */}
          <Paper elevation={4} sx={{ flex: 1, minWidth: 400, p: 3, borderRadius: 4, boxShadow: '0 4px 24px #b2dfdb33', display: 'flex', flexDirection: 'column', justifyContent: 'center', background: 'linear-gradient(135deg, #e8f5e9 0%, #ffffff 100%)' }}>
            <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
              <MuiBarChart color="success" sx={{ mr: 1 }} />
              <Typography color="success.dark" fontWeight={700} variant="subtitle1">
                Money Flow {selectedMonth ? `- ${monthOptions.find(m => m.value === selectedMonth)?.label} ${selectedYear}` : `- ${selectedYear}`}
              </Typography>
            </Box>

            <Box sx={{ width: '100%', height: 220, mb: 2 }}>
              <ResponsiveContainer width="100%" height="100%">
                <BarChart data={monthTransactions} margin={{ top: 10, right: 20, left: 0, bottom: 10 }}>
                  <CartesianGrid strokeDasharray="3 3" />
                  <XAxis dataKey="month" tick={{ fontSize: 12 }} />
                  <YAxis tick={{ fontSize: 12 }} />
                  <Tooltip formatter={(value) => `€${value.toLocaleString()}`} />
                  <Legend verticalAlign="top" height={36} />
                  <Bar dataKey="Income" fill="#43a047" radius={[6, 6, 0, 0]} barSize={18} name="Income" />
                  <Bar dataKey="Expense" fill="#e53935" radius={[6, 6, 0, 0]} barSize={18} name="Expense" />
                  <Bar dataKey="Savings" fill="#3541e5ff" radius={[6, 6, 0, 0]} barSize={18} name="Savings" />
                </BarChart>
              </ResponsiveContainer>
            </Box>
          </Paper>
        </Box>


        {/* Category Trends Over Time */}
        <Paper elevation={4} sx={{ p: 3, borderRadius: 4, boxShadow: '0 4px 24px #b2dfdb33', mb: 3, background: 'linear-gradient(135deg, #fff3e0 0%, #ffffff 100%)' }}>
          <Box sx={{ display: 'flex', alignItems: 'center', mb: 3 }}>
            <Timeline color="warning" sx={{ mr: 1 }} />
            <Typography color="warning.dark" fontWeight={700} variant="h6">
              Category Trends Over Time - {selectedMonth ? `${monthOptions.find(m => m.value === selectedMonth)?.label} ${selectedYear}` : `${selectedYear}`}
            </Typography>
          </Box>
          <Box sx={{ width: '100%', height: 400, mb: 2 }}>
            <ResponsiveContainer width="100%" height="100%">
              <LineChart data={trendsChartData} margin={{ top: 20, right: 30, left: 20, bottom: 60 }}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis
                  dataKey="period"
                  angle={-45}
                  textAnchor="end"
                  height={80}
                  interval={0}
                  fontSize={12}
                />
                <YAxis
                  tickFormatter={(value) => `$${value.toLocaleString()}`}
                  width={80}
                />
                <Tooltip
                  formatter={(value: number, name: string) => [`$${value.toLocaleString()}`, name]}
                  labelFormatter={(label) => `Period: ${label}`}
                />
                <Legend />
                {uniqueCategories.map((category) => (
                  <Line
                    key={category}
                    type="monotone"
                    dataKey={category}
                    stroke={categoryColors[category] || '#666666'}
                    strokeWidth={3}
                    dot={{ r: 6 }}
                    name={category}
                  />
                ))}
              </LineChart>
            </ResponsiveContainer>
          </Box>
          <Typography variant="caption" color="text.secondary" sx={{ mt: 1 }}>
            Track spending trends across different categories over time
          </Typography>
        </Paper>
      </Box>


    </Layout>
  );
}
