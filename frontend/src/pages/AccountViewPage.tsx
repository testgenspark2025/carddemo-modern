import { FormEvent, useState } from "react";
import { api, AccountView, ApiError, formatMoney } from "../lib/api";
import { useAuth } from "../lib/auth";
import { useNavigate } from "react-router-dom";

function Money({ n }: { n: number }) {
  const cls = n >= 0 ? "amount-pos" : "amount-neg";
  return <span className={cls}>{formatMoney(n)}</span>;
}

export function AccountViewPage() {
  const auth = useAuth();
  const nav = useNavigate();
  const [acctId, setAcctId] = useState("");
  const [account, setAccount] = useState<AccountView | null>(null);
  const [err, setErr] = useState("");
  const [info, setInfo] = useState("Enter or update id of account to display");
  const [batchOut, setBatchOut] = useState<string>("");

  async function search(e: FormEvent) {
    e.preventDefault();
    setErr(""); setAccount(null);
    if (!acctId.trim()) {
      setErr("Account number not provided"); return;
    }
    const n = Number(acctId);
    if (!Number.isInteger(n) || n <= 0) {
      setErr("Account number must be a non zero 11 digit number"); return;
    }
    try {
      const a = await api.getAccount(n);
      setAccount(a);
      setInfo("Displaying details of given Account");
    } catch (e) {
      setErr(e instanceof ApiError ? e.message : String(e));
    }
  }

  async function runBatch() {
    setBatchOut("Running...");
    try {
      const today = new Date().toISOString().slice(0, 10);
      const r = await api.runInterestCalc(today);
      setBatchOut(`Job ${r.status} (${r.exitCode}) - read=${r.readCount}, wrote=${r.writeCount}`);
    } catch (e) {
      setBatchOut(e instanceof ApiError ? "Error: " + e.message : String(e));
    }
  }

  return (
    <div className="term">
      <div className="term-header">
        <div>Tran : CAVW</div>
        <div className="title">CardDemo - Account View (COACTVWC)</div>
        <div className="flex-row" style={{ justifyContent: "flex-end" }}>
          <span>{auth.firstName} {auth.lastName} ({auth.userType === "A" ? "ADMIN" : "USER"})</span>
          <button className="term-btn" onClick={() => { auth.logout(); nav("/"); }}>Logout</button>
        </div>
      </div>

      <form onSubmit={search} className="flex-row">
        <label style={{ color: "var(--label)" }}>Account Number:</label>
        <input maxLength={11} value={acctId}
               onChange={e => setAcctId(e.target.value.replace(/\D/g,""))}
               style={{ background: "var(--input-bg)", color: "var(--fg)",
                        border: "1px solid var(--fg-dim)", padding: "4px",
                        fontFamily: "inherit" }} />
        <button className="term-btn" type="submit">View</button>
        {auth.isAdmin && (
          <button className="term-btn" type="button" onClick={runBatch}>
            Run Interest Batch (CBACT04C)
          </button>
        )}
      </form>

      {batchOut && <div style={{ color: "var(--title)", marginTop: 6 }}>{batchOut}</div>}
      <div style={{ color: "var(--label)", marginTop: 6 }}>{info}</div>
      <div className="err">{err}</div>

      {account && (
        <>
          <div className="section-title">Account</div>
          <table className="kvtable">
            <tbody>
              <tr><td className="k">Account ID:</td><td className="v">{account.acctId}</td>
                  <td className="k">Active Status:</td><td className="v">{account.activeStatus}</td></tr>
              <tr><td className="k">Current Balance:</td><td className="v"><Money n={account.currentBalance}/></td>
                  <td className="k">Credit Limit:</td><td className="v"><Money n={account.creditLimit}/></td></tr>
              <tr><td className="k">Cash Limit:</td><td className="v"><Money n={account.cashCreditLimit}/></td>
                  <td className="k">Group ID:</td><td className="v">{account.groupId ?? ""}</td></tr>
              <tr><td className="k">Open Date:</td><td className="v">{account.openDate ?? ""}</td>
                  <td className="k">Expiration:</td><td className="v">{account.expirationDate ?? ""}</td></tr>
              <tr><td className="k">Reissue:</td><td className="v">{account.reissueDate ?? ""}</td>
                  <td className="k">Card Num:</td><td className="v">{account.cardNum}</td></tr>
              <tr><td className="k">Cycle Credit:</td><td className="v"><Money n={account.currCycCredit}/></td>
                  <td className="k">Cycle Debit:</td><td className="v"><Money n={account.currCycDebit}/></td></tr>
            </tbody>
          </table>

          <div className="section-title">Customer</div>
          <table className="kvtable">
            <tbody>
              <tr><td className="k">Customer ID:</td><td className="v">{account.customer.custId}</td>
                  <td className="k">SSN:</td><td className="v">{account.customer.ssn}</td></tr>
              <tr><td className="k">First Name:</td><td className="v">{account.customer.firstName}</td>
                  <td className="k">Middle Name:</td><td className="v">{account.customer.middleName}</td></tr>
              <tr><td className="k">Last Name:</td><td className="v">{account.customer.lastName}</td>
                  <td className="k">DOB:</td><td className="v">{account.customer.dob}</td></tr>
              <tr><td className="k">Address 1:</td><td className="v" colSpan={3}>{account.customer.addrLine1}</td></tr>
              <tr><td className="k">Address 2:</td><td className="v" colSpan={3}>{account.customer.addrLine2}</td></tr>
              <tr><td className="k">City/State/Zip:</td>
                  <td className="v" colSpan={3}>
                    {account.customer.city}, {account.customer.stateCd} {account.customer.zip}
                    {account.customer.countryCd ? ` (${account.customer.countryCd})` : ""}
                  </td></tr>
              <tr><td className="k">Phone 1:</td><td className="v">{account.customer.phone1}</td>
                  <td className="k">Phone 2:</td><td className="v">{account.customer.phone2}</td></tr>
              <tr><td className="k">FICO:</td><td className="v">{account.customer.ficoCreditScore}</td>
                  <td className="k">Pri Card Holder:</td><td className="v">{account.customer.priCardHolderInd}</td></tr>
              <tr><td className="k">EFT Account:</td><td className="v">{account.customer.eftAccountId}</td>
                  <td className="k">Govt ID:</td><td className="v">{account.customer.govtIssuedId}</td></tr>
            </tbody>
          </table>
        </>
      )}

      <div className="hotkeys">ENTER=View   PF03=Exit (Logout)</div>
    </div>
  );
}
