// Thin fetch wrapper. Paths are always relative ("/api/v1/...") rather than
// an absolute URL: in dev, Vite's proxy (vite.config.ts) forwards them to
// the backend; in prod, Nginx serves this app and proxies the same paths to
// the backend on the same origin (see ../nginx/nginx.conf). Either way the
// browser never makes a cross-origin request, so there's nothing to add a
// VITE_API_BASE_URL env var or backend CORS config for.

const TOKEN_KEY = 'seatlock_token'

export function getToken(): string | null {
  return localStorage.getItem(TOKEN_KEY)
}

export function setToken(token: string): void {
  localStorage.setItem(TOKEN_KEY, token)
}

export function clearToken(): void {
  localStorage.removeItem(TOKEN_KEY)
}

// Backend errors are RFC 7807 ProblemDetail bodies (see
// GlobalExceptionHandler + SecurityConfig's 401/403 handlers) - all of them
// carry a human-readable "detail" field, so that's what gets surfaced.
export class ApiError extends Error {
  status: number

  constructor(status: number, message: string) {
    super(message)
    this.name = 'ApiError'
    this.status = status
  }
}

interface RequestOptions {
  method?: 'GET' | 'POST' | 'DELETE' | 'PUT'
  body?: unknown
  // Attaches "Authorization: Bearer <token>" from localStorage. Left off
  // for the genuinely public endpoints (auth, events GET/POST) so a missing
  // token there is never mistaken for an auth bug.
  auth?: boolean
}

export async function apiFetch<T>(path: string, options: RequestOptions = {}): Promise<T> {
  const { method = 'GET', body, auth = false } = options
  const headers: Record<string, string> = {}

  if (body !== undefined) {
    headers['Content-Type'] = 'application/json'
  }
  if (auth) {
    const token = getToken()
    if (token) {
      headers.Authorization = `Bearer ${token}`
    }
  }

  const response = await fetch(path, {
    method,
    headers,
    body: body !== undefined ? JSON.stringify(body) : undefined,
  })

  // DELETE /api/v1/bookings/{id} returns 204 with no body.
  if (response.status === 204) {
    return undefined as T
  }

  const text = await response.text()
  const data: unknown = text ? JSON.parse(text) : null

  if (!response.ok) {
    const problem = data as { detail?: string; error?: string } | null
    const message = problem?.detail ?? problem?.error ?? response.statusText ?? 'Request failed'
    throw new ApiError(response.status, message)
  }

  return data as T
}
