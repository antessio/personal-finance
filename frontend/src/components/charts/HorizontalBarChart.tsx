'use client';

import { Box, Paper, Typography, useTheme } from '@mui/material';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer, Cell } from 'recharts';

interface BarChartData {
  name: string;
  value: number;
  budget?: number;
}

interface HorizontalBarChartProps {
  title: string;
  data: BarChartData[];
  color: string;
  showBudget?: boolean;
}

export default function HorizontalBarChart({ title, data, color, showBudget = false }: HorizontalBarChartProps) {
  const theme = useTheme();
  const isDark = theme.palette.mode === 'dark';

  return (
    <Paper elevation={4} sx={{ p: 3, borderRadius: 4, boxShadow: '0 4px 24px rgba(0,0,0,0.08)' }}>
      <Typography variant="h6" fontWeight={700} color="text.primary" mb={2}>
        {title}
      </Typography>
      <Box sx={{ width: '100%', height: 400 }}>
        <ResponsiveContainer width="100%" height="100%">
          <BarChart
            data={data}
            layout="vertical"
            margin={{ top: 5, right: 30, left: 100, bottom: 5 }}
          >
            <CartesianGrid strokeDasharray="3 3" stroke={isDark ? '#444' : '#ccc'} />
            <XAxis
              type="number"
              tickFormatter={(value) => `€${value.toLocaleString()}`}
              stroke={theme.palette.text.secondary}
              tick={{ fill: theme.palette.text.secondary }}
            />
            <YAxis
              type="category"
              dataKey="name"
              width={90}
              tick={{ fontSize: 12, fill: theme.palette.text.primary }}
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
            {showBudget && <Legend wrapperStyle={{ color: theme.palette.text.primary }} />}
            <Bar dataKey="value" fill={color} radius={[0, 4, 4, 0]} name="Actual">
              {data.map((entry, index) => (
                <Cell key={`cell-${index}`} fill={color} />
              ))}
            </Bar>
            {showBudget && (
              <Bar dataKey="budget" fill="#90caf9" radius={[0, 4, 4, 0]} name="Budget" />
            )}
          </BarChart>
        </ResponsiveContainer>
      </Box>
    </Paper>
  );
}
