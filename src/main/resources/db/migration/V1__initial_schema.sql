-- Users
CREATE TABLE users (
    id              UUID PRIMARY KEY,
    email           VARCHAR(255) NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    full_name       VARCHAR(255) NOT NULL,
    role            VARCHAR(32)  NOT NULL,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Catalog
CREATE TABLE cities (
    id          UUID PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    timezone    VARCHAR(64)  NOT NULL DEFAULT 'UTC',
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE theaters (
    id          UUID PRIMARY KEY,
    city_id     UUID NOT NULL REFERENCES cities(id),
    name        VARCHAR(255) NOT NULL,
    address     VARCHAR(512),
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE screens (
    id              UUID PRIMARY KEY,
    theater_id      UUID NOT NULL REFERENCES theaters(id),
    name            VARCHAR(255) NOT NULL,
    total_rows      INT NOT NULL,
    total_columns   INT NOT NULL,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE seats (
    id              UUID PRIMARY KEY,
    screen_id       UUID NOT NULL REFERENCES screens(id),
    row_label       VARCHAR(8) NOT NULL,
    seat_number     INT NOT NULL,
    tier            VARCHAR(32) NOT NULL,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (screen_id, row_label, seat_number)
);

CREATE TABLE movies (
    id              UUID PRIMARY KEY,
    title           VARCHAR(255) NOT NULL,
    description     VARCHAR(2000),
    duration_minutes INT NOT NULL,
    language        VARCHAR(64),
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE shows (
    id                  UUID PRIMARY KEY,
    movie_id            UUID NOT NULL REFERENCES movies(id),
    screen_id           UUID NOT NULL REFERENCES screens(id),
    start_time          TIMESTAMP NOT NULL,
    regular_base_price  DECIMAL(10, 2) NOT NULL,
    premium_base_price  DECIMAL(10, 2) NOT NULL,
    weekend_multiplier  DECIMAL(4, 2) NOT NULL DEFAULT 1.20,
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Per-show seat inventory (concurrency anchor)
CREATE TABLE show_seats (
    id                  UUID PRIMARY KEY,
    show_id             UUID NOT NULL REFERENCES shows(id),
    seat_id             UUID NOT NULL REFERENCES seats(id),
    status              VARCHAR(32) NOT NULL DEFAULT 'AVAILABLE',
    held_by_user_id     UUID REFERENCES users(id),
    hold_expires_at     TIMESTAMP,
    seat_hold_id        UUID,
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (show_id, seat_id)
);

CREATE INDEX idx_show_seats_show_status ON show_seats (show_id, status);
CREATE INDEX idx_show_seats_hold_expiry ON show_seats (status, hold_expires_at);

-- Holds
CREATE TABLE seat_holds (
    id              UUID PRIMARY KEY,
    show_id         UUID NOT NULL REFERENCES shows(id),
    user_id         UUID NOT NULL REFERENCES users(id),
    status          VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    expires_at      TIMESTAMP NOT NULL,
    subtotal        DECIMAL(10, 2) NOT NULL,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE seat_hold_items (
    id              UUID PRIMARY KEY,
    seat_hold_id    UUID NOT NULL REFERENCES seat_holds(id),
    show_seat_id    UUID NOT NULL REFERENCES show_seats(id),
    unit_price      DECIMAL(10, 2) NOT NULL,
    UNIQUE (seat_hold_id, show_seat_id)
);

ALTER TABLE show_seats
    ADD CONSTRAINT fk_show_seats_seat_hold
    FOREIGN KEY (seat_hold_id) REFERENCES seat_holds(id);

-- Bookings
CREATE TABLE bookings (
    id                  UUID PRIMARY KEY,
    show_id             UUID NOT NULL REFERENCES shows(id),
    user_id             UUID NOT NULL REFERENCES users(id),
    seat_hold_id        UUID REFERENCES seat_holds(id),
    status              VARCHAR(32) NOT NULL DEFAULT 'CONFIRMED',
    subtotal            DECIMAL(10, 2) NOT NULL,
    discount_amount     DECIMAL(10, 2) NOT NULL DEFAULT 0,
    final_amount        DECIMAL(10, 2) NOT NULL,
    discount_code_id    UUID,
    confirmation_code   VARCHAR(32) NOT NULL UNIQUE,
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE booking_seats (
    id              UUID PRIMARY KEY,
    booking_id      UUID NOT NULL REFERENCES bookings(id),
    show_seat_id    UUID NOT NULL REFERENCES show_seats(id),
    unit_price      DECIMAL(10, 2) NOT NULL,
    UNIQUE (booking_id, show_seat_id)
);

-- Payments
CREATE TABLE payments (
    id                  UUID PRIMARY KEY,
    booking_id          UUID NOT NULL UNIQUE REFERENCES bookings(id),
    amount              DECIMAL(10, 2) NOT NULL,
    status              VARCHAR(32) NOT NULL,
    transaction_id      VARCHAR(128) NOT NULL UNIQUE,
    failure_reason      VARCHAR(512),
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Refund policies
CREATE TABLE refund_policies (
    id              UUID PRIMARY KEY,
    theater_id      UUID NOT NULL REFERENCES theaters(id),
    name            VARCHAR(255) NOT NULL,
    active          BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE refund_policy_tiers (
    id                  UUID PRIMARY KEY,
    refund_policy_id    UUID NOT NULL REFERENCES refund_policies(id),
    min_hours_before    DECIMAL(10, 2) NOT NULL,
    refund_percentage   DECIMAL(5, 2) NOT NULL,
    sort_order          INT NOT NULL
);

-- Refunds
CREATE TABLE refunds (
    id                  UUID PRIMARY KEY,
    booking_id          UUID NOT NULL UNIQUE REFERENCES bookings(id),
    payment_id          UUID NOT NULL REFERENCES payments(id),
    refund_amount       DECIMAL(10, 2) NOT NULL,
    refund_percentage   DECIMAL(5, 2) NOT NULL,
    status              VARCHAR(32) NOT NULL,
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Discount codes
CREATE TABLE discount_codes (
    id                      UUID PRIMARY KEY,
    code                    VARCHAR(64) NOT NULL UNIQUE,
    discount_type           VARCHAR(32) NOT NULL,
    discount_value          DECIMAL(10, 2) NOT NULL,
    max_discount_amount     DECIMAL(10, 2),
    min_order_amount        DECIMAL(10, 2),
    valid_from              TIMESTAMP NOT NULL,
    valid_until             TIMESTAMP NOT NULL,
    max_usage_count         INT,
    current_usage_count     INT NOT NULL DEFAULT 0,
    active                  BOOLEAN NOT NULL DEFAULT TRUE,
    created_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

ALTER TABLE bookings
    ADD CONSTRAINT fk_bookings_discount_code
    FOREIGN KEY (discount_code_id) REFERENCES discount_codes(id);

-- Notifications
CREATE TABLE notification_logs (
    id              UUID PRIMARY KEY,
    user_id         UUID NOT NULL REFERENCES users(id),
    booking_id      UUID REFERENCES bookings(id),
    type            VARCHAR(64) NOT NULL,
    channel         VARCHAR(32) NOT NULL DEFAULT 'MOCK',
    recipient       VARCHAR(255) NOT NULL,
    subject         VARCHAR(255) NOT NULL,
    body            VARCHAR(2000) NOT NULL,
    status          VARCHAR(32) NOT NULL DEFAULT 'SENT',
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_shows_screen_start ON shows (screen_id, start_time);
CREATE INDEX idx_bookings_user ON bookings (user_id, created_at);
CREATE INDEX idx_seat_holds_expiry ON seat_holds (status, expires_at);
