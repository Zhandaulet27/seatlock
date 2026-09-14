import { createContext, useCallback, useContext, useState, type ReactNode } from 'react'
import { apiFetch, clearToken, getToken, setToken } from '../api/client'
import type { AuthResponse, LoginRequest, RegisterRequest } from '../api/types'

// The backend hands back only a JWT (AuthResponse { token }), not a user
// object - JwtService.generateToken puts the email in the "sub" claim and
// the numeric id in a custom "uid" claim, so decoding the token client-side
// is enough to know who's logged in without a separate lookup call. This is
// a plain base64url decode of the payload, not a signature check - the
// frontend trusts the token because it just received it from /auth/login
// (or read it back from localStorage where only this app wrote it); the
// backend is the one that verifies the signature on every request.
interface DecodedToken {
  sub: string
  uid: number
  exp: number
}

function decodeToken(token: string): DecodedToken | null {
  try {
    const payload = token.split('.')[1]
    const base64 = payload.replace(/-/g, '+').replace(/_/g, '/')
    const json = decodeURIComponent(
      atob(base64)
        .split('')
        .map((c) => '%' + c.charCodeAt(0).toString(16).padStart(2, '0'))
        .join(''),
    )
    return JSON.parse(json) as DecodedToken
  } catch {
    return null
  }
}

interface CurrentUser {
  email: string
  userId: number
}

function readValidUser(): CurrentUser | null {
  const token = getToken()
  if (!token) return null

  const decoded = decodeToken(token)
  if (!decoded) return null

  if (decoded.exp * 1000 < Date.now()) {
    // Stale token left over from a previous session (default expiry is 24h,
    // see application.yml's jwt.expiration-ms) - drop it instead of sending
    // requests that the backend will reject anyway.
    clearToken()
    return null
  }

  return { email: decoded.sub, userId: decoded.uid }
}

interface AuthContextValue {
  isAuthenticated: boolean
  email: string | null
  login: (email: string, password: string) => Promise<void>
  register: (email: string, password: string) => Promise<void>
  logout: () => void
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined)

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<CurrentUser | null>(() => readValidUser())

  const login = useCallback(async (email: string, password: string) => {
    const response = await apiFetch<AuthResponse>('/api/v1/auth/login', {
      method: 'POST',
      body: { email, password } satisfies LoginRequest,
    })
    setToken(response.token)
    setUser(readValidUser())
  }, [])

  const register = useCallback(
    async (email: string, password: string) => {
      // POST /api/v1/auth/register returns a UserResponse, not a token -
      // logging in right after is what makes "create an account" feel like
      // one step instead of two.
      await apiFetch('/api/v1/auth/register', {
        method: 'POST',
        body: { email, password } satisfies RegisterRequest,
      })
      await login(email, password)
    },
    [login],
  )

  const logout = useCallback(() => {
    clearToken()
    setUser(null)
  }, [])

  const value: AuthContextValue = {
    isAuthenticated: user !== null,
    email: user?.email ?? null,
    login,
    register,
    logout,
  }

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth(): AuthContextValue {
  const ctx = useContext(AuthContext)
  if (!ctx) {
    throw new Error('useAuth must be used within an AuthProvider')
  }
  return ctx
}
