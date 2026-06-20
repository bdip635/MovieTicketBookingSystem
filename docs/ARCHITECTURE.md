# Architecture

Detailed architecture for the Movie Ticket Booking System.

## System Context

```mermaid
flowchart TB
    subgraph clients [API Clients]
        Admin[Admin - Postman / curl]
        Customer[Customer - Postman / curl]
    end

    subgraph app [Spring Boot Monolith]
        Security[JWT + RBAC Filter]
        Controllers[REST Controllers]
        Services[Domain Services]
        Schedulers[Scheduled Jobs]
        AsyncExec[Async Executor]
    end

    subgraph infra [Infrastructure]
        H2[(H2 File DB)]
        Notif[Notification Adapter - Logger]
        Pay[Payment Adapter - Mock]
    end

    Admin --> Security
    Customer --> Security
    Security --> Controllers
    Controllers --> Services
    Services --> H2
    Services --> Pay
    Services --> AsyncExec
    AsyncExec --> Notif
    Schedulers --> Services
```

## Layer Structure

| Layer | Responsibility |
|-------|----------------|
| **web** | HTTP endpoints, request/response DTOs, input validation |
| **service** | Business rules, transactions, orchestration |
| **repository** | Data access via Spring Data JPA |
| **domain** | Entities, enums, domain constants |
| **config** | Security, JWT, async thread pool, scheduling |
| **scheduler** | Hold expiry and show reminder jobs |
| **exception** | Global error handling, consistent API error format |

### Package layout

```
com.movieticket
├── MovieTicketApplication.java
├── config/
│   ├── SecurityConfig.java
│   ├── JwtConfig.java
│   ├── AsyncConfig.java
│   └── OpenApiConfig.java
├── domain/
│   ├── entity/          JPA entities
│   └── enums/           Role, SeatStatus, DiscountType, etc.
├── repository/
├── service/
│   ├── catalog/         City, Theater, Screen, Movie, Show
│   ├── booking/         Hold, confirm, cancel
│   ├── pricing/         PriceCalculator, DiscountService, strategies
│   ├── payment/         MockPaymentGateway
│   ├── refund/          RefundPolicyEngine
│   └── notification/    Async notification sender
├── web/
│   ├── controller/
│   ├── dto/
│   └── mapper/
├── exception/
└── scheduler/
    ├── HoldExpiryScheduler.java
    └── ShowReminderScheduler.java
```

## Entity Relationship

```mermaid
erDiagram
    City ||--o{ Theater : has
    Theater ||--o{ Screen : has
    Screen ||--o{ Seat : contains
    Screen ||--o{ Show : hosts
    Movie ||--o{ Show : plays_at
    Show ||--o{ ShowSeat : tracks
    Seat ||--o{ ShowSeat : instance_of
    User ||--o{ SeatHold : creates
    User ||--o{ Booking : owns
    SeatHold ||--o{ SeatHoldItem : contains
    SeatHoldItem }o--|| ShowSeat : locks
    Booking ||--o{ BookingSeat : contains
    BookingSeat }o--|| ShowSeat : reserves
    Booking ||--|| Payment : has
    Booking ||--o| Refund : may_have
    Theater ||--o{ RefundPolicy : defines
    DiscountCode ||--o{ Booking : applied_to

    ShowSeat {
        uuid id
        enum status
        uuid held_by_user_id
        timestamp hold_expires_at
    }
```

### ShowSeat — concurrency anchor

Every show gets one `ShowSeat` row per physical seat when the show is created. All hold and book operations lock and update these rows.

| Column | Purpose |
|--------|---------|
| `show_id`, `seat_id` | Unique inventory key |
| `status` | `AVAILABLE`, `HELD`, `BOOKED` |
| `held_by_user_id` | Current hold owner |
| `hold_expires_at` | TTL for auto-release |
| `seat_hold_id` | Link to active hold record |

## Core Flow Sequences

### Hold seats

```mermaid
sequenceDiagram
    participant C as Customer
    participant API as BookingController
    participant S as BookingService
    participant DB as H2

    C->>API: POST /shows/{id}/holds {seatIds}
    API->>S: createHold(showId, seatIds, userId)
    S->>DB: BEGIN TRANSACTION
    S->>DB: SELECT show_seat FOR UPDATE
    alt any seat not AVAILABLE
        S->>DB: ROLLBACK
        S-->>C: 409 Conflict
    else all available
        S->>DB: UPDATE status=HELD, expires_at=now+10min
        S->>DB: INSERT seat_hold + seat_hold_items
        S->>DB: COMMIT
        S-->>C: 201 {holdId, expiresAt, subtotal}
    end
```

### Confirm booking

```mermaid
sequenceDiagram
    participant C as Customer
    participant S as BookingService
    participant P as PricingService
    participant D as DiscountService
    participant Pay as PaymentService
    participant N as NotificationService
    participant DB as H2

    C->>S: POST /holds/{id}/confirm {discountCode}
    S->>DB: validate hold (not expired, owned by user)
    S->>P: calculateSubtotal(seats, show)
    S->>D: applyDiscount(code, context)
    S->>Pay: charge(finalAmount)
    Pay-->>S: SUCCESS + transactionId
    S->>DB: ShowSeat → BOOKED, create Booking + Payment
    S->>N: sendConfirmationAsync(booking)
    S-->>C: 201 BookingResponse
```

### Hold expiry

```
HoldExpiryScheduler (every 30s)
  1. Find show_seat WHERE status=HELD AND hold_expires_at < NOW()
  2. SET status=AVAILABLE, clear hold fields
  3. UPDATE seat_hold SET status=EXPIRED
```

### Cancel and refund

```
POST /bookings/{id}/cancel
  1. Load booking + show + theater refund policy
  2. RefundPolicyEngine.compute(showStart, now) → refundAmount
  3. Create Refund record; update Payment status
  4. ShowSeat → AVAILABLE
  5. sendCancellationAsync(booking)
```

## Concurrency Design

```mermaid
flowchart LR
    A[Request hold seat X] --> B{Acquire pessimistic lock on ShowSeat}
    B --> C{status == AVAILABLE?}
    C -->|No| D[409 Conflict]
    C -->|Yes| E[SET HELD + expiry]
    E --> F[Commit]
```

| Mechanism | Detail |
|-----------|--------|
| Lock target | Individual `ShowSeat` rows |
| Lock mode | `PESSIMISTIC_WRITE` via `@Lock` |
| Guard | Conditional update + affected row count check |
| Constraint | `UNIQUE(show_id, seat_id)` |
| Transaction scope | One transaction per hold; one per confirm |

## Discount Strategy

```mermaid
classDiagram
    class DiscountStrategy {
        <<interface>>
        +supports(DiscountType) boolean
        +validate(DiscountCode, DiscountContext) void
        +apply(DiscountCode, DiscountContext) DiscountResult
    }

    class PercentageDiscountStrategy {
        subtotal × value/100
        cap at maxDiscountAmount
    }

    class FixedAmountDiscountStrategy {
        min(value, subtotal)
    }

    class DiscountService {
        -List~DiscountStrategy~ strategies
        +applyDiscount(code, context) DiscountResult
    }

    DiscountStrategy <|.. PercentageDiscountStrategy
    DiscountStrategy <|.. FixedAmountDiscountStrategy
    DiscountService --> DiscountStrategy
```

### DiscountCode fields

| Field | Type | Notes |
|-------|------|-------|
| `code` | String | Unique identifier |
| `type` | DiscountType | `PERCENTAGE`, `FIXED_AMOUNT` |
| `value` | BigDecimal | 20 = 20% or currency units off |
| `maxDiscountAmount` | BigDecimal | Cap for percentage discounts |
| `minOrderAmount` | BigDecimal | Minimum subtotal to apply |
| `validFrom`, `validUntil` | Instant | Validity window |
| `maxUsageCount` | Integer | Optional global usage limit |
| `currentUsageCount` | Integer | Incremented on successful booking |
| `active` | boolean | Admin toggle |

### Extensibility

To add a new discount type:

1. Add enum value to `DiscountType`
2. Implement `DiscountStrategy`
3. Register as a Spring `@Component` — auto-wired into `DiscountService`

No changes required in `BookingService` or controllers.

## Pricing Rules

```
basePrice = seat.tier.basePrice (REGULAR or PREMIUM)
if show.startTime is weekend (Sat/Sun in city timezone):
    basePrice = basePrice × weekendMultiplier
subtotal = sum(basePrice for each seat)
discount = DiscountService.apply(code, subtotal)
finalAmount = subtotal - discount  (min 0)
```

## Refund Policy

Policies are defined per theater as ordered tiers:

| Hours before show | Refund % |
|-------------------|----------|
| \> 24 | 100 |
| 2 – 24 | 50 |
| \< 2 | 0 |

`RefundPolicyEngine` selects the matching tier based on `Duration.between(now, showStart)`.

## Security

```mermaid
flowchart LR
    Request --> JwtFilter
    JwtFilter -->|valid token| AuthContext
    AuthContext --> RoleCheck
    RoleCheck -->|ADMIN| AdminEndpoints
    RoleCheck -->|CUSTOMER| CustomerEndpoints
    JwtFilter -->|missing/invalid| 401
    RoleCheck -->|forbidden| 403
```

- Registration creates `CUSTOMER` role only; admin users seeded via Flyway.
- JWT contains `userId`, `email`, `role`.
- Stateless sessions — no server-side session store.

## Notification Design

Notifications are **fire-and-forget** via `@Async`:

| Event | Trigger | Channel |
|-------|---------|---------|
| Booking confirmed | After successful payment | Mock (logged) |
| Show reminder | 2h before show start | Mock (logged) |
| Booking cancelled | After refund processed | Mock (logged) |

`NotificationLog` table records every sent notification for audit.

## Error Response Format

```json
{
  "timestamp": "2026-06-20T10:00:00Z",
  "status": 409,
  "error": "Conflict",
  "message": "Seat A5 is already held or booked",
  "path": "/api/v1/shows/abc/holds"
}
```

## Configuration

```yaml
app:
  booking:
    hold-duration-minutes: 10
  notification:
    reminder-hours-before: 2
  jwt:
    secret: ${JWT_SECRET:dev-secret-change-in-production}
    expiration-ms: 86400000
```

## Database

- **Engine:** H2 file-based (`./data/movieticket`)
- **Mode:** PostgreSQL compatibility
- **Schema management:** Flyway migrations only (`ddl-auto: validate`)
- **Rationale:** Zero external setup; schema portable to PostgreSQL
