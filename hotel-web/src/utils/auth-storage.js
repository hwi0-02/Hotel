const isBrowser = typeof window !== 'undefined';
const USER_KEY = 'auth.user';

const getStorage = () => {
  if (!isBrowser) return null;
  try {
    return window.sessionStorage;
  } catch (error) {
    console.warn('Session storage unavailable', error);
    return null;
  }
};

export const setAuthUser = (user) => {
  const storage = getStorage();
  if (!storage) return;
  if (user) {
    try {
      storage.setItem(USER_KEY, JSON.stringify(user));
    } catch (error) {
      console.error('Failed to persist auth user', error);
    }
  } else {
    storage.removeItem(USER_KEY);
  }
};

export const getAuthUser = () => {
  const storage = getStorage();
  if (!storage) return null;
  const raw = storage.getItem(USER_KEY);
  if (!raw) return null;
  try {
    return JSON.parse(raw);
  } catch (error) {
    console.warn('Failed to parse auth user', error);
    storage.removeItem(USER_KEY);
    return null;
  }
};

export const clearAuthUser = () => {
  const storage = getStorage();
  if (!storage) return;
  storage.removeItem(USER_KEY);
};

export const getAuthRole = () => {
  const user = getAuthUser();
  return user?.role || null;
};

export const getOwnerId = () => {
  const user = getAuthUser();
  if (!user) return null;
  if (user.ownerId) return String(user.ownerId);
  if (user.id) return String(user.id);
  return null;
};

export const notifyAuthChanged = () => {
  if (!isBrowser) return;
  window.dispatchEvent(new Event('authchange'));
  try {
    window.localStorage.setItem('auth.lastChange', String(Date.now()));
  } catch (error) {
    console.warn('Failed to broadcast authchange', error);
  }
};
