import { createContext, useContext, useEffect, useMemo, useState, ReactNode } from "react";
import { tokenStore } from "./api";

interface AuthState {
  userId: string | null;
  userType: "A" | "U" | null;
  firstName: string | null;
  lastName: string | null;
  isAdmin: boolean;
  setSession(s: { token: string; userId: string; userType: "A" | "U"; firstName: string; lastName: string }): void;
  logout(): void;
}

const Ctx = createContext<AuthState | null>(null);
const SESSION_KEY = "carddemo.session";

interface PersistedSession {
  userId: string;
  userType: "A" | "U";
  firstName: string;
  lastName: string;
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [session, setSessionState] = useState<PersistedSession | null>(null);

  useEffect(() => {
    const raw = localStorage.getItem(SESSION_KEY);
    if (raw) setSessionState(JSON.parse(raw));
  }, []);

  const value = useMemo<AuthState>(() => ({
    userId: session?.userId ?? null,
    userType: session?.userType ?? null,
    firstName: session?.firstName ?? null,
    lastName: session?.lastName ?? null,
    isAdmin: session?.userType === "A",
    setSession(s) {
      tokenStore.set(s.token);
      const persisted: PersistedSession = {
        userId: s.userId, userType: s.userType,
        firstName: s.firstName, lastName: s.lastName
      };
      localStorage.setItem(SESSION_KEY, JSON.stringify(persisted));
      setSessionState(persisted);
    },
    logout() {
      tokenStore.clear();
      localStorage.removeItem(SESSION_KEY);
      setSessionState(null);
    }
  }), [session]);

  return <Ctx.Provider value={value}>{children}</Ctx.Provider>;
}

export function useAuth(): AuthState {
  const v = useContext(Ctx);
  if (!v) throw new Error("useAuth outside AuthProvider");
  return v;
}
