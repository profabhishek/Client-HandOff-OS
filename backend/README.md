# Client Handoff OS — Backend

Spring Boot 4.1 · Java 21 · PostgreSQL 16 · Flyway · Maven

This is the starting point. It has **one finished feature (clients)**. Build every other module by copying the clients pattern.

---

## 1. What you need installed

| Tool | Check it works |
| --- | --- |
| JDK 21 | `java -version` shows 21 |
| Maven 3.9+ (or just use IntelliJ, which bundles Maven) | `mvn -v` |
| Docker Desktop | `docker compose version` |
| IntelliJ IDEA (Community is fine) | — |

## 2. Run it

```bash
cd backend

# 1. Start Postgres (runs on port 5433 on your machine)
docker compose up -d

# 2. Run the tests
mvn test

# 3. Start the app
mvn spring-boot:run
```

In IntelliJ: *File → Open →* select the `backend` folder, wait for Maven to import, then run `HandoffOsApplication`.

When it starts you'll see Flyway apply `V1` and `V2`, then `Tomcat started on port 8080`.

Check: <http://localhost:8080/actuator/health> → `{"status":"UP"}`

## 3. Try the API

Open `http/clients.http` in IntelliJ and click the green arrows. Or with curl:

```bash
curl -X POST http://localhost:8080/api/v1/clients \
  -H "X-Tenant-Id: 11111111-1111-1111-1111-111111111111" \
  -H "Content-Type: application/json" \
  -d '{"name":"ABC Retail Pvt Ltd","primaryContactEmail":"asha@abc-retail.example"}'

curl http://localhost:8080/api/v1/clients \
  -H "X-Tenant-Id: 11111111-1111-1111-1111-111111111111"
```

**About `X-Tenant-Id`:** login doesn't exist yet, so for now you tell the API which agency you are with this header. There are two demo agencies (tenant A `1111…` and tenant B `2222…`). Create a client as A and list as B — B must see nothing. In week 2 this header is replaced by the logged-in user's token; the rest of the code stays the same.

## 4. How the code is organised

```
src/main/java/com/handoffos/
├── HandoffOsApplication.java     start here
├── common/                       shared by all modules
│   ├── BaseEntity.java           id, publicId, createdAt, updatedAt, version
│   ├── TenantOwnedEntity.java    adds tenant_id (filled automatically)
│   ├── tenant/TenantContext      "which agency is this request for?"
│   ├── tenant/TenantFilter       reads X-Tenant-Id (temporary)
│   ├── exception/                NotFound (404), Conflict (409), GlobalExceptionHandler
│   └── web/PageResponse          standard list response
├── tenant/                       Tenant entity + repository
├── clients/                      ✅ THE EXAMPLE MODULE
│   ├── Client.java               entity (table: client)
│   ├── ClientRepository.java     database queries
│   ├── ClientService.java        business logic
│   ├── ClientController.java     HTTP endpoints
│   └── dto/                      request + response records
└── auth, users, projects, ...    empty, one package-info.java describing each

src/main/resources/
├── application.yml               config
└── db/migration/                 Flyway SQL files (V1__..., V2__...)
```

One request flows like this:

```
HTTP → TenantFilter (sets tenant) → Controller → Service → Repository → PostgreSQL
                                        ↓
                            returns DTO (never the entity)
```

## 5. Rules we both follow

1. **Every tenant-owned table has `tenant_id`**, and every repository method takes `tenantId`. Never write `findByPublicId(id)` alone on tenant data.
2. **The API only uses `publicId` (UUID).** The numeric `id` never leaves the server.
3. **Controllers are thin.** Logic goes in the service. Controllers never touch repositories.
4. **Return DTOs, never entities.** Requests and responses are Java `record`s in `dto/`.
5. **Validate input** with annotations (`@NotBlank`, `@Email`, `@Size`) plus `@Valid` in the controller.
6. **Throw, don't catch.** Services throw `NotFoundException` / `ConflictException`; `GlobalExceptionHandler` turns them into JSON errors.
7. **Database changes = a new Flyway file.** Never edit a migration after it's merged. Never set `ddl-auto` to `update`.
8. **Soft delete** (`deleted_at`) for business data, so history survives.
9. **A test for every service** (see `ClientServiceTest`).

## 6. Adding a new module (recipe)

Example: `projects`.

1. `db/migration/V3__create_project.sql` — table with `id`, `public_id`, `tenant_id`, your columns, `created_at`, `updated_at`, `version`, `deleted_at`, plus `create index ... on project (tenant_id)`.
2. `projects/Project.java` — `extends TenantOwnedEntity`, protected no-args constructor, a real constructor, methods instead of setters.
3. `projects/ProjectRepository.java` — methods all include `TenantId`.
4. `projects/dto/` — `CreateProjectRequest`, `UpdateProjectRequest`, `ProjectResponse` with `from(Project)`.
5. `projects/ProjectService.java` — copy the structure of `ClientService`.
6. `projects/ProjectController.java` — `@RequestMapping("/api/v1/projects")`.
7. `ProjectServiceTest.java` — at least: create works, not-found in other tenant, one business rule.
8. Add requests to `http/projects.http` and try them by hand.

Two numbering rules for migrations when two people work in parallel: pull `main` before creating one, and if both of you made `V3`, whoever merges second renames theirs to `V4`.

## 7. Suggested split for this week

| | Dev 1 | Dev 2 |
| --- | --- | --- |
| Day 1 | Both: run the project, read every file in `clients/` and `common/`, try every request in `clients.http` | same |
| Days 2–4 | `projects` module (belongs to a client — needs `client_id`, check the client exists in the same tenant) | `users` module (users table, roles enum, list/invite placeholder — no passwords yet) |
| Day 5 | Review each other's PR. Then start reading Spring Security docs for week 2 (login + JWT). | same |

Git: `main` is always runnable. Work on a branch (`feature/projects`), open a PR, the other person reviews before merge.

## 8. Useful commands

```bash
docker compose ps                     # is Postgres running?
docker compose down -v                # wipe the local database and start fresh
docker exec -it handoffos-postgres psql -U handoffos -d handoffos   # SQL console
  \dt                                 # list tables
  select * from flyway_schema_history;
mvn test -Dtest=ClientServiceTest     # run one test class
```

## 9. Deliberately NOT here yet

Spring Security / login (week 2), S3 file storage, email, Testcontainers integration tests, OpenAPI docs, CI. Each gets added when its week comes, so there's less to learn at once.
