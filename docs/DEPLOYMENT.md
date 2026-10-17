# Heroku portfolio conversion runbook

The initial conversion was executed on 2026-10-17 after the owner instructed “execute 1-4.” See the execution record below. For future operations, do not reset PostgreSQL, scale the site to zero, enable maintenance, purchase a service, or destroy an add-on without explicit owner approval. No action here targets Softball/Diamond Connected or any other application.

## Completed conversion: 2026-10-17

- Saved pre-conversion configuration, buildpack/release metadata and a verified database backup in the access-restricted local directory `C:/Users/rebel/banana-math-backups/2026-10-17-h2/`, outside the repository. The archive is `banana-math-b002.dump` (60,965 bytes), captured as Heroku backup `b002` at 18:47 UTC. SHA256: `D4C4925CCF3090690F1DBFF1BF855F69D421AD87954128103AE5A3F14D1C77BB`.
- Verified the custom archive with PostgreSQL 18 `pg_restore --list`, then restored it successfully with `--exit-on-error --no-owner --no-acl` into an isolated, network-disabled temporary local PostgreSQL container. Confirmed four users, four courses, one class, one assessment and successful Flyway V1/V2 history. Removed only that temporary verification container afterward; the backup remains available.
- Deployed tested commit `4bb3b06a2b9055abcc3b4518ad8051dc78d31a8a` as release v14. Release v15 enabled `SPRING_PROFILES_ACTIVE=portfolio-demo`, retaining the existing strong JWT secret and demo flags. Startup logs confirmed embedded `jdbc:h2:mem:...`, both migrations and successful application startup.
- Passed live authenticated acceptance for Student, Teacher, Parent and Administrator: courses, both leaderboard scopes, password and one-click logins, scored drills/flashcards, seeded assessment, progress visibility, parent mutation denial, class and learner creation, required learner password replacement, announcement, generated assignment publication/submission, parent linking and administrator user/role management. Browser checks confirmed all four dashboards and the temporary-progress notice. A Heroku restart restored zero XP, four original accounts, one original class and an uncompleted assessment.
- Rechecked exact add-on identity/ownership and all attachments immediately before removal: only `banana-math-modern::DATABASE` was attached. Under the owner's authorization, destroyed `postgresql-cubed-85210` (`ab7dc2e7-8ecd-4253-a2d2-44750d13a1b2`). Release v16 removed its attachment/configuration. Heroku's app add-on list is empty and `DATABASE_URL` is absent; no explicit datasource/Flyway overrides remain.
- Repeated the complete live API workflow acceptance after deletion, then restarted Banana Math again and confirmed the initial clean demo. The site remains public on one Eco web dyno. No other application, database or service was changed, and no new service was purchased.

The Essential 0 database add-on ($5/month) is removed. Previously accrued charges and separate web hosting charges can still appear on the bill. Keep the downloaded backup and private configuration export secure; PostgreSQL restoration would require separate approval for a replacement service.

## Read-only inspection: 2026-10-17

- App: `banana-math-modern`, Java buildpack, Java 21 configuration, `heroku-26`, one Eco web dyno.
- Public URL: https://banana-math-modern-179fda30edfa.herokuapp.com/
- Owning/billing app for `postgresql-cubed-85210`: `banana-math-modern`.
- Plan: `heroku-postgresql:essential-0`, billed price $5/month (prorated). Add-on ID: `ab7dc2e7-8ecd-4253-a2d2-44750d13a1b2`.
- Both `addons --json` and `addons:info` reported a single attachment: `banana-math-modern::DATABASE`. No other attached app was listed. Recheck immediately before removal.
- Config keys: `DATABASE_URL`, `DEMO_DATA=true`, `DEMO_LOGIN=true`, `JWT_SECRET` present. No explicit Spring datasource variables or active profile were configured. No credential values were printed.
- Release observed: v13; maintenance disabled. No hosted configuration, data, releases, or add-ons were changed during inspection.

## Database behavior and prerequisites

`portfolio-demo` supplies an explicit embedded H2 datasource with a unique in-memory database per application context. It ignores `SPRING_DATASOURCE_*`, including variables injected by Heroku's Java buildpack while PostgreSQL remains attached. Separate Flyway URL, user, password, or driver overrides are rejected before connecting. Flyway V1/V2 create the schema and Hibernate uses `validate`; the application runner then seeds four roles/accounts, 12 legacy arithmetic questions, grade 3–6 courses, a classroom, parent link, announcement, and published assessment. Question values and timestamps vary; the catalog and initial relationships are predictable. Existing migration files and PostgreSQL runtime dependencies are retained unchanged.

All visitors share the demo accounts. Visitor progress and edits are temporary, and restart restores the initial demo without database files. Use fictional information. Keep exactly one web process: multiple processes would have independent data. Eco sleep/wake and Heroku restarts can reset progress. This removes the database charge, not web dyno charges.

Before changing the live app, run `./mvnw verify` on Java 21 or newer and `npm run lint` in `src/frontend`. Review and record the exact tested Git SHA. Keep `JWT_SECRET` strong and private. Do not override `app.demo-*`, Flyway enablement, migration locations, or `ddl-auto=validate`.

See [validation evidence](PORTFOLIO-VALIDATION.md) for completed local and hosted acceptance.

## Backup and recovery preparation

Owner/operator runs these in a private terminal. Backup files and config exports contain sensitive information; keep them outside the repository. Do not print config values, backup signed URLs, or database URLs into logs or this thread.

1. Record the current release with `heroku releases -a banana-math-modern`, the Git SHA, process formation, buildpacks, and current profile configuration. Privately export configuration for rollback, for example PowerShell: `heroku config --json -a banana-math-modern | Set-Content -LiteralPath C:/PRIVATE/banana-math-config.json` (create a protected directory first).
2. Reinspect `heroku addons:info postgresql-cubed-85210 -a banana-math-modern` and `heroku addons --json -a banana-math-modern`. Stop if the owning app, ID, plan, or attachments differ. Any attachment to another app requires separate review; never detach or modify that app as part of this task.
3. Capture: `heroku pg:backups:capture DATABASE -a banana-math-modern`.
4. Verify completed status: `heroku pg:backups -a banana-math-modern`. Record the new backup ID (for example `b123`; substitute the actual ID below).
5. Download outside the repository: `heroku pg:backups:download b123 -a banana-math-modern --output C:/PRIVATE/banana-math-before-h2.dump`.
6. Verify a nonempty file and checksum, and inspect it with a compatible `pg_restore --list`. Ideally restore it into an isolated local PostgreSQL instance and validate expected tables/row counts before approving deletion. A Heroku-hosted backup alone is insufficient for deleting the add-on.

## Deploy and select H2 (after owner authorizes conversion)

Keep PostgreSQL attached throughout deployment and smoke testing. No reset or maintenance outage is required. The code push and config change below create releases/restarts; old visitor sessions can be interrupted.

1. Deploy the exact tested SHA, substituting it for `TESTED_SHA`: `git push heroku TESTED_SHA:refs/heads/main`. Confirm Heroku build/release success. This first release retains PostgreSQL behavior until the profile is enabled.
2. Recheck configuration privately. If any `SPRING_FLYWAY_URL`, `SPRING_FLYWAY_USER`, `SPRING_FLYWAY_PASSWORD`, or `SPRING_FLYWAY_DRIVER_CLASS_NAME` overrides exist, record and remove only those keys on this app. Remove conflicting demo/Flyway/Hibernate overrides as reviewed. Current inspection found none.
3. Select the demo: `heroku config:set SPRING_PROFILES_ACTIVE=portfolio-demo DEMO_DATA=true DEMO_LOGIN=true -a banana-math-modern`. Retain the existing `JWT_SECRET`. No database URL is needed or copied.
4. Check release status and filtered logs. Expect active `portfolio-demo`, `jdbc:h2:mem:...`, two successful Flyway migrations, Hibernate validation, successful startup, and available demo login. Investigate any PostgreSQL connection, migration error, or restart loop. Avoid publishing raw logs that could contain secrets.

## Smoke acceptance before removing the add-on

At the public URL, confirm the temporary-data notice and run:

- Student: one-click and password login (`Student` / `password`), grade 3–6 courses and skill guides, practice, flashcards, a scored drill, published assessment submission, progress/mastery XP, global and classroom leaderboards.
- Teacher: dashboard/class roster, linked learner progress, create a class, create a learner (record temporary password privately), learner password replacement, post announcement, publish an assignment, and verify its student visibility/submission.
- Parent: linked Student progress and announcements; confirm class creation and practice submission are denied.
- Administrator: dashboard, users and roles, classroom oversight, role update using a temporary learner, and both leaderboards.
- Restart this app only: `heroku restart -a banana-math-modern`. Log in again and confirm the four seeded accounts, original class/assessment, zero Student progress, and disappearance of test edits. No database files are required.

Do not destroy the add-on unless all checks pass and the downloaded backup is verified.

## Stop the database charge (separate explicit owner approval required)

Reinspect add-on ID, owning app, and every attachment immediately before deletion. Approval must name `postgresql-cubed-85210` and acknowledge permanent loss of its hosted data. Stop if another app is attached; do not alter that app.

Only after that approval, the operator runs:

```bash
heroku addons:destroy postgresql-cubed-85210 -a banana-math-modern --confirm banana-math-modern
```

This destroys the database; it is not a detach or reset. It can trigger a config release/restart. Then confirm `heroku addons -a banana-math-modern` no longer lists it, inspect the billing dashboard for cessation of future charges (already accrued charges can remain), repeat four-role login and Student drill/assessment smoke tests, and restart once more to verify independence. Remove only stale, explicitly set database config keys on Banana Math if any remain; never print their values. Keep the strong JWT secret and demo profile. The initial conversion completed this removal as recorded above; do not rerun it.

## Rollback and future restoration

Before deletion, roll back to the recorded release **and** restore prior profile/configuration (code rollback alone does not revert config). If there was previously no active profile: `heroku config:unset SPRING_PROFILES_ACTIVE -a banana-math-modern`. Retain the still-attached PostgreSQL database; this conversion never writes H2 visitor edits back into it. Do not restore/reset PostgreSQL merely to undo the profile switch.

After deletion, code rollback cannot recover the database. Restoring PostgreSQL requires explicit approval to provision a paid replacement, restore the downloaded backup, and restore the historical configuration. See [POSTGRESQL-HISTORY.md](POSTGRESQL-HISTORY.md). Keep shared migrations unchanged and use new versioned migrations for future changes.

Heroku references: [PGBackups](https://devcenter.heroku.com/articles/heroku-postgres-backups), [provisioning/removing PostgreSQL](https://devcenter.heroku.com/articles/provisioning-heroku-postgres), [add-on ownership and attachments](https://devcenter.heroku.com/articles/managing-add-ons).
