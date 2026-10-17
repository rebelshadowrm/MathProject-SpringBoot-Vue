# Math Practice

A portfolio-focused grade 3–6 learning platform built with Vue and Spring Boot. Students move through courses, guided practice, flashcards, drills, published assessments, mastery, and classroom or global leaderboards. Teachers manage classrooms and learning work; linked parents have read-only progress access; administrators oversee the demo platform.

## Modern stack

- Java 21 LTS and Spring Boot 4.1
- Spring Security 7 with JWT authentication
- Flyway migrations and Hibernate schema validation with embedded H2 for the portfolio demo; PostgreSQL support retained
- Vue 3.5, Vue Router 5, Vite 8, Node.js 22, and ESLint 10
- Maven 3.9 wrapper with a reproducible frontend build

## Run locally

Build and test the complete application:

```bash
./mvnw verify
```

Run the packaged application at `http://localhost:8080`:

```bash
java -jar target/mathproject-0.0.1-SNAPSHOT.jar --spring.profiles.active=portfolio-demo
```

For frontend hot reload, run `npm install && npm run dev` from `src/frontend` while the Spring application is running.

The `portfolio-demo` profile creates a fresh in-memory H2 database, runs the shared Flyway migrations, validates the schema, and seeds the course map, demo classroom, parent link, announcement, and published assessment. Demo login is enabled. It requires no database files or external database, even when `SPRING_DATASOURCE_*` variables are present. All demo accounts use the password `password`:

- `Admin`
- `Student`
- `Teacher`
- `Parent`

Teacher-created learners receive a one-time temporary password and must replace it with a password of at least 10 characters before using the application.

Visitor progress, accounts, classroom edits, and assessments are intentionally temporary. Every process restart restores the seeded demo. All visitors share the demo accounts; use fictional information only. Run a single web process: each process has its own independent database. An Eco dyno may sleep and wake with a fresh demo.

Without this profile, the historical configuration remains available: local development uses file-backed H2 under `data/`, or `SPRING_DATASOURCE_*` selects PostgreSQL. `DEMO_DATA=true` and `DEMO_LOGIN=true` opt into seeding and demo login in that configuration.

## Configuration

Production secrets and services are configured through environment variables:

| Variable | Purpose |
| --- | --- |
| `SPRING_PROFILES_ACTIVE` | Set to `portfolio-demo` for disposable embedded H2 with seeded demo logins |
| `JWT_SECRET` | Strong random JWT signing secret; required in production |
| `SPRING_DATASOURCE_URL` | JDBC database URL; Heroku supplies this for attached Postgres |
| `SPRING_DATASOURCE_USERNAME` | Database username |
| `SPRING_DATASOURCE_PASSWORD` | Database password |
| `DEMO_DATA` | Set to `true` to seed portfolio demo accounts and questions |
| `DEMO_LOGIN` | Enable one-click demo authentication; defaults to `DEMO_DATA` |
| `PORT` | HTTP port; assigned automatically by Heroku |

Keep a strong `JWT_SECRET` on Heroku. Remove any separate Flyway connection overrides (`SPRING_FLYWAY_URL`, `SPRING_FLYWAY_USER`, `SPRING_FLYWAY_PASSWORD`, `SPRING_FLYWAY_DRIVER_CLASS_NAME`) before selecting the demo profile; these are rejected to prevent a separate migration connection to an external database. Do not override the profile's demo-data, demo-login, Flyway, or Hibernate settings.

## Heroku deployment

The repository includes `system.properties` for Java 21 and a `Procfile`. For a new, empty app (no database add-on required):

```bash
heroku config:set SPRING_PROFILES_ACTIVE=portfolio-demo JWT_SECRET=YOUR_LONG_RANDOM_VALUE -a YOUR_APP
git push heroku HEAD:main
```

For the existing `banana-math-modern` site, follow [the conversion runbook](docs/DEPLOYMENT.md), including backup, deployment, smoke testing, and owner approval before database removal. The database charge continues until the add-on is destroyed; switching profiles alone does not stop billing. Web hosting charges remain separate. [Historical PostgreSQL migration and restoration instructions](docs/POSTGRESQL-HISTORY.md) are preserved.

## Project history

The original Banana.math school application is preserved by the annotated Git tag `snapshot/pre-modernization-2026-09-07`. Modernization work lives on `modernize/2026-stack`; the snapshot is not deployed separately.
