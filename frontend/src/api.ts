const API_URL = import.meta.env.VITE_API_URL ?? 'http://localhost:8082'

export const accessTokenKey = 'society.accessToken'
export const refreshTokenKey = 'society.refreshToken'
export const sessionExpiredEvent = 'society:session-expired'

type Tokens = { accessToken: string; refreshToken: string }

export class ApiError extends Error {
  constructor(public readonly status: number, message: string) { super(message) }
}

export const storedAccessToken = () => localStorage.getItem(accessTokenKey) ?? undefined
export const clearStoredSession = () => {
  localStorage.removeItem(accessTokenKey)
  localStorage.removeItem(refreshTokenKey)
}

export async function api<T>(path: string, options: RequestInit = {}, token?: string): Promise<T> {
  const headers = new Headers(options.headers)
  if (options.body) headers.set('Content-Type', 'application/json')
  if (token) headers.set('Authorization', `Bearer ${token}`)
  const response = await fetch(`${API_URL}${path}`, { ...options, headers })
  const text = await response.text()
  let data: unknown = null
  if (text) {
    try { data = JSON.parse(text) } catch { data = text }
  }
  if (!response.ok) {
    const message = typeof data === 'object' && data !== null && 'message' in data
      ? String(data.message) : 'Request failed'
    throw new ApiError(response.status, message)
  }
  return data as T
}

let refreshInFlight: Promise<string | undefined> | null = null

export async function refreshStoredSession(): Promise<string | undefined> {
  if (!refreshInFlight) {
    refreshInFlight = (async () => {
      const refreshToken = localStorage.getItem(refreshTokenKey)
      if (!refreshToken) return undefined
      const tokens = await api<Tokens>('/api/auth/refresh', { method: 'POST', body: JSON.stringify({ refreshToken }) })
      localStorage.setItem(accessTokenKey, tokens.accessToken)
      localStorage.setItem(refreshTokenKey, tokens.refreshToken)
      return tokens.accessToken
    })().finally(() => { refreshInFlight = null })
  }
  return refreshInFlight
}

/** Sends an authenticated request and renews an expired access token once. */
export async function authenticatedApi<T>(path: string, options: RequestInit = {}): Promise<T> {
  try {
    return await api<T>(path, options, storedAccessToken())
  } catch (error) {
    if (!(error instanceof ApiError) || error.status !== 401 || path.startsWith('/api/auth/')) throw error
    let newToken: string | undefined
    try {
      newToken = await refreshStoredSession()
      if (!newToken) throw new Error('No refresh token')
    } catch {
      clearStoredSession()
      window.dispatchEvent(new Event(sessionExpiredEvent))
      throw new ApiError(401, 'Your session has ended. Please sign in again.')
    }
    try {
      return await api<T>(path, options, newToken)
    } catch (retryError) {
      // A new access token was accepted by the refresh endpoint. Do not sign the
      // person out because one resource has an authorisation/configuration issue.
      if (retryError instanceof ApiError && retryError.status === 401) {
        throw new ApiError(401, 'This page rejected a valid session. Please refresh the page or contact an administrator.')
      }
      throw retryError
    }
  }
}
