'use client';

import { useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { useAuth } from '../contexts/AuthContext';
import { isAuthEnabled } from '../config/auth';

interface ProtectedRouteProps {
  children: React.ReactNode;
}

export default function ProtectedRoute({ children }: ProtectedRouteProps) {
  const router = useRouter();
  const { isAuthenticated, isLoading } = useAuth();

  useEffect(() => {
    // If auth is disabled, always allow access
    if (!isAuthEnabled()) {
      return;
    }

    // If auth is enabled and user is not authenticated, redirect to login
    if (!isLoading && !isAuthenticated) {
      router.push('/login');
    }
  }, [isAuthenticated, isLoading, router]);

  // If auth is disabled, always render children
  if (!isAuthEnabled()) {
    return <>{children}</>;
  }

  // If loading, show loading state
  if (isLoading) {
    return <div>Loading...</div>;
  }

  // If not authenticated, don't render anything (redirect will happen)
  if (!isAuthenticated) {
    return null;
  }

  // If authenticated, render children
  return <>{children}</>;
} 