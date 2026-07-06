'use client';

import { Skeleton, TableCell, TableRow } from '@mui/material';

interface TableRowsSkeletonProps {
  columns: number;
  rows?: number;
}

export default function TableRowsSkeleton({ columns, rows = 5 }: TableRowsSkeletonProps) {
  return (
    <>
      {Array.from({ length: rows }).map((_, rowIndex) => (
        <TableRow key={rowIndex}>
          {Array.from({ length: columns }).map((_, columnIndex) => (
            <TableCell key={columnIndex}>
              <Skeleton variant="text" />
            </TableCell>
          ))}
        </TableRow>
      ))}
    </>
  );
}
