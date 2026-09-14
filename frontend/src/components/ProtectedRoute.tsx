import type { ReactNode } from 'react'
import { Navigate, useLocation } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'

// Wraps a route element that requires a logged-in user (only /my-bookings
// right now - BookingController.listMine and everything else under
// /api/v1/bookings needs a Bearer token per SecurityConfig). Redirects to
// /login and remembers where the user was headed so LoginPage can send them
// straight back after a successful login.
export function ProtectedRoute({ children }: { children: ReactNode }) {
  const { isAuthenticated } = useAuth()
  const location = useLocation()

  if (!isAuthenticated) {
    return <Navigate to="/login" state={{ from: { pathname: location.pathname } }} replace />
  }

  return <>{children}</>
}
