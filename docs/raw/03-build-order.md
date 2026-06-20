# Raw Notes: Build Order

Agreed implementation sequence:

1. ✅ Requirements and architecture documentation
2. ✅ Project scaffold + Flyway schema
3. ✅ JPA entities and repositories
4. ✅ Auth + RBAC (JWT, seed admin, exception handler)
5. ✅ Admin catalog APIs
6. Customer browse + seat map
7. Hold + expiry job
8. Pricing + discount strategies + mock payment + confirm
9. Cancel + refund policy engine
10. Async notifications + reminder job
11. Integration tests + seed data for demo

## Commit Strategy

Multiple commits during development:

- `docs:` — documentation and AI artifacts
- `feat:` — feature implementations
- `test:` — test additions

Each phase should compile and pass `mvn test` before committing.
