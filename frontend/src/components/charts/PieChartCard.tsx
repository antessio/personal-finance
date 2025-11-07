'use client';

import { Box, Paper, Typography } from '@mui/material';
import { PieChart, Pie, Cell, ResponsiveContainer, Legend, Tooltip } from 'recharts';

interface PieChartData {
  name: string;
  value: number;
}

interface PieChartCardProps {
  title: string;
  data: PieChartData[];
  colors: string[];
}

export default function PieChartCard({ title, data, colors }: PieChartCardProps) {
  // Custom label to show only percentage for slices > 5%
  const renderLabel = (entry: any) => {
    const percent = (entry.percent * 100).toFixed(0);
    return parseFloat(percent) > 5 ? `${percent}%` : '';
  };

  return (
    <Paper elevation={4} sx={{ p: 3, borderRadius: 4, boxShadow: '0 4px 24px rgba(0,0,0,0.08)', height: '100%' }}>
      <Typography variant="h6" fontWeight={700} color="text.primary" mb={2} textAlign="center">
        {title}
      </Typography>
      <Box sx={{ width: '100%', height: 400 }}>
        <ResponsiveContainer width="100%" height="100%">
          <PieChart>
            <Pie
              data={data}
              cx="50%"
              cy="45%"
              labelLine={false}
              label={renderLabel}
              outerRadius={110}
              fill="#8884d8"
              dataKey="value"
            >
              {data.map((entry, index) => (
                <Cell key={`cell-${index}`} fill={colors[index % colors.length]} />
              ))}
            </Pie>
            <Tooltip
              formatter={(value: number, name: string) => [`€${value.toLocaleString()}`, name]}
              contentStyle={{ fontSize: '14px', borderRadius: '8px' }}
            />
            <Legend
              wrapperStyle={{ fontSize: '12px', paddingTop: '10px' }}
              iconType="circle"
              iconSize={8}
            />
          </PieChart>
        </ResponsiveContainer>
      </Box>
    </Paper>
  );
}
