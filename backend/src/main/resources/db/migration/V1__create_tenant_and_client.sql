-- V1: first two tables.
-- RULE: never edit a migration after it is merged. Make a new file (V3__..., V4__...) instead.

create table tenant (
    id          bigint generated always as identity primary key,
    public_id   uuid         not null unique,        -- the ONLY id we expose in the API
    name        varchar(200) not null,
    slug        varchar(100) not null unique,
    created_at  timestamptz  not null,
    updated_at  timestamptz  not null,
    version     integer      not null default 0      -- optimistic locking (@Version)
);

create table client (
    id                     bigint generated always as identity primary key,
    public_id              uuid         not null unique,
    tenant_id              bigint       not null references tenant (id),
    name                   varchar(200) not null,
    primary_contact_name   varchar(200),
    primary_contact_email  varchar(320),
    notes                  text,
    created_at             timestamptz  not null,
    updated_at             timestamptz  not null,
    version                integer      not null default 0,
    deleted_at             timestamptz                 -- soft delete: null = active
);

-- Every tenant-owned query filters by tenant_id, so index it.
create index ix_client_tenant on client (tenant_id);

-- Client names are unique inside one tenant (case-insensitive), ignoring deleted clients.
create unique index ux_client_tenant_name
    on client (tenant_id, lower(name))
    where deleted_at is null;
