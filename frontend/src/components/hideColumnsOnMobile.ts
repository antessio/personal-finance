import type { SxProps, Theme } from '@mui/material';

// Hides the given table columns (1-based) below the `sm` breakpoint.
// Put the result on the TableContainer; it targets header and body cells by position.
export const hideColumnsOnMobile = (columns: number[]): SxProps<Theme> => ({
  [columns.flatMap((c) => [`& th:nth-of-type(${c})`, `& td:nth-of-type(${c})`]).join(', ')]: {
    display: { xs: 'none', sm: 'table-cell' },
  },
});
