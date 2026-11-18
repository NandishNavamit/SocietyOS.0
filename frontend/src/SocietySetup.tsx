import { FormEvent, useEffect, useState } from "react";
import { ApiError, authenticatedApi } from "./api";

type Society = { id: number; name: string };
type Invitation = { id: number; email: string; status: string; expiresAt?: string; inviteUrl?: string };
type Setup = {
  societyId: number; societyName: string; totalFlats: number; ownerReadyFlats: number;
  pendingOwnerInvitations: number; activeLogbookOperators: number; pendingOperatorInvitations: number;
  operators: { roleId: number; userId: number; fullName: string; email: string }[];
  pendingOperatorInvitationsList: Invitation[];
  blocks: { id: number; name: string; totalFloors?: number; flats: Flat[] }[];
};
type Flat = { id: number; flatNumber: string; floorNumber?: number; flatType?: string; areaSqft?: number; owner?: { fullName: string; email: string }; pendingInvitation?: Invitation };
const secure = <T,>(path: string, options: RequestInit = {}) => authenticatedApi<T>(path, options);
const errorMessage = (error: unknown) => error instanceof ApiError ? error.message : "We could not complete that action. Please try again.";

export default function SocietySetup() {
  const [societies, setSocieties] = useState<Society[]>([]);
  const [societyId, setSocietyId] = useState("");
  const [setup, setSetup] = useState<Setup | null>(null);
  const [feedback, setFeedback] = useState("");
  const [block, setBlock] = useState({ name: "", totalFloors: "" });
  const [flat, setFlat] = useState({ blockId: "", flatNumber: "", floorNumber: "", flatType: "", areaSqft: "" });
  const [owner, setOwner] = useState({ flatId: "", email: "" });
  const [operatorEmail, setOperatorEmail] = useState("");
  const [busy, setBusy] = useState("");
  const [inviteLink, setInviteLink] = useState("");

  const loadSetup = async (id = societyId) => {
    if (!id) { setSetup(null); return; }
    try { setSetup(await secure<Setup>(`/api/society-setup/societies/${id}`)); }
    catch (error) { setFeedback(errorMessage(error)); }
  };
  useEffect(() => { void secure<Society[]>("/api/onboarding/managed-societies").then(setSocieties).catch(error => setFeedback(errorMessage(error))); }, []);
  useEffect(() => { void loadSetup(societyId); }, [societyId]);

  const submit = async (key: string, action: () => Promise<unknown>, success: string) => {
    setBusy(key); setFeedback("");
    try { await action(); setFeedback(success); await loadSetup(); }
    catch (error) { setFeedback(errorMessage(error)); }
    finally { setBusy(""); }
  };
  const addBlock = (event: FormEvent) => { event.preventDefault(); if (!societyId) return; void submit("block", () => secure(`/api/society-setup/societies/${societyId}/blocks`, { method: "POST", body: JSON.stringify({ name: block.name, totalFloors: block.totalFloors ? Number(block.totalFloors) : null }) }), "Block added to the setup.").then(() => setBlock({ name: "", totalFloors: "" })); };
  const addFlat = (event: FormEvent) => { event.preventDefault(); if (!flat.blockId) return; void submit("flat", () => secure(`/api/society-setup/blocks/${flat.blockId}/flats`, { method: "POST", body: JSON.stringify({ flatNumber: flat.flatNumber, floorNumber: flat.floorNumber ? Number(flat.floorNumber) : null, flatType: flat.flatType || null, areaSqft: flat.areaSqft ? Number(flat.areaSqft) : null }) }), "Flat added. You can invite its owner next.").then(() => setFlat({ blockId: "", flatNumber: "", floorNumber: "", flatType: "", areaSqft: "" })); };
  const inviteOwner = (event: FormEvent) => { event.preventDefault(); if (!owner.flatId) return; void submit("owner", async () => { const created = await secure<Invitation>(`/api/society-setup/flats/${owner.flatId}/owner-invitations`, { method: "POST", body: JSON.stringify({ email: owner.email }) }); setInviteLink(created.inviteUrl ?? ""); }, "Owner invitation created. The flat stays unassigned until it is accepted.").then(() => setOwner({ flatId: "", email: "" })); };
  const inviteOperator = (event: FormEvent) => { event.preventDefault(); if (!societyId) return; void submit("operator", async () => { const created = await secure<Invitation>(`/api/society-setup/societies/${societyId}/operator-invitations`, { method: "POST", body: JSON.stringify({ email: operatorEmail }) }); setInviteLink(created.inviteUrl ?? ""); }, "Logbook operator invitation created.").then(() => setOperatorEmail("")); };
  const resend = (invitation: Invitation) => { void submit(`resend-${invitation.id}`, async () => { const refreshed = await secure<Invitation>(`/api/invitations/${invitation.id}/resend`, { method: "POST" }); setInviteLink(refreshed.inviteUrl ?? ""); }, `Invitation resent to ${invitation.email}.`); };
  const revoke = (invitation: Invitation) => { if (!window.confirm(`Revoke the pending invitation for ${invitation.email}?`)) return; void submit(`revoke-${invitation.id}`, () => secure(`/api/invitations/${invitation.id}`, { method: "DELETE" }), `Invitation revoked for ${invitation.email}.`); };
  const revokeOperator = (operator: Setup["operators"][number]) => { if (!window.confirm(`Remove ${operator.fullName}'s access to this private logbook? Existing log entries remain private.`)) return; void submit(`operator-revoke-${operator.roleId}`, () => secure(`/api/society-setup/societies/${societyId}/operator-assignments/${operator.roleId}`, { method: "DELETE" }), `Logbook access revoked for ${operator.fullName}.`); };
  const progress = setup ? [setup.totalFlats > 0, setup.totalFlats > 0 && setup.ownerReadyFlats === setup.totalFlats, setup.activeLogbookOperators > 0] : [false, false, false];

  return <section className="admin-page">
    <header className="page-heading"><p className="eyebrow">Guided society setup</p><h1>Set up your society, step by step.</h1><p className="muted">Add the homes you manage, appoint one owner for each flat, then invite the people who run the private visitor logbook.</p></header>
    {feedback && <p className={/added|created|resent|revoked/i.test(feedback) ? "notice success" : "notice error"} role="alert">{feedback}</p>}
    <section className="filter-card"><label>Society<select value={societyId} onChange={event => setSocietyId(event.target.value)}><option value="">Select society</option>{societies.map(society => <option key={society.id} value={society.id}>{society.name}</option>)}</select></label></section>
    {setup && <>
      <section className="setup-progress" aria-label="Setup progress">
        {["Create flats", "Assign flat owners", "Add a logbook operator"].map((label, index) => <article key={label} className={progress[index] ? "complete" : ""}><span>{progress[index] ? "✓" : index + 1}</span><div><strong>{label}</strong><small>{index === 0 ? `${setup.totalFlats} flats ready` : index === 1 ? `${setup.ownerReadyFlats} of ${setup.totalFlats} owners assigned` : `${setup.activeLogbookOperators} active operator${setup.activeLogbookOperators === 1 ? "" : "s"}`}</small></div></article>)}
      </section>
      <div className="metric-grid"><Metric label="Flats" value={setup.totalFlats} helper={`${setup.blocks.length} block${setup.blocks.length === 1 ? "" : "s"}`} /><Metric label="Owners assigned" value={setup.ownerReadyFlats} helper={`${setup.pendingOwnerInvitations} invitation${setup.pendingOwnerInvitations === 1 ? "" : "s"} pending`} /><Metric label="Logbook operators" value={setup.activeLogbookOperators} helper={`${setup.pendingOperatorInvitations} invitation${setup.pendingOperatorInvitations === 1 ? "" : "s"} pending`} /></div>
      <div className="content-grid">
        <section className="page-panel form-panel"><h2>1. Add a block</h2><form onSubmit={addBlock}><label>Block name<input value={block.name} onChange={event => setBlock({ ...block, name: event.target.value })} placeholder="e.g. North Tower" required /></label><label>Floors <small>(optional)</small><input type="number" min="0" value={block.totalFloors} onChange={event => setBlock({ ...block, totalFloors: event.target.value })} /></label><button className="button" disabled={busy === "block"}>{busy === "block" ? "Adding…" : "Add block"}</button></form></section>
        <section className="page-panel form-panel"><h2>2. Add a flat</h2><form onSubmit={addFlat}><label>Block<select value={flat.blockId} onChange={event => setFlat({ ...flat, blockId: event.target.value })} required><option value="">Select block</option>{setup.blocks.map(item => <option key={item.id} value={item.id}>{item.name}</option>)}</select></label><div className="two-cols"><label>Flat number<input value={flat.flatNumber} onChange={event => setFlat({ ...flat, flatNumber: event.target.value })} placeholder="N-101" required /></label><label>Floor <input type="number" min="0" value={flat.floorNumber} onChange={event => setFlat({ ...flat, floorNumber: event.target.value })} /></label></div><label>Flat type <small>(optional)</small><input value={flat.flatType} onChange={event => setFlat({ ...flat, flatType: event.target.value })} placeholder="2 BHK" /></label><button className="button" disabled={busy === "flat"}>{busy === "flat" ? "Adding…" : "Add flat"}</button></form></section>
      </div>
      <div className="content-grid">
        <section className="page-panel form-panel"><h2>3. Invite a flat owner</h2><p className="muted">Each flat has one active owner. The same person may own multiple flats; send and accept one invitation for each flat.</p><form onSubmit={inviteOwner}><label>Flat<select value={owner.flatId} onChange={event => setOwner({ ...owner, flatId: event.target.value })} required><option value="">Select an unassigned flat</option>{setup.blocks.flatMap(item => item.flats.filter(home => !home.owner && !home.pendingInvitation).map(home => <option key={home.id} value={home.id}>{item.name} · {home.flatNumber}</option>))}</select></label><label>Owner email<input type="email" value={owner.email} onChange={event => setOwner({ ...owner, email: event.target.value })} required /></label><button className="button" disabled={busy === "owner"}>{busy === "owner" ? "Creating…" : "Invite flat owner"}</button></form></section>
        <section className="page-panel form-panel"><h2>4. Invite a logbook operator</h2><p className="muted">Operators get only the private gate logbook. They cannot access finance or owner information.</p><form onSubmit={inviteOperator}><label>Operator email<input type="email" value={operatorEmail} onChange={event => setOperatorEmail(event.target.value)} required /></label><button className="button" disabled={busy === "operator"}>{busy === "operator" ? "Creating…" : "Invite operator"}</button></form></section>
      </div>
      {inviteLink && <section className="inline-note"><strong>Invitation link</strong><p className="break-all">{inviteLink}</p><button className="button quiet small" onClick={() => void navigator.clipboard.writeText(inviteLink)}>Copy link</button></section>}
      <section className="page-panel wide"><h2>Logbook operator access</h2><p className="muted">Operators can read and write this society's private visitor register. Administrators cannot open or view the register itself.</p>{setup.operators.length ? <div className="request-list">{setup.operators.map(operator => <article className="request-row" key={operator.roleId}><div><h3>{operator.fullName}</h3><p>{operator.email}</p></div><button className="button quiet small" disabled={busy === `operator-revoke-${operator.roleId}`} onClick={() => revokeOperator(operator)}>{busy === `operator-revoke-${operator.roleId}` ? "Removing…" : "Remove access"}</button></article>)}</div> : <div className="empty-state compact"><h2>No active operators</h2><p>Invite a logbook operator above to give someone access to the private gate register.</p></div>}</section>
      {setup.pendingOperatorInvitationsList.length > 0 && <section className="page-panel wide"><h2>Pending operator invitations</h2><div className="request-list">{setup.pendingOperatorInvitationsList.map(invitation => <article className="request-row" key={invitation.id}><div><h3>{invitation.email}</h3><p>Logbook operator</p><small>Expires {invitation.expiresAt ? new Date(invitation.expiresAt).toLocaleString() : "—"}</small></div><div className="button-row"><button className="button quiet small" disabled={busy === `resend-${invitation.id}`} onClick={() => resend(invitation)}>{busy === `resend-${invitation.id}` ? "Sending…" : "Resend"}</button><button className="button quiet small" disabled={busy === `revoke-${invitation.id}`} onClick={() => revoke(invitation)}>Revoke</button></div></article>)}</div></section>}
      <section className="page-panel wide"><h2>Homes and assignments</h2>{setup.totalFlats ? <div className="table-wrap"><table><thead><tr><th>Block</th><th>Flat</th><th>Owner</th><th>Setup status</th><th>Invitation</th></tr></thead><tbody>{setup.blocks.flatMap(item => item.flats.map(home => <tr key={home.id}><td>{item.name}</td><td>{home.flatNumber}</td><td>{home.owner ? <><strong>{home.owner.fullName}</strong><br /><small>{home.owner.email}</small></> : home.pendingInvitation ? home.pendingInvitation.email : <span className="muted">Not assigned</span>}</td><td><span className={`status ${home.owner ? "approved" : home.pendingInvitation ? "pending" : ""}`}>{home.owner ? "Owner assigned" : home.pendingInvitation ? "Invitation pending" : "Needs owner"}</span></td><td>{home.pendingInvitation ? <div className="button-row"><button className="button quiet small" disabled={busy === `resend-${home.pendingInvitation.id}`} onClick={() => resend(home.pendingInvitation!)}>{busy === `resend-${home.pendingInvitation.id}` ? "Sending…" : "Resend"}</button><button className="button quiet small" disabled={busy === `revoke-${home.pendingInvitation.id}`} onClick={() => revoke(home.pendingInvitation!)}>Revoke</button></div> : "—"}</td></tr>))}</tbody></table></div> : <p className="muted">Start with a block, then add its flats. Nothing is visible to owners until they accept an invitation.</p>}</section>
    </>}
  </section>;
}
function Metric({ label, value, helper }: { label: string; value: number; helper: string }) { return <article className="metric"><p>{label}</p><strong>{value}</strong><small>{helper}</small></article>; }
