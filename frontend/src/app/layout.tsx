'use client';

import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { ThemeProvider, createTheme } from '@mui/material';
import CssBaseline from '@mui/material/CssBaseline';
import { LocalizationProvider } from '@mui/x-date-pickers';
import { AdapterDateFns } from '@mui/x-date-pickers/AdapterDateFns';
import { Geist, Geist_Mono } from 'next/font/google';
import { AuthProvider } from '../contexts/AuthContext';
import './globals.css';

const geistSans = Geist({
  subsets: ['latin'],
  variable: '--font-geist-sans',
});

const geistMono = Geist_Mono({
  subsets: ['latin'],
  variable: '--font-geist-mono',
});

const queryClient = new QueryClient();

const theme = createTheme({
  palette: {
    mode: 'light',
  },
});

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html lang="en">
      <body className={`${geistSans.variable} ${geistMono.variable} antialiased`}>
        <QueryClientProvider client={queryClient}>
          <ThemeProvider theme={theme}>
            <LocalizationProvider dateAdapter={AdapterDateFns}>
              <AuthProvider>
                <CssBaseline />
                {children}
              </AuthProvider>
            </LocalizationProvider>
          </ThemeProvider>
        </QueryClientProvider>
      </body>
    </html>
  );
}
