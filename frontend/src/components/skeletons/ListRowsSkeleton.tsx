'use client';

import { Box, Skeleton } from '@mui/material';

interface ListRowsSkeletonProps {
  rows?: number;
}

export default function ListRowsSkeleton({ rows = 5 }: ListRowsSkeletonProps) {
  return (
    <Box sx={{ display: 'flex', flexDirection: 'column' }}>
      {Array.from({ length: rows }).map((_, index) => (
        <Box key={index} sx={{ py: 1.25, borderBottom: '1px solid', borderColor: 'grey.200' }}>
          <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', gap: 1 }}>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, flex: 1, minWidth: 0 }}>
              <Skeleton variant="circular" width={10} height={10} sx={{ flexShrink: 0 }} />
              <Skeleton variant="text" width="40%" />
            </Box>
            <Skeleton variant="text" width={60} />
          </Box>
          <Box sx={{ mt: 0.5, pl: 2.5 }}>
            <Skeleton variant="rounded" height={4} width="90%" />
          </Box>
        </Box>
      ))}
    </Box>
  );
}
