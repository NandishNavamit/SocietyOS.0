# SocietyOS — Simplified Product Contract

**Status:** agreed design baseline before Phase 1 implementation  
**Scope:** prototype before production hardening

This document is the source of truth for the simplified SocietyOS build. A later
feature must not reintroduce a retired role, workflow, or charge type unless this
contract is deliberately changed first.

## 1. Product boundary

SocietyOS is a maintenance collection, general-complaint, and private visitor-log
application for a housing society. It is not a general staff, security, task, or
property-management platform.

The physical structure remains:

```text
Society → Block → Flat
```

Blocks remain an internal organisational detail. They do not create a Block
Manager role or a separate workflow.

## 2. Final roles

| Role | Purpose | May access |
| --- | --- | --- |
| Bootstrap Admin | Operates the platform and approves society maintenance policy requests. | Bootstrap approval and society-administration tools only. |
| Society Admin | Operates one society. | Setup, owner/operator assignment, finance, complaints, policy requests, offline payment recording. |
| Flat Owner | The one active owner for a flat. | Their own bills, payments, notices, and complaint submission. |
| Logbook Operator | A person manually assigned to a society by its Society Admin. | Only that society's private visitor logbook entry/exit workspace. |

### Role invariants

1. A flat has **at most one active Flat Owner** at a time.
2. A society may have multiple active Logbook Operators.
3. A Society Admin can assign or revoke Logbook Operators, but can never read
   visitor-log entries.
4. A Flat Owner cannot read visitor-log entries.
5. A Logbook Operator cannot access finance, complaints, society setup, or other
   administration features.
6. Bootstrap Admin approval is required before a maintenance policy takes effect.

## 3. Explicitly retired features

The following must be removed from active routes, menus, invitations, demo data,
and new authorization logic:

- Finance Manager
- Block Manager
- Staff and Staff assignments
- Security Manager and Security Guard
- Tenant and Family member workflows
- Visitor access passes, guard shifts, security work assignment, and security dashboards
- Individual service charges, repair charges, and individual bill creation
- Complaint categories, staff assignment, and complaint-linked finance

Historical rows can be retained during migration if necessary, but they must not
be available through the simplified prototype experience.

## 4. Finance rules

### Maintenance policy

A Society Admin submits a policy request containing:

- monthly maintenance amount
- due day/date
- fixed rupee late fee for each overdue month
- effective month

The Bootstrap Admin approves or rejects the request. Policy history is retained;
every generated bill stores its own snapshot of the amount and late-fee rule.

When an approved policy changes, every active Flat Owner receives a mandatory
in-app notice and an email copy. The notice remains visible until acknowledged.

### Billing

- Exactly one monthly maintenance bill is generated for each active flat.
- A duplicate bill for the same flat and billing month is prohibited.
- Overdue late fees are automatic and fixed in rupees per overdue month.
- Bill statuses are: `PENDING`, `PARTIALLY_PAID`, `PAID`, and `OVERDUE`.
- Society Admins cannot create arbitrary charges or bills for a particular owner.

### Payments

- A Flat Owner can pay one bill or all selected pending bills in one Razorpay
  checkout.
- A combined payment is allocated safely across the selected bills.
- Society Admins can record an offline cash/bank payment with a reference.
- Payment history, receipts/references, verified Razorpay signatures, and webhook
  processing remain part of the prototype.

## 5. Complaints

- A Flat Owner submits only a title and description.
- Society Admin sees and manages all complaints for their society.
- The lifecycle is: `OPEN → IN_PROGRESS → RESOLVED → CLOSED`.
- The Society Admin adds a resolution note.
- There are no categories, staff assignments, complex service workflow, or finance
  coupling.

## 6. Private visitor logbook

Each visitor log entry belongs to one society and records the logged-in Logbook
Operator who made it. A Logbook Operator can register a visitor, select an
optional destination flat, record entry, and record exit.

Only the assigned operator workspace can display these entries. Society Admins
may manage operator assignments but must receive neither a log view nor log
counts/analytics.

## 7. Onboarding and invitations

1. Bootstrap Admin creates the society and initial Society Admin.
2. Society Admin creates blocks and flats.
3. Society Admin invites/assigns exactly one Flat Owner to each flat.
4. Society Admin invites/assigns Logbook Operators.
5. Recipients sign in or register using the invited email, accept the invitation,
   and are sent to their correct workspace.

The only invitation types are **Flat Owner** and **Logbook Operator**.

## 8. Experience standard

Every phase includes backend authorization, data rules, API work, and a polished
frontend—not frontend mock-ups alone.

The Society Admin finance workspace is a familiar ledger experience: fast search,
filters, sorting, totals for the current view, detail panels, and CSV export. It
is intentionally not an unrestricted spreadsheet with unsafe inline editing.

All workspaces must have clear empty states, loading states, access-denied states,
and payment/error recovery paths. A valid user must never see an unexplained
generic authentication error because a screen still calls an obsolete API.

## 9. Delivery sequence

1. Role and authorization simplification.
2. Society, flat, owner, and operator onboarding.
3. Maintenance policy approval.
4. Monthly billing and overdue calculation.
5. Online/offline payments and allocation.
6. Society Admin finance workspace.
7. General complaints.
8. Private visitor logbook.
9. Notifications and invitations.
10. Obsolete-feature cleanup, demo data, full-journey verification, and final UI refinement.

