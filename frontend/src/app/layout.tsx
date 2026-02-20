'use client';

import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { ThemeProvider, createTheme } from '@mui/material';
import CssBaseline from '@mui/material/CssBaseline';
import { LocalizationProvider } from '@mui/x-date-pickers';
import { AdapterDateFns } from '@mui/x-date-pickers/AdapterDateFns';
import { Geist, Geist_Mono } from 'next/font/google';
import { AuthProvider } from '../contexts/AuthContext';
import { ThemeContextProvider, useThemeContext } from '../contexts/ThemeContext';
import './globals.css';
import { useMemo } from 'react';

const geistSans = Geist({
  subsets: ['latin'],
  variable: '--font-geist-sans',
});

const geistMono = Geist_Mono({
  subsets: ['latin'],
  variable: '--font-geist-mono',
});

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
        },
      }),
    [mode]
  );

  return <ThemeProvider theme={theme}>{children}</ThemeProvider>;
}

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html lang="en">
      <body className={`${geistSans.variable} ${geistMono.variable} antialiased`}>
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
      </body>
    </html>
  );
}
