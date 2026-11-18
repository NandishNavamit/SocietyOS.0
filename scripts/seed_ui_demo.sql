-- Repeatable Phase 1 demo data for the simplified SocietyOS model.
-- Every demo login below uses the password: DemoPassword@123
-- This script adds data only; it does not delete any existing records.

BEGIN;

ALTER TABLE user_society_roles DROP CONSTRAINT IF EXISTS user_society_roles_role_check;
ALTER TABLE user_society_roles ADD CONSTRAINT user_society_roles_role_check CHECK (role IN (
    'ADMIN', 'SOCIETY_ADMIN', 'FLAT_OWNER', 'LOGBOOK_OPERATOR',
    'SOCIETY_MANAGER', 'FINANCE_MANAGER', 'SECURITY_MANAGER',
    'BLOCK_MANAGER', 'RESIDENT', 'STAFF', 'SECURITY'
));

CREATE EXTENSION IF NOT EXISTS pgcrypto;

INSERT INTO societies (name, address, city, state, pincode, active, created_at)
SELECT 'Demo Heights Society', '42 Test Avenue', 'Bhubaneswar', 'Odisha', '751024', true, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM societies WHERE name = 'Demo Heights Society');

INSERT INTO blocks (society_id, name, total_floors, total_flats, active, created_at)
SELECT s.id, 'North Tower', 5, 2, true, CURRENT_TIMESTAMP FROM societies s
WHERE s.name = 'Demo Heights Society'
  AND NOT EXISTS (SELECT 1 FROM blocks b WHERE b.society_id = s.id AND b.name = 'North Tower');

INSERT INTO flats (block_id, flat_number, floor_number, flat_type, area_sqft, active, created_at)
SELECT b.id, v.flat_number, v.floor_number, v.flat_type, v.area_sqft, true, CURRENT_TIMESTAMP
FROM blocks b
CROSS JOIN (VALUES ('N-101', 1, '2 BHK', 980.0), ('N-102', 1, '3 BHK', 1260.0)) AS v(flat_number, floor_number, flat_type, area_sqft)
WHERE b.name = 'North Tower' AND b.society_id = (SELECT id FROM societies WHERE name = 'Demo Heights Society')
  AND NOT EXISTS (SELECT 1 FROM flats f WHERE f.block_id = b.id AND f.flat_number = v.flat_number);

INSERT INTO users (full_name, email, phone_number, password_hash, active, created_at)
SELECT v.full_name, v.email, v.phone, crypt('DemoPassword@123', gen_salt('bf', 10)), true, CURRENT_TIMESTAMP
FROM (VALUES
    ('Nisha Rao', 'demo.admin@societyos.test', '+919900000001'),
    ('Ananya Das', 'demo.owner@societyos.test', '+919900000002'),
    ('Rohit Kumar', 'demo.logbook@societyos.test', '+919900000003')
) AS v(full_name, email, phone)
WHERE NOT EXISTS (SELECT 1 FROM users u WHERE lower(u.email) = lower(v.email));

INSERT INTO user_society_roles (user_id, society_id, role, active, created_at)
SELECT u.id, s.id, v.role, true, CURRENT_TIMESTAMP
FROM societies s
CROSS JOIN (VALUES
    ('demo.admin@societyos.test', 'SOCIETY_ADMIN'),
    ('demo.owner@societyos.test', 'FLAT_OWNER'),
    ('demo.logbook@societyos.test', 'LOGBOOK_OPERATOR')
) AS v(email, role)
JOIN users u ON lower(u.email) = lower(v.email)
WHERE s.name = 'Demo Heights Society'
ON CONFLICT (user_id, society_id, role) DO UPDATE SET active = true;

-- Existing old demo-role assignments are deliberately disabled. No obsolete
-- account is usable after this script runs.
UPDATE user_society_roles SET active = false
WHERE role IN ('SOCIETY_MANAGER', 'FINANCE_MANAGER', 'SECURITY_MANAGER',
               'BLOCK_MANAGER', 'RESIDENT', 'STAFF', 'SECURITY');

UPDATE users SET active = false
WHERE email IN ('demo.manager@societyos.test', 'demo.blockmanager@societyos.test',
                'demo.finance@societyos.test', 'demo.securitymanager@societyos.test',
                'demo.security@societyos.test', 'demo.staff@societyos.test',
                'demo.family@societyos.test', 'demo.tenant@societyos.test');

INSERT INTO residents (user_id, active, created_at)
SELECT u.id, true, CURRENT_TIMESTAMP FROM users u
WHERE u.email = 'demo.owner@societyos.test'
  AND NOT EXISTS (SELECT 1 FROM residents r WHERE r.user_id = u.id);

INSERT INTO flat_occupancies (flat_id, resident_id, occupancy_type, resident_role, start_date, active, created_at)
SELECT f.id, r.id, 'OWNER', 'PRIMARY', DATE '2026-01-01', true, CURRENT_TIMESTAMP
FROM flats f
JOIN users u ON u.email = 'demo.owner@societyos.test'
JOIN residents r ON r.user_id = u.id
WHERE f.flat_number = 'N-101'
  AND NOT EXISTS (SELECT 1 FROM flat_occupancies o WHERE o.flat_id = f.id AND o.active = true);

COMMIT;
