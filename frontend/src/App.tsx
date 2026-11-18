import { FormEvent, useEffect, useState, type ReactNode } from "react";
import {
  Link,
  Navigate,
  NavLink,
  Route,
  Routes,
  useNavigate,
} from "react-router-dom";
import { ApiError, api, authenticatedApi } from "./api";
import { useAuth, type Role } from "./auth/AuthContext";
import SocietySetup from "./SocietySetup";
import MaintenancePolicyWorkspace from "./MaintenancePolicyWorkspace";
import MonthlyMaintenanceWorkspace from "./MonthlyMaintenanceWorkspace";
import GeneralComplaintsWorkspace from "./GeneralComplaintsWorkspace";
import PrivateLogbookWorkspace from "./PrivateLogbookWorkspace";

type Society = { id: number; name: string; city?: string };
type MembershipRequest = {
  id: number;
  userName: string;
  userEmail: string;
  societyId: number;
  requestedRole: Role;
  status: "PENDING" | "APPROVED" | "REJECTED";
  note?: string;
  createdAt: string;
};
type Enquiry = {
  id: number;
  contactName: string;
  email: string;
  societyName: string;
  city?: string;
  status: "NEW" | "CONTACTED" | "CLOSED";
  createdAt: string;
};
type DashboardReport = {
  societyId: number;
  societyName: string;
  activeBlocks: number;
  activeFlats: number;
  occupiedFlats: number;
  vacantFlats: number;
  activeResidents: number;
  activeStaff: number;
  totalBilled: number;
  totalCollected: number;
  outstandingAmount: number;
  openComplaints: number;
  visitsToday: number;
  unreadNotifications: number;
};
type Notification = {
  id: number;
  title: string;
  message: string;
  type: string;
  read: boolean;
  createdAt: string;
};
type ResidentHome = {
  resident: { id: number; fullName: string };
  flats: {
    flatId: number;
    societyId: number;
    societyName: string;
    blockName: string;
    flatNumber: string;
    occupancyType: string;
    residentRole: string;
    startDate: string;
  }[];
  bills: {
    id: number;
    chargeName: string;
    flatNumber: string;
    dueDate: string;
    amount: number;
    paidAmount: number;
    status: string;
  }[];
  notifications: Notification[];
};
type Complaint = {
  id: number;
  societyId: number;
  flatId?: number;
  category: string;
  title: string;
  description: string;
  priority: string;
  status: string;
  createdAt: string;
  updatedAt: string;
};
type Block = {
  id: number;
  name: string;
  totalFloors?: number;
  totalFlats?: number;
  active: boolean;
};
type Flat = {
  id: number;
  flatNumber: string;
  floorNumber?: number;
  flatType?: string;
  areaSqft?: number;
  blockId: number;
  active: boolean;
};
type ResidentRecord = { id: number; fullName: string; active: boolean };
type Occupancy = {
  id: number;
  flatId: number;
  residentId: number;
  occupancyType: string;
  residentRole: string;
  startDate: string;
  active: boolean;
};
type SocietyMember = {
  userId: number;
  fullName: string;
  email: string;
  roles: Role[];
};
type RoleAssignment = {
  id: number;
  userId: number;
  societyId: number;
  role: Role;
  active: boolean;
  createdAt: string;
};
type BlockManagerAssignment = {
  id: number;
  userId: number;
  userName: string;
  blockId: number;
  blockName: string;
  active: boolean;
  createdAt: string;
};
type StaffProfile = {
  id: number;
  fullName: string;
  phoneNumber: string;
  email?: string;
  userId?: number;
};
type StaffAssignment = {
  id: number;
  staffProfileId: number;
  userId?: number;
  staffName: string;
  societyId: number;
  staffType: string;
  joiningDate?: string;
  active: boolean;
};
type VisitorVisit = {
  id: number;
  visitorId: number;
  societyId: number;
  flatId?: number;
  visitorType: string;
  purpose: string;
  entryTime: string;
  exitTime?: string;
  status: string;
};
type VisitorAccessPass = {
  id: number;
  visitorId: number;
  societyId: number;
  accessType: string;
  status: string;
  validFrom: string;
  validUntil?: string;
  purpose: string;
};
type StaffWorkspace = {
  assignments: StaffAssignment[];
  complaints: Complaint[];
};
type ComplaintReport = {
  totalComplaints: number;
  countsByStatus: Record<string, number>;
};
type StaffReport = {
  activeStaffCount: number;
  countsByType: Record<string, number>;
};
type VisitorReport = {
  totalVisits: number;
  checkedIn: number;
  checkedOut: number;
  denied: number;
};
type ChargePlan = {
  id: number;
  societyId: number;
  name: string;
  amount: number;
  frequency: string;
  dueDayOfMonth?: number;
  active: boolean;
};
type Expense = {
  id: number;
  societyId: number;
  category: string;
  description: string;
  amount: number;
  expenseDate: string;
  vendorName?: string;
  active: boolean;
};
type GatewayOrder = {
  razorpayOrderId: string;
  amountPaise: number;
  currency: string;
  keyId: string;
};
type Invitation = {
  id: number;
  societyId: number;
  societyName: string;
  email: string;
  role: Role;
  status: string;
  expiresAt: string;
  createdAt: string;
  inviteUrl?: string;
};
const secure = <T,>(path: string, options: RequestInit = {}) =>
  authenticatedApi<T>(path, options);
const readError = (error: unknown) => {
  if (error instanceof ApiError && error.status === 401)
    return error.message || "Your session has ended. Please sign in again.";
  if (error instanceof ApiError && error.status === 403)
    return error.message && error.message !== "Access is denied"
      ? error.message
      : "This action is not available for your current role.";
  return error instanceof ApiError
    ? error.message
    : "We could not complete that request. Please try again.";
};
const roleLabel: Record<Role, string> = {
  ADMIN: "Platform administrator",
  SOCIETY_ADMIN: "Society administrator",
  FLAT_OWNER: "Flat owner",
  LOGBOOK_OPERATOR: "Logbook operator",
  SOCIETY_MANAGER: "Retired role",
  FINANCE_MANAGER: "Retired role",
  SECURITY_MANAGER: "Retired role",
  BLOCK_MANAGER: "Retired role",
  RESIDENT: "Retired role",
  STAFF: "Retired role",
  SECURITY: "Retired role",
};
const isPlatformAdmin = (roles: Role[] = []) => roles.includes("ADMIN");
const canManage = (roles: Role[] = []) => roles.includes("SOCIETY_ADMIN");
const primaryRole = (roles: Role[] = []): Role | undefined =>
  [
    "ADMIN",
    "SOCIETY_ADMIN",
    "FLAT_OWNER",
    "LOGBOOK_OPERATOR",
  ].find((role) => roles.includes(role as Role)) as Role | undefined;

function Notice({ message }: { message: string }) {
  const positive =
    /created|sent|submitted|approved|assigned|saved|checked in/i.test(message);
  return message ? (
    <p className={positive ? "notice success" : "notice error"} role="alert">
      {message}
    </p>
  ) : null;
}
function PasswordInput({
  value,
  onChange,
  autoComplete,
}: {
  value: string;
  onChange: (value: string) => void;
  autoComplete: string;
}) {
  const [shown, setShown] = useState(false);
  return (
    <div className="password-field">
      <input
        type={shown ? "text" : "password"}
        value={value}
        onChange={(event) => onChange(event.target.value)}
        autoComplete={autoComplete}
        required
      />
      <button
        className="password-toggle"
        type="button"
        onClick={() => setShown(!shown)}
        aria-label={shown ? "Hide password" : "Show password"}
      >
        {shown ? "Hide" : "Show"}
      </button>
    </div>
  );
}
function PublicFrame({
  title,
  subtitle,
  children,
}: {
  title: string;
  subtitle: string;
  children: ReactNode;
}) {
  return (
    <main className="auth-page">
      <section className="auth-card">
        <Link className="wordmark dark" to="/">
          Society<span>OS</span>
        </Link>
        <p className="eyebrow">A better way to run a community</p>
        <h1>{title}</h1>
        <p className="muted">{subtitle}</p>
        {children}
      </section>
    </main>
  );
}
function Landing() {
  return (
    <main className="landing">
      <nav className="public-nav">
        <Link className="wordmark dark" to="/">
          Society<span>OS</span>
        </Link>
        <div>
          <Link className="text-link" to="/login">
            Sign in
          </Link>
          <Link className="button small" to="/signup">
            Create account
          </Link>
        </div>
      </nav>
      <section className="hero">
        <p className="eyebrow">Society operations, in one place</p>
        <h1>Everyday society management, made clear.</h1>
        <p>
          Give managers, residents, staff and security teams the right workspace
          for maintenance, payments, visitors, requests and notices.
        </p>
        <div className="hero-actions">
          <Link className="button" to="/signup">
            Get started
          </Link>
          <Link className="button ghost" to="/login">
            Sign in
          </Link>
        </div>
      </section>
      <section className="journey-grid">
        <article>
          <span>01</span>
          <h2>Create an account</h2>
          <p>Every person starts with a secure personal account.</p>
        </article>
        <article>
          <span>02</span>
          <h2>Choose your path</h2>
          <p>Register a new society, or ask to join one that exists.</p>
        </article>
        <article>
          <span>03</span>
          <h2>Get the right workspace</h2>
          <p>
            Your approved role determines the pages and information you can use.
          </p>
        </article>
      </section>
    </main>
  );
}
function Login() {
  const { user, login } = useAuth();
  const navigate = useNavigate();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [feedback, setFeedback] = useState("");
  const inviteToken = new URLSearchParams(window.location.search).get("invite");
  if (user) return <Navigate to="/dashboard" replace />;
  const submit = async (event: FormEvent) => {
    event.preventDefault();
    setFeedback("");
    try {
      await login(email, password);
      navigate(inviteToken ? `/invite?token=${encodeURIComponent(inviteToken)}` : "/dashboard");
    } catch (error) {
      setFeedback(readError(error));
    }
  };
  return (
    <PublicFrame
      title="Welcome back"
      subtitle="Sign in to continue to your SocietyOS workspace."
    >
      <form onSubmit={submit}>
        <label>
          Email address
          <input
            type="email"
            autoComplete="email"
            value={email}
            onChange={(event) => setEmail(event.target.value)}
            required
          />
        </label>
        <label>
          Password
          <PasswordInput
            value={password}
            onChange={setPassword}
            autoComplete="current-password"
          />
        </label>
        <Notice message={feedback} />
        <button className="button full">Sign in</button>
      </form>
      <p className="form-footer">
        New here? <Link to="/signup">Create an account</Link>.
      </p>
    </PublicFrame>
  );
}
function Signup() {
  const navigate = useNavigate();
  const [form, setForm] = useState({
    fullName: "",
    email: "",
    phoneNumber: "",
    password: "",
  });
  const [feedback, setFeedback] = useState("");
  const [created, setCreated] = useState(false);
  const inviteToken = new URLSearchParams(window.location.search).get("invite");
  const submit = async (event: FormEvent) => {
    event.preventDefault();
    setFeedback("");
    try {
      await api("/api/auth/register", {
        method: "POST",
        body: JSON.stringify(form),
      });
      setCreated(true);
      setFeedback("Account created. You can now sign in and choose your path.");
    } catch (error) {
      setCreated(false);
      setFeedback(readError(error));
    }
  };
  return (
    <PublicFrame
      title="Create your account"
      subtitle="First create a secure personal account. You will choose a society journey after signing in."
    >
      <form onSubmit={submit}>
        <label>
          Full name
          <input
            autoComplete="name"
            value={form.fullName}
            onChange={(event) =>
              setForm({ ...form, fullName: event.target.value })
            }
            required
          />
        </label>
        <label>
          Email address
          <input
            type="email"
            autoComplete="email"
            value={form.email}
            onChange={(event) =>
              setForm({ ...form, email: event.target.value })
            }
            required
          />
        </label>
        <label>
          Mobile number
          <input
            type="tel"
            inputMode="numeric"
            autoComplete="tel"
            pattern="(?:\\+91[ -]?)?[6-9][0-9]{9}"
            maxLength={13}
            placeholder="9876543210"
            title="Use a valid 10-digit Indian mobile number"
            value={form.phoneNumber}
            onChange={(event) =>
              setForm({ ...form, phoneNumber: event.target.value })
            }
            required
          />
          <small>10 digits, starting with 6, 7, 8, or 9.</small>
        </label>
        <label>
          Password
          <PasswordInput
            value={form.password}
            onChange={(value) => setForm({ ...form, password: value })}
            autoComplete="new-password"
          />
          <small>At least 8 characters.</small>
        </label>
        <Notice message={feedback} />
        {created ? (
          <button
            className="button full"
            type="button"
            onClick={() => navigate(inviteToken ? `/login?invite=${encodeURIComponent(inviteToken)}` : "/login")}
          >
            Continue to sign in
          </button>
        ) : (
          <button className="button full">Create account</button>
        )}
      </form>
      <p className="form-footer">
        Already have an account? <Link to="/login">Sign in</Link>.
      </p>
    </PublicFrame>
  );
}

function EmptyAccount() {
  return (
    <section className="welcome-card">
      <p className="eyebrow">Choose your journey</p>
      <h1>What would you like to do?</h1>
      <p className="muted">
        Your account is ready. You have not submitted any society-access request
        yet.
      </p>
      <div className="choice-grid">
        <Link className="choice-card" to="/register-society">
          <span className="choice-icon">⌂</span>
          <h2>Register a new society</h2>
          <p>I represent a society and want the platform team to set it up.</p>
          <b>Start registration →</b>
        </Link>
        <Link className="choice-card" to="/login">
          <span className="choice-icon">↗</span>
          <h2>Join an existing society</h2>
          <p>
          I am a flat owner or a logbook operator of an existing society.
          </p>
          <b>Sign in with your invitation →</b>
        </Link>
      </div>
    </section>
  );
}
function PlatformDashboard() {
  return (
    <section className="welcome-card">
      <p className="eyebrow">Platform administration</p>
      <h1>SocietyOS control centre</h1>
      <p className="muted">
        This account belongs to the SocietyOS platform team. Review registration
        enquiries, create verified societies, build their structure, and appoint
        their initial Society Administrator.
      </p>
      <div className="choice-grid">
        <Link className="choice-card" to="/admin/onboarding">
          <span className="choice-icon">⌂</span>
          <h2>Society onboarding</h2>
          <p>
            Review enquiries, create a society workspace, and appoint its first
            Society Administrator.
          </p>
          <b>Open onboarding →</b>
        </Link>
        <Link className="choice-card" to="/admin/structure">
          <span className="choice-icon">▦</span>
          <h2>Society structure</h2>
          <p>
            Create and maintain the blocks and flats for a verified society.
          </p>
          <b>Set up structure →</b>
        </Link>
        <Link className="choice-card" to="/profile">
          <span className="choice-icon">◉</span>
          <h2>My platform account</h2>
          <p>Review your own account and platform access.</p>
          <b>View profile →</b>
        </Link>
      </div>
    </section>
  );
}
function ManagerDashboard() {
  const { user } = useAuth();
  const [societies, setSocieties] = useState<Society[]>([]);
  const [societyId, setSocietyId] = useState("");
  const [report, setReport] = useState<DashboardReport | null>(null);
  const [feedback, setFeedback] = useState("");
  useEffect(() => {
    void secure<Society[]>("/api/onboarding/managed-societies")
      .then(setSocieties)
      .catch((error) => setFeedback(readError(error)));
  }, []);
  useEffect(() => {
    if (!societyId) return;
    void secure<DashboardReport>(
      `/api/reports/societies/${societyId}/dashboard`,
    )
      .then(setReport)
      .catch((error) => setFeedback(readError(error)));
  }, [societyId]);
  return (
    <>
      <header className="page-heading">
        <p className="eyebrow">Society administration</p>
        <h1>Welcome, {user?.fullName.split(" ")[0]}.</h1>
        <p className="muted">
          Choose one of your societies to review the transition overview.
        </p>
      </header>
      <section className="filter-card">
        <label>
          View society
          <select
            value={societyId}
            onChange={(event) => {
              setSocietyId(event.target.value);
              setReport(null);
            }}
          >
            <option value="">Select society</option>
            {societies.map((society) => (
              <option key={society.id} value={society.id}>
                {society.name}
              </option>
            ))}
          </select>
        </label>
      </section>
      <Notice message={feedback} />
      {report ? (
        <div className="metric-grid">
          <Metric
            label="Occupied flats"
            value={report.occupiedFlats}
            helper={`${report.vacantFlats} vacant`}
          />
          <Metric label="Active flat owners" value={report.activeResidents} />
          <Metric
            label="Outstanding maintenance"
            value={formatCurrency(report.outstandingAmount)}
          />
          <Metric label="Open complaints" value={report.openComplaints} />
          <Metric label="Unread notices" value={report.unreadNotifications} />
        </div>
      ) : (
        <section className="empty-state">
          <span>⌂</span>
          <h2>Select a society</h2>
          <p>
            Your dashboard only shows data for societies where you are an
            approved Society Administrator.
          </p>
        </section>
      )}
    </>
  );
}
function Dashboard() {
  const { user } = useAuth();
  if (!user?.roles.length) return <EmptyAccount />;
  if (isPlatformAdmin(user.roles)) return <PlatformDashboard />;
  if (canManage(user.roles)) return <ManagerDashboard />;
  if (user.roles.includes("FLAT_OWNER")) return <ResidentDashboard />;
  if (user.roles.includes("LOGBOOK_OPERATOR")) return <RoleWelcome roles={user.roles} />;
  return (
    <>
      <header className="page-heading">
        <p className="eyebrow">My workspace</p>
        <h1>Welcome, {user.fullName.split(" ")[0]}.</h1>
      </header>
      <RoleWelcome roles={user.roles} />
    </>
  );
}
function ResidentDashboard() {
  const [home, setHome] = useState<ResidentHome | null>(null);
  const [feedback, setFeedback] = useState("");
  const load = () =>
    secure<ResidentHome>("/api/resident-workspace")
      .then(setHome)
      .catch((error) => setFeedback(readError(error)));
  useEffect(() => {
    void load();
  }, []);
  const markRead = async (id: number) => {
    try {
      await secure(`/api/notifications/${id}/read`, { method: "PATCH" });
      await load();
    } catch (error) {
      setFeedback(readError(error));
    }
  };
  if (!home)
    return (
      <section className="empty-state">
        <span>…</span>
        <h2>Loading your home</h2>
        <Notice message={feedback} />
      </section>
    );
  const outstanding = home.bills.reduce(
    (total, bill) => total + Number(bill.amount) - Number(bill.paidAmount),
    0,
  );
  const unread = home.notifications.filter(
    (notification) => !notification.read,
  );
  return (
    <>
      <header className="page-heading">
        <p className="eyebrow">Resident workspace</p>
        <h1>Welcome home, {home.resident.fullName.split(" ")[0]}.</h1>
        <p className="muted">
          Your flat, maintenance, and society updates in one place.
        </p>
      </header>
      <Notice message={feedback} />
      <div className="metric-grid">
        <Metric label="My flats" value={home.flats.length} />
        <Metric label="Outstanding dues" value={formatCurrency(outstanding)} />
        <Metric label="Unread notices" value={unread.length} />
      </div>
      <div className="content-grid">
        <section className="page-panel">
          <h2>My home</h2>
          {home.flats.length ? (
            <div className="request-list">
              {home.flats.map((flat) => (
                <article key={flat.flatId} className="request-row">
                  <div>
                    <h3>
                      {flat.blockName} · Flat {flat.flatNumber}
                    </h3>
                    <p>{flat.societyName}</p>
                    <small>
                      {flat.occupancyType.toLowerCase()} ·{" "}
                      {flat.residentRole.toLowerCase()} since{" "}
                      {new Date(flat.startDate).toLocaleDateString()}
                    </small>
                  </div>
                </article>
              ))}
            </div>
          ) : (
            <p className="muted">
              Your Society Manager has not linked a flat to your profile yet.
            </p>
          )}
        </section>
        <section className="page-panel">
          <div className="section-title">
            <div>
              <h2>Latest notices</h2>
              <p>Announcements and account updates</p>
            </div>
            <Link to="/notifications">View all</Link>
          </div>
          {home.notifications.slice(0, 3).map((notification) => (
            <article
              key={notification.id}
              className={`notice-row ${notification.read ? "" : "unread"}`}
            >
              <div>
                <b>{notification.title}</b>
                <p>{notification.message}</p>
                <small>
                  {new Date(notification.createdAt).toLocaleString()}
                </small>
              </div>
              {!notification.read && (
                <button
                  className="button quiet small"
                  onClick={() => void markRead(notification.id)}
                >
                  Mark read
                </button>
              )}
            </article>
          ))}
          {!home.notifications.length && (
            <p className="muted">No notices yet.</p>
          )}
        </section>
        <section className="page-panel wide">
          <div className="section-title">
            <div>
              <h2>Maintenance bills</h2>
              <p>Payments and due dates for your flat</p>
            </div>
          </div>
          {home.bills.length ? (
            <div className="table-wrap">
              <table>
                <thead>
                  <tr>
                    <th>Charge</th>
                    <th>Flat</th>
                    <th>Due</th>
                    <th>Amount</th>
                    <th>Status</th>
                    <th>Payment</th>
                  </tr>
                </thead>
                <tbody>
                  {home.bills.map((bill) => (
                    <tr key={bill.id}>
                      <td>{bill.chargeName}</td>
                      <td>{bill.flatNumber}</td>
                      <td>{new Date(bill.dueDate).toLocaleDateString()}</td>
                      <td>{formatCurrency(bill.amount)}</td>
                      <td>
                        <Status value={bill.status} />
                      </td>
                      <td>{bill.status !== "PAID" ? <PayBill billId={bill.id} onPaid={load} /> : <span className="muted">Paid</span>}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          ) : (
            <p className="muted">
              No maintenance bills have been issued for your flat.
            </p>
          )}
        </section>
      </div>
    </>
  );
}
function ResidentComplaints() {
  const [home, setHome] = useState<ResidentHome | null>(null);
  const [complaints, setComplaints] = useState<Complaint[]>([]);
  const [form, setForm] = useState({
    flatId: "",
    category: "OTHER",
    priority: "MEDIUM",
    title: "",
    description: "",
  });
  const [feedback, setFeedback] = useState("");
  const load = async () => {
    try {
      const workspace = await secure<ResidentHome>("/api/resident-workspace");
      setHome(workspace);
      const items = await secure<Complaint[]>(
        `/api/complaints/resident/${workspace.resident.id}`,
      );
      setComplaints(items);
    } catch (error) {
      setFeedback(readError(error));
    }
  };
  useEffect(() => {
    void load();
  }, []);
  const submit = async (event: FormEvent) => {
    event.preventDefault();
    if (!home) return;
    const flat = home.flats.find((item) => item.flatId === Number(form.flatId));
    if (!flat) return setFeedback("Choose the flat this complaint concerns.");
    try {
      await secure("/api/complaints", {
        method: "POST",
        body: JSON.stringify({
          societyId: flat.societyId,
          flatId: flat.flatId,
          raisedByResidentId: home.resident.id,
          category: form.category,
          priority: form.priority,
          title: form.title,
          description: form.description,
        }),
      });
      setForm({
        flatId: "",
        category: "OTHER",
        priority: "MEDIUM",
        title: "",
        description: "",
      });
      setFeedback("Complaint submitted.");
      await load();
    } catch (error) {
      setFeedback(readError(error));
    }
  };
  return (
    <section className="admin-page">
      <header className="page-heading">
        <p className="eyebrow">Resident support</p>
        <h1>My complaints</h1>
        <p className="muted">Report a problem and track its resolution.</p>
      </header>
      <div className="content-grid">
        <section className="page-panel form-panel">
          <h2>Raise a complaint</h2>
          <form onSubmit={submit}>
            <label>
              Flat
              <select
                value={form.flatId}
                onChange={(event) =>
                  setForm({ ...form, flatId: event.target.value })
                }
                required
              >
                <option value="">Select flat</option>
                {home?.flats.map((flat) => (
                  <option key={flat.flatId} value={flat.flatId}>
                    {flat.blockName} · {flat.flatNumber}
                  </option>
                ))}
              </select>
            </label>
            <div className="two-cols">
              <label>
                Category
                <select
                  value={form.category}
                  onChange={(event) =>
                    setForm({ ...form, category: event.target.value })
                  }
                >
                  {[
                    "PLUMBING",
                    "ELECTRICAL",
                    "CLEANING",
                    "SECURITY",
                    "COMMON_AREA",
                    "OTHER",
                  ].map((value) => (
                    <option key={value}>{value}</option>
                  ))}
                </select>
              </label>
              <label>
                Priority
                <select
                  value={form.priority}
                  onChange={(event) =>
                    setForm({ ...form, priority: event.target.value })
                  }
                >
                  {["LOW", "MEDIUM", "HIGH", "URGENT"].map((value) => (
                    <option key={value}>{value}</option>
                  ))}
                </select>
              </label>
            </div>
            <label>
              Title
              <input
                value={form.title}
                onChange={(event) =>
                  setForm({ ...form, title: event.target.value })
                }
                required
              />
            </label>
            <label>
              Describe the problem
              <textarea
                value={form.description}
                onChange={(event) =>
                  setForm({ ...form, description: event.target.value })
                }
                required
              />
            </label>
            <Notice message={feedback} />
            <button className="button">Submit complaint</button>
          </form>
        </section>
        <section className="page-panel">
          <h2>Complaint history</h2>
          {complaints.length ? (
            <div className="request-list">
              {complaints.map((complaint) => (
                <article key={complaint.id} className="request-row">
                  <div>
                    <h3>{complaint.title}</h3>
                    <p>
                      {complaint.category.replaceAll("_", " ")} ·{" "}
                      {complaint.priority}
                    </p>
                    <small>
                      Created {new Date(complaint.createdAt).toLocaleString()}
                    </small>
                  </div>
                  <Status value={complaint.status} />
                </article>
              ))}
            </div>
          ) : (
            <p className="muted">You have not raised a complaint yet.</p>
          )}
        </section>
      </div>
    </section>
  );
}
function Metric({
  label,
  value,
  helper,
}: {
  label: string;
  value: string | number;
  helper?: string;
}) {
  return (
    <article className="metric">
      <span>{label}</span>
      <strong>{value}</strong>
      {helper && <small>{helper}</small>}
    </article>
  );
}
let razorpayLoader: Promise<void> | null = null;
function loadRazorpay() {
  if ((window as { Razorpay?: unknown }).Razorpay) return Promise.resolve();
  if (!razorpayLoader)
    razorpayLoader = new Promise((resolve, reject) => {
      const script = document.createElement("script");
      script.src = "https://checkout.razorpay.com/v1/checkout.js";
      script.onload = () => resolve();
      script.onerror = () =>
        reject(new Error("Razorpay checkout could not be loaded."));
      document.head.appendChild(script);
    });
  return razorpayLoader;
}
function PayBill({ billId, disabled, onPaid }: { billId: number; disabled?: boolean; onPaid: () => Promise<void> }) {
  const [feedback, setFeedback] = useState("");
  const [paying, setPaying] = useState(false);
  const pay = async () => {
    try {
      setPaying(true);
      setFeedback("");
      const order = await secure<GatewayOrder>("/api/razorpay/orders", {
        method: "POST",
        body: JSON.stringify({ billId }),
      });
      await loadRazorpay();
      const Checkout = (
        window as unknown as {
          Razorpay: new (options: {
            key: string;
            amount: number;
            currency: string;
            name: string;
            description: string;
            order_id: string;
            handler: (response: {
              razorpay_order_id: string;
              razorpay_payment_id: string;
              razorpay_signature: string;
            }) => Promise<void>;
          }) => { open: () => void };
        }
      ).Razorpay;
      new Checkout({
        key: order.keyId,
        amount: order.amountPaise,
        currency: order.currency,
        name: "SocietyOS",
        description: "Maintenance payment",
        order_id: order.razorpayOrderId,
        handler: async (response) => {
          try {
            await secure("/api/razorpay/verify", {
              method: "POST",
              body: JSON.stringify({
                razorpayOrderId: response.razorpay_order_id,
                razorpayPaymentId: response.razorpay_payment_id,
                razorpaySignature: response.razorpay_signature,
              }),
            });
            await onPaid();
            setFeedback("Payment verified successfully.");
          } catch (error) {
            setFeedback(readError(error));
          } finally {
            setPaying(false);
          }
        },
      }).open();
    } catch (error) {
      setFeedback(readError(error));
      setPaying(false);
    }
  };
  return (
    <span>
      <button
        className="button quiet small"
        disabled={disabled || paying}
        onClick={() => void pay()}
      >
        {paying ? "Opening…" : "Pay online"}
      </button>
      <Notice message={feedback} />
    </span>
  );
}
function RoleWelcome({ roles }: { roles: Role[] }) {
  const main = roles[0];
  const descriptions: Partial<Record<Role, string>> = {
    LOGBOOK_OPERATOR: "Your visitor logbook will be available once the private logbook is introduced in the next security phase.",
  };
  return (
    <section className="empty-state">
      <span>✓</span>
      <h2>{roleLabel[main]}</h2>
      <p>
        {descriptions[main] ??
          "Your workspace is ready. Choose an item from the sidebar to continue."}
      </p>
    </section>
  );
}
function SocietySetupPlaceholder() {
  return <section className="empty-state"><span>✓</span><h2>Society setup is being simplified</h2><p>Phase 2 will provide one guided place to create flats, appoint their owners, and invite logbook operators.</p></section>;
}
function FinancePlaceholder() {
  return <section className="empty-state"><span>₹</span><h2>Finance is being rebuilt for maintenance only</h2><p>The old finance manager and individual-charge screens are retired. The next finance phases will provide the maintenance policy, monthly bills, payments, and a clear ledger for Society Administrators.</p></section>;
}
function LogbookPlaceholder() {
  return <section className="empty-state"><span>⌁</span><h2>Your private logbook is being prepared</h2><p>Only assigned Logbook Operators will use this workspace. Visitor entry and exit will be introduced in the dedicated logbook phase.</p></section>;
}
function RegisterSociety() {
  const { user } = useAuth();
  const [form, setForm] = useState({
    contactName: user?.fullName ?? "",
    email: user?.email ?? "",
    phoneNumber: user?.phoneNumber ?? "",
    societyName: "",
    city: "",
    estimatedBlocks: "",
    estimatedFlats: "",
    message: "",
  });
  const [feedback, setFeedback] = useState("");
  const submit = async (event: FormEvent) => {
    event.preventDefault();
    setFeedback("");
    try {
      await secure("/api/onboarding/society-enquiries", {
        method: "POST",
        body: JSON.stringify({
          ...form,
          estimatedBlocks: form.estimatedBlocks
            ? Number(form.estimatedBlocks)
            : null,
          estimatedFlats: form.estimatedFlats
            ? Number(form.estimatedFlats)
            : null,
        }),
      });
      setFeedback(
        "Registration request submitted. Our team will contact you after review.",
      );
    } catch (error) {
      setFeedback(readError(error));
    }
  };
  return (
    <section className="page-panel form-panel">
      <p className="eyebrow">Society representative</p>
      <h1>Register a society</h1>
      <p className="muted">
        For a president, secretary, committee member, or authorised
        representative. The platform team verifies this request, creates the
        physical structure, and appoints the initial Society Manager.
      </p>
      <form onSubmit={submit}>
        <label>
          Your name
          <input
            value={form.contactName}
            onChange={(event) =>
              setForm({ ...form, contactName: event.target.value })
            }
            required
          />
        </label>
        <div className="two-cols">
          <label>
            Email address
            <input
              type="email"
              value={form.email}
              onChange={(event) =>
                setForm({ ...form, email: event.target.value })
              }
              required
            />
          </label>
          <label>
            Mobile number
            <input
              type="tel"
              inputMode="numeric"
              pattern="(?:\\+91[ -]?)?[6-9][0-9]{9}"
              value={form.phoneNumber}
              onChange={(event) =>
                setForm({ ...form, phoneNumber: event.target.value })
              }
              required
            />
          </label>
        </div>
        <label>
          Society name
          <input
            value={form.societyName}
            onChange={(event) =>
              setForm({ ...form, societyName: event.target.value })
            }
            required
          />
        </label>
        <label>
          City
          <input
            value={form.city}
            onChange={(event) => setForm({ ...form, city: event.target.value })}
          />
        </label>
        <div className="two-cols">
          <label>
            Estimated blocks
            <input
              type="number"
              min="0"
              value={form.estimatedBlocks}
              onChange={(event) =>
                setForm({ ...form, estimatedBlocks: event.target.value })
              }
            />
          </label>
          <label>
            Estimated flats
            <input
              type="number"
              min="0"
              value={form.estimatedFlats}
              onChange={(event) =>
                setForm({ ...form, estimatedFlats: event.target.value })
              }
            />
          </label>
        </div>
        <label>
          Additional details
          <textarea
            value={form.message}
            onChange={(event) =>
              setForm({ ...form, message: event.target.value })
            }
            maxLength={2000}
            placeholder="Tell us anything helpful about the society."
          />
        </label>
        <Notice message={feedback} />
        <button className="button">Send registration request</button>
      </form>
    </section>
  );
}
function JoinSociety() {
  const [societies, setSocieties] = useState<Society[]>([]);
  const [form, setForm] = useState({
    societyId: "",
    requestedRole: "RESIDENT",
    note: "",
  });
  const [feedback, setFeedback] = useState("");
  useEffect(() => {
    void secure<Society[]>("/api/onboarding/societies")
      .then(setSocieties)
      .catch((error) => setFeedback(readError(error)));
  }, []);
  const submit = async (event: FormEvent) => {
    event.preventDefault();
    setFeedback("");
    try {
      await secure("/api/onboarding/membership-requests", {
        method: "POST",
        body: JSON.stringify({
          societyId: Number(form.societyId),
          requestedRole: form.requestedRole,
          note: form.note,
        }),
      });
      setFeedback("Access request sent. A Society Manager will review it.");
    } catch (error) {
      setFeedback(readError(error));
    }
  };
  return (
    <section className="page-panel form-panel">
      <p className="eyebrow">Existing society</p>
      <h1>Join a society</h1>
      <p className="muted">
        Request the role you have in the society. Management roles are appointed
        after approval; they cannot be requested here.
      </p>
      <form onSubmit={submit}>
        <label>
          Society
          <select
            value={form.societyId}
            onChange={(event) =>
              setForm({ ...form, societyId: event.target.value })
            }
            required
          >
            <option value="">Select your society</option>
            {societies.map((society) => (
              <option key={society.id} value={society.id}>
                {society.name}
                {society.city ? ` — ${society.city}` : ""}
              </option>
            ))}
          </select>
        </label>
        <label>
          My role
          <select
            value={form.requestedRole}
            onChange={(event) =>
              setForm({ ...form, requestedRole: event.target.value })
            }
          >
            <option value="RESIDENT">Resident</option>
            <option value="STAFF">Society staff</option>
            <option value="SECURITY">Security guard</option>
          </select>
        </label>
        <label>
          Optional note
          <textarea
            value={form.note}
            onChange={(event) => setForm({ ...form, note: event.target.value })}
            placeholder="For example: flat number or employee number."
          />
        </label>
        <Notice message={feedback} />
        <button className="button">Request access</button>
      </form>
    </section>
  );
}
function MyRequests() {
  const [requests, setRequests] = useState<MembershipRequest[]>([]);
  const [feedback, setFeedback] = useState("");
  useEffect(() => {
    void secure<MembershipRequest[]>("/api/onboarding/membership-requests/mine")
      .then(setRequests)
      .catch((error) => setFeedback(readError(error)));
  }, []);
  return (
    <section className="page-panel">
      <p className="eyebrow">My onboarding</p>
      <h1>Access requests</h1>
      <Notice message={feedback} />
      {requests.length === 0 ? (
        <div className="empty-state">
          <span>○</span>
          <h2>No requests yet</h2>
          <p>
            You have not asked to join a society. This is not a pending
            approval.
          </p>
          <Link className="button" to="/join">
            Join a society
          </Link>
        </div>
      ) : (
        <div className="request-list">
          {requests.map((request) => (
            <article key={request.id} className="request-row">
              <div>
                <h2>{roleLabel[request.requestedRole]}</h2>
                <p>
                  Society #{request.societyId}
                  {request.note ? ` · ${request.note}` : ""}
                </p>
                <small>
                  Sent {new Date(request.createdAt).toLocaleString()}
                </small>
              </div>
              <Status value={request.status} />
            </article>
          ))}
        </div>
      )}
    </section>
  );
}
function Status({ value }: { value: string }) {
  return (
    <span className={`status ${value.toLowerCase()}`}>
      {value[0] + value.slice(1).toLowerCase()}
    </span>
  );
}
function Approvals() {
  const [societies, setSocieties] = useState<Society[]>([]);
  const [societyId, setSocietyId] = useState("");
  const [requests, setRequests] = useState<MembershipRequest[]>([]);
  const [feedback, setFeedback] = useState("");
  useEffect(() => {
    void secure<Society[]>("/api/onboarding/managed-societies")
      .then(setSocieties)
      .catch((error) => setFeedback(readError(error)));
  }, []);
  const load = async () => {
    if (!societyId) return setFeedback("Select a society first.");
    try {
      setFeedback("");
      setRequests(
        await secure<MembershipRequest[]>(
          `/api/onboarding/membership-requests/society/${societyId}`,
        ),
      );
    } catch (error) {
      setFeedback(readError(error));
    }
  };
  const decide = async (id: number, action: "approve" | "reject") => {
    try {
      await secure(`/api/onboarding/membership-requests/${id}/${action}`, {
        method: "POST",
      });
      setFeedback(`Request ${action}d.`);
      await load();
    } catch (error) {
      setFeedback(readError(error));
    }
  };
  return (
    <section className="page-panel">
      <p className="eyebrow">Society management</p>
      <h1>Membership approvals</h1>
      <p className="muted">
        Review pending Resident, Staff, and Security membership requests.
      </p>
      <div className="toolbar">
        <select
          value={societyId}
          onChange={(event) => {
            setSocietyId(event.target.value);
            setRequests([]);
          }}
        >
          <option value="">Select a society</option>
          {societies.map((society) => (
            <option key={society.id} value={society.id}>
              {society.name}
            </option>
          ))}
        </select>
        <button className="button" onClick={() => void load()}>
          View requests
        </button>
      </div>
      <Notice message={feedback} />
      {requests.length > 0 && (
        <div className="request-list">
          {requests.map((request) => (
            <article key={request.id} className="request-row">
              <div>
                <h2>{request.userName}</h2>
                <p>
                  {request.userEmail} · requested as{" "}
                  {roleLabel[request.requestedRole]}
                </p>
                {request.note && <small>{request.note}</small>}
              </div>
              <div className="row-actions">
                <button
                  className="button small"
                  onClick={() => void decide(request.id, "approve")}
                >
                  Approve
                </button>
                <button
                  className="button quiet small"
                  onClick={() => void decide(request.id, "reject")}
                >
                  Reject
                </button>
              </div>
            </article>
          ))}
        </div>
      )}
    </section>
  );
}
function ManagerComplaints() {
  const [societies, setSocieties] = useState<Society[]>([]);
  const [societyId, setSocietyId] = useState("");
  const [complaints, setComplaints] = useState<Complaint[]>([]);
  const [staff, setStaff] = useState<StaffAssignment[]>([]);
  const [feedback, setFeedback] = useState("");
  useEffect(() => {
    void secure<Society[]>("/api/onboarding/managed-societies")
      .then(setSocieties)
      .catch((error) => setFeedback(readError(error)));
  }, []);
  const load = async () => {
    if (!societyId) return setFeedback("Select a society first.");
    try {
      setFeedback("");
      const [items, workers] = await Promise.all([
        secure<Complaint[]>(`/api/complaints/society/${societyId}`),
        secure<StaffAssignment[]>(
          `/api/staff-assignments/society/${societyId}`,
        ),
      ]);
      setComplaints(items);
      setStaff(workers.filter((item) => item.active));
    } catch (error) {
      setFeedback(readError(error));
    }
  };
  const updateStatus = async (id: number, status: string) => {
    try {
      await secure(`/api/complaints/${id}/status`, {
        method: "PUT",
        body: JSON.stringify({
          status,
          note: "Updated from the management workspace",
        }),
      });
      await load();
      setFeedback("Complaint status updated.");
    } catch (error) {
      setFeedback(readError(error));
    }
  };
  const assign = async (id: number, staffAssignmentId: string) => {
    if (!staffAssignmentId) return;
    try {
      await secure(`/api/complaints/${id}/assign`, {
        method: "PUT",
        body: JSON.stringify({
          staffAssignmentId: Number(staffAssignmentId),
          note: "Assigned from the management workspace",
        }),
      });
      setFeedback("Complaint assigned.");
      await load();
    } catch (error) {
      setFeedback(readError(error));
    }
  };
  return (
    <section className="admin-page">
      <header className="page-heading">
        <p className="eyebrow">Society management</p>
        <h1>Complaint operations</h1>
        <p className="muted">
          Assign resident concerns to active staff and track their resolution.
        </p>
      </header>
      <div className="toolbar">
        <select
          value={societyId}
          onChange={(event) => {
            setSocietyId(event.target.value);
            setComplaints([]);
          }}
        >
          <option value="">Select a society</option>
          {societies.map((society) => (
            <option key={society.id} value={society.id}>
              {society.name}
            </option>
          ))}
        </select>
        <button className="button" onClick={() => void load()}>
          Load complaints
        </button>
      </div>
      <Notice message={feedback} />
      {complaints.length ? (
        <div className="request-list manager-list">
          {complaints.map((complaint) => (
            <article className="request-row" key={complaint.id}>
              <div>
                <h3>{complaint.title}</h3>
                <p>
                  {complaint.category.replaceAll("_", " ")} ·{" "}
                  {complaint.priority} priority
                </p>
                <small>{complaint.description}</small>
              </div>
              <div className="row-actions">
                <Status value={complaint.status} />
                {complaint.status === "OPEN" && (
                  <select
                    aria-label="Assign staff"
                    defaultValue=""
                    onChange={(event) =>
                      void assign(complaint.id, event.target.value)
                    }
                  >
                    <option value="">Assign staff</option>
                    {staff.map((item) => (
                      <option key={item.id} value={item.id}>
                        {item.staffName} · {item.staffType.replaceAll("_", " ")}
                      </option>
                    ))}
                  </select>
                )}
                {["ASSIGNED", "IN_PROGRESS", "RESOLVED"].includes(
                  complaint.status,
                ) && (
                  <select
                    aria-label="Complaint status"
                    defaultValue=""
                    onChange={(event) =>
                      event.target.value &&
                      void updateStatus(complaint.id, event.target.value)
                    }
                  >
                    <option value="">Update status</option>
                    {complaint.status === "ASSIGNED" && (
                      <option value="IN_PROGRESS">In progress</option>
                    )}
                    {complaint.status === "IN_PROGRESS" && (
                      <option value="RESOLVED">Resolved</option>
                    )}
                    {complaint.status === "RESOLVED" && (
                      <option value="CLOSED">Closed</option>
                    )}
                  </select>
                )}
              </div>
            </article>
          ))}
        </div>
      ) : (
        <section className="empty-state">
          <span>✓</span>
          <h2>No complaints loaded</h2>
          <p>Select a society to review its complaints.</p>
        </section>
      )}
    </section>
  );
}
function BlockManagerDashboard() {
  const [assignments, setAssignments] = useState<BlockManagerAssignment[]>([]);
  const [complaints, setComplaints] = useState<Complaint[]>([]);
  const [feedback, setFeedback] = useState("");
  const load = async () => {
    try {
      const blocks = await secure<BlockManagerAssignment[]>(
        "/api/block-manager-assignments/mine",
      );
      setAssignments(blocks);
      const responses = await Promise.all(
        blocks.map((block) =>
          secure<Complaint[]>(`/api/complaints/block/${block.blockId}`),
        ),
      );
      setComplaints(responses.flat());
    } catch (error) {
      setFeedback(readError(error));
    }
  };
  useEffect(() => {
    void load();
  }, []);
  const updateStatus = async (id: number, status: string) => {
    try {
      await secure(`/api/complaints/${id}/status`, {
        method: "PUT",
        body: JSON.stringify({
          status,
          note: "Updated from the block management workspace",
        }),
      });
      setFeedback("Complaint status updated.");
      await load();
    } catch (error) {
      setFeedback(readError(error));
    }
  };
  const transitions: Record<string, string[]> = {
    ASSIGNED: ["IN_PROGRESS"],
    IN_PROGRESS: ["RESOLVED"],
    RESOLVED: ["CLOSED"],
  };
  return (
    <section className="admin-page">
      <header className="page-heading">
        <p className="eyebrow">Block management</p>
        <h1>My blocks and complaints</h1>
        <p className="muted">
          You can view and manage work only within the blocks assigned to you.
        </p>
      </header>
      <Notice message={feedback} />
      <div className="content-grid">
        <section className="page-panel">
          <h2>Assigned blocks</h2>
          {assignments.length ? (
            <div className="request-list">
              {assignments.map((assignment) => (
                <article className="request-row" key={assignment.id}>
                  <div>
                    <h3>{assignment.blockName}</h3>
                    <p>Block Manager scope</p>
                  </div>
                  <Status value="ASSIGNED" />
                </article>
              ))}
            </div>
          ) : (
            <div className="empty-state">
              <span>⌂</span>
              <h2>No blocks assigned</h2>
              <p>
                Ask your Society Manager to assign one or more blocks to your
                account.
              </p>
            </div>
          )}
        </section>
        <section className="page-panel wide">
          <h2>Complaints in my blocks</h2>
          {complaints.length ? (
            <div className="request-list manager-list">
              {complaints.map((complaint) => (
                <article className="request-row" key={complaint.id}>
                  <div>
                    <h3>{complaint.title}</h3>
                    <p>
                      {complaint.category.replaceAll("_", " ")} ·{" "}
                      {complaint.priority} priority
                    </p>
                    <small>{complaint.description}</small>
                  </div>
                  <div className="row-actions">
                    <Status value={complaint.status} />
                    {(transitions[complaint.status] ?? []).map((status) => (
                      <button
                        className="button quiet small"
                        key={status}
                        onClick={() => void updateStatus(complaint.id, status)}
                      >
                        Mark {status.replaceAll("_", " ").toLowerCase()}
                      </button>
                    ))}
                  </div>
                </article>
              ))}
            </div>
          ) : (
            <p className="muted">
              There are no complaints in your assigned blocks.
            </p>
          )}
        </section>
      </div>
    </section>
  );
}
function ResidentManagement() {
  const [societies, setSocieties] = useState<Society[]>([]);
  const [residents, setResidents] = useState<ResidentRecord[]>([]);
  const [societyId, setSocietyId] = useState("");
  const [blocks, setBlocks] = useState<Block[]>([]);
  const [blockId, setBlockId] = useState("");
  const [flats, setFlats] = useState<Flat[]>([]);
  const [flatId, setFlatId] = useState("");
  const [occupancies, setOccupancies] = useState<Occupancy[]>([]);
  const [form, setForm] = useState({
    residentId: "",
    occupancyType: "OWNER",
    residentRole: "PRIMARY",
    startDate: new Date().toISOString().slice(0, 10),
  });
  const [feedback, setFeedback] = useState("");
  useEffect(() => {
    void Promise.all([
      secure<Society[]>("/api/onboarding/managed-societies"),
      secure<ResidentRecord[]>("/api/residents"),
    ])
      .then(([loadedSocieties, loadedResidents]) => {
        setSocieties(loadedSocieties);
        setResidents(loadedResidents);
      })
      .catch((error) => setFeedback(readError(error)));
  }, []);
  useEffect(() => {
    if (!societyId) {
      setBlocks([]);
      return;
    }
    void secure<Block[]>(`/api/blocks/society/${societyId}`)
      .then(setBlocks)
      .catch((error) => setFeedback(readError(error)));
  }, [societyId]);
  useEffect(() => {
    if (!blockId) {
      setFlats([]);
      return;
    }
    void secure<Flat[]>(`/api/flats/block/${blockId}`)
      .then(setFlats)
      .catch((error) => setFeedback(readError(error)));
  }, [blockId]);
  useEffect(() => {
    if (!flatId) {
      setOccupancies([]);
      return;
    }
    void secure<Occupancy[]>(`/api/flat-occupancies/flat/${flatId}`)
      .then(setOccupancies)
      .catch((error) => setFeedback(readError(error)));
  }, [flatId]);
  const assign = async (event: FormEvent) => {
    event.preventDefault();
    try {
      await secure("/api/flat-occupancies", {
        method: "POST",
        body: JSON.stringify({
          flatId: Number(flatId),
          residentId: Number(form.residentId),
          occupancyType: form.occupancyType,
          residentRole: form.residentRole,
          startDate: form.startDate,
          active: true,
        }),
      });
      setFeedback("Resident linked to the selected flat.");
      setForm({ ...form, residentId: "" });
      const updated = await secure<Occupancy[]>(
        `/api/flat-occupancies/flat/${flatId}`,
      );
      setOccupancies(updated);
    } catch (error) {
      setFeedback(readError(error));
    }
  };
  const deactivate = async (id: number) => {
    try {
      await secure(`/api/flat-occupancies/${id}`, { method: "DELETE" });
      setFeedback("Flat occupancy deactivated.");
      setOccupancies(
        await secure<Occupancy[]>(`/api/flat-occupancies/flat/${flatId}`),
      );
    } catch (error) {
      setFeedback(readError(error));
    }
  };
  return (
    <section className="admin-page">
      <header className="page-heading">
        <p className="eyebrow">Society management</p>
        <h1>Residents and homes</h1>
        <p className="muted">
          Approved resident accounts need an active flat link before resident
          features such as bills and complaints can work.
        </p>
      </header>
      <Notice message={feedback} />
      <div className="content-grid">
        <section className="page-panel form-panel">
          <h2>Link a resident to a flat</h2>
          <form onSubmit={assign}>
            <label>
              Society
              <select
                value={societyId}
                onChange={(event) => {
                  setSocietyId(event.target.value);
                  setBlockId("");
                  setFlatId("");
                }}
                required
              >
                <option value="">Select society</option>
                {societies.map((society) => (
                  <option key={society.id} value={society.id}>
                    {society.name}
                  </option>
                ))}
              </select>
            </label>
            <label>
              Block
              <select
                value={blockId}
                onChange={(event) => {
                  setBlockId(event.target.value);
                  setFlatId("");
                }}
                required
              >
                <option value="">Select block</option>
                {blocks.map((block) => (
                  <option key={block.id} value={block.id}>
                    {block.name}
                  </option>
                ))}
              </select>
            </label>
            <label>
              Flat
              <select
                value={flatId}
                onChange={(event) => setFlatId(event.target.value)}
                required
              >
                <option value="">Select flat</option>
                {flats
                  .filter((flat) => flat.active)
                  .map((flat) => (
                    <option key={flat.id} value={flat.id}>
                      {flat.flatNumber}
                    </option>
                  ))}
              </select>
            </label>
            <label>
              Resident
              <select
                value={form.residentId}
                onChange={(event) =>
                  setForm({ ...form, residentId: event.target.value })
                }
                required
              >
                <option value="">Select approved resident</option>
                {residents
                  .filter((resident) => resident.active)
                  .map((resident) => (
                    <option key={resident.id} value={resident.id}>
                      {resident.fullName} · #{resident.id}
                    </option>
                  ))}
              </select>
            </label>
            <div className="two-cols">
              <label>
                Occupancy
                <select
                  value={form.occupancyType}
                  onChange={(event) =>
                    setForm({ ...form, occupancyType: event.target.value })
                  }
                >
                  <option value="OWNER">Owner</option>
                  <option value="TENANT">Tenant</option>
                </select>
              </label>
              <label>
                Resident type
                <select
                  value={form.residentRole}
                  onChange={(event) =>
                    setForm({ ...form, residentRole: event.target.value })
                  }
                >
                  <option value="PRIMARY">Primary resident</option>
                  <option value="FAMILY_MEMBER">Family member</option>
                </select>
              </label>
            </div>
            <label>
              Start date
              <input
                type="date"
                value={form.startDate}
                onChange={(event) =>
                  setForm({ ...form, startDate: event.target.value })
                }
                required
              />
            </label>
            <button
              className="button"
              disabled={!societyId || !blockId || !flatId}
            >
              Link resident to flat
            </button>
          </form>
        </section>
        <section className="page-panel">
          <h2>Current flat residents</h2>
          <label>
            Society
            <select
              value={societyId}
              onChange={(event) => {
                setSocietyId(event.target.value);
                setBlockId("");
                setFlatId("");
              }}
            >
              <option value="">Select society</option>
              {societies.map((society) => (
                <option key={society.id} value={society.id}>
                  {society.name}
                </option>
              ))}
            </select>
          </label>
          <label>
            Block
            <select
              value={blockId}
              onChange={(event) => {
                setBlockId(event.target.value);
                setFlatId("");
              }}
            >
              <option value="">Select block</option>
              {blocks.map((block) => (
                <option key={block.id} value={block.id}>
                  {block.name}
                </option>
              ))}
            </select>
          </label>
          <label>
            Flat
            <select
              value={flatId}
              onChange={(event) => setFlatId(event.target.value)}
            >
              <option value="">Select flat</option>
              {flats.map((flat) => (
                <option key={flat.id} value={flat.id}>
                  {flat.flatNumber}
                </option>
              ))}
            </select>
          </label>
          {occupancies.filter((occupancy) => occupancy.active).length ? (
            <div className="request-list">
              {occupancies
                .filter((occupancy) => occupancy.active)
                .map((occupancy) => {
                  const resident = residents.find(
                    (item) => item.id === occupancy.residentId,
                  );
                  return (
                    <article className="request-row" key={occupancy.id}>
                      <div>
                        <h3>
                          {resident?.fullName ??
                            `Resident #${occupancy.residentId}`}
                        </h3>
                        <p>
                          {occupancy.occupancyType.toLowerCase()} ·{" "}
                          {occupancy.residentRole
                            .replaceAll("_", " ")
                            .toLowerCase()}
                        </p>
                        <small>
                          Since{" "}
                          {new Date(occupancy.startDate).toLocaleDateString()}
                        </small>
                      </div>
                      <button
                        className="button quiet small"
                        onClick={() => void deactivate(occupancy.id)}
                      >
                        End occupancy
                      </button>
                    </article>
                  );
                })}
            </div>
          ) : (
            <p className="inline-note">
              Choose a flat to view its active residents.
            </p>
          )}
        </section>
      </div>
    </section>
  );
}
function StaffManagement() {
  const [societies, setSocieties] = useState<Society[]>([]);
  const [members, setMembers] = useState<SocietyMember[]>([]);
  const [profiles, setProfiles] = useState<StaffProfile[]>([]);
  const [assignments, setAssignments] = useState<StaffAssignment[]>([]);
  const [societyId, setSocietyId] = useState("");
  const [form, setForm] = useState({
    userId: "",
    phoneNumber: "",
    staffType: "MAINTENANCE",
  });
  const [feedback, setFeedback] = useState("");
  useEffect(() => {
    void secure<Society[]>("/api/onboarding/managed-societies")
      .then(setSocieties)
      .catch((error) => setFeedback(readError(error)));
  }, []);
  const load = async () => {
    if (!societyId) return;
    try {
      const [loadedMembers, loadedProfiles, loadedAssignments] =
        await Promise.all([
          secure<SocietyMember[]>(
            `/api/society-roles/societies/${societyId}/members`,
          ),
          secure<StaffProfile[]>("/api/staff-profiles"),
          secure<StaffAssignment[]>(
            `/api/staff-assignments/society/${societyId}`,
          ),
        ]);
      setMembers(loadedMembers);
      setProfiles(loadedProfiles);
      setAssignments(loadedAssignments);
    } catch (error) {
      setFeedback(readError(error));
    }
  };
  useEffect(() => {
    setMembers([]);
    setAssignments([]);
    void load();
  }, [societyId]);
  const selected = members.find(
    (member) => member.userId === Number(form.userId),
  );
  const save = async (event: FormEvent) => {
    event.preventDefault();
    if (!societyId || !selected) return;
    try {
      let profile = profiles.find((item) => item.userId === selected.userId);
      if (!profile)
        profile = await secure<StaffProfile>("/api/staff-profiles", {
          method: "POST",
          body: JSON.stringify({
            userId: selected.userId,
            fullName: selected.fullName,
            email: selected.email,
            phoneNumber: form.phoneNumber,
          }),
        });
      const existing = assignments.find(
        (item) => item.staffProfileId === profile!.id,
      );
      if (existing)
        await secure(`/api/staff-assignments/${existing.id}`, {
          method: "PUT",
          body: JSON.stringify({
            staffProfileId: profile.id,
            societyId: Number(societyId),
            staffType: form.staffType,
            joiningDate: new Date().toISOString().slice(0, 10),
            active: true,
          }),
        });
      else
        await secure("/api/staff-assignments", {
          method: "POST",
          body: JSON.stringify({
            staffProfileId: profile.id,
            societyId: Number(societyId),
            staffType: form.staffType,
            joiningDate: new Date().toISOString().slice(0, 10),
            active: true,
          }),
        });
      setFeedback("Staff assignment saved.");
      setForm({ userId: "", phoneNumber: "", staffType: "MAINTENANCE" });
      await load();
    } catch (error) {
      setFeedback(readError(error));
    }
  };
  const deactivate = async (id: number) => {
    try {
      await secure(`/api/staff-assignments/${id}`, { method: "DELETE" });
      setFeedback("Staff assignment deactivated.");
      await load();
    } catch (error) {
      setFeedback(readError(error));
    }
  };
  const candidates = members.filter(
    (member) =>
      member.roles.includes("STAFF") || member.roles.includes("SECURITY"),
  );
  return (
    <section className="admin-page">
      <header className="page-heading">
        <p className="eyebrow">Society management</p>
        <h1>Staff and security</h1>
        <p className="muted">
          Create employment assignments for approved staff and security
          accounts.
        </p>
      </header>
      <Notice message={feedback} />
      <section className="filter-card">
        <label>
          Society
          <select
            value={societyId}
            onChange={(event) => setSocietyId(event.target.value)}
          >
            <option value="">Select society</option>
            {societies.map((society) => (
              <option key={society.id} value={society.id}>
                {society.name}
              </option>
            ))}
          </select>
        </label>
      </section>
      {societyId && (
        <div className="content-grid">
          <section className="page-panel form-panel">
            <h2>Assign staff work</h2>
            <form onSubmit={save}>
              <label>
                Approved staff or security user
                <select
                  value={form.userId}
                  onChange={(event) =>
                    setForm({ ...form, userId: event.target.value })
                  }
                  required
                >
                  <option value="">Select user</option>
                  {candidates.map((member) => (
                    <option key={member.userId} value={member.userId}>
                      {member.fullName} · {member.email}
                    </option>
                  ))}
                </select>
              </label>
              <label>
                Phone number
                <input
                  value={form.phoneNumber}
                  onChange={(event) =>
                    setForm({ ...form, phoneNumber: event.target.value })
                  }
                  placeholder="Required for a new staff profile"
                  required={
                    !profiles.some(
                      (profile) => profile.userId === Number(form.userId),
                    )
                  }
                />
              </label>
              <label>
                Designation
                <select
                  value={form.staffType}
                  onChange={(event) =>
                    setForm({ ...form, staffType: event.target.value })
                  }
                >
                  {[
                    "SECURITY_GUARD",
                    "CLEANER",
                    "ELECTRICIAN",
                    "PLUMBER",
                    "MAINTENANCE",
                    "GARDENER",
                    "OTHER",
                  ].map((type) => (
                    <option key={type} value={type}>
                      {type.replaceAll("_", " ")}
                    </option>
                  ))}
                </select>
              </label>
              <button className="button">Save staff assignment</button>
            </form>
          </section>
          <section className="page-panel">
            <h2>Current staff</h2>
            {assignments.length ? (
              <div className="request-list">
                {assignments.map((assignment) => (
                  <article className="request-row" key={assignment.id}>
                    <div>
                      <h3>{assignment.staffName}</h3>
                      <p>{assignment.staffType.replaceAll("_", " ")}</p>
                      <small>{assignment.active ? "Active" : "Inactive"}</small>
                    </div>
                    {assignment.active && (
                      <button
                        className="button quiet small"
                        onClick={() => void deactivate(assignment.id)}
                      >
                        Deactivate
                      </button>
                    )}
                  </article>
                ))}
              </div>
            ) : (
              <p className="muted">No staff assignments yet.</p>
            )}
          </section>
        </div>
      )}
    </section>
  );
}
function StaffDashboard() {
  const [workspace, setWorkspace] = useState<StaffWorkspace | null>(null);
  const [feedback, setFeedback] = useState("");
  const load = () =>
    secure<StaffWorkspace>("/api/staff-workspace")
      .then(setWorkspace)
      .catch((error) => setFeedback(readError(error)));
  useEffect(() => {
    void load();
  }, []);
  const update = async (id: number, status: string) => {
    try {
      await secure(`/api/complaints/${id}/status`, {
        method: "PUT",
        body: JSON.stringify({
          status,
          note: "Updated by assigned staff member",
        }),
      });
      setFeedback("Work status updated.");
      await load();
    } catch (error) {
      setFeedback(readError(error));
    }
  };
  return (
    <section className="admin-page">
      <header className="page-heading">
        <p className="eyebrow">Staff workspace</p>
        <h1>My assigned work</h1>
        <p className="muted">Update only the complaints assigned to you.</p>
      </header>
      <Notice message={feedback} />
      {workspace && (
        <>
          <section className="page-panel">
            <h2>My assignments</h2>
            {workspace.assignments.length ? (
              workspace.assignments.map((item) => (
                <p key={item.id} className="inline-note">
                  {item.staffType.replaceAll("_", " ")} · Society #
                  {item.societyId}
                </p>
              ))
            ) : (
              <p className="muted">
                Your manager has not created an active staff assignment yet.
              </p>
            )}
          </section>
          <section className="page-panel wide">
            <h2>Assigned complaints</h2>
            {workspace.complaints.length ? (
              <div className="request-list">
                {workspace.complaints.map((complaint) => (
                  <article className="request-row" key={complaint.id}>
                    <div>
                      <h3>{complaint.title}</h3>
                      <p>
                        {complaint.category.replaceAll("_", " ")} ·{" "}
                        {complaint.priority}
                      </p>
                      <small>{complaint.description}</small>
                    </div>
                    <div className="row-actions">
                      <Status value={complaint.status} />
                      {complaint.status === "ASSIGNED" && (
                        <button
                          className="button quiet small"
                          onClick={() =>
                            void update(complaint.id, "IN_PROGRESS")
                          }
                        >
                          Start work
                        </button>
                      )}
                      {complaint.status === "IN_PROGRESS" && (
                        <button
                          className="button quiet small"
                          onClick={() => void update(complaint.id, "RESOLVED")}
                        >
                          Mark resolved
                        </button>
                      )}
                    </div>
                  </article>
                ))}
              </div>
            ) : (
              <p className="muted">No complaints are assigned to you.</p>
            )}
          </section>
        </>
      )}
    </section>
  );
}
function RolesAndAuthority() {
  const [societies, setSocieties] = useState<Society[]>([]);
  const [members, setMembers] = useState<SocietyMember[]>([]);
  const [roles, setRoles] = useState<RoleAssignment[]>([]);
  const [blocks, setBlocks] = useState<Block[]>([]);
  const [blockAssignments, setBlockAssignments] = useState<
    BlockManagerAssignment[]
  >([]);
  const [societyId, setSocietyId] = useState("");
  const [roleForm, setRoleForm] = useState({
    userId: "",
    role: "FINANCE_MANAGER" as Role,
  });
  const [blockForm, setBlockForm] = useState({ userId: "", blockId: "" });
  const [feedback, setFeedback] = useState("");
  useEffect(() => {
    void secure<Society[]>("/api/onboarding/managed-societies")
      .then(setSocieties)
      .catch((error) => setFeedback(readError(error)));
  }, []);
  const load = async (id = societyId) => {
    if (!id) return;
    try {
      const [loadedMembers, loadedRoles, loadedBlocks, loadedAssignments] =
        await Promise.all([
          secure<SocietyMember[]>(`/api/society-roles/societies/${id}/members`),
          secure<RoleAssignment[]>(`/api/society-roles/societies/${id}`),
          secure<Block[]>(`/api/blocks/society/${id}`),
          secure<BlockManagerAssignment[]>(
            `/api/block-manager-assignments/society/${id}`,
          ),
        ]);
      setMembers(loadedMembers);
      setRoles(loadedRoles);
      setBlocks(loadedBlocks);
      setBlockAssignments(loadedAssignments);
    } catch (error) {
      setFeedback(readError(error));
    }
  };
  useEffect(() => {
    setMembers([]);
    setRoles([]);
    setBlocks([]);
    setBlockAssignments([]);
    setRoleForm({ userId: "", role: "FINANCE_MANAGER" });
    setBlockForm({ userId: "", blockId: "" });
    void load(societyId);
  }, [societyId]);
  const assignRole = async (event: FormEvent) => {
    event.preventDefault();
    if (!societyId || !roleForm.userId) return;
    try {
      await secure(`/api/admin/users/${roleForm.userId}/roles`, {
        method: "POST",
        body: JSON.stringify({
          societyId: Number(societyId),
          role: roleForm.role,
        }),
      });
      setFeedback(`${roleLabel[roleForm.role]} assigned.`);
      setRoleForm({ userId: "", role: "FINANCE_MANAGER" });
      await load();
    } catch (error) {
      setFeedback(readError(error));
    }
  };
  const revokeRole = async (id: number) => {
    try {
      await secure(`/api/admin/users/roles/${id}`, { method: "DELETE" });
      setFeedback("Delegated role removed.");
      await load();
    } catch (error) {
      setFeedback(readError(error));
    }
  };
  const assignBlock = async (event: FormEvent) => {
    event.preventDefault();
    if (!blockForm.userId || !blockForm.blockId) return;
    try {
      await secure("/api/block-manager-assignments", {
        method: "POST",
        body: JSON.stringify({
          userId: Number(blockForm.userId),
          blockId: Number(blockForm.blockId),
        }),
      });
      setFeedback("Block manager assignment saved.");
      setBlockForm({ userId: "", blockId: "" });
      await load();
    } catch (error) {
      setFeedback(readError(error));
    }
  };
  const revokeBlock = async (id: number) => {
    try {
      await secure(`/api/block-manager-assignments/${id}`, {
        method: "DELETE",
      });
      setFeedback("Block manager assignment removed.");
      await load();
    } catch (error) {
      setFeedback(readError(error));
    }
  };
  const delegatedRoles: Role[] = [
    "FINANCE_MANAGER",
    "SECURITY_MANAGER",
    "BLOCK_MANAGER",
  ];
  const delegated = roles.filter((item) => delegatedRoles.includes(item.role));
  const blockManagers = members.filter((member) =>
    member.roles.includes("BLOCK_MANAGER"),
  );
  return (
    <section className="admin-page">
      <header className="page-heading">
        <p className="eyebrow">Society management</p>
        <h1>Roles and authority</h1>
        <p className="muted">
          Delegate operational responsibility only to people who are already
          approved members of this society.
        </p>
      </header>
      <Notice message={feedback} />
      <section className="filter-card">
        <label>
          Society
          <select
            value={societyId}
            onChange={(event) => setSocietyId(event.target.value)}
          >
            <option value="">Select society</option>
            {societies.map((society) => (
              <option key={society.id} value={society.id}>
                {society.name}
              </option>
            ))}
          </select>
        </label>
      </section>
      {societyId && (
        <div className="content-grid">
          <section className="page-panel form-panel">
            <h2>Assign a delegated role</h2>
            <form onSubmit={assignRole}>
              <label>
                Approved member
                <select
                  value={roleForm.userId}
                  onChange={(event) =>
                    setRoleForm({ ...roleForm, userId: event.target.value })
                  }
                  required
                >
                  <option value="">Select member</option>
                  {members.map((member) => (
                    <option key={member.userId} value={member.userId}>
                      {member.fullName} · {member.email}
                    </option>
                  ))}
                </select>
              </label>
              <label>
                Operational role
                <select
                  value={roleForm.role}
                  onChange={(event) =>
                    setRoleForm({
                      ...roleForm,
                      role: event.target.value as Role,
                    })
                  }
                >
                  {delegatedRoles.map((role) => (
                    <option key={role} value={role}>
                      {roleLabel[role]}
                    </option>
                  ))}
                </select>
              </label>
              <p className="inline-note">
                Only the Platform Admin appoints Society Managers. This page
                delegates finance, security, and block responsibilities.
              </p>
              <button className="button">Assign role</button>
            </form>
            <h2 className="subheading">Current delegated roles</h2>
            {delegated.length ? (
              <div className="request-list">
                {delegated.map((assignment) => {
                  const member = members.find(
                    (item) => item.userId === assignment.userId,
                  );
                  return (
                    <article className="request-row" key={assignment.id}>
                      <div>
                        <h3>
                          {member?.fullName ?? `User #${assignment.userId}`}
                        </h3>
                        <p>{roleLabel[assignment.role]}</p>
                        <small>{member?.email}</small>
                      </div>
                      <button
                        className="button quiet small"
                        onClick={() => void revokeRole(assignment.id)}
                      >
                        Remove role
                      </button>
                    </article>
                  );
                })}
              </div>
            ) : (
              <p className="muted">No delegated roles have been assigned.</p>
            )}
          </section>
          <section className="page-panel form-panel">
            <h2>Assign blocks to a Block Manager</h2>
            <form onSubmit={assignBlock}>
              <label>
                Block Manager
                <select
                  value={blockForm.userId}
                  onChange={(event) =>
                    setBlockForm({ ...blockForm, userId: event.target.value })
                  }
                  required
                >
                  <option value="">Select Block Manager</option>
                  {blockManagers.map((member) => (
                    <option key={member.userId} value={member.userId}>
                      {member.fullName}
                    </option>
                  ))}
                </select>
              </label>
              <label>
                Block
                <select
                  value={blockForm.blockId}
                  onChange={(event) =>
                    setBlockForm({ ...blockForm, blockId: event.target.value })
                  }
                  required
                >
                  <option value="">Select active block</option>
                  {blocks
                    .filter((block) => block.active)
                    .map((block) => (
                      <option key={block.id} value={block.id}>
                        {block.name}
                      </option>
                    ))}
                </select>
              </label>
              <p className="inline-note">
                A Block Manager can be assigned to more than one block. Assign
                the Block Manager role first.
              </p>
              <button className="button" disabled={!blockManagers.length}>
                Assign block
              </button>
            </form>
            <h2 className="subheading">Block assignments</h2>
            {blockAssignments.length ? (
              <div className="request-list">
                {blockAssignments.map((assignment) => (
                  <article className="request-row" key={assignment.id}>
                    <div>
                      <h3>{assignment.userName}</h3>
                      <p>{assignment.blockName}</p>
                      <small>Block Manager scope</small>
                    </div>
                    <button
                      className="button quiet small"
                      onClick={() => void revokeBlock(assignment.id)}
                    >
                      Remove block
                    </button>
                  </article>
                ))}
              </div>
            ) : (
              <p className="muted">
                No blocks have been assigned to a Block Manager.
              </p>
            )}
          </section>
          <section className="page-panel wide">
            <h2>Approved society members</h2>
            {members.length ? (
              <div className="table-wrap">
                <table>
                  <thead>
                    <tr>
                      <th>Name</th>
                      <th>Email</th>
                      <th>Current access</th>
                    </tr>
                  </thead>
                  <tbody>
                    {members.map((member) => (
                      <tr key={member.userId}>
                        <td>{member.fullName}</td>
                        <td>{member.email}</td>
                        <td>
                          {member.roles
                            .map((role) => roleLabel[role])
                            .join(", ")}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            ) : (
              <p className="muted">
                No approved members are available yet. Approve membership
                requests first.
              </p>
            )}
          </section>
        </div>
      )}
    </section>
  );
}
function Notifications() {
  const [notifications, setNotifications] = useState<Notification[]>([]);
  const [feedback, setFeedback] = useState("");
  const [mode, setMode] = useState<"ALL" | "UNREAD">("ALL");
  const [type, setType] = useState("ALL");
  const load = () =>
    secure<Notification[]>("/api/notifications/me")
      .then(setNotifications)
      .catch((error) => setFeedback(readError(error)));
  useEffect(() => {
    void load();
  }, []);
  const markRead = async (id: number) => {
    try {
      await secure(`/api/notifications/${id}/read`, { method: "PATCH" });
      await load();
    } catch (error) {
      setFeedback(readError(error));
    }
  };
  const markAllRead = async () => {
    try {
      const result = await secure<{ markedRead: number }>("/api/notifications/read-all", { method: "PATCH" });
      setFeedback(result.markedRead ? `${result.markedRead} notification${result.markedRead === 1 ? "" : "s"} marked as read.` : "There are no unread notifications.");
      await load();
    } catch (error) { setFeedback(readError(error)); }
  };
  const visible = notifications.filter(notification => (mode === "ALL" || !notification.read) && (type === "ALL" || notification.type === type));
  const unread = notifications.filter(notification => !notification.read).length;
  return (
    <section className="admin-page">
      <header className="page-heading"><p className="eyebrow">My inbox</p><h1>Notifications</h1><p className="muted">Important changes to your society access, maintenance, payments, and complaints appear here. Email copies are sent when SMTP delivery is enabled.</p></header>
      <Notice message={feedback} />
      <section className="filter-card notification-toolbar"><label>Show<select value={mode} onChange={event => setMode(event.target.value as "ALL" | "UNREAD")}><option value="ALL">All notifications</option><option value="UNREAD">Unread only</option></select></label><label>Type<select value={type} onChange={event => setType(event.target.value)}><option value="ALL">All types</option>{[...new Set(notifications.map(notification => notification.type))].map(value => <option key={value} value={value}>{value.replaceAll("_", " ").toLowerCase()}</option>)}</select></label><span className="inline-note"><strong>{unread}</strong> unread</span><button className="button quiet small" disabled={!unread} onClick={() => void markAllRead()}>Mark all read</button></section>
      {visible.length ? (
        <div className="request-list">
          {visible.map((notification) => (
            <article
              key={notification.id}
              className={`notice-row ${notification.read ? "" : "unread"}`}
            >
              <div>
                <h3>{notification.title}</h3>
                <p>{notification.message}</p>
                <small>
                  {notification.type.replaceAll("_", " ")} ·{" "}
                  {new Date(notification.createdAt).toLocaleString()}
                </small>
              </div>
              {!notification.read && (
                <button
                  className="button quiet small"
                  onClick={() => void markRead(notification.id)}
                >
                  Mark read
                </button>
              )}
            </article>
          ))}
        </div>
      ) : (
        <div className="empty-state">
          <span>✓</span>
          <h2>{notifications.length ? "No notifications match these filters" : "You are up to date"}</h2>
          <p>{notifications.length ? "Change the filters to view more messages." : "There are no notifications yet."}</p>
        </div>
      )}
    </section>
  );
}
function AdminOnboarding() {
  const [enquiries, setEnquiries] = useState<Enquiry[]>([]);
  const [users, setUsers] = useState<
    { id: number; fullName: string; email: string }[]
  >([]);
  const [societies, setSocieties] = useState<Society[]>([]);
  const [society, setSociety] = useState({
    name: "",
    address: "",
    city: "",
    state: "",
    pincode: "",
  });
  const [manager, setManager] = useState({ userId: "", societyId: "" });
  const [feedback, setFeedback] = useState("");
  const load = () =>
    Promise.all([
      secure<Enquiry[]>("/api/onboarding/society-enquiries"),
      secure<{ id: number; fullName: string; email: string }[]>("/api/users"),
      secure<Society[]>("/api/societies"),
    ])
      .then(([loadedEnquiries, loadedUsers, loadedSocieties]) => {
        setEnquiries(loadedEnquiries);
        setUsers(loadedUsers);
        setSocieties(loadedSocieties);
      })
      .catch((error) => setFeedback(readError(error)));
  useEffect(() => {
    void load();
  }, []);
  const createSociety = async (event: FormEvent) => {
    event.preventDefault();
    try {
      await secure("/api/societies", {
        method: "POST",
        body: JSON.stringify({ ...society, active: true }),
      });
      setSociety({ name: "", address: "", city: "", state: "", pincode: "" });
      setFeedback(
        "Society created. Appoint its initial Society Administrator below.",
      );
      void load();
    } catch (error) {
      setFeedback(readError(error));
    }
  };
  const assignManager = async (event: FormEvent) => {
    event.preventDefault();
    try {
      await secure(`/api/admin/users/${manager.userId}/roles`, {
        method: "POST",
        body: JSON.stringify({
          societyId: Number(manager.societyId),
          role: "SOCIETY_ADMIN",
        }),
      });
      setFeedback("Initial Society Administrator assigned.");
    } catch (error) {
      setFeedback(readError(error));
    }
  };
  return (
    <section className="admin-page">
      <header className="page-heading">
        <p className="eyebrow">Platform administration</p>
        <h1>Society onboarding</h1>
        <p className="muted">
          Review registration enquiries, create verified societies, and appoint
          their first Society Administrator.
        </p>
      </header>
      <Notice message={feedback} />
      <div className="admin-grid">
        <section className="page-panel">
          <h2>Registration enquiries</h2>
          {enquiries.length ? (
            <div className="request-list">
              {enquiries.map((enquiry) => (
                <article key={enquiry.id} className="request-row">
                  <div>
                    <h3>{enquiry.societyName}</h3>
                    <p>
                      {enquiry.contactName} · {enquiry.email}
                    </p>
                    <small>
                      {enquiry.city || "City not provided"} ·{" "}
                      {new Date(enquiry.createdAt).toLocaleString()}
                    </small>
                  </div>
                  <Status value={enquiry.status} />
                </article>
              ))}
            </div>
          ) : (
            <p className="muted">No registration enquiries yet.</p>
          )}
        </section>
        <section className="page-panel form-panel">
          <h2>Create verified society</h2>
          <form onSubmit={createSociety}>
            <label>
              Society name
              <input
                value={society.name}
                onChange={(event) =>
                  setSociety({ ...society, name: event.target.value })
                }
                required
              />
            </label>
            <label>
              Address
              <input
                value={society.address}
                onChange={(event) =>
                  setSociety({ ...society, address: event.target.value })
                }
              />
            </label>
            <div className="two-cols">
              <label>
                City
                <input
                  value={society.city}
                  onChange={(event) =>
                    setSociety({ ...society, city: event.target.value })
                  }
                />
              </label>
              <label>
                State
                <input
                  value={society.state}
                  onChange={(event) =>
                    setSociety({ ...society, state: event.target.value })
                  }
                />
              </label>
            </div>
            <label>
              Pincode
              <input
                value={society.pincode}
                onChange={(event) =>
                  setSociety({ ...society, pincode: event.target.value })
                }
              />
            </label>
            <button className="button">Create society</button>
          </form>
        </section>
        <section className="page-panel form-panel">
          <h2>Appoint initial Society Administrator</h2>
          <form onSubmit={assignManager}>
            <label>
              Registered user
              <select
                value={manager.userId}
                onChange={(event) =>
                  setManager({ ...manager, userId: event.target.value })
                }
                required
              >
                <option value="">Select user</option>
                {users.map((user) => (
                  <option key={user.id} value={user.id}>
                    {user.fullName} — {user.email}
                  </option>
                ))}
              </select>
            </label>
            <label>
              Society
              <select
                value={manager.societyId}
                onChange={(event) =>
                  setManager({ ...manager, societyId: event.target.value })
                }
                required
              >
                <option value="">Select society</option>
                {societies.map((item) => (
                  <option key={item.id} value={item.id}>
                    {item.name}
                  </option>
                ))}
              </select>
            </label>
            <button className="button">Assign Society Administrator</button>
          </form>
        </section>
      </div>
    </section>
  );
}
function SocietyStructureSetup() {
  const [societies, setSocieties] = useState<Society[]>([]);
  const [blocks, setBlocks] = useState<Block[]>([]);
  const [flats, setFlats] = useState<Flat[]>([]);
  const [societyId, setSocietyId] = useState("");
  const [blockId, setBlockId] = useState("");
  const [feedback, setFeedback] = useState("");
  const [blockForm, setBlockForm] = useState({
    name: "",
    totalFloors: "",
    totalFlats: "",
  });
  const [flatForm, setFlatForm] = useState({
    flatNumber: "",
    floorNumber: "",
    flatType: "",
    areaSqft: "",
  });
  useEffect(() => {
    void secure<Society[]>("/api/societies")
      .then(setSocieties)
      .catch((error) => setFeedback(readError(error)));
  }, []);
  const loadBlocks = async (id: string) => {
    if (!id) return setBlocks([]);
    try {
      setBlocks(await secure<Block[]>(`/api/blocks/society/${id}`));
    } catch (error) {
      setFeedback(readError(error));
    }
  };
  const loadFlats = async (id: string) => {
    if (!id) return setFlats([]);
    try {
      setFlats(await secure<Flat[]>(`/api/flats/block/${id}`));
    } catch (error) {
      setFeedback(readError(error));
    }
  };
  useEffect(() => {
    setBlockId("");
    setFlats([]);
    void loadBlocks(societyId);
  }, [societyId]);
  useEffect(() => {
    void loadFlats(blockId);
  }, [blockId]);
  const createBlock = async (event: FormEvent) => {
    event.preventDefault();
    if (!societyId) return setFeedback("Select a society first.");
    try {
      await secure("/api/blocks", {
        method: "POST",
        body: JSON.stringify({
          name: blockForm.name,
          totalFloors: blockForm.totalFloors
            ? Number(blockForm.totalFloors)
            : null,
          totalFlats: blockForm.totalFlats
            ? Number(blockForm.totalFlats)
            : null,
          active: true,
          societyId: Number(societyId),
        }),
      });
      setBlockForm({ name: "", totalFloors: "", totalFlats: "" });
      setFeedback("Block created.");
      await loadBlocks(societyId);
    } catch (error) {
      setFeedback(readError(error));
    }
  };
  const createFlat = async (event: FormEvent) => {
    event.preventDefault();
    if (!blockId) return setFeedback("Select a block first.");
    try {
      await secure("/api/flats", {
        method: "POST",
        body: JSON.stringify({
          flatNumber: flatForm.flatNumber,
          floorNumber: flatForm.floorNumber
            ? Number(flatForm.floorNumber)
            : null,
          flatType: flatForm.flatType || null,
          areaSqft: flatForm.areaSqft ? Number(flatForm.areaSqft) : null,
          active: true,
          blockId: Number(blockId),
        }),
      });
      setFlatForm({
        flatNumber: "",
        floorNumber: "",
        flatType: "",
        areaSqft: "",
      });
      setFeedback("Flat created.");
      await loadFlats(blockId);
    } catch (error) {
      setFeedback(readError(error));
    }
  };
  const deactivateBlock = async (id: number) => {
    try {
      await secure(`/api/blocks/${id}`, { method: "DELETE" });
      setFeedback("Block deactivated.");
      await loadBlocks(societyId);
    } catch (error) {
      setFeedback(readError(error));
    }
  };
  const deactivateFlat = async (id: number) => {
    try {
      await secure(`/api/flats/${id}`, { method: "DELETE" });
      setFeedback("Flat deactivated.");
      await loadFlats(blockId);
    } catch (error) {
      setFeedback(readError(error));
    }
  };
  return (
    <section className="admin-page">
      <header className="page-heading">
        <p className="eyebrow">Platform administration</p>
        <h1>Society structure</h1>
        <p className="muted">
          Create the physical structure before residents are linked to homes.
          Deactivation preserves historical records.
        </p>
      </header>
      <Notice message={feedback} />
      <section className="filter-card">
        <label>
          Verified society
          <select
            value={societyId}
            onChange={(event) => setSocietyId(event.target.value)}
          >
            <option value="">Select society</option>
            {societies
              .filter((society) => society.id)
              .map((society) => (
                <option key={society.id} value={society.id}>
                  {society.name}
                  {society.city ? ` · ${society.city}` : ""}
                </option>
              ))}
          </select>
        </label>
      </section>
      {societyId && (
        <div className="admin-grid two">
          <section className="page-panel form-panel">
            <h2>Create a block</h2>
            <form onSubmit={createBlock}>
              <label>
                Block name
                <input
                  value={blockForm.name}
                  onChange={(event) =>
                    setBlockForm({ ...blockForm, name: event.target.value })
                  }
                  placeholder="A Wing"
                  required
                />
              </label>
              <div className="two-cols">
                <label>
                  Total floors
                  <input
                    type="number"
                    min="0"
                    value={blockForm.totalFloors}
                    onChange={(event) =>
                      setBlockForm({
                        ...blockForm,
                        totalFloors: event.target.value,
                      })
                    }
                  />
                </label>
                <label>
                  Expected flats
                  <input
                    type="number"
                    min="0"
                    value={blockForm.totalFlats}
                    onChange={(event) =>
                      setBlockForm({
                        ...blockForm,
                        totalFlats: event.target.value,
                      })
                    }
                  />
                </label>
              </div>
              <button className="button">Create block</button>
            </form>
            <h2 className="subheading">Blocks</h2>
            {blocks.length ? (
              <div className="request-list">
                {blocks.map((block) => (
                  <article className="request-row" key={block.id}>
                    <div>
                      <h3>{block.name}</h3>
                      <p>
                        {block.totalFloors ?? 0} floors ·{" "}
                        {block.totalFlats ?? 0} expected flats
                      </p>
                      <small>{block.active ? "Active" : "Inactive"}</small>
                    </div>
                    {block.active && (
                      <button
                        className="button quiet small"
                        onClick={() => void deactivateBlock(block.id)}
                      >
                        Deactivate
                      </button>
                    )}
                  </article>
                ))}
              </div>
            ) : (
              <p className="muted">No blocks have been added yet.</p>
            )}
          </section>
          <section className="page-panel form-panel">
            <h2>Create a flat</h2>
            <form onSubmit={createFlat}>
              <label>
                Block
                <select
                  value={blockId}
                  onChange={(event) => setBlockId(event.target.value)}
                  required
                >
                  <option value="">Select active block</option>
                  {blocks
                    .filter((block) => block.active)
                    .map((block) => (
                      <option key={block.id} value={block.id}>
                        {block.name}
                      </option>
                    ))}
                </select>
              </label>
              <label>
                Flat number
                <input
                  value={flatForm.flatNumber}
                  onChange={(event) =>
                    setFlatForm({ ...flatForm, flatNumber: event.target.value })
                  }
                  placeholder="A-101"
                  required
                />
              </label>
              <div className="two-cols">
                <label>
                  Floor number
                  <input
                    type="number"
                    min="0"
                    value={flatForm.floorNumber}
                    onChange={(event) =>
                      setFlatForm({
                        ...flatForm,
                        floorNumber: event.target.value,
                      })
                    }
                  />
                </label>
                <label>
                  Flat type
                  <input
                    value={flatForm.flatType}
                    onChange={(event) =>
                      setFlatForm({ ...flatForm, flatType: event.target.value })
                    }
                    placeholder="2 BHK"
                  />
                </label>
              </div>
              <label>
                Area (sq ft)
                <input
                  type="number"
                  min="0"
                  step="0.01"
                  value={flatForm.areaSqft}
                  onChange={(event) =>
                    setFlatForm({ ...flatForm, areaSqft: event.target.value })
                  }
                />
              </label>
              <button className="button" disabled={!blockId}>
                Create flat
              </button>
            </form>
            <h2 className="subheading">Flats in selected block</h2>
            {blockId ? (
              flats.length ? (
                <div className="request-list">
                  {flats.map((flat) => (
                    <article className="request-row" key={flat.id}>
                      <div>
                        <h3>{flat.flatNumber}</h3>
                        <p>
                          {flat.flatType || "Flat"}
                          {flat.floorNumber !== undefined
                            ? ` · Floor ${flat.floorNumber}`
                            : ""}
                          {flat.areaSqft ? ` · ${flat.areaSqft} sq ft` : ""}
                        </p>
                        <small>{flat.active ? "Active" : "Inactive"}</small>
                      </div>
                      {flat.active && (
                        <button
                          className="button quiet small"
                          onClick={() => void deactivateFlat(flat.id)}
                        >
                          Deactivate
                        </button>
                      )}
                    </article>
                  ))}
                </div>
              ) : (
                <p className="muted">No flats have been added to this block.</p>
              )
            ) : (
              <p className="inline-note">
                Select a block to create and view flats.
              </p>
            )}
          </section>
        </div>
      )}
    </section>
  );
}
function Announcements() {
  const [societies, setSocieties] = useState<Society[]>([]);
  const [form, setForm] = useState({ societyId: "", title: "", message: "" });
  const [feedback, setFeedback] = useState("");
  useEffect(() => {
    void secure<Society[]>("/api/onboarding/managed-societies")
      .then(setSocieties)
      .catch((error) => setFeedback(readError(error)));
  }, []);
  const submit = async (event: FormEvent) => {
    event.preventDefault();
    try {
      await secure("/api/notifications/announcements", {
        method: "POST",
        body: JSON.stringify({ ...form, societyId: Number(form.societyId) }),
      });
      setForm({ societyId: "", title: "", message: "" });
      setFeedback("Announcement sent to society members.");
    } catch (error) {
      setFeedback(readError(error));
    }
  };
  return (
    <section className="page-panel form-panel">
      <p className="eyebrow">Society management</p>
      <h1>Post an announcement</h1>
      <p className="muted">
        Send an in-app notice to members of a society you manage.
      </p>
      <form onSubmit={submit}>
        <label>
          Society
          <select
            value={form.societyId}
            onChange={(event) =>
              setForm({ ...form, societyId: event.target.value })
            }
            required
          >
            <option value="">Select a society</option>
            {societies.map((society) => (
              <option key={society.id} value={society.id}>
                {society.name}
              </option>
            ))}
          </select>
        </label>
        <label>
          Title
          <input
            value={form.title}
            onChange={(event) =>
              setForm({ ...form, title: event.target.value })
            }
            maxLength={255}
            required
          />
        </label>
        <label>
          Message
          <textarea
            value={form.message}
            onChange={(event) =>
              setForm({ ...form, message: event.target.value })
            }
            maxLength={2000}
            required
          />
        </label>
        <Notice message={feedback} />
        <button className="button">Send announcement</button>
      </form>
    </section>
  );
}
function OperationalReports() {
  const [societies, setSocieties] = useState<Society[]>([]);
  const [societyId, setSocietyId] = useState("");
  const [complaints, setComplaints] = useState<ComplaintReport | null>(null);
  const [staff, setStaff] = useState<StaffReport | null>(null);
  const [visitors, setVisitors] = useState<VisitorReport | null>(null);
  const [feedback, setFeedback] = useState("");
  useEffect(() => {
    void secure<Society[]>("/api/onboarding/managed-societies")
      .then(setSocieties)
      .catch((error) => setFeedback(readError(error)));
  }, []);
  const load = async () => {
    if (!societyId) return;
    const today = new Date();
    const from = new Date(today.getFullYear(), today.getMonth(), 1)
      .toISOString()
      .slice(0, 10);
    const to = today.toISOString().slice(0, 10);
    try {
      const [complaintData, staffData, visitorData] = await Promise.all([
        secure<ComplaintReport>(
          `/api/reports/societies/${societyId}/complaints`,
        ),
        secure<StaffReport>(`/api/reports/societies/${societyId}/staff`),
        secure<VisitorReport>(
          `/api/reports/societies/${societyId}/visitors?from=${from}&to=${to}`,
        ),
      ]);
      setComplaints(complaintData);
      setStaff(staffData);
      setVisitors(visitorData);
    } catch (error) {
      setFeedback(readError(error));
    }
  };
  return (
    <section className="admin-page">
      <header className="page-heading">
        <p className="eyebrow">Society management</p>
        <h1>Operational reports</h1>
        <p className="muted">
          A simple current-month summary for complaints, staff, and visitors.
        </p>
      </header>
      <div className="toolbar">
        <select
          value={societyId}
          onChange={(event) => setSocietyId(event.target.value)}
        >
          <option value="">Select society</option>
          {societies.map((society) => (
            <option key={society.id} value={society.id}>
              {society.name}
            </option>
          ))}
        </select>
        <button className="button" onClick={() => void load()}>
          Load reports
        </button>
      </div>
      <Notice message={feedback} />
      {complaints && staff && visitors && (
        <div className="metric-grid">
          <Metric label="Total complaints" value={complaints.totalComplaints} />
          <Metric
            label="Open complaints"
            value={complaints.countsByStatus.OPEN ?? 0}
          />
          <Metric
            label="In progress"
            value={complaints.countsByStatus.IN_PROGRESS ?? 0}
          />
          <Metric label="Active staff" value={staff.activeStaffCount} />
          <Metric label="Visitors this month" value={visitors.totalVisits} />
          <Metric label="Visitors on site" value={visitors.checkedIn} />
        </div>
      )}
    </section>
  );
}
function VisitorOperations() {
  const { user } = useAuth();
  const supervisor = Boolean(user?.roles.includes("SOCIETY_MANAGER") || user?.roles.includes("SECURITY_MANAGER"));
  const [societies, setSocieties] = useState<Society[]>([]);
  const [flats, setFlats] = useState<Flat[]>([]);
  const [visits, setVisits] = useState<VisitorVisit[]>([]);
  const [staff, setStaff] = useState<StaffAssignment[]>([]);
  const [passes, setPasses] = useState<VisitorAccessPass[]>([]);
  const [visitor, setVisitor] = useState({
    fullName: "",
    phoneNumber: "",
    vehicleNumber: "",
  });
  const [createdVisitorId, setCreatedVisitorId] = useState<number | null>(null);
  const [passForm, setPassForm] = useState({ accessType: "TEMPORARY", validUntil: "", purpose: "" });
  const [checkIn, setCheckIn] = useState({
    societyId: "",
    flatId: "",
    purpose: "",
    visitorType: "GUEST",
  });
  const [feedback, setFeedback] = useState("");
  useEffect(() => {
    void secure<Society[]>("/api/onboarding/my-societies")
      .then(setSocieties)
      .catch((error) => setFeedback(readError(error)));
  }, []);
  const loadVisits = async (id = checkIn.societyId) => {
    if (!id) return;
    try {
      const [loadedVisits, assignments, loadedPasses] = await Promise.all([
        secure<VisitorVisit[]>(`/api/visitor-visits/society/${id}`),
        secure<StaffAssignment[]>(
          `/api/staff-assignments/society/${id}/security-context`,
        ),
        supervisor ? secure<VisitorAccessPass[]>(`/api/visitor-access-passes/society/${id}`) : Promise.resolve([]),
      ]);
      setVisits(loadedVisits);
      setStaff(assignments);
      setPasses(loadedPasses);
    } catch (error) {
      setFeedback(readError(error));
    }
  };
  useEffect(() => {
    setVisits([]);
    setFlats([]);
    if (checkIn.societyId)
      void Promise.all([
        loadVisits(checkIn.societyId),
        secure<Flat[]>(
          `/api/visitor-workspace/societies/${checkIn.societyId}/flats`,
        )
          .then(setFlats)
          .catch((error) => setFeedback(readError(error))),
      ]);
  }, [checkIn.societyId]);
  const createVisitor = async (event: FormEvent) => {
    event.preventDefault();
    try {
      const result = await secure<{ id: number }>("/api/visitors", {
        method: "POST",
        body: JSON.stringify(visitor),
      });
      setCreatedVisitorId(result.id);
      setFeedback("Visitor created. Continue with check-in below.");
    } catch (error) {
      setFeedback(readError(error));
    }
  };
  const submitCheckIn = async (event: FormEvent) => {
    event.preventDefault();
    if (!createdVisitorId) return setFeedback("Create the visitor first.");
    try {
      const assignment = staff.find(
        (item) => item.userId === user?.id && item.active,
      );
      await secure("/api/visitor-visits/check-in", {
        method: "POST",
        body: JSON.stringify({
          visitorId: createdVisitorId,
          societyId: Number(checkIn.societyId),
          flatId: checkIn.flatId ? Number(checkIn.flatId) : null,
          securityAssignmentId: assignment?.id ?? null,
          visitorType: checkIn.visitorType,
          purpose: checkIn.purpose,
        }),
      });
      setFeedback("Visitor checked in successfully.");
      setCreatedVisitorId(null);
      setVisitor({ fullName: "", phoneNumber: "", vehicleNumber: "" });
      setCheckIn({ ...checkIn, flatId: "", purpose: "" });
      await loadVisits();
    } catch (error) {
      setFeedback(readError(error));
    }
  };
  const checkOut = async (id: number) => {
    try {
      await secure(`/api/visitor-visits/${id}/check-out`, { method: "PUT" });
      setFeedback("Visitor checked out.");
      await loadVisits();
    } catch (error) {
      setFeedback(readError(error));
    }
  };
  const deny = async (id: number) => {
    try {
      await secure(`/api/visitor-visits/${id}/deny`, { method: "PUT" });
      setFeedback("Visitor entry was denied.");
      await loadVisits();
    } catch (error) {
      setFeedback(readError(error));
    }
  };
  const updatePass = async (id: number, action: "approve" | "revoke") => {
    try {
      await secure(`/api/visitor-access-passes/${id}/${action}`, { method: "PUT" });
      setFeedback(`Access pass ${action}d.`);
      await loadVisits();
    } catch (error) {
      setFeedback(readError(error));
    }
  };
  const createPass = async (event: FormEvent) => {
    event.preventDefault();
    if (!createdVisitorId) return setFeedback("Create the visitor first, then issue their access pass.");
    try {
      await secure("/api/visitor-access-passes", { method: "POST", body: JSON.stringify({ visitorId: createdVisitorId, societyId: Number(checkIn.societyId), accessType: passForm.accessType, validFrom: new Date().toISOString().slice(0, 10), validUntil: passForm.accessType === "PERMANENT" ? null : passForm.validUntil, purpose: passForm.purpose }) });
      setFeedback("Access pass created and is awaiting approval.");
      setPassForm({ accessType: "TEMPORARY", validUntil: "", purpose: "" });
      await loadVisits();
    } catch (error) { setFeedback(readError(error)); }
  };
  return (
    <section className="admin-page">
      <header className="page-heading">
        <p className="eyebrow">Security operations</p>
        <h1>Visitor entry and history</h1>
        <p className="muted">{supervisor ? "Review all visitor entries and control visitor access passes." : "Register a visitor, record entry or exit, and view the last 24 hours for your assigned society."}</p>
      </header>
      <Notice message={feedback} />
      <section className="filter-card">
        <label>
          Society
          <select
            value={checkIn.societyId}
            onChange={(event) =>
              setCheckIn({
                ...checkIn,
                societyId: event.target.value,
                flatId: "",
              })
            }
          >
            <option value="">Select authorised society</option>
            {societies.map((society) => (
              <option key={society.id} value={society.id}>
                {society.name}
              </option>
            ))}
          </select>
        </label>
      </section>
      {checkIn.societyId && (
        <>
          <div className="admin-grid two">
            <section className="page-panel form-panel">
              <h2>1. Visitor details</h2>
              <form onSubmit={createVisitor}>
                <label>
                  Full name
                  <input
                    value={visitor.fullName}
                    onChange={(event) =>
                      setVisitor({ ...visitor, fullName: event.target.value })
                    }
                    required
                  />
                </label>
                <label>
                  Phone number
                  <input
                    value={visitor.phoneNumber}
                    onChange={(event) =>
                      setVisitor({
                        ...visitor,
                        phoneNumber: event.target.value,
                      })
                    }
                  />
                </label>
                <label>
                  Vehicle number
                  <input
                    value={visitor.vehicleNumber}
                    onChange={(event) =>
                      setVisitor({
                        ...visitor,
                        vehicleNumber: event.target.value,
                      })
                    }
                  />
                </label>
                <button className="button">Save visitor</button>
              </form>
            </section>
            <section className="page-panel form-panel">
              <h2>2. Check in</h2>
              <form onSubmit={submitCheckIn}>
                <label>
                  Destination flat <small>(optional)</small>
                  <select
                    value={checkIn.flatId}
                    onChange={(event) =>
                      setCheckIn({ ...checkIn, flatId: event.target.value })
                    }
                  >
                    <option value="">Common area / no flat</option>
                    {flats.map((flat) => (
                      <option key={flat.id} value={flat.id}>
                        {flat.flatNumber}
                      </option>
                    ))}
                  </select>
                </label>
                <label>
                  Visitor type
                  <select
                    value={checkIn.visitorType}
                    onChange={(event) =>
                      setCheckIn({
                        ...checkIn,
                        visitorType: event.target.value,
                      })
                    }
                  >
                    <option value="GUEST">Guest</option>
                    <option value="DELIVERY">Delivery</option>
                    <option value="SERVICE">Service provider</option>
                    <option value="CAB">Cab</option>
                    <option value="OTHER">Other</option>
                  </select>
                </label>
                <label>
                  Purpose
                  <input
                    value={checkIn.purpose}
                    onChange={(event) =>
                      setCheckIn({ ...checkIn, purpose: event.target.value })
                    }
                    required
                  />
                </label>
                <button className="button" disabled={!createdVisitorId}>
                  Check in visitor
                </button>
              </form>
              {!supervisor && !staff.some(
                (item) => item.userId === user?.id && item.active,
              ) && (
                <p className="inline-note">
                  An active security-guard staff assignment is required before you can check in a visitor.
                </p>
              )}
            </section>
          </div>
          <section className="page-panel wide">
            <h2>Visitor history</h2>
            {visits.length ? (
              <div className="table-wrap">
                <table>
                  <thead>
                    <tr>
                      <th>Visitor</th>
                      <th>Purpose</th>
                      <th>Entry</th>
                      <th>Status</th>
                      <th></th>
                    </tr>
                  </thead>
                  <tbody>
                    {visits.map((visit) => (
                      <tr key={visit.id}>
                        <td>Visitor #{visit.visitorId}</td>
                        <td>{visit.purpose}</td>
                        <td>{new Date(visit.entryTime).toLocaleString()}</td>
                        <td>
                          <Status value={visit.status} />
                        </td>
                        <td>
                          {visit.status === "CHECKED_IN" && (
                            <div className="button-row">
                              <button className="button quiet small" onClick={() => void checkOut(visit.id)}>Check out</button>
                              <button className="button quiet small" onClick={() => void deny(visit.id)}>Deny</button>
                            </div>
                          )}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            ) : (
              <p className="muted">No visitor entries for this society yet.</p>
            )}
          </section>
          {supervisor && (
            <section className="page-panel wide">
              <h2>Access passes</h2>
              <p className="muted">Approve only after confirming the visitor, host, and permitted dates. Guards can view passes during entry but cannot change them.</p>
              <form className="inline-form" onSubmit={createPass}><label>Pass type<select value={passForm.accessType} onChange={event => setPassForm({ ...passForm, accessType: event.target.value })}><option value="TEMPORARY">Temporary</option><option value="ONE_TIME">One-time</option><option value="PERMANENT">Permanent</option></select></label>{passForm.accessType !== "PERMANENT" && <label>Valid until<input type="date" value={passForm.validUntil} min={new Date().toISOString().slice(0, 10)} onChange={event => setPassForm({ ...passForm, validUntil: event.target.value })} required /></label>}<label>Purpose<input value={passForm.purpose} onChange={event => setPassForm({ ...passForm, purpose: event.target.value })} required /></label><button className="button" disabled={!createdVisitorId}>Create pass for latest visitor</button></form>
              {passes.length ? <div className="table-wrap"><table><thead><tr><th>Visitor</th><th>Type</th><th>Valid period</th><th>Purpose</th><th>Status</th><th></th></tr></thead><tbody>{passes.map(pass => <tr key={pass.id}><td>Visitor #{pass.visitorId}</td><td>{pass.accessType.replaceAll('_', ' ')}</td><td>{pass.validFrom}{pass.validUntil ? ` — ${pass.validUntil}` : ' — no expiry'}</td><td>{pass.purpose}</td><td><Status value={pass.status} /></td><td>{pass.status === 'PENDING' && <button className="button quiet small" onClick={() => void updatePass(pass.id, 'approve')}>Approve</button>}{pass.status === 'APPROVED' && <button className="button quiet small" onClick={() => void updatePass(pass.id, 'revoke')}>Revoke</button>}</td></tr>)}</tbody></table></div> : <p className="muted">No access passes for this society yet.</p>}
            </section>
          )}
        </>
      )}
    </section>
  );
}
function Profile() {
  const { user, refresh } = useAuth();
  const [feedback, setFeedback] = useState("");
  return (
    <section className="page-panel profile">
      <p className="eyebrow">Account</p>
      <h1>My profile</h1>
      <p className="muted">Personal details and current platform access.</p>
      <dl>
        <dt>Full name</dt>
        <dd>{user?.fullName}</dd>
        <dt>Email address</dt>
        <dd>{user?.email}</dd>
        <dt>Mobile number</dt>
        <dd>{user?.phoneNumber}</dd>
        <dt>Account status</dt>
        <dd>
          <span className="status approved">
            {user?.active ? "Active" : "Inactive"}
          </span>
        </dd>
        <dt>Member since</dt>
        <dd>
          {user?.joinedAt ? new Date(user.joinedAt).toLocaleDateString() : "—"}
        </dd>
        <dt>Roles</dt>
        <dd>
          {user?.roles.length
            ? user.roles.map((role) => roleLabel[role]).join(", ")
            : "No society role has been assigned yet."}
        </dd>
      </dl>
      <button
        className="button quiet"
        onClick={() =>
          refresh()
            .then(() => setFeedback("Profile refreshed."))
            .catch((error) => setFeedback(readError(error)))
        }
      >
        Refresh profile
      </button>
      <Notice message={feedback} />
    </section>
  );
}
function Invitations() {
  const [societies, setSocieties] = useState<Society[]>([]); const [societyId, setSocietyId] = useState(''); const [items, setItems] = useState<Invitation[]>([]); const [form, setForm] = useState({ email: '', role: 'FLAT_OWNER' as Role }); const [feedback, setFeedback] = useState(''); const [link, setLink] = useState(''); const [submitting, setSubmitting] = useState(false)
  const load = async (id = societyId) => { if (!id) return; try { setItems(await secure<Invitation[]>(`/api/invitations/society/${id}`)) } catch (error) { setFeedback(readError(error)) } }
  useEffect(() => { void secure<Society[]>('/api/onboarding/managed-societies').then(setSocieties).catch(error => setFeedback(readError(error))) }, [])
  useEffect(() => { setItems([]); void load(societyId) }, [societyId])
  const invite = async (event: FormEvent) => { event.preventDefault(); if (submitting) return; setSubmitting(true); try { const created = await secure<Invitation>('/api/invitations', { method: 'POST', body: JSON.stringify({ societyId: Number(societyId), email: form.email, role: form.role }) }); setForm({ email: '', role: 'FLAT_OWNER' }); setLink(created.inviteUrl ?? ''); setFeedback('Invitation created. The email is sent when SMTP is enabled; you can also copy the secure link below.'); await load() } catch (error) { setFeedback(readError(error)) } finally { setSubmitting(false) } }
  const revoke = async (id: number) => { try { await secure(`/api/invitations/${id}`, { method: 'DELETE' }); setFeedback('Invitation revoked.'); await load() } catch (error) { setFeedback(readError(error)) } }
  return <section className="admin-page"><header className="page-heading"><p className="eyebrow">Society onboarding</p><h1>Invite owners and operators</h1><p className="muted">Invite a person by email. They create or sign in with that email, then accept their secure invitation.</p></header><Notice message={feedback} /><section className="filter-card"><label>Society<select value={societyId} onChange={e => setSocietyId(e.target.value)}><option value="">Select society</option>{societies.map(s => <option key={s.id} value={s.id}>{s.name}</option>)}</select></label></section>{societyId && <div className="content-grid"><section className="page-panel form-panel"><h2>Send invitation</h2><form onSubmit={invite}><label>Email address<input type="email" value={form.email} onChange={e => setForm({ ...form, email: e.target.value })} required /></label><label>Invite as<select value={form.role} onChange={e => setForm({ ...form, role: e.target.value as Role })}><option value="FLAT_OWNER">Flat owner</option><option value="LOGBOOK_OPERATOR">Logbook operator</option></select></label><button className="button" disabled={submitting}>{submitting ? 'Creating invitation…' : 'Create invitation'}</button></form>{link && <div className="inline-note"><strong>Secure invitation link</strong><p className="break-all">{link}</p><button className="button quiet small" onClick={() => void navigator.clipboard.writeText(link)}>Copy link</button></div>}</section><section className="page-panel"><h2>Invitation status</h2>{items.length ? <div className="request-list">{items.map(item => <article className="request-row" key={item.id}><div><h3>{item.email}</h3><p>{roleLabel[item.role]}</p><small>{item.status.toLowerCase()} · expires {new Date(item.expiresAt).toLocaleString()}</small></div>{item.status === 'PENDING' && <button className="button quiet small" onClick={() => void revoke(item.id)}>Revoke</button>}</article>)}</div> : <p className="muted">No invitations for this society yet.</p>}</section></div>}</section>
}
function InviteAcceptance() {
  const { user, refresh, logout } = useAuth(); const navigate = useNavigate(); const token = new URLSearchParams(window.location.search).get('token') ?? ''; const [invite, setInvite] = useState<{ societyName: string; role: Role; status: string; expiresAt: string; recipientEmailHint: string; flatNumber?: string } | null>(null); const [feedback, setFeedback] = useState(''); const [accepting, setAccepting] = useState(false)
  useEffect(() => { if (token) void api<{ societyName: string; role: Role; status: string; expiresAt: string; recipientEmailHint: string; flatNumber?: string }>(`/api/invitations/preview?token=${encodeURIComponent(token)}`).then(setInvite).catch(error => setFeedback(readError(error))) }, [token])
  const accept = async () => { if (!invite || accepting) return; setAccepting(true); try { await secure(`/api/invitations/accept?token=${encodeURIComponent(token)}`, { method: 'POST' }); await refresh(); const target = invite.role === 'LOGBOOK_OPERATOR' ? '/logbook' : '/my-dues'; setFeedback(`Invitation accepted. Taking you to ${invite.role === 'LOGBOOK_OPERATOR' ? 'your private visitor logbook' : 'your maintenance workspace'}…`); setTimeout(() => navigate(target), 900) } catch (error) { setFeedback(readError(error)); } finally { setAccepting(false) } }
  return <PublicFrame title="Society invitation" subtitle="Use the same email address that received this invitation."><Notice message={feedback} />{invite && <><p><strong>{invite.societyName}</strong> invited you as <strong>{roleLabel[invite.role]}</strong>{invite.flatNumber ? ` for flat ${invite.flatNumber}` : ''}.</p><p className="muted">This invitation was sent to {invite.recipientEmailHint}. It expires {new Date(invite.expiresAt).toLocaleString()}.</p>{invite.status !== 'PENDING' && <p className="inline-note">This invitation is {invite.status.toLowerCase()} and can no longer be accepted.</p>}{user ? <><p className="inline-note">Signed in as <strong>{user.email}</strong>. This must match the invited address.</p><button className="button full" onClick={() => void accept()} disabled={invite.status !== 'PENDING' || accepting}>{accepting ? 'Accepting…' : 'Accept invitation'}</button><button className="button quiet full" onClick={() => void logout().finally(() => navigate(`/login?invite=${encodeURIComponent(token)}`))}>Use a different account</button></> : invite.status === 'PENDING' ? <><Link className="button full" to={`/login?invite=${encodeURIComponent(token)}`}>Sign in to accept</Link><p className="form-footer">New to SocietyOS? <Link to={`/signup?invite=${encodeURIComponent(token)}`}>Create an account</Link>.</p></> : <Link className="button quiet full" to="/login">Go to sign in</Link>}</>}</PublicFrame>
}
function Shell() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const roles = user?.roles ?? [];
  const platformAdmin = isPlatformAdmin(roles);
  const societyAdmin = !platformAdmin && canManage(roles);
  const flatOwner = !platformAdmin && roles.includes("FLAT_OWNER");
  const logbookOperator = !platformAdmin && roles.includes("LOGBOOK_OPERATOR");
  const hasNotifications = !platformAdmin && roles.length > 0;
  const role = primaryRole(roles);
  return (
    <div className="app-shell">
      <aside className="sidebar">
        <Link className="wordmark" to="/dashboard">
          Society<span>OS</span>
        </Link>
        <p className="sidebar-label">
          {platformAdmin ? "Platform" : "Workspace"}
        </p>
        <nav>
          <NavLink to="/dashboard">Overview</NavLink>
          {platformAdmin && (
            <>
              <NavLink to="/admin/onboarding">Society onboarding</NavLink>
              <NavLink to="/admin/structure">Society structure</NavLink>
              <NavLink to="/admin/policies">Policy approvals</NavLink>
            </>
          )}
          {flatOwner && <><NavLink to="/my-dues">My maintenance</NavLink><NavLink to="/my-complaints">My complaints</NavLink></>}
          {hasNotifications && (
            <NavLink to="/notifications">Notifications</NavLink>
          )}
          {societyAdmin && (
            <>
              <p className="sidebar-label">Society management</p>
              <NavLink to="/finance">Maintenance policy</NavLink>
              <NavLink to="/monthly-billing">Monthly billing</NavLink>
              <NavLink to="/complaints">General complaints</NavLink>
              <NavLink to="/society-setup">Society setup</NavLink>
            </>
          )}
          {logbookOperator && <NavLink to="/logbook">Visitor logbook</NavLink>}
          <p className="sidebar-label">Account</p>
          <NavLink to="/profile">My profile</NavLink>
        </nav>
        <div className="account-box">
          <span className="avatar">
            {user?.fullName.slice(0, 1).toUpperCase()}
          </span>
          <div>
            <strong>{user?.fullName}</strong>
            <small>{role ? roleLabel[role] : "New account"}</small>
          </div>
          <button
            className="signout"
            onClick={() => logout().finally(() => navigate("/"))}
          >
            Sign out
          </button>
        </div>
      </aside>
      <main className="workspace">
        <Routes>
          <Route path="/dashboard" element={<Dashboard />} />
          <Route path="/my-dues" element={flatOwner ? <MonthlyMaintenanceWorkspace manager={false} /> : <Navigate to="/dashboard" replace />} />
          <Route path="/my-complaints" element={flatOwner ? <GeneralComplaintsWorkspace manager={false} /> : <Navigate to="/dashboard" replace />} />
          <Route
            path="/notifications"
            element={
              hasNotifications ? (
                <Notifications />
              ) : (
                <Navigate to="/dashboard" replace />
              )
            }
          />
          <Route path="/finance" element={societyAdmin ? <MaintenancePolicyWorkspace platformAdmin={false} /> : <Navigate to="/dashboard" replace />} />
          <Route path="/monthly-billing" element={societyAdmin ? <MonthlyMaintenanceWorkspace manager /> : <Navigate to="/dashboard" replace />} />
          <Route path="/complaints" element={societyAdmin ? <GeneralComplaintsWorkspace manager /> : <Navigate to="/dashboard" replace />} />
          <Route path="/society-setup" element={societyAdmin ? <SocietySetup /> : <Navigate to="/dashboard" replace />} />
          <Route path="/admin/policies" element={platformAdmin ? <MaintenancePolicyWorkspace platformAdmin /> : <Navigate to="/dashboard" replace />} />
          <Route path="/invitations" element={<Navigate to="/society-setup" replace />} />
          <Route path="/logbook" element={logbookOperator ? <PrivateLogbookWorkspace /> : <Navigate to="/dashboard" replace />} />
          <Route
            path="/admin/onboarding"
            element={
              platformAdmin ? (
                <AdminOnboarding />
              ) : (
                <Navigate to="/dashboard" replace />
              )
            }
          />
          <Route
            path="/admin/structure"
            element={
              platformAdmin ? (
                <SocietyStructureSetup />
              ) : (
                <Navigate to="/dashboard" replace />
              )
            }
          />
          <Route path="/profile" element={<Profile />} />
          <Route path="*" element={<Navigate to="/dashboard" replace />} />
        </Routes>
      </main>
    </div>
  );
}
function formatCurrency(value: number) {
  return new Intl.NumberFormat("en-IN", {
    style: "currency",
    currency: "INR",
    maximumFractionDigits: 0,
  }).format(value ?? 0);
}
export default function App() {
  const { user, loading } = useAuth();
  if (loading)
    return <main className="loading">Loading your secure workspace…</main>;
  return (
    <Routes>
      <Route path="/" element={<Landing />} />
      <Route path="/login" element={<Login />} />
      <Route path="/signup" element={<Signup />} />
      <Route path="/invite" element={<InviteAcceptance />} />
      <Route
        path="/*"
        element={user ? <Shell /> : <Navigate to="/login" replace />}
      />
    </Routes>
  );
}
