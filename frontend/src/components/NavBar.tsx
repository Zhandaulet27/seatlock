import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'

export function NavBar() {
  const { isAuthenticated, email, logout } = useAuth()
  const navigate = useNavigate()

  function handleLogout() {
    logout()
    navigate('/login')
  }

  return (
    <nav className="navbar">
      <Link to="/events" className="brand">
        SeatLock
      </Link>
      <div className="nav-links">
        <Link to="/events">Events</Link>
        {isAuthenticated && <Link to="/my-bookings">My Bookings</Link>}
        {isAuthenticated ? (
          <>
            <span className="nav-user">{email}</span>
            <button type="button" onClick={handleLogout} className="link-button">
              Log out
            </button>
          </>
        ) : (
          <>
            <Link to="/login">Log in</Link>
            <Link to="/register">Register</Link>
          </>
        )}
      </div>
    </nav>
  )
}
