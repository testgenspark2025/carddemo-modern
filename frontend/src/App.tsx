import { Navigate, Route, Routes } from "react-router-dom";
import { AuthProvider, useAuth } from "./lib/auth";
import { LoginPage } from "./pages/LoginPage";
import { AccountViewPage } from "./pages/AccountViewPage";
import { ReactNode } from "react";

function RequireAuth({ children }: { children: ReactNode }) {
  const { userId } = useAuth();
  if (!userId) return <Navigate to="/" replace />;
  return <>{children}</>;
}

export function App() {
  return (
    <AuthProvider>
      <Routes>
        <Route path="/" element={<LoginPage />} />
        <Route path="/accounts" element={
          <RequireAuth><AccountViewPage /></RequireAuth>
        } />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </AuthProvider>
  );
}
