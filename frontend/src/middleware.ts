import { NextResponse } from 'next/server';
import type { NextRequest } from 'next/server';

export function middleware(request: NextRequest) {
  // In middleware, we can't access sessionStorage directly
  // Instead, we'll use a cookie to store the auth state
  const isAuthenticated = request.cookies.has('auth-token');
  const isAuthPage = request.nextUrl.pathname.startsWith('/login') || 
                    request.nextUrl.pathname.startsWith('/signup');

  // If user is not authenticated and trying to access a protected route
  if (!isAuthenticated && !isAuthPage) {
    return NextResponse.redirect(new URL('/login', request.url));
  }

  // If user is authenticated and trying to access auth pages
  if (isAuthenticated && isAuthPage) {
    return NextResponse.redirect(new URL('/', request.url));
  }

  return NextResponse.next();
}

// This config ensures the middleware runs on all routes except static files
export const config = {
  matcher: [
    /*
     * Match all request paths except for the ones starting with:
     * - api (API routes)
     * - _next/static (static files)
     * - _next/image (image optimization files)
     * - favicon.ico (favicon file)
     */
    '/((?!api|_next/static|_next/image|favicon.ico).*)',
  ],
}; 