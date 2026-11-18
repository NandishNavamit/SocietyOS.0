import { createContext, useContext, useEffect, useState, type ReactNode } from 'react'
import { accessTokenKey, api, clearStoredSession, refreshStoredSession, refreshTokenKey, sessionExpiredEvent, storedAccessToken } from '../api'

// Legacy values remain in this client type only while retired components are
// removed in later phases. The API returns only the four current product roles.
export type Role = 'ADMIN' | 'SOCIETY_ADMIN' | 'FLAT_OWNER' | 'LOGBOOK_OPERATOR' | 'SOCIETY_MANAGER' | 'FINANCE_MANAGER' | 'SECURITY_MANAGER' | 'BLOCK_MANAGER' | 'RESIDENT' | 'STAFF' | 'SECURITY'
export type CurrentUser = { id: number; fullName: string; email: string; phoneNumber: string; active: boolean; joinedAt: string; roles: Role[] }
type Tokens = { accessToken: string; refreshToken: string }
type Auth = { user: CurrentUser | null; loading: boolean; login: (email: string, password: string) => Promise<void>; logout: () => Promise<void>; refresh: () => Promise<void> }
const AuthContext = createContext<Auth | null>(null)

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<CurrentUser | null>(null)
  const [loading, setLoading] = useState(true)
  const getMe = async () => { setUser(await api<CurrentUser>('/api/auth/me', {}, storedAccessToken())) }
  const refresh = async () => { const token = await refreshStoredSession(); if (!token) throw new Error('No saved session'); await getMe() }
  const login = async (email: string, password: string) => {
    const tokens = await api<Tokens>('/api/auth/login', { method: 'POST', body: JSON.stringify({ email, password }) })
    localStorage.setItem(accessTokenKey, tokens.accessToken)
    localStorage.setItem(refreshTokenKey, tokens.refreshToken)
    await getMe()
  }
  const logout = async () => {
    const refreshToken = localStorage.getItem(refreshTokenKey)
    try { if (refreshToken) await api('/api/auth/logout', { method: 'POST', body: JSON.stringify({ refreshToken }) }) }
    finally { clearStoredSession(); setUser(null) }
  }

  useEffect(() => {
    const initialise = async () => {
      try { await getMe() }
      catch { try { await refresh() } catch { clearStoredSession(); setUser(null) } }
      finally { setLoading(false) }
    }
    const expire = () => setUser(null)
    window.addEventListener(sessionExpiredEvent, expire)
    void initialise()
    return () => window.removeEventListener(sessionExpiredEvent, expire)
  }, [])

  return <AuthContext.Provider value={{ user, loading, login, logout, refresh }}>{children}</AuthContext.Provider>
}
export const useAuth = () => { const context = useContext(AuthContext); if (!context) throw new Error('AuthProvider is required'); return context }
