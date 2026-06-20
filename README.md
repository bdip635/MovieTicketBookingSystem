# Movie Ticket Booking System

SDE-2 take-home assignment — a Spring Boot backend for movie ticket booking at scale with seat-level holds, pricing, payments, refunds, and role-based access.

## Table of Contents

- [Overview](#overview)
- [Scope](#scope)
- [Assumptions](#assumptions)
- [Tech Stack](#tech-stack)
- [Architecture](#architecture)
- [Domain Model](#domain-model)
- [Core Flows](#core-flows)
- [API Overview](#api-overview)
- [Concurrency Strategy](#concurrency-strategy)
- [Discount Model](#discount-model)
- [Testing Strategy](#testing-strategy)
- [Getting Started](#getting-started)
- [Project Structure](#project-structure)

## Overview

The system supports multiple cities, theaters, screens, and shows with **seat-level booking**. Customers browse shows, hold seats for a time-bound window, apply discount codes, pay, and receive booking confirmation. Admins manage the catalog, pricing, discount codes, and refund policies.

**Roles:** `ADMIN` (catalog management) and `CUSTOMER` (browse, book, cancel, view history).

## Scope

### In Scope (committed)

| # | Capability | Implementation |
|---|------------|----------------|
| 1 | Catalog hierarchy | City → Theater → Screen → Seat layout |
| 2 | Shows | Movie + Screen + start time; admin-managed |
| 3 | Seat availability | Per-show state: `AVAILABLE`, `HELD`, `BOOKED` |
| 4 | Seat holds | **10-minute** TTL; auto-release on expiry |
| 5 | Booking + payment | Hold → discount → mock payment → confirm |
| 6 | Pricing | Seat tier (`REGULAR`, `PREMIUM`) + weekend surcharge + discount codes |
| 7 | Refunds | Configurable policy; cancel → compute refund amount |
| 8 | Concurrency | Pessimistic row locks; no double booking |
| 9 | RBAC | Spring Security + JWT for `ADMIN` and `CUSTOMER` |
| 10 | Notifications | Async confirmation + scheduled show reminder (mock sender) |
| 11 | Customer history | List past and upcoming bookings |
| 12 | Validation & errors | Bean validation + consistent error responses |
| 13 | Tests | Unit tests (pricing, refund, discount) + integration tests (hold → book → cancel) |

### Stretch (if core is solid)

- Booking analytics per show (seat fill rate)
- Bulk show creation for a screen
- Idempotent payment via `Idempotency-Key` header
- Partial seat release from a hold

### Out of Scope

- Frontend / UI
- Deployment, Docker, CI/CD
- Microservices / distributed systems
- OAuth, SSO, MFA
- Real payment gateway (Stripe, Razorpay, etc.)
- Real email/SMS delivery
- Redis, Kafka, distributed locks
- Production observability and alerting

## Assumptions

1. One booking covers one show and one or more seats — no cross-show cart.
2. Seat layout is fixed per screen; the same physical seats apply to every show on that screen.
3. Final price = `(base price by seat tier) × (weekend multiplier if applicable) − discount`.
4. Weekend = Saturday or Sunday in the show's city timezone.
5. Hold TTL = **10 minutes** (configurable via `app.booking.hold-duration-minutes`).
6. Only the holding user can confirm or release a hold.
7. Payment is a synchronous mock gateway — immediate success/failure, no webhooks.
8. Refund policy example tiers:
   - \>24h before show → 100% refund
   - 2–24h before show → 50% refund
   - \<2h before show → 0% refund
9. Admins bootstrap all catalog data — no self-service theater onboarding.
10. Customers must register and log in — no guest checkout.
11. Show reminder fires once, 2 hours before show start.
12. Discount codes support **percentage** and **fixed amount** via a pluggable strategy pattern.
13. **H2** embedded file database for zero-setup local development and tests.
14. Discount never drives the final amount below zero.

## Tech Stack

| Layer | Choice | Rationale |
|-------|--------|-----------|
| Framework | Spring Boot 3.x | Assignment requirement |
| Language | Java 21 | Modern LTS; records for DTOs |
| Database | H2 (file-based) | No external DB setup required |
| ORM | Spring Data JPA | Pessimistic locking support |
| Migrations | Flyway | Versioned, reproducible schema |
| Security | Spring Security + JWT | Simple RBAC without OAuth |
| Validation | Jakarta Validation | `@Valid` on request DTOs |
| API Docs | springdoc-openapi | Swagger UI for demo |
| Testing | JUnit 5 + MockMvc + H2 | Integration tests without Testcontainers |
| Build | Maven | Standard Spring ecosystem tool |

> H2 runs in PostgreSQL compatibility mode (`MODE=PostgreSQL`) so the schema and locking patterns can migrate to PostgreSQL with a config change only.

## Architecture

See [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) for diagrams, layer details, and flow sequences.

High-level layout:

```
REST Controllers  →  Domain Services  →  Repositories  →  H2
                          ↓
              Mock Payment / Notification Adapters
                          ↓
              Scheduled Jobs (hold expiry, reminders)
```

## Domain Model

```
City → Theater → Screen → Seat (static catalog)
Movie + Screen + datetime → Show
Show + Seat → ShowSeat (per-show inventory; concurrency anchor)
User → SeatHold → SeatHoldItem → ShowSeat
User → Booking → BookingSeat → ShowSeat
Booking → Payment, optional Refund
Theater → RefundPolicy
DiscountCode → applied at booking confirmation
```

Key design choice: **`ShowSeat`** is one row per `(show_id, seat_id)` and is the unit of pessimistic locking.

## Core Flows

### Browse and hold

1. Customer lists shows (filter by city, date).
2. Customer views seat map for a show.
3. Customer holds seats → rows locked, status set to `HELD`, expiry = now + 10 min.

### Confirm booking

1. Customer confirms hold (optional discount code).
2. Price calculated (tier + weekend − discount).
3. Mock payment charged.
4. `ShowSeat` → `BOOKED`; booking and payment records created.
5. Confirmation notification sent asynchronously.

### Hold expiry (background job, every 30s)

1. Find `HELD` seats where `hold_expires_at < now()`.
2. Reset to `AVAILABLE`; mark hold as `EXPIRED`.

### Cancel and refund

1. Customer cancels booking.
2. Refund policy engine computes refund percentage based on time until show.
3. Refund record created; seats released to `AVAILABLE`.
4. Cancellation notification sent asynchronously.

## API Overview

Base path: `/api/v1`

### Auth (public)

| Method | Path | Description |
|--------|------|-------------|
| POST | `/auth/register` | Register customer |
| POST | `/auth/login` | Login; returns JWT |
| GET | `/auth/me` | Current user profile (authenticated) |

**Default admin (seeded on startup):** `admin@movieticket.com` / `admin123`

### Admin

| Method | Path | Description |
|--------|------|-------------|
| POST/GET | `/admin/cities` | Manage cities |
| POST/GET | `/admin/theaters` | Manage theaters |
| POST/GET | `/admin/screens` | Manage screens |
| POST | `/admin/screens/{id}/seats` | Define seat layout |
| POST/GET | `/admin/movies` | Manage movies |
| POST/GET | `/admin/shows` | Manage shows |
| POST/GET | `/admin/refund-policies` | Manage refund policies |
| POST/GET | `/admin/discount-codes` | Manage discount codes |

### Customer

| Method | Path | Description |
|--------|------|-------------|
| GET | `/shows` | Browse shows |
| GET | `/shows/{id}/seats` | Seat map |
| POST | `/shows/{id}/holds` | Hold seats |
| GET | `/holds/{id}` | Hold details |
| DELETE | `/holds/{id}` | Release hold |
| POST | `/holds/{id}/confirm` | Pay and confirm |
| GET | `/bookings` | Booking history |
| GET | `/bookings/{id}` | Booking details |
| POST | `/bookings/{id}/cancel` | Cancel and refund |

## Concurrency Strategy

1. **Pessimistic write lock** on `ShowSeat` rows during hold and confirm.
2. **Conditional update:** `UPDATE ... WHERE status = 'AVAILABLE'`; zero affected rows → `409 Conflict`.
3. **Unique constraint** on `(show_id, seat_id)` in `show_seat`.
4. **Single transaction** per hold creation and per booking confirmation.

Race scenario: two users hold the same seat → first transaction wins; second receives `409 Seat already held or booked`.

## Discount Model

Extensible **strategy pattern**:

| Type | Behavior |
|------|----------|
| `PERCENTAGE` | `subtotal × (value / 100)`, optional `maxDiscountAmount` cap |
| `FIXED_AMOUNT` | Flat amount off, capped at subtotal |

Future types (e.g. `TIER_SPECIFIC`, `FIRST_BOOKING`, `BOGO`) add a new enum value and strategy class — no changes to booking flow.

See [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md#discount-strategy) for class diagram.

## Testing Strategy

| Type | Coverage |
|------|----------|
| Unit | `PriceCalculator`, `RefundPolicyEngine`, discount strategies |
| Integration | Hold → confirm → booking; concurrent hold conflict; hold expiry; cancel refund |
| Security | Customer blocked from admin routes; user cannot confirm another user's hold |

## Getting Started

### Prerequisites

- Java 21+
- Maven 3.9+

### Run

```bash
mvn spring-boot:run
```

- API: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- H2 Console: `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:file:./data/movieticket`)

### Test

```bash
mvn test
```

## Project Structure

```
AGENTS.md              AI agent workflow and conventions
docs/
├── ARCHITECTURE.md
├── skills-used.md     Cursor skills referenced during development
└── raw/               Raw planning notes from AI sessions
src/main/java/com/movieticket/
├── config/          Security, JWT, async, scheduling
├── domain/          JPA entities and enums
├── repository/      Spring Data JPA repositories
├── service/         Business logic
│   ├── catalog/
│   ├── booking/
│   ├── pricing/
│   ├── payment/
│   ├── refund/
│   └── notification/
├── web/             Controllers, DTOs, mappers
├── exception/       Global exception handler
└── scheduler/       Hold expiry and reminder jobs
```

## Build Order

1. ✅ Requirements and architecture documentation
2. ✅ Project scaffold + Flyway schema
3. ✅ JPA entities and repositories
4. ✅ Auth + RBAC
5. ✅ Admin catalog APIs
6. ✅ Customer browse + seat map
7. ✅ Hold + expiry job
8. ✅ Pricing + discount + payment + confirm
9. Cancel + refund
10. Async notifications + reminder job
11. Tests + seed data
