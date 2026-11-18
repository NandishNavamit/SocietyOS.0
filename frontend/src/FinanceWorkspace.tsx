import { FormEvent, useEffect, useState } from "react";
import { ApiError, authenticatedApi } from "./api";

type Society = { id: number; name: string };
type Plan = {
  id: number;
  name: string;
  amount: number;
  frequency: string;
  dueDayOfMonth?: number;
  active: boolean;
};
type Bill = {
  id: number;
  chargeName: string;
  flatNumber: string;
  amount: number;
  paidAmount: number;
  dueDate: string;
  status: string;
};
type Expense = {
  id: number;
  description: string;
  category: string;
  amount: number;
  expenseDate: string;
};
type Complaint = { id: number; flatId?: number; title: string; status: string };
type ServiceCharge = { id: number; complaintId: number; complaintTitle: string; billId: number; flatNumber: string; description: string; amount: number; dueDate: string; vendorName?: string; invoiceNumber?: string; status: string };
const api = <T,>(path: string, options: RequestInit = {}) =>
  authenticatedApi<T>(path, options);
const message = (error: unknown) =>
  error instanceof ApiError
    ? error.message
    : "We could not complete that request.";
const money = (value: number) =>
  new Intl.NumberFormat("en-IN", {
    style: "currency",
    currency: "INR",
    maximumFractionDigits: 0,
  }).format(value);

export default function FinanceWorkspace() {
  const [societies, setSocieties] = useState<Society[]>([]);
  const [societyId, setSocietyId] = useState("");
  const [plans, setPlans] = useState<Plan[]>([]);
  const [bills, setBills] = useState<Bill[]>([]);
  const [expenses, setExpenses] = useState<Expense[]>([]);
  const [complaints, setComplaints] = useState<Complaint[]>([]);
  const [serviceCharges, setServiceCharges] = useState<ServiceCharge[]>([]);
  const [feedback, setFeedback] = useState("");
  const [plan, setPlan] = useState({
    name: "",
    amount: "",
    frequency: "MONTHLY",
    dueDayOfMonth: "10",
  });
  const [expense, setExpense] = useState({
    description: "",
    category: "UTILITY",
    amount: "",
    expenseDate: new Date().toISOString().slice(0, 10),
    vendorName: "",
  });
  const [serviceCharge, setServiceCharge] = useState({ complaintId: "", description: "", amount: "", dueDate: new Date().toISOString().slice(0, 10), vendorName: "", invoiceNumber: "", internalNote: "" });
  const load = async (id = societyId) => {
    if (!id) return;
    try {
      const [p, b, e, c, sc] = await Promise.all([
        api<Plan[]>(`/api/charge-plans/society/${id}`),
        api<Bill[]>(`/api/maintenance-bills/society/${id}`),
        api<Expense[]>(`/api/society-expenses/society/${id}`),
        api<Complaint[]>(`/api/complaints/society/${id}`),
        api<ServiceCharge[]>(`/api/service-charges/society/${id}`),
      ]);
      setPlans(p);
      setBills(b);
      setExpenses(e);
      setComplaints(c);
      setServiceCharges(sc);
    } catch (error) {
      setFeedback(message(error));
    }
  };
  useEffect(() => {
    void api<Society[]>("/api/onboarding/my-societies")
      .then(setSocieties)
      .catch((error) => setFeedback(message(error)));
  }, []);
  useEffect(() => {
    setPlans([]);
    setBills([]);
    setExpenses([]);
    setComplaints([]);
    setServiceCharges([]);
    void load(societyId);
  }, [societyId]);
  const savePlan = async (event: FormEvent) => {
    event.preventDefault();
    try {
      await api("/api/charge-plans", {
        method: "POST",
        body: JSON.stringify({
          societyId: Number(societyId),
          name: plan.name,
          amount: Number(plan.amount),
          frequency: plan.frequency,
          dueDayOfMonth: Number(plan.dueDayOfMonth),
          active: true,
        }),
      });
      setPlan({ ...plan, name: "", amount: "" });
      setFeedback("Charge plan created. Generate bills when ready.");
      await load();
    } catch (error) {
      setFeedback(message(error));
    }
  };
  const generate = async (id: number) => {
    try {
      await api(`/api/charge-plans/${id}/generate-bills`, {
        method: "POST",
        body: JSON.stringify({
          billingPeriodStart: new Date().toISOString().slice(0, 8) + "01",
        }),
      });
      setFeedback("Bills generated for all active flats.");
      await load();
    } catch (error) {
      setFeedback(message(error));
    }
  };
  const saveExpense = async (event: FormEvent) => {
    event.preventDefault();
    try {
      await api("/api/society-expenses", {
        method: "POST",
        body: JSON.stringify({
          ...expense,
          societyId: Number(societyId),
          amount: Number(expense.amount),
          active: true,
        }),
      });
      setExpense({ ...expense, description: "", amount: "", vendorName: "" });
      setFeedback("Expense recorded.");
      await load();
    } catch (error) {
      setFeedback(message(error));
    }
  };
  const issueServiceCharge = async (event: FormEvent) => { event.preventDefault(); try { await api('/api/service-charges', { method: 'POST', body: JSON.stringify({ ...serviceCharge, complaintId: Number(serviceCharge.complaintId), amount: Number(serviceCharge.amount) }) }); setServiceCharge({ ...serviceCharge, complaintId: '', description: '', amount: '', vendorName: '', invoiceNumber: '', internalNote: '' }); setFeedback('Service charge issued to the selected flat and the resident has been notified.'); await load(); } catch (error) { setFeedback(message(error)); } };
  const voidServiceCharge = async (id: number) => { try { await api(`/api/service-charges/${id}/void`, { method: 'POST' }); setFeedback('Service charge voided before payment.'); await load(); } catch (error) { setFeedback(message(error)); } };
  const outstanding = bills.reduce(
    (sum, bill) => sum + Number(bill.amount) - Number(bill.paidAmount),
    0,
  );
  return (
    <section className="admin-page">
      <header className="page-heading">
        <p className="eyebrow">Finance workspace</p>
        <h1>Charges, bills and collections</h1>
        <p className="muted">
          Set recurring charges, issue bills, record society expenses, and track
          outstanding amounts.
        </p>
      </header>
      <div className="toolbar">
        <select
          value={societyId}
          onChange={(e) => setSocietyId(e.target.value)}
        >
          <option value="">Select society</option>
          {societies.map((s) => (
            <option key={s.id} value={s.id}>
              {s.name}
            </option>
          ))}
        </select>
        <button className="button" onClick={() => void load()}>
          Refresh
        </button>
      </div>
      {feedback && <p className="notice error">{feedback}</p>}
      {societyId && (
        <>
          <div className="metric-grid">
            <article className="metric">
              <span>Active charge plans</span>
              <strong>{plans.filter((p) => p.active).length}</strong>
            </article>
            <article className="metric">
              <span>Bills issued</span>
              <strong>{bills.length}</strong>
            </article>
            <article className="metric">
              <span>Outstanding</span>
              <strong>{money(outstanding)}</strong>
            </article>
            <article className="metric">
              <span>Expenses</span>
              <strong>
                {money(
                  expenses.reduce((sum, item) => sum + Number(item.amount), 0),
                )}
              </strong>
            </article>
          </div>
          <div className="content-grid">
            <section className="page-panel form-panel">
              <h2>Create charge plan</h2>
              <form onSubmit={savePlan}>
                <label>
                  Name
                  <input
                    value={plan.name}
                    onChange={(e) => setPlan({ ...plan, name: e.target.value })}
                    placeholder="Monthly maintenance"
                    required
                  />
                </label>
                <label>
                  Amount
                  <input
                    type="number"
                    min="1"
                    value={plan.amount}
                    onChange={(e) =>
                      setPlan({ ...plan, amount: e.target.value })
                    }
                    required
                  />
                </label>
                <label>
                  Frequency
                  <select
                    value={plan.frequency}
                    onChange={(e) =>
                      setPlan({ ...plan, frequency: e.target.value })
                    }
                  >
                    <option value="MONTHLY">Monthly</option>
                    <option value="QUARTERLY">Quarterly</option>
                    <option value="ONE_TIME">One time</option>
                  </select>
                </label>
                <label>
                  Due day
                  <input
                    type="number"
                    min="1"
                    max="31"
                    value={plan.dueDayOfMonth}
                    onChange={(e) =>
                      setPlan({ ...plan, dueDayOfMonth: e.target.value })
                    }
                    required
                  />
                </label>
                <button className="button">Save charge plan</button>
              </form>
            </section>
            <section className="page-panel">
              <h2>Charge plans</h2>
              {plans.map((item) => (
                <article className="request-row" key={item.id}>
                  <div>
                    <h3>{item.name}</h3>
                    <p>
                      {money(item.amount)} · {item.frequency.toLowerCase()}
                    </p>
                  </div>
                  <button
                    className="button quiet small"
                    onClick={() => void generate(item.id)}
                  >
                    Generate bills
                  </button>
                </article>
              ))}
              {!plans.length && <p className="muted">No charge plans yet.</p>}
            </section>
            <section className="page-panel form-panel">
              <h2>Record expense</h2>
              <form onSubmit={saveExpense}>
                <label>
                  Description
                  <input
                    value={expense.description}
                    onChange={(e) =>
                      setExpense({ ...expense, description: e.target.value })
                    }
                    required
                  />
                </label>
                <label>
                  Category
                  <select
                    value={expense.category}
                    onChange={(e) =>
                      setExpense({ ...expense, category: e.target.value })
                    }
                  >
                    {[
                      "REPAIR",
                      "UTILITY",
                      "CLEANING",
                      "SECURITY",
                      "EVENT",
                      "OTHER",
                    ].map((v) => (
                      <option key={v}>{v}</option>
                    ))}
                  </select>
                </label>
                <label>
                  Amount
                  <input
                    type="number"
                    min="1"
                    value={expense.amount}
                    onChange={(e) =>
                      setExpense({ ...expense, amount: e.target.value })
                    }
                    required
                  />
                </label>
                <label>
                  Date
                  <input
                    type="date"
                    value={expense.expenseDate}
                    onChange={(e) =>
                      setExpense({ ...expense, expenseDate: e.target.value })
                    }
                    required
                  />
                </label>
                <label>
                  Vendor <small>(optional)</small>
                  <input
                    value={expense.vendorName}
                    onChange={(e) =>
                      setExpense({ ...expense, vendorName: e.target.value })
                    }
                  />
                </label>
                <button className="button">Record expense</button>
              </form>
            </section>
            <section className="page-panel form-panel">
              <h2>Issue repair or service charge</h2>
              <p className="inline-note">Only resolved or closed flat-linked complaints can be billed. The bill is sent only to that flat's active residents.</p>
              <form onSubmit={issueServiceCharge}>
                <label>Completed complaint<select value={serviceCharge.complaintId} onChange={(e) => setServiceCharge({ ...serviceCharge, complaintId: e.target.value })} required><option value="">Select resolved complaint</option>{complaints.filter((c) => c.flatId && (c.status === 'RESOLVED' || c.status === 'CLOSED')).map((c) => <option key={c.id} value={c.id}>#{c.id} · {c.title}</option>)}</select></label>
                <label>Charge description<input value={serviceCharge.description} onChange={(e) => setServiceCharge({ ...serviceCharge, description: e.target.value })} placeholder="Bathroom tap replacement" required /></label>
                <label>Actual cost<input type="number" min="1" value={serviceCharge.amount} onChange={(e) => setServiceCharge({ ...serviceCharge, amount: e.target.value })} required /></label>
                <label>Due date<input type="date" value={serviceCharge.dueDate} onChange={(e) => setServiceCharge({ ...serviceCharge, dueDate: e.target.value })} required /></label>
                <label>Vendor <small>(optional)</small><input value={serviceCharge.vendorName} onChange={(e) => setServiceCharge({ ...serviceCharge, vendorName: e.target.value })} /></label>
                <label>Invoice number <small>(optional)</small><input value={serviceCharge.invoiceNumber} onChange={(e) => setServiceCharge({ ...serviceCharge, invoiceNumber: e.target.value })} /></label>
                <label>Internal note <small>(not shown to resident)</small><textarea value={serviceCharge.internalNote} onChange={(e) => setServiceCharge({ ...serviceCharge, internalNote: e.target.value })} /></label>
                <button className="button">Issue one-off bill</button>
              </form>
            </section>
            <section className="page-panel">
              <h2>Service-charge history</h2>
              {serviceCharges.length ? <div className="request-list">{serviceCharges.map((charge) => <article className="request-row" key={charge.id}><div><h3>{charge.description}</h3><p>{charge.flatNumber} · {money(charge.amount)} · {charge.status}</p><small>Complaint #{charge.complaintId}{charge.invoiceNumber ? ` · Invoice ${charge.invoiceNumber}` : ''}</small></div>{charge.status === 'ISSUED' && <button className="button quiet small" onClick={() => void voidServiceCharge(charge.id)}>Void</button>}</article>)}</div> : <p className="muted">No one-off service charges issued.</p>}
            </section>
            <section className="page-panel wide">
              <h2>Issued bills</h2>
              {bills.length ? (
                <div className="table-wrap">
                  <table>
                    <thead>
                      <tr>
                        <th>Charge</th>
                        <th>Flat</th>
                        <th>Due</th>
                        <th>Amount</th>
                        <th>Paid</th>
                        <th>Status</th>
                      </tr>
                    </thead>
                    <tbody>
                      {bills.map((b) => (
                        <tr key={b.id}>
                          <td>{b.chargeName}</td>
                          <td>{b.flatNumber}</td>
                          <td>{new Date(b.dueDate).toLocaleDateString()}</td>
                          <td>{money(b.amount)}</td>
                          <td>{money(b.paidAmount)}</td>
                          <td>{b.status}</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              ) : (
                <p className="muted">
                  Generate bills from a charge plan to begin collections.
                </p>
              )}
            </section>
          </div>
        </>
      )}
    </section>
  );
}
