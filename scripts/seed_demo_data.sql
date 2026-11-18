-- Safe, repeatable demo data for the existing Green Valley Residency sample.
-- Each block only inserts records when its named sample record is missing.

INSERT INTO users (full_name, email, phone_number, active, created_at)
SELECT 'Meera Sharma', 'meera.sharma@example.com', '+919876543211', true, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'meera.sharma@example.com');

INSERT INTO users (full_name, email, phone_number, active, created_at)
SELECT 'Kunal Das', 'kunal.das@example.com', '+919876543212', true, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'kunal.das@example.com');

INSERT INTO residents (user_id, active, created_at)
SELECT u.id, true, CURRENT_TIMESTAMP FROM users u
WHERE u.email = 'aarav.sharma@example.com'
  AND NOT EXISTS (SELECT 1 FROM residents r WHERE r.user_id = u.id);

INSERT INTO residents (user_id, active, created_at)
SELECT u.id, true, CURRENT_TIMESTAMP FROM users u
WHERE u.email = 'meera.sharma@example.com'
  AND NOT EXISTS (SELECT 1 FROM residents r WHERE r.user_id = u.id);

INSERT INTO residents (user_id, active, created_at)
SELECT u.id, true, CURRENT_TIMESTAMP FROM users u
WHERE u.email = 'kunal.das@example.com'
  AND NOT EXISTS (SELECT 1 FROM residents r WHERE r.user_id = u.id);

INSERT INTO flat_occupancies (flat_id, resident_id, occupancy_type, resident_role, start_date, active, created_at)
SELECT f.id, r.id, 'OWNER', 'PRIMARY', DATE '2026-01-01', true, CURRENT_TIMESTAMP
FROM flats f JOIN residents r ON r.user_id = (SELECT id FROM users WHERE email = 'aarav.sharma@example.com')
WHERE f.flat_number = 'A-101' AND NOT EXISTS (SELECT 1 FROM flat_occupancies o WHERE o.flat_id = f.id AND o.resident_id = r.id AND o.active = true);

INSERT INTO flat_occupancies (flat_id, resident_id, occupancy_type, resident_role, start_date, active, created_at)
SELECT f.id, r.id, 'OWNER', 'FAMILY_MEMBER', DATE '2026-01-01', true, CURRENT_TIMESTAMP
FROM flats f JOIN residents r ON r.user_id = (SELECT id FROM users WHERE email = 'meera.sharma@example.com')
WHERE f.flat_number = 'A-101' AND NOT EXISTS (SELECT 1 FROM flat_occupancies o WHERE o.flat_id = f.id AND o.resident_id = r.id AND o.active = true);

INSERT INTO flat_occupancies (flat_id, resident_id, occupancy_type, resident_role, start_date, active, created_at)
SELECT f.id, r.id, 'TENANT', 'PRIMARY', DATE '2026-03-01', true, CURRENT_TIMESTAMP
FROM flats f JOIN residents r ON r.user_id = (SELECT id FROM users WHERE email = 'kunal.das@example.com')
WHERE f.flat_number = 'A-102' AND NOT EXISTS (SELECT 1 FROM flat_occupancies o WHERE o.flat_id = f.id AND o.resident_id = r.id AND o.active = true);

INSERT INTO staff_profiles (full_name, phone_number, email, created_at)
SELECT 'Ramesh Nayak', '+919876543220', 'ramesh.nayak@example.com', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM staff_profiles WHERE email = 'ramesh.nayak@example.com');

INSERT INTO staff_profiles (full_name, phone_number, email, created_at)
SELECT 'Suresh Das', '+919876543221', 'suresh.das@example.com', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM staff_profiles WHERE email = 'suresh.das@example.com');

INSERT INTO staff_assignments (staff_profile_id, society_id, staff_type, joining_date, active, created_at)
SELECT p.id, 1, 'SECURITY_GUARD', DATE '2026-01-01', true, CURRENT_TIMESTAMP FROM staff_profiles p
WHERE p.email = 'ramesh.nayak@example.com' AND NOT EXISTS (SELECT 1 FROM staff_assignments a WHERE a.staff_profile_id = p.id AND a.society_id = 1);

INSERT INTO staff_assignments (staff_profile_id, society_id, staff_type, joining_date, active, created_at)
SELECT p.id, 1, 'PLUMBER', DATE '2026-01-01', true, CURRENT_TIMESTAMP FROM staff_profiles p
WHERE p.email = 'suresh.das@example.com' AND NOT EXISTS (SELECT 1 FROM staff_assignments a WHERE a.staff_profile_id = p.id AND a.society_id = 1);

INSERT INTO charge_plans (society_id, name, amount, frequency, due_day_of_month, active, created_at)
SELECT 1, 'Monthly Maintenance', 2500.00, 'MONTHLY', 10, true, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM charge_plans WHERE society_id = 1 AND name = 'Monthly Maintenance');

INSERT INTO maintenance_bills (charge_plan_id, flat_id, billing_period_start, billing_period_end, due_date, amount, paid_amount, status, created_at)
SELECT p.id, f.id, DATE '2026-10-01', DATE '2026-10-31', DATE '2026-10-10', 2500.00,
       CASE WHEN f.flat_number = 'A-101' THEN 2500.00 ELSE 0.00 END,
       CASE WHEN f.flat_number = 'A-101' THEN 'PAID' ELSE 'OVERDUE' END, CURRENT_TIMESTAMP
FROM charge_plans p JOIN flats f ON f.block_id = 1
WHERE p.society_id = 1 AND p.name = 'Monthly Maintenance'
  AND NOT EXISTS (SELECT 1 FROM maintenance_bills b WHERE b.charge_plan_id = p.id AND b.flat_id = f.id AND b.billing_period_start = DATE '2026-10-01');

INSERT INTO payments (bill_id, amount, payment_date, payment_method, reference_number, notes, status, created_at)
SELECT b.id, 2500.00, DATE '2026-10-05', 'ONLINE', 'DEMO-UPI-1001', 'Demo payment', 'COMPLETED', CURRENT_TIMESTAMP
FROM maintenance_bills b JOIN flats f ON f.id = b.flat_id
WHERE f.flat_number = 'A-101' AND b.billing_period_start = DATE '2026-10-01'
  AND NOT EXISTS (SELECT 1 FROM payments p WHERE p.bill_id = b.id AND p.reference_number = 'DEMO-UPI-1001');

INSERT INTO society_expenses (society_id, category, description, amount, expense_date, vendor_name, reference_number, active, created_at)
SELECT 1, 'REPAIR', 'Water-pump service', 4200.00, DATE '2026-10-06', 'ABC Electricals', 'DEMO-EXP-1001', true, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM society_expenses WHERE reference_number = 'DEMO-EXP-1001');

INSERT INTO complaints (society_id, flat_id, raised_by_resident_id, assigned_staff_assignment_id, category, title, description, priority, status, created_at, updated_at)
SELECT 1, f.id, r.id, a.id, 'PLUMBING', 'Kitchen tap leaking', 'Demo complaint: kitchen tap is leaking.', 'MEDIUM', 'IN_PROGRESS', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM flats f JOIN residents r ON r.user_id = (SELECT id FROM users WHERE email = 'aarav.sharma@example.com')
JOIN staff_assignments a ON a.society_id = 1 AND a.staff_type = 'PLUMBER'
WHERE f.flat_number = 'A-101' AND NOT EXISTS (SELECT 1 FROM complaints WHERE title = 'Kitchen tap leaking');

INSERT INTO visitors (full_name, phone_number, vehicle_number, created_at)
SELECT 'Priya Patel', '+919876543230', 'OD02AB1234', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM visitors WHERE phone_number = '+919876543230');

INSERT INTO notifications (society_id, recipient_user_id, type, title, message, is_read, created_at)
SELECT 1, u.id, 'ANNOUNCEMENT', 'Welcome to Green Valley', 'Demo announcement for the in-app notification list.', false, CURRENT_TIMESTAMP
FROM users u WHERE u.email = 'aarav.sharma@example.com'
  AND NOT EXISTS (SELECT 1 FROM notifications WHERE title = 'Welcome to Green Valley' AND recipient_user_id = u.id);
