# AI Agent Instructions — Movie Ticket Booking System

This file documents how Cursor AI agents were used during development of this project.

## Project Context

SDE-2 take-home assignment: build a Spring Boot movie ticket booking backend with seat holds, pricing, payments, refunds, RBAC, and tests.

## Workflow

1. **Requirements extraction** — Agent read the assignment PDF and summarized scope, in-scope/out-of-scope items, and deliverables.
2. **Collaborative scoping** — Human and agent finalized requirements (10-min holds, extensible discount model, H2 database) and architecture before coding.
3. **Step-by-step implementation** — Build in ordered phases with git commits after each milestone:
   - Documentation + architecture
   - Project scaffold + domain model
   - Auth + RBAC (current phase)
   - Admin catalog APIs (next)
   - Booking flows, pricing, notifications, tests
4. **Verification** — Agent runs `mvn test` after each phase to confirm the app compiles and context loads.

## Conventions for Agents

- **Stack:** Spring Boot 3.x, Java 21, H2, Flyway, JWT, JUnit 5
- **Package root:** `com.movieticket`
- **API prefix:** `/api/v1`
- **Do not add:** frontend, Docker, microservices, OAuth, real payment gateways
- **Document assumptions** in `README.md`
- **Commit frequently** with descriptive messages; never force-push
- **Match existing code style** — Lombok on entities, layered architecture (web → service → repository)

## Key Design Decisions (do not reverse without discussion)

| Decision | Choice |
|----------|--------|
| Hold duration | 10 minutes |
| Database | H2 file-based (`MODE=PostgreSQL`) |
| Concurrency | Pessimistic lock on `ShowSeat` rows |
| Discount model | Strategy pattern (`PERCENTAGE`, `FIXED_AMOUNT`) |
| Auth | JWT stateless; roles `ADMIN`, `CUSTOMER` |

## Raw Development Files

Planning notes and scope decisions captured during AI sessions live in `docs/raw/`.

## Skills Reference

See `docs/skills-used.md` for Cursor skills referenced during development.
