export interface LoginResponse {
  token: string;
  userId: string;
  userType: "A" | "U";
  firstName: string;
  lastName: string;
}

export interface AccountView {
  acctId: number;
  activeStatus: string;
  currentBalance: number;
  creditLimit: number;
  cashCreditLimit: number;
  openDate: string | null;
  expirationDate: string | null;
  reissueDate: string | null;
  currCycCredit: number;
  currCycDebit: number;
  groupId: string | null;
  cardNum: string;
  customer: {
    custId: number;
    firstName: string;
    middleName: string;
    lastName: string;
    addrLine1: string;
    addrLine2: string;
    city: string;
    stateCd: string;
    countryCd: string;
    zip: string;
    phone1: string;
    phone2: string;
    ssn: string;
    govtIssuedId: string;
    dob: string;
    eftAccountId: string;
    priCardHolderInd: string;
    ficoCreditScore: number;
  };
}

const TOKEN_KEY = "carddemo.token";

export const tokenStore = {
  get(): string | null { return localStorage.getItem(TOKEN_KEY); },
  set(t: string) { localStorage.setItem(TOKEN_KEY, t); },
  clear() { localStorage.removeItem(TOKEN_KEY); }
};

async function http<T>(path: string, init: RequestInit = {}): Promise<T> {
  const headers: Record<string, string> = {
    "Content-Type": "application/json",
    ...(init.headers as Record<string, string> ?? {})
  };
  const tok = tokenStore.get();
  if (tok) headers["Authorization"] = `Bearer ${tok}`;
  const res = await fetch(path, { ...init, headers });
  const text = await res.text();
  const body = text ? JSON.parse(text) : null;
  if (!res.ok) {
    const msg = body?.message ?? `HTTP ${res.status}`;
    throw new ApiError(msg, res.status);
  }
  return body as T;
}

export class ApiError extends Error {
  status: number;
  constructor(message: string, status: number) { super(message); this.status = status; }
}

export const api = {
  login: (userId: string, password: string) =>
    http<LoginResponse>("/api/auth/login", {
      method: "POST",
      body: JSON.stringify({ userId, password })
    }),
  getAccount: (acctId: number) =>
    http<AccountView>(`/api/accounts/${acctId}`),
  runInterestCalc: (date: string) =>
    http<{ readCount: number; writeCount: number; status: string; exitCode: string }>(
      `/api/batch/interest-calc?date=${encodeURIComponent(date)}`,
      { method: "POST" })
};

export function formatMoney(n: number | null | undefined): string {
  if (n == null) return "";
  return n.toLocaleString("en-US", { style: "currency", currency: "USD" });
}
