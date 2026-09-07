const token = () => localStorage.getItem('access_token')

export async function api(path, options = {}) {
  const headers = { ...(options.headers || {}) }
  if (token()) headers.Authorization = `Bearer ${token()}`
  if (options.body && !headers['Content-Type']) headers['Content-Type'] = 'application/json'
  const response = await fetch(path, { ...options, headers })
  if (!response.ok) {
    let message = `Request failed (${response.status})`
    try { message = (await response.json()).message || message } catch { /* non-JSON error */ }
    throw new Error(message)
  }
  if (response.status === 204) return null
  return response.json()
}

export function hasRole(role) {
  const raw = localStorage.getItem('access_token')
  if (!raw) return false
  try { return JSON.parse(atob(raw.split('.')[1])).roles?.includes(`ROLE_${role}`) }
  catch { return false }
}
