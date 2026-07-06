'use client';

import { Skeleton } from '@mui/material';

interface ChartSkeletonProps {
  height: number;
}

export default function ChartSkeleton({ height }: ChartSkeletonProps) {
  return <Skeleton variant="rounded" width="100%" height={height} />;
}
