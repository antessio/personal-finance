import type { SxProps, Theme } from '@mui/material';

// Row of filter controls (FormControl / TextField): wraps instead of overflowing,
// and on phones each control takes half the row.
export const filterBarSx: SxProps<Theme> = [
  { display: 'flex', flexWrap: 'wrap', gap: 2, mb: 2 },
  (theme: Theme) => ({
    [theme.breakpoints.down('sm')]: {
      gap: 1.5,
      '& > .MuiFormControl-root': {
        flex: '1 1 calc(50% - 6px)',
        minWidth: 0,
      },
    },
  }),
];
