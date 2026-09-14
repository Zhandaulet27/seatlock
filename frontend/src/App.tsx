import { Navigate, Route, Routes } from 'react-router-dom'
import { NavBar } from './components/NavBar'
import { ProtectedRoute } from './components/ProtectedRoute'
import { EventDetailPage } from './pages/EventDetailPage'
import { EventsPage } from './pages/EventsPage'
import { LoginPage } from './pages/LoginPage'
import { MyBookingsPage } from './pages/MyBookingsPage'
import { RegisterPage } from './pages/RegisterPage'

export default function App() {
  return (
    <>
      <NavBar />
      <main>
        <Routes>
          <Route path="/" element={<Navigate to="/events" replace />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />
          <Route path="/events" element={<EventsPage />} />
          <Route path="/events/:id" element={<EventDetailPage />} />
          <Route
            path="/my-bookings"
            element={
              <ProtectedRoute>
                <MyBookingsPage />
              </ProtectedRoute>
            }
          />
          <Route path="*" element={<Navigate to="/events" replace />} />
        </Routes>
      </main>
    </>
  )
}
