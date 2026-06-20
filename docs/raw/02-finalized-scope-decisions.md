# Raw Notes: Finalized Scope Decisions

Session decisions after initial architecture discussion.

## Locked In

| Topic | Decision |
|-------|----------|
| Hold duration | **10 minutes** (configurable) |
| Discount model | **Percentage + fixed amount**, extensible via strategy pattern |
| Database | **H2** embedded file DB — no external setup |
| Payment | Mock gateway (synchronous) |
| Notifications | Mock/logger adapter, async |
| Auth | Email/password + JWT (no OAuth) |

## Must-Have Features

1. Catalog: City → Theater → Screen → Seat
2. Shows with per-show `ShowSeat` inventory rows
3. Hold → pay → confirm flow
4. Hold expiry background job (every 30s)
5. Cancel with refund policy engine
6. Pessimistic locking for seat concurrency
7. Admin + Customer RBAC

## Stretch (time permitting)

- Show analytics, bulk show creation, idempotent payment, partial hold release

## Assumptions (14 total)

Documented in README.md — includes single-show bookings, fixed seat layouts, weekend = Sat/Sun in city timezone, no guest checkout, admin-seeded catalog.
