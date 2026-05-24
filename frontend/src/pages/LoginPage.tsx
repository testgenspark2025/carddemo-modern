import { FormEvent, useState } from "react";
import { useNavigate } from "react-router-dom";
import { api, ApiError } from "../lib/api";
import { useAuth } from "../lib/auth";

const CARD_ART = `+========================================+
|%%%%%%%  NATIONAL RESERVE NOTE  %%%%%%%%|
|%(1)  THE UNITED STATES OF KICSLAND (1)%|
|%$$              ___       ********  $$%|
|%$    {x}       (o o)                 $%|
|%$     ******  (  V  )      O N E     $%|
|%(1)          ---m-m---             (1)%|
|%%~~~~~~~~~~~ ONE DOLLAR ~~~~~~~~~~~~~%%|
+========================================+`;

export function LoginPage() {
  const auth = useAuth();
  const nav = useNavigate();
  const [userId, setUserId] = useState("");
  const [password, setPassword] = useState("");
  const [err, setErr] = useState("");
  const [busy, setBusy] = useState(false);

  async function submit(e: FormEvent) {
    e.preventDefault();
    setErr("");
    setBusy(true);
    try {
      const res = await api.login(userId, password);
      auth.setSession(res);
      nav("/accounts");
    } catch (e) {
      setErr(e instanceof ApiError ? e.message : String(e));
    } finally {
      setBusy(false);
    }
  }

  const now = new Date();
  const date = now.toLocaleDateString("en-US",
    { month: "2-digit", day: "2-digit", year: "2-digit" });
  const time = now.toLocaleTimeString("en-US", { hour12: false });

  return (
    <div className="term">
      <div className="term-header">
        <div>Tran : CC00</div>
        <div className="title">CardDemo - Mainframe Modernization</div>
        <div>Date : {date}</div>
        <div>Prog : COSGN00C</div>
        <div className="title">Sign-on Screen</div>
        <div>Time : {time}</div>
      </div>

      <div className="term-banner">This is a Credit Card Demo Application for Mainframe Modernization</div>
      <div className="term-card">{CARD_ART}</div>

      <p style={{ color: "var(--label)", textAlign: "center" }}>
        Type your User ID and Password, then press ENTER:
      </p>

      <form onSubmit={submit}>
        <div className="form-row">
          <label htmlFor="userId">User ID     :</label>
          <input id="userId" maxLength={8} autoFocus
                 value={userId} onChange={e => setUserId(e.target.value.toUpperCase())} />
          <span className="hint">(8 Char)</span>
        </div>
        <div className="form-row">
          <label htmlFor="password">Password    :</label>
          <input id="password" type="password" maxLength={8}
                 value={password} onChange={e => setPassword(e.target.value)} />
          <span className="hint">(8 Char)</span>
        </div>

        <div className="err">{err}</div>

        <div className="hotkeys flex-row">
          <button className="term-btn" type="submit" disabled={busy}>
            {busy ? "..." : "ENTER=Sign-on"}
          </button>
          <span style={{ color: "var(--header)" }}>
            Try ADMIN001 / PASSWORD or USER0001 / PASSWORD
          </span>
        </div>
      </form>
    </div>
  );
}
