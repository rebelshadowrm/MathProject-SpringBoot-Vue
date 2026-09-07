# Heroku release runbook

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
