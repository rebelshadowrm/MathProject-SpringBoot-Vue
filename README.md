# Math Practice

A portfolio-focused grade 3–6 learning platform built with Vue and Spring Boot. Students move through courses, guided practice, flashcards, drills, published assessments, mastery, and classroom or global leaderboards. Teachers manage classrooms and learning work; linked parents have read-only progress access; administrators oversee the demo platform.

## Modern stack

- Java 21 LTS and Spring Boot 4.1
- Spring Security 7 with JWT authentication
- Flyway-managed PostgreSQL with Hibernate schema validation and H2 for local development
- Vue 3.5, Vue Router 5, Vite 8, Node.js 22, and ESLint 10
- Maven 3.9 wrapper with a reproducible frontend build

## Run locally

Build and test the complete application:

```bash
./mvnw verify
```

Run the packaged application at `http://localhost:8080`:

```bash
java -jar target/mathproject-0.0.1-SNAPSHOT.jar
```

For frontend hot reload, run `npm install && npm run dev` from `src/frontend` while the Spring application is running.

To load the deterministic course map, demo classroom, announcement, and published test, set `DEMO_DATA=true`. Set `DEMO_LOGIN=true` to expose the one-click demo-role login API. All demo accounts use the password `password`:

- `Admin`
- `Student`
- `Teacher`
- `Parent`

Teacher-created learners receive a one-time temporary password and must replace it with a password of at least 10 characters before using the application.

## Configuration

Production secrets and services are configured through environment variables:

| Variable | Purpose |
| --- | --- |
| `JWT_SECRET` | Strong random JWT signing secret; required in production |
| `SPRING_DATASOURCE_URL` | JDBC database URL; Heroku supplies this for attached Postgres |
| `SPRING_DATASOURCE_USERNAME` | Database username |
| `SPRING_DATASOURCE_PASSWORD` | Database password |
| `DEMO_DATA` | Set to `true` to seed portfolio demo accounts and questions |
| `DEMO_LOGIN` | Enable one-click demo authentication; defaults to `DEMO_DATA` |
| `PORT` | HTTP port; assigned automatically by Heroku |

Without database variables, local development uses a file-backed H2 database under `data/`.

## Heroku deployment

The repository includes `system.properties` for Java 21 and a `Procfile`. After creating an app:

```bash
heroku addons:create heroku-postgresql:essential-0 -a YOUR_APP
heroku config:set JWT_SECRET=YOUR_LONG_RANDOM_VALUE DEMO_DATA=true -a YOUR_APP
git push heroku modernize/2026-stack:main
```

Heroku's Java buildpack exposes the attached database through Spring's `SPRING_DATASOURCE_*` variables. Flyway runs migrations during application startup; Hibernate then validates the resulting schema. See [docs/DEPLOYMENT.md](docs/DEPLOYMENT.md) for the backup, maintenance, deploy, smoke-test, and rollback sequence.

## Project history

The original Banana.math school application is preserved by the annotated Git tag `snapshot/pre-modernization-2026-09-07`. Modernization work lives on `modernize/2026-stack`; the snapshot is not deployed separately.
