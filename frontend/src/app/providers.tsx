'use client';

import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { ThemeProvider, createTheme } from '@mui/material';
import CssBaseline from '@mui/material/CssBaseline';
import { LocalizationProvider } from '@mui/x-date-pickers';
import { AdapterDateFns } from '@mui/x-date-pickers/AdapterDateFns';
import { AuthProvider } from '../contexts/AuthContext';
import { ThemeContextProvider, useThemeContext } from '../contexts/ThemeContext';
import { useMemo } from 'react';

const queryClient = new QueryClient();

function AppThemeProvider({ children }: { children: React.ReactNode }) {
  const { mode } = useThemeContext();

  const theme = useMemo(
    () =>
      createTheme({
        palette: {
          mode,
          ...(mode === 'light'
            ? {
                // Light mode colors
                primary: {
                  main: '#1976d2',
                },
                secondary: {
                  main: '#dc004e',
                },
                background: {
                  default: '#f5f5f5',
                  paper: '#ffffff',
                },
              }
            : {
                // Dark mode colors
                primary: {
                  main: '#90caf9',
                },
                secondary: {
                  main: '#f48fb1',
                },
                background: {
                  default: '#121212',
                  paper: '#1e1e1e',
                },
              }),
        },
        components: {
          MuiPaper: {
            styleOverrides: {
              root: {
                backgroundImage: 'none',
              },
            },
          },
          // Denser tables on phones: less cell padding, so more columns fit
          MuiTableCell: {
            styleOverrides: {
              root: ({ theme }) => ({
                [theme.breakpoints.down('sm')]: {
                  padding: '8px 6px',
                  fontSize: '0.8125rem',
                },
              }),
            },
          },
          // Dialogs go full-screen on phones instead of floating with side margins
          MuiDialog: {
            styleOverrides: {
              paper: ({ theme }) => ({
                [theme.breakpoints.down('sm')]: {
                  margin: 8,
                  width: 'calc(100% - 16px)',
                  maxWidth: 'calc(100% - 16px)',
                },
              }),
            },
          },
        },
      }),
    [mode]
  );

  return <ThemeProvider theme={theme}>{children}</ThemeProvider>;
}

export default function Providers({ children }: { children: React.ReactNode }) {
  return (
    <QueryClientProvider client={queryClient}>
      <ThemeContextProvider>
        <AppThemeProvider>
          <LocalizationProvider dateAdapter={AdapterDateFns}>
            <AuthProvider>
              <CssBaseline />
              {children}
            </AuthProvider>
          </LocalizationProvider>
        </AppThemeProvider>
      </ThemeContextProvider>
    </QueryClientProvider>
  );
}
