import { FormEvent, useEffect, useMemo, useState } from "react";
import { ApiError, authenticatedApi } from "./api";

type Society = { id: number; name: string };
type OwnedFlat = { flatId: number; flatNumber: string; blockName: string; societyId: number; societyName: string };
type Complaint = { id: number; societyId: number; societyName: string; flatId: number; flatNumber: string; ownerName: string; title: string; description: string; status: "OPEN" | "IN_PROGRESS" | "RESOLVED" | "CLOSED"; resolutionNote?: string; createdAt: string; updatedAt: string; resolvedAt?: string; closedAt?: string };
const api = <T,>(path: string, options: RequestInit = {}) => authenticatedApi<T>(path, options);
const errorMessage = (error: unknown) => error instanceof ApiError ? error.message : "We could not complete that request.";
const normalise = (value: string) => value.toLowerCase().replace(/[^a-z0-9]/g, "");
const label = (value: string) => value.toLowerCase().replaceAll("_", " ");

export default function GeneralComplaintsWorkspace({ manager }: { manager: boolean }) {
  const [complaints, setComplaints] = useState<Complaint[]>([]);
  const [societies, setSocieties] = useState<Society[]>([]);
  const [flats, setFlats] = useState<OwnedFlat[]>([]);
  const [societyId, setSocietyId] = useState("");
  const [feedback, setFeedback] = useState("");
  const [busy, setBusy] = useState(false);
  const [form, setForm] = useState({ flatId: "", title: "", description: "" });
  const [statusFilter, setStatusFilter] = useState("ALL");
  const [flatSearch, setFlatSearch] = useState("");
  const [editing, setEditing] = useState<number | null>(null);
  const [nextStatus, setNextStatus] = useState<Record<number, string>>({});
  const [notes, setNotes] = useState<Record<number, string>>({});

  const load = async (id = societyId) => {
    try {
      const path = manager ? `/api/general-complaints/societies/${id}` : "/api/general-complaints/mine";
      setComplaints(await api<Complaint[]>(path));
    } catch (error) { setFeedback(errorMessage(error)); }
  };
  useEffect(() => {
    if (manager) {
      void api<Society[]>("/api/onboarding/managed-societies").then(setSocieties).catch(error => setFeedback(errorMessage(error)));
    } else {
      void Promise.all([api<OwnedFlat[]>("/api/monthly-maintenance/my-flats"), load()]).then(([items]) => {
        setFlats(items);
        if (items.length === 1) setForm(current => ({ ...current, flatId: String(items[0].flatId) }));
      }).catch(error => setFeedback(errorMessage(error)));
    }
  }, [manager]);
  useEffect(() => { if (manager) { setComplaints([]); if (societyId) void load(societyId); } }, [manager, societyId]);

  const submit = async (event: FormEvent) => {
    event.preventDefault(); if (busy) return; setBusy(true);
    try {
      await api("/api/general-complaints", { method: "POST", body: JSON.stringify({ ...form, flatId: Number(form.flatId) }) });
      setForm(current => ({ ...current, title: "", description: "" })); setFeedback("Complaint submitted. You can follow its status here."); await load();
    } catch (error) { setFeedback(errorMessage(error)); } finally { setBusy(false); }
  };
  const update = async (item: Complaint) => {
    const status = nextStatus[item.id] ?? following(item.status); if (!status) return;
    setBusy(true);
    try {
      await api(`/api/general-complaints/${item.id}`, { method: "PATCH", body: JSON.stringify({ status, resolutionNote: notes[item.id] ?? "" }) });
      setEditing(null); setFeedback("Complaint status updated."); await load();
    } catch (error) { setFeedback(errorMessage(error)); } finally { setBusy(false); }
  };
  const visible = useMemo(() => complaints.filter(item =>
    (statusFilter === "ALL" || item.status === statusFilter) &&
    (flatSearch === "" || normalise(item.flatNumber).includes(normalise(flatSearch))),
  ), [complaints, statusFilter, flatSearch]);

  if (!manager) return <section className="admin-page">
    <header className="page-heading"><p className="eyebrow">My home</p><h1>General complaints</h1><p className="muted">Raise one clear issue for one of your flats. Your society administrator will update the progress and resolution.</p></header>
    {feedback && <p className="notice">{feedback}</p>}
    <section className="page-panel form-panel"><h2>Raise a complaint</h2><form onSubmit={submit}>
      <label>Flat<select value={form.flatId} onChange={event => setForm({ ...form, flatId: event.target.value })} required><option value="">Select your flat</option>{flats.map(flat => <option key={flat.flatId} value={flat.flatId}>{flat.blockName} · {flat.flatNumber}</option>)}</select></label>
      <label>What is the issue?<input value={form.title} maxLength={160} onChange={event => setForm({ ...form, title: event.target.value })} placeholder="e.g. Water leakage near the lift" required /></label>
      <label>Details<textarea value={form.description} maxLength={3000} onChange={event => setForm({ ...form, description: event.target.value })} placeholder="Describe the issue and where it is happening." required /></label>
      <button className="button" disabled={busy || !form.flatId}>{busy ? "Submitting…" : "Submit complaint"}</button>
    </form></section>
    <ComplaintList complaints={complaints} owner />
  </section>;

  return <section className="admin-page">
    <header className="page-heading"><p className="eyebrow">Society management</p><h1>General complaints</h1><p className="muted">Review owner-raised issues for your society and move each one through a simple, visible resolution path.</p></header>
    {feedback && <p className="notice">{feedback}</p>}
    <section className="toolbar"><select value={societyId} onChange={event => setSocietyId(event.target.value)}><option value="">Select society</option>{societies.map(society => <option key={society.id} value={society.id}>{society.name}</option>)}</select><button className="button quiet small" onClick={() => void load()} disabled={!societyId}>Refresh</button></section>
    {societyId && <><section className="filter-card complaint-filters"><label>Find flat<input value={flatSearch} onChange={event => setFlatSearch(event.target.value)} placeholder="e.g. N-201" /></label><label>Status<select value={statusFilter} onChange={event => setStatusFilter(event.target.value)}><option value="ALL">All statuses</option><option value="OPEN">Open</option><option value="IN_PROGRESS">In progress</option><option value="RESOLVED">Resolved</option><option value="CLOSED">Closed</option></select></label><span className="inline-note">{visible.length} complaint{visible.length === 1 ? "" : "s"} in view</span></section><ComplaintList complaints={visible} onEdit={setEditing} editing={editing} nextStatus={nextStatus} setNextStatus={setNextStatus} notes={notes} setNotes={setNotes} onSave={update} busy={busy} /></>}
  </section>;
}

function following(status: Complaint["status"]) { return status === "OPEN" ? "IN_PROGRESS" : status === "IN_PROGRESS" ? "RESOLVED" : status === "RESOLVED" ? "CLOSED" : ""; }
function ComplaintList({ complaints, owner = false, onEdit, editing, nextStatus, setNextStatus, notes, setNotes, onSave, busy }: { complaints: Complaint[]; owner?: boolean; onEdit?: (id: number | null) => void; editing?: number | null; nextStatus?: Record<number, string>; setNextStatus?: (value: Record<number, string>) => void; notes?: Record<number, string>; setNotes?: (value: Record<number, string>) => void; onSave?: (item: Complaint) => void; busy?: boolean }) {
  return <section className="page-panel wide"><h2>{owner ? "Your submitted complaints" : "Complaint queue"}</h2>{complaints.length ? <div className="request-list complaints-list">{complaints.map(item => <article className="complaint-card" key={item.id}><div className="complaint-summary"><div><div className="complaint-meta"><span>{item.flatNumber}</span>{!owner && <span>{item.ownerName}</span>}<span>{new Date(item.createdAt).toLocaleString()}</span></div><h3>{item.title}</h3><p>{item.description}</p>{item.resolutionNote && <div className="resolution-note"><strong>Resolution note</strong><p>{item.resolutionNote}</p></div>}</div><span className={`status-pill ${item.status.toLowerCase()}`}>{label(item.status)}</span></div>{!owner && item.status !== "CLOSED" && <>{editing === item.id ? <div className="complaint-action"><label>Move to<select value={nextStatus?.[item.id] ?? following(item.status)} onChange={event => setNextStatus?.({ ...nextStatus, [item.id]: event.target.value })}><option value={following(item.status)}>{label(following(item.status))}</option></select></label>{(nextStatus?.[item.id] ?? following(item.status)) === "RESOLVED" && <label>Resolution note<textarea value={notes?.[item.id] ?? ""} onChange={event => setNotes?.({ ...notes, [item.id]: event.target.value })} placeholder="Explain what was resolved" required /></label>}<div className="button-row"><button className="button small" disabled={busy} onClick={() => onSave?.(item)}>Save status</button><button className="button quiet small" onClick={() => onEdit?.(null)}>Cancel</button></div></div> : <button className="button quiet small" onClick={() => onEdit?.(item.id)}>Update status</button>}</>}</article>)}</div> : <div className="empty-state compact"><h2>No complaints yet</h2><p>{owner ? "You have not submitted a general complaint." : "There are no owner-raised complaints for the selected society."}</p></div>}</section>;
}
