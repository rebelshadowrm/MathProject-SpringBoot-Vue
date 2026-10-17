# Historical PostgreSQL release and restoration notes

Preserved from before the disposable H2 portfolio conversion. The reset/maintenance sequence below is historical, not the current portfolio deployment procedure. Never execute it without explicit owner approval. Use [DEPLOYMENT.md](DEPLOYMENT.md) for the current conversion procedure.

## Restore PostgreSQL support in the future

The PostgreSQL driver, Flyway PostgreSQL module, V1/V2 migrations, Hibernate mappings and JDBC services remain in the codebase. Deactivate `portfolio-demo` and provide `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, and `SPRING_DATASOURCE_PASSWORD` privately (or use Heroku's attached Postgres buildpack integration). The default local fallback is file-backed H2. Keep `spring.jpa.hibernate.ddl-auto=validate`; Flyway owns schema changes.

Provisioning a replacement paid database requires owner approval. Restore the verified pre-conversion dump to the approved empty PostgreSQL instance using compatible PostgreSQL tooling; never restore over an existing database without separate approval. Validate tables, row counts, Flyway history and all four roles before switching traffic. For a new empty PostgreSQL database, Flyway creates the modern schema and optional `DEMO_DATA=true` seeds it. Keep `DEMO_LOGIN` disabled for real users. H2 visitor data is deliberately discarded and has no migration back to PostgreSQL.

For pre-modernization schema/data, preserve the original dump and annotated `snapshot/pre-modernization-2026-09-07` tag. V1 creates the modern schema; it does not transform arbitrary legacy data. Do not enable Flyway baseline or reset a populated legacy database to bypass incompatibilities. Develop and test an explicit legacy-to-modern transformation on an isolated restored copy before authorizing production migration.

## Original runbook (historical)

The portfolio database is demo-only, but every reset begins with a recoverable PostgreSQL backup.

1. Verify `modernize/2026-stack` is clean and `./mvnw verify` passes on Java 21 or newer.
2. Capture a backup with `heroku pg:backups:capture -a banana-math-modern` and verify it appears in `heroku pg:backups -a banana-math-modern`.
3. Enable maintenance mode: `heroku maintenance:on -a banana-math-modern`.
4. Confirm the exact database attachment with `heroku pg:info -a banana-math-modern`, scale the old web process to zero, then reset only the approved demo database. Scaling down prevents the previous release from recreating its legacy schema after the reset.
5. Ensure `DEMO_DATA=true`, `DEMO_LOGIN=true`, and a strong `JWT_SECRET` are configured.
6. Deploy the modern branch with `git push heroku modernize/2026-stack:main`, then scale the web process back to one.
7. Check release logs for both successful Flyway migrations and completed deterministic seeding.
8. Smoke-test the Student, Teacher, Parent, and Admin demo logins. Verify a student can complete a drill and the seeded test, teacher tools load, parent access is read-only, and both leaderboard scopes load.
9. Disable maintenance mode only after smoke tests pass: `heroku maintenance:off -a banana-math-modern`.

If deployment fails, roll back the Heroku release. If the migrated/reset data is the problem, restore the captured backup before taking the app out of maintenance mode.
