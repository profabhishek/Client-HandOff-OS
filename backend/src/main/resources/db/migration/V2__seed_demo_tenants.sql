-- V2: two demo tenants so we can test tenant isolation before signup/login exists.
-- Use these UUIDs in the X-Tenant-Id header (see README).
-- This seed will move to a dev-only location once real signup exists (week 2).

insert into tenant (public_id, name, slug, created_at, updated_at)
values
    ('11111111-1111-1111-1111-111111111111', 'Demo Agency A', 'demo-agency-a', now(), now()),
    ('22222222-2222-2222-2222-222222222222', 'Demo Agency B', 'demo-agency-b', now(), now());
